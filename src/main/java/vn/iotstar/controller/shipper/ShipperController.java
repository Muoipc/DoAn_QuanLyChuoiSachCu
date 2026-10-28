package vn.iotstar.controller.shipper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IOrderService;

import java.util.List;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Giao diện dành riêng cho nhân viên giao hàng (Shipper)
     */
    @GetMapping({"", "/orders"})
    public String shipperOrders(
            @RequestParam(name = "status", required = false) Order.OrderStatus status,
            @RequestParam(name = "shipperId", required = false) Long shipperIdParam,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login?redirectURL=/shipper/orders";
        }

        boolean isAdmin = "ROLE_ADMIN".equals(userDetails.getRoleName());
        Long currentShipperId = userDetails.getId();
        // Chỉ ADMIN mới có quyền xem đơn hàng của các shipper khác
        if (isAdmin && shipperIdParam != null) {
            currentShipperId = shipperIdParam;
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderService.findOrdersForShipper(currentShipperId, status, pageable);

        User shipper = userRepository.findById(currentShipperId).orElse(null);
        List<User> allShippers = isAdmin ? orderService.findAllShippers() : List.of();

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentShipper", shipper);
        model.addAttribute("allShippers", allShippers);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());

        // 5 Chỉ số KPI vận hành thực tế cho Shipper
        model.addAttribute("countAll", orderService.countOrdersForShipper(currentShipperId, null));
        model.addAttribute("countPickedUp", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.PICKED_UP));
        model.addAttribute("countShipping", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.SHIPPING));
        model.addAttribute("countDelivered", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.DELIVERED));
        model.addAttribute("countReturned", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.RETURNED));
        model.addAttribute("totalCodCollected", orderService.sumCodCollectedByShipper(currentShipperId));

        return "shipper/orders";
    }

    /**
     * Shipper cập nhật trạng thái giao hàng thành công (DELIVERED) hoặc hoàn trả (RETURNED)
     */
    @PostMapping("/orders/{id}/update")
    public String updateDelivery(
            @PathVariable("id") Long id,
            @RequestParam("status") Order.OrderStatus status,
            @RequestParam(name = "note", required = false) String note,
            @RequestParam(name = "shipperId", required = false) Long shipperId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null) {
            return "redirect:/login?redirectURL=/shipper/orders";
        }

        Order order = orderService.findById(id);
        if (order == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng #" + id);
            return "redirect:/shipper/orders";
        }

        boolean isAdmin = "ROLE_ADMIN".equals(userDetails.getRoleName());
        boolean isAssignedShipper = order.getShipper() != null && order.getShipper().getId().equals(userDetails.getId());
        if (!isAdmin && !isAssignedShipper) {
            redirect.addFlashAttribute("errorMessage", "Bạn không có quyền cập nhật đơn hàng được phân công cho người khác!");
            return "redirect:/shipper/orders";
        }

        try {
            orderService.updateOrderStatus(id, status, note);
            if (status == Order.OrderStatus.PICKED_UP) {
                redirect.addFlashAttribute("successMessage", "Đã xác nhận lấy hàng thành công từ chi nhánh cho đơn #" + id + "!");
            } else if (status == Order.OrderStatus.SHIPPING) {
                redirect.addFlashAttribute("successMessage", "Đang tiến hành giao đơn hàng #" + id + " tới khách hàng!");
            } else if (status == Order.OrderStatus.DELIVERED) {
                redirect.addFlashAttribute("successMessage", "Xác nhận giao đơn hàng #" + id + " thành công!");
            } else if (status == Order.OrderStatus.RETURNED) {
                redirect.addFlashAttribute("successMessage", "Đã cập nhật đơn hàng #" + id + " sang trạng thái TRẢ HÀNG / HOÀN TIỀN!");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shipper/orders";
    }
}
