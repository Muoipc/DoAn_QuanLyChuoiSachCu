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
     * Tạo đơn hàng trực tiếp ("Mua Ngay" / Instant Checkout) cho 1 cuốn sách duy nhất.
     * Ghi chú cho Cường:
     * - Áp dụng cho tính năng "Mua Ngay" từ trang chi tiết sách mà không can thiệp hay xóa các món đồ đang có trong giỏ hàng.
     * - Tự động tính phí ship (miễn phí nếu đơn >= 150.000đ hoặc nhận tại chi nhánh).
     * - Áp dụng voucher hợp lệ nếu có (kiểm tra hạn dùng, giá trị đơn tối thiểu).
     * - Trừ tồn kho trong bảng Inventory tại đúng chi nhánh đã chọn.
     *
     * @param userId          ID người đặt hàng
     * @param bookId          ID cuốn sách mua ngay
     * @param storeId         ID chi nhánh phân bổ lấy sách
     * @param quantity        Số lượng sách mua
     * @param deliveryMethod  Phương thức nhận (STORE_PICKUP hoặc HOME_DELIVERY)
     * @param receiverName    Tên người nhận hàng
     * @param receiverPhone   Số điện thoại người nhận
     * @param receiverAddress Địa chỉ nhận hàng
     * @param shippingUnitId  ID đơn vị vận chuyển (nếu giao tận nơi)
     * @param paymentMethod   Phương thức thanh toán (COD hoặc VNPAY)
     * @param voucherCode     Mã giảm giá (nếu có)
     * @param customerNotes   Ghi chú đơn hàng từ khách
     * @return Đối tượng Order sau khi lưu vào CSDL
     */
    Order createOrderDirect(
            Long userId,
            Long bookId,
            Long storeId,
            int quantity,
            Order.DeliveryMethod deliveryMethod,
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
}
