package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.security.CustomUserDetails;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Autowired
    private vn.iotstar.service.ICartService cartService;

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Yêu cầu đăng nhập trước khi thực hiện thao tác này!");
    }

    /**
     * Màn hình danh sách đơn hàng đã mua (Lịch sử đơn hàng phong cách Shopee).
     * URL: /orders
     */
    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String orderHistoryView(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "status", required = false) String statusStr,
            HttpServletRequest request,
            Model model) {

        if (userDetails == null || userDetails.getId() == null) {
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String fullUrl = (qs != null && !qs.isBlank()) ? (uri + "?" + qs) : uri;
            return "redirect:/login?redirectURL=" + URLEncoder.encode(fullUrl, StandardCharsets.UTF_8);
        }

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

        // Lấy danh sách ID các cuốn sách mà user đã đánh giá theo từng đơn hàng (kiểm tra null an toàn tránh NullPointerException)
        List<Review> userReviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Set<String> reviewedKeySet = userReviews.stream()
                .filter(r -> r.getOrder() != null && r.getOrder().getId() != null && r.getBook() != null && r.getBook().getId() != null)
                .map(r -> r.getOrder().getId() + "_" + r.getBook().getId())
                .collect(Collectors.toSet());

        // Đếm số lượng đơn hàng theo từng trạng thái để làm badge trên các tab
        List<Order> allUserOrders = orderRepository.findByUserIdWithItems(userId);
        long countAll = allUserOrders.size();
        long countNew = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.NEW).count();
        long countConfirmed = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.CONFIRMED).count();
        long countPickedUp = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.PICKED_UP).count();
        long countShipping = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.SHIPPING).count();
        long countDelivered = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.DELIVERED).count();
        long countCancelled = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.CANCELLED).count();
        long countReturned = allUserOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.RETURNED).count();

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", statusStr.toUpperCase());
        model.addAttribute("reviewedKeySet", reviewedKeySet);

        model.addAttribute("countAll", countAll);
        model.addAttribute("countNew", countNew);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countPickedUp", countPickedUp);
        model.addAttribute("countShipping", countShipping);
        model.addAttribute("countDelivered", countDelivered);
        model.addAttribute("countCancelled", countCancelled);
        model.addAttribute("countReturned", countReturned);

        model.addAttribute("pageTitle", "Đơn Mua Của Tôi - Chuỗi Sách Cũ TP.HCM");

        return "order-history";
    }

    /**
     * Màn hình chi tiết đơn hàng & theo dõi hành trình chuẩn Shopee (Order Tracking).
     * Hỗ trợ cả 2 URL: /orders/{orderRef} và /user/purchase/order/{orderRef}
     */
    @GetMapping({"/orders/{orderRef}", "/user/purchase/order/{orderRef}"})
    @Transactional(readOnly = true)
    public String orderDetailView(
            @PathVariable("orderRef") String orderRef,
            @RequestParam(value = "type", required = false) String type,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            Model model) {

        if (userDetails == null || userDetails.getId() == null) {
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String fullUrl = (qs != null && !qs.isBlank()) ? (uri + "?" + qs) : uri;
            return "redirect:/login?redirectURL=" + URLEncoder.encode(fullUrl, StandardCharsets.UTF_8);
        }

        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithDetails(orderRef);
        if (orderOpt.isEmpty()) {
            try {
                Long orderId = Long.parseLong(orderRef);
                orderOpt = orderRepository.findByIdWithDetails(orderId);
            } catch (NumberFormatException ignored) {}
        }

        // Nếu người dùng gõ mã test tùy ý (như link Shopee mẫu), fallback sang đơn hàng gần nhất
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin && (userDetails == null || order.getUser() == null || !order.getUser().getId().equals(userDetails.getId()))) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi Tiết Đơn Hàng #" + order.getOrderCode() + " — Chuỗi Sách Cũ TP.HCM");

        return "order-detail";
    }

    /**
     * Khách hàng gửi yêu cầu Hủy đơn hàng khi đơn còn ở trạng thái Mới (NEW).
     * Tự động hoàn lại số lượng tồn kho sách cho chi nhánh.
     * URL: POST /orders/cancel/{orderCode}
     */
    @PostMapping("/orders/cancel/{orderCode}")
    @Transactional
    public String cancelOrder(
            @PathVariable("orderCode") String orderCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/orders";
        }

        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithDetails(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            boolean isAdmin = "ROLE_ADMIN".equals(userDetails.getRoleName());
            if (!isAdmin && (order.getUser() == null || !order.getUser().getId().equals(userDetails.getId()))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn hàng của tài khoản khác!");
                return "redirect:/orders";
            }
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
     * Khách hàng bấm Mua Lại đơn hàng cũ: Thêm toàn bộ sách từ đơn cũ vào giỏ hàng và chuyển hướng tới /cart.
     * URL: POST /orders/reorder/{orderCode}
     */
    @PostMapping("/orders/reorder/{orderCode}")
    @Transactional
    public String reorderOldOrder(
            @PathVariable("orderCode") String orderCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/orders";
        }

        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithDetails(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getUser() == null || !order.getUser().getId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng!");
                return "redirect:/orders";
            }

            int count = 0;
            Long storeId = order.getStore() != null ? order.getStore().getId() : 1L;
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getBook() != null) {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                        cartService.addToCart(userDetails.getId(), item.getBook().getId(), storeId, qty);
                        count++;
                    }
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã thêm " + count + " cuốn sách từ đơn hàng #" + orderCode + " vào giỏ hàng thành công!");
            return "redirect:/cart";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng cần mua lại!");
        return "redirect:/orders";
    }

    /**
     * Khách hàng gửi yêu cầu Đổi trả / Hoàn tiền đối với đơn đã giao (DELIVERED).
     * URL: POST /orders/request-return/{orderCode}
     */
    @PostMapping("/orders/request-return/{orderCode}")
    @Transactional
    public String requestOrderReturn(
            @PathVariable("orderCode") String orderCode,
            @RequestParam("returnReason") String returnReason,
            @RequestParam(value = "returnNote", required = false) String returnNote,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/orders";
        }

        Optional<Order> orderOpt = orderRepository.findByOrderCodeWithDetails(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getUser() == null || !order.getUser().getId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng!");
                return "redirect:/orders";
            }

            if (order.getOrderStatus() != Order.OrderStatus.DELIVERED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể yêu cầu đổi trả đối với đơn hàng đã giao thành công!");
                return "redirect:/orders/" + orderCode;
            }

            String notePrefix = (order.getCustomerNotes() != null && !order.getCustomerNotes().isBlank()) 
                    ? order.getCustomerNotes() + " | " : "";
            String fullReason = "[Yêu cầu đổi trả]: " + returnReason + 
                    (returnNote != null && !returnNote.isBlank() ? " - Chi tiết: " + returnNote.trim() : "");
            order.setCustomerNotes(notePrefix + fullReason);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage", 
                    "Yêu cầu đổi trả đơn hàng #" + orderCode + " đã được gửi tới Quản lý chi nhánh. Chúng tôi sẽ liên hệ trong 24h để hỗ trợ!");
            return "redirect:/orders/" + orderCode;
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
        return "redirect:/orders";
    }
}
