package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.security.CustomUserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller Quản lý Lịch sử đơn hàng, Chi tiết đơn hàng và Đánh giá sách cũ phía Khách hàng.
 * Ghi chú cho Cường:
 * 1. Hiển thị các tab đơn hàng chuẩn Shopee: Tất cả, Chờ xác nhận, Đang xử lý, Đang giao / Chờ nhận tại quầy, Đã giao, Đã hủy.
 * 2. Hỗ trợ khách hàng hủy đơn hàng mới (status = NEW) và tự động hoàn trả số lượng tồn kho lại chi nhánh.
 * 3. Hỗ trợ khách đánh giá chất lượng sách (1 - 5 sao kèm nhận xét chi tiết) sau khi đơn hoàn tất (DELIVERED).
 */
@Controller
public class OrderHistoryController {

    private static final Long DEFAULT_GUEST_USER_ID = 4L;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        return DEFAULT_GUEST_USER_ID;
    }

    /**
     * Màn hình danh sách đơn hàng đã mua (Lịch sử đơn hàng phong cách Shopee).
     * URL: /orders
     */
    @GetMapping("/orders")
    public String orderHistoryView(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "status", required = false) String statusStr,
            Model model) {

        Long userId = resolveUserId(userDetails);

        List<Order> orders;
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
                orders = orderRepository.findByUserIdAndOrderStatusWithItems(userId, status);
            } catch (IllegalArgumentException e) {
                orders = orderRepository.findByUserIdWithItems(userId);
                statusStr = "ALL";
            }
        } else {
            orders = orderRepository.findByUserIdWithItems(userId);
            statusStr = "ALL";
        }

        // Lấy danh sách ID các cuốn sách mà user đã đánh giá theo từng đơn hàng
        List<Review> userReviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Set<String> reviewedKeySet = userReviews.stream()
                .map(r -> r.getOrder().getId() + "_" + r.getBook().getId())
                .collect(Collectors.toSet());

        // Đếm số lượng đơn hàng theo từng trạng thái để làm badge trên các tab
        List<Order> allUserOrders = orderRepository.findByUserIdWithItems(userId);
        long countAll = allUserOrders.size();
        long countNew = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.NEW).count();
        long countConfirmed = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.CONFIRMED).count();
        long countShipping = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.SHIPPING).count();
        long countDelivered = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.DELIVERED).count();
        long countCancelled = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.CANCELLED).count();

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", statusStr.toUpperCase());
        model.addAttribute("reviewedKeySet", reviewedKeySet);

        model.addAttribute("countAll", countAll);
        model.addAttribute("countNew", countNew);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countShipping", countShipping);
        model.addAttribute("countDelivered", countDelivered);
        model.addAttribute("countCancelled", countCancelled);

        model.addAttribute("pageTitle", "Đơn Mua Của Tôi - Chuỗi Sách Cũ TP.HCM");

        return "order-history";
    }

    /**
     * Màn hình chi tiết đơn hàng & theo dõi hành trình (Order Tracking).
     * URL: /orders/{orderCode}
     */
    @GetMapping("/orders/{orderCode}")
    public String orderDetailView(
            @PathVariable("orderCode") String orderCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithDetails(orderCode);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi Tiết Đơn Hàng #" + order.getOrderCode() + " - Chuỗi Sách Cũ");

        return "order-detail";
    }

    /**
     * Khách hàng gửi yêu cầu Hủy đơn hàng khi đơn còn ở trạng thái Mới (NEW).
     * Tự động hoàn lại số lượng tồn kho sách cho chi nhánh.
     * URL: POST /orders/cancel/{orderCode}
     */
    @PostMapping("/orders/cancel/{orderCode}")
    public String cancelOrder(
            @PathVariable("orderCode") String orderCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithDetails(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getOrderStatus() == Order.OrderStatus.NEW) {
                order.setOrderStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);

                // Hoàn lại số lượng vào bảng inventory
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getBook() != null && order.getStore() != null) {
                            Optional<Inventory> invOpt = inventoryRepository.findByStoreIdAndBookId(order.getStore().getId(), item.getBook().getId());
                            if (invOpt.isPresent()) {
                                Inventory inv = invOpt.get();
                                inv.setQuantity((inv.getQuantity() != null ? inv.getQuantity() : 0) + item.getQuantity());
                                inventoryRepository.save(inv);
                            }
                        }
                    }
                }

                redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + orderCode + " thành công!");
                return "redirect:/orders?status=CANCELLED";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng vì chi nhánh đã xác nhận đóng gói hoặc đang vận chuyển!");
            }
        }

        return "redirect:/orders";
    }

    /**
     * Khách hàng gửi đánh giá & nhận xét chất lượng sách cũ đã nhận.
     * URL: POST /reviews/create
     */
    @PostMapping("/reviews/create")
    public String submitReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("orderId") Long orderId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            RedirectAttributes redirectAttributes) {

        Long userId = resolveUserId(userDetails);

        // Kiểm tra xem đã đánh giá chưa
        if (reviewRepository.existsByUserIdAndBookIdAndOrderId(userId, bookId, orderId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá cuốn sách này trong đơn hàng rồi!");
            return "redirect:/orders";
        }

        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);
        Order order = orderRepository.findById(orderId).orElse(null);

        if (user != null && book != null && order != null) {
            Review review = new Review();
            review.setUser(user);
            review.setBook(book);
            review.setOrder(order);
            review.setRating(Math.max(1, Math.min(5, rating)));
            review.setComment(comment != null ? comment.trim() : "Sách đúng mô tả.");
            review.setMediaType(Review.MediaType.NONE);
            reviewRepository.save(review);

            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá chất lượng sách cũ! Đánh giá đã được ghi nhận.");
        }

        return "redirect:/orders";
    }
}
