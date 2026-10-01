package vn.iotstar.service;

import vn.iotstar.entity.Order;

import java.util.Optional;

/**
 * Interface nghiệp vụ Đặt hàng & Xử lý đơn hàng chuỗi sách cũ.
 * Ghi chú cho Cường: Service này xử lý:
 * 1. Chuyển đổi toàn bộ CartItem sang Order và OrderItem tương ứng.
 * 2. Phân biệt 2 phương thức nhận hàng: Nhận tại 1 trong 5 chi nhánh (Click & Collect) hoặc Giao tận nơi qua đơn vị vận chuyển.
 * 3. Tự động trừ tồn kho sách cũ tại đúng chi nhánh đã chọn trong bảng inventory.
 * 4. Cập nhật trạng thái thanh toán VNPay khi giao dịch thành công.
 */
public interface IOrderService {

    /**
     * Tạo đơn hàng mới từ giỏ hàng hiện tại của khách.
     */
    Order createOrderFromCart(
            Long userId,
            Order.DeliveryMethod deliveryMethod,
            Long storeId,
            String receiverName,
            String receiverPhone,
            String receiverAddress,
            Integer shippingUnitId,
            Order.PaymentMethod paymentMethod,
            String voucherCode,
            String customerNotes
    );

    /**
     * Lấy chi tiết đơn hàng theo mã đơn (orderCode).
     */
    Optional<Order> findByOrderCode(String orderCode);

    /**
     * Cập nhật trạng thái đơn hàng khi thanh toán VNPay thành công.
     */
    void updatePaymentSuccess(String orderCode, String transactionId);

    org.springframework.data.domain.Page<Order> searchAdminOrders(
        String keyword, Order.OrderStatus status, Long storeId, Order.DeliveryMethod deliveryMethod, org.springframework.data.domain.Pageable pageable
    );

    Order findById(Long id);

    void updateOrderStatus(Long orderId, Order.OrderStatus newStatus, String note);

    void assignShipper(Long orderId, Long shipperId);

    org.springframework.data.domain.Page<Order> findOrdersForShipper(
        Long shipperId, Order.OrderStatus status, org.springframework.data.domain.Pageable pageable
    );

    void shipperUpdateDelivery(Long orderId, Long shipperId, Order.OrderStatus status, String note);

    long countByStatus(Order.OrderStatus status);

    java.util.List<vn.iotstar.entity.User> findAllShippers();
}
