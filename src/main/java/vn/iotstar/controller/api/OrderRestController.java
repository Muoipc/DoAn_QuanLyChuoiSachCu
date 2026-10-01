package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.OrderItem;
import vn.iotstar.service.IOrderService;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    @Autowired
    private IOrderService orderService;

    /**
     * API Lấy chi tiết đơn hàng theo mã đơn
     */
    @GetMapping("/{orderCode}")
    public ResponseEntity<?> getOrderByCode(@PathVariable("orderCode") String orderCode) {
        Optional<Order> orderOpt = orderService.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order o = orderOpt.get();
        Map<String, Object> res = new HashMap<>();
        res.put("id", o.getId());
        res.put("orderCode", o.getOrderCode());
        res.put("receiverName", o.getReceiverName());
        res.put("receiverPhone", o.getReceiverPhone());
        res.put("receiverAddress", o.getReceiverAddress());
        res.put("deliveryMethod", o.getDeliveryMethod().name());
        res.put("paymentMethod", o.getPaymentMethod().name());
        res.put("paymentStatus", o.getPaymentStatus().name());
        res.put("orderStatus", o.getOrderStatus().name());
        res.put("subtotal", o.getSubtotal());
        res.put("shippingFee", o.getShippingFee());
        res.put("discountAmount", o.getDiscountAmount());
        res.put("finalAmount", o.getFinalAmount());
        res.put("store", o.getStore() != null ? o.getStore().getStoreName() : "");
        res.put("shipper", o.getShipper() != null ? o.getShipper().getFullName() : null);

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem oi : o.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("bookId", oi.getBook() != null ? oi.getBook().getId() : null);
            itemMap.put("bookTitle", oi.getBookTitle());
            itemMap.put("conditionPercent", oi.getConditionPercent());
            itemMap.put("price", oi.getPrice());
            itemMap.put("quantity", oi.getQuantity());
            itemMap.put("totalPrice", oi.getTotalPrice());
            items.add(itemMap);
        }
        res.put("items", items);

        return ResponseEntity.ok(res);
    }

    /**
     * API Cập nhật trạng thái đơn hàng (Dành cho hệ thống / đối tác)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        String note = body.get("note");

        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu tham số status"));
        }

        try {
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            orderService.updateOrderStatus(id, status, note);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái đơn hàng thành công", "newStatus", status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Trạng thái không hợp lệ hoặc không tìm thấy đơn: " + e.getMessage()));
        }
    }
}
