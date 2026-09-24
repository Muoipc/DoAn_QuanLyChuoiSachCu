package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.ICartService;
import vn.iotstar.service.IOrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Hiện thực nghiệp vụ Đặt hàng & Xử lý đơn hàng chuỗi sách cũ.
 * Ghi chú cho Cường:
 * - Tự động cập nhật bảng inventory (trừ tồn kho tại đúng chi nhánh đã phân bổ) sau khi khách tạo đơn.
 * - Hỗ trợ cả 2 hình thức: Đến nhận tại shop (Store Pickup - Free ship) và Giao hàng tận nơi (Home Delivery).
 * - Tự động xóa sạch các mục trong giỏ sau khi chuyển đổi thành đơn hàng thành công.
 */
@Service
@Transactional
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ICartService cartService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ShippingUnitRepository shippingUnitRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Order createOrderFromCart(
            Long userId,
            Order.DeliveryMethod deliveryMethod,
            Long storeId,
            String receiverName,
            String receiverPhone,
            String receiverAddress,
            Integer shippingUnitId,
            Order.PaymentMethod paymentMethod,
            String voucherCode,
            String customerNotes) {

        Cart cart = cartService.getOrCreateCartForUser(userId);
        List<CartItem> cartItems = cartService.getCartItemsWithDetails(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống, không thể tạo đơn hàng!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + storeId));

        // 1. Tính toán tổng tiền hàng (subtotal)
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Tính phí vận chuyển
        BigDecimal shippingFee = BigDecimal.ZERO;
        ShippingUnit shippingUnit = null;

        if (deliveryMethod == Order.DeliveryMethod.HOME_DELIVERY) {
            if (shippingUnitId != null) {
                shippingUnit = shippingUnitRepository.findById(shippingUnitId).orElse(null);
            }
            if (shippingUnit != null && shippingUnit.getBaseFee() != null) {
                shippingFee = shippingUnit.getBaseFee();
            } else {
                shippingFee = new BigDecimal("25000.00"); // Phí mặc định nếu chưa chọn
            }

            // Chính sách Shopee: Đơn sách cũ từ 150.000₫ được miễn phí vận chuyển
            if (subtotal.compareTo(new BigDecimal("150000.00")) >= 0) {
                shippingFee = BigDecimal.ZERO;
            }
        }

        // 3. Xử lý Voucher giảm giá nếu có
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {
            Optional<Voucher> voucherOpt = voucherRepository.findByCodeAndIsActiveTrue(voucherCode.trim().toUpperCase());
            if (voucherOpt.isPresent()) {
                Voucher v = voucherOpt.get();
                if (v.getMinOrderAmount() == null || subtotal.compareTo(v.getMinOrderAmount()) >= 0) {
                    voucher = v;
                    if (v.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
                        discountAmount = v.getDiscountValue();
                    } else if (v.getDiscountType() == Voucher.DiscountType.PERCENT) {
                        discountAmount = subtotal.multiply(v.getDiscountValue()).divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
                        if (v.getMaxDiscount() != null && discountAmount.compareTo(v.getMaxDiscount()) > 0) {
                            discountAmount = v.getMaxDiscount();
                        }
                    }
                    // Tăng số lượt đã sử dụng của voucher
                    v.setUsedCount((v.getUsedCount() != null ? v.getUsedCount() : 0) + 1);
                    voucherRepository.save(v);
                }
            }
        }

        // 4. Tính tổng thanh toán cuối cùng (finalAmount)
        BigDecimal finalAmount = subtotal.add(shippingFee).subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 5. Khởi tạo thực thể Order
        Order order = new Order();
        String orderCode = "ORD" + (System.currentTimeMillis() % 1000000000L);
        order.setOrderCode(orderCode);
        order.setUser(user);
        order.setStore(store);
        order.setDeliveryMethod(deliveryMethod);
        order.setShippingUnit(shippingUnit);
        order.setVoucher(voucher);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);
        order.setOrderStatus(Order.OrderStatus.NEW);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setCustomerNotes(customerNotes);

        Order savedOrder = orderRepository.save(order);

        // 6. Chuyển từng CartItem thành OrderItem & Trừ tồn kho tại đúng chi nhánh
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setBook(ci.getBook());
            oi.setBookTitle(ci.getBook().getTitle());
            oi.setConditionPercent(ci.getBook().getConditionPercent());
            oi.setPrice(ci.getBook().getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setTotalPrice(ci.getItemTotal());
            orderItemRepository.save(oi);

            // Cập nhật tồn kho (trừ số lượng ở kho tương ứng)
            Store itemStore = ci.getStore() != null ? ci.getStore() : store;
            Optional<Inventory> invOpt = inventoryRepository.findByStoreIdAndBookId(itemStore.getId(), ci.getBook().getId());
            if (invOpt.isPresent()) {
                Inventory inv = invOpt.get();
                int currentQty = inv.getQuantity() != null ? inv.getQuantity() : 0;
                inv.setQuantity(Math.max(0, currentQty - ci.getQuantity()));
                inventoryRepository.save(inv);
            }

            // Tăng tổng số lượng đã bán (totalSold) của sách
            Book book = ci.getBook();
            book.setTotalSold((book.getTotalSold() != null ? book.getTotalSold() : 0) + ci.getQuantity());
            bookRepository.save(book);
        }

        // 7. Xóa sạch giỏ hàng của khách sau khi tạo đơn thành công
        cartService.clearCart(cart.getId());

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCodeWithDetails(orderCode);
    }

    @Override
    public void updatePaymentSuccess(String orderCode, String transactionId) {
        Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
    }
}
