package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IOrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API dành riêng cho Nhân viên giao hàng (Shipper)
 * Cung cấp API cho Ứng dụng di động (Mobile App Shipper) xác thực bằng JWT Bearer Token
 */
@RestController
@RequestMapping("/api/shipper")
public class ShipperRestController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy danh sách đơn hàng được phân công cho Shipper
     * GET /api/shipper/orders?status=SHIPPING&page=0&size=10
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getAssignedOrders(
            @RequestParam(name = "status", required = false) Order.OrderStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Long shipperId = getCurrentShipperId();
        if (shipperId == null) {
            shipperId = 6L; // Default shipper id in demo data
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderService.findOrdersForShipper(shipperId, status, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("shipperId", shipperId);
        response.put("orders", orderPage.getContent());
        response.put("currentPage", orderPage.getNumber());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Xem chi tiết 1 đơn hàng giao
     * GET /api/shipper/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("id") Long id) {
        Order order = orderService.findById(id);
        if (order != null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("order", order);
            return ResponseEntity.ok(resp);
        } else {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Không tìm thấy đơn hàng #" + id);
            return ResponseEntity.status(404).body(err);
        }
    }

    /**
     * Shipper cập nhật trạng thái đơn hàng (DELIVERED, RETURNED, hoặc SHIPPING)
     * POST /api/shipper/orders/{id}/status
     */
    @PostMapping("/orders/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload
    ) {
        try {
            String statusStr = payload.get("status");
            String note = payload.get("note");

            if (statusStr == null || statusStr.trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Thiếu trạng thái 'status' cần cập nhật");
                return ResponseEntity.badRequest().body(err);
            }

            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            orderService.updateOrderStatus(id, status, note);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Cập nhật trạng thái đơn hàng #" + id + " thành " + status.name() + " thành công!");
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Trạng thái không hợp lệ: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Lỗi cập nhật: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    /**
     * Tiện ích nhanh: Xác nhận giao hàng thành công
     * POST /api/shipper/orders/{id}/deliver
     */
    @PostMapping("/orders/{id}/deliver")
    public ResponseEntity<?> deliverSuccess(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String note = (payload != null) ? payload.getOrDefault("note", "Giao hàng thành công - Khách đã ký nhận và thanh toán") : "Giao hàng thành công";
        try {
            orderService.updateOrderStatus(id, Order.OrderStatus.DELIVERED, note);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Đã xác nhận giao thành công đơn hàng #" + id);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    private Long getCurrentShipperId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
}
