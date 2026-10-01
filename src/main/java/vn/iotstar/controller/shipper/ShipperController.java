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
        Long currentShipperId = shipperIdParam;
        if (currentShipperId == null && userDetails != null) {
            currentShipperId = userDetails.getId();
        }
        if (currentShipperId == null) {
            // Mặc định lấy tài khoản shipper mẫu ID 6 trong database.sql nếu chưa đăng nhập
            currentShipperId = 6L;
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderService.findOrdersForShipper(currentShipperId, status, pageable);

        User shipper = userRepository.findById(currentShipperId).orElse(null);
        List<User> allShippers = orderService.findAllShippers();

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentShipper", shipper);
        model.addAttribute("allShippers", allShippers);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());

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
            RedirectAttributes redirect
    ) {
        try {
            orderService.updateOrderStatus(id, status, note);
            if (status == Order.OrderStatus.DELIVERED) {
                redirect.addFlashAttribute("successMessage", "Xác nhận giao đơn hàng #" + id + " thành công!");
            } else if (status == Order.OrderStatus.RETURNED) {
                redirect.addFlashAttribute("successMessage", "Đã cập nhật đơn hàng #" + id + " sang trạng thái TRẢ HÀNG / HOÀN TIỀN!");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/shipper/orders" + (shipperId != null ? "?shipperId=" + shipperId : "");
    }
}
