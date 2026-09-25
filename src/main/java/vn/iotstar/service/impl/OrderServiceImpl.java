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

    /**
     * Tạo đơn hàng trực tiếp ("Mua Ngay" / Direct Buy Now / Instant Checkout) cho 1 cuốn sách.
     * Ghi chú cho Cường:
     * 1. Lấy thông tin sách từ BookRepository, tính subtotal = book.price * quantity.
     * 2. Phí vận chuyển: Miễn phí nếu nhận tại chi nhánh (STORE_PICKUP) hoặc subtotal >= 150.000₫. Nếu giao tận nơi < 150.000₫ thì lấy baseFee của đơn vị vận chuyển (mặc định 25.000₫).
     * 3. Áp dụng Voucher nếu hợp lệ: kiểm tra ngày bắt đầu/kết thúc, giới hạn lượt dùng, giá trị đơn tối thiểu (minOrderAmount) và tính discountAmount.
     * 4. Tạo OrderItem duy nhất cho cuốn sách này và gắn vào đơn hàng.
     * 5. Trừ tồn kho trong bảng Inventory tại chi nhánh storeId đã chỉ định và tăng số lượng đã bán (totalSold) của sách.
     * 6. LƯU Ý ĐẶC BIỆT QUAN TRỌNG: Không xóa hay can thiệp vào các sản phẩm trong giỏ hàng hiện tại của khách!
     */
    @Override
    public Order createOrderDirect(
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
            String customerNotes) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua sách phải lớn hơn 0!");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + bookId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + storeId));

        // 1. Tính toán tổng tiền hàng (subtotal)
        BigDecimal price = book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO;
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

        // 2. Tính phí vận chuyển (nếu HOME_DELIVERY, miễn phí nếu subtotal >= 150.000đ)
        BigDecimal shippingFee = BigDecimal.ZERO;
        ShippingUnit shippingUnit = null;

        if (deliveryMethod == Order.DeliveryMethod.HOME_DELIVERY) {
            if (shippingUnitId != null) {
                shippingUnit = shippingUnitRepository.findById(shippingUnitId).orElse(null);
            }
            if (shippingUnit != null && shippingUnit.getBaseFee() != null) {
                shippingFee = shippingUnit.getBaseFee();
            } else {
                shippingFee = new BigDecimal("25000.00"); // Phí giao hàng tiêu chuẩn mặc định
            }

            // Chính sách Shopee: Đơn sách cũ từ 150.000₫ được miễn phí vận chuyển
            if (subtotal.compareTo(new BigDecimal("150000.00")) >= 0) {
                shippingFee = BigDecimal.ZERO;
            }
        }

        // 3. Áp dụng Voucher giảm giá nếu có (kiểm tra hạn dùng, minOrderAmount, tính discount)
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {
            Optional<Voucher> voucherOpt = voucherRepository.findByCodeAndIsActiveTrue(voucherCode.trim().toUpperCase());
            if (voucherOpt.isPresent()) {
                Voucher v = voucherOpt.get();
                LocalDateTime now = LocalDateTime.now();

                boolean isValidDate = (v.getStartDate() == null || !now.isBefore(v.getStartDate()))
                        && (v.getEndDate() == null || !now.isAfter(v.getEndDate()));
                boolean isUnderLimit = v.getUsageLimit() == null || v.getUsedCount() == null || v.getUsedCount() < v.getUsageLimit();
                boolean isMinAmountMet = v.getMinOrderAmount() == null || subtotal.compareTo(v.getMinOrderAmount()) >= 0;

                if (isValidDate && isUnderLimit && isMinAmountMet) {
                    voucher = v;
                    if (v.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
                        discountAmount = v.getDiscountValue();
                    } else if (v.getDiscountType() == Voucher.DiscountType.PERCENT) {
                        discountAmount = subtotal.multiply(v.getDiscountValue())
                                .divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
                        if (v.getMaxDiscount() != null && v.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0 
                                && discountAmount.compareTo(v.getMaxDiscount()) > 0) {
                            discountAmount = v.getMaxDiscount();
                        }
                    }
                    if (discountAmount.compareTo(subtotal) > 0) {
                        discountAmount = subtotal;
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

        // 6. Tạo OrderItem duy nhất cho cuốn sách mua ngay
        OrderItem oi = new OrderItem();
        oi.setOrder(savedOrder);
        oi.setBook(book);
        oi.setBookTitle(book.getTitle());
        oi.setConditionPercent(book.getConditionPercent());
        oi.setPrice(book.getPrice());
        oi.setQuantity(quantity);
        oi.setTotalPrice(subtotal);
        orderItemRepository.save(oi);

        // 7. Trừ tồn kho tại đúng chi nhánh đã chọn trong bảng Inventory
        Optional<Inventory> invOpt = inventoryRepository.findByStoreIdAndBookId(store.getId(), book.getId());
        if (invOpt.isPresent()) {
            Inventory inv = invOpt.get();
            int currentQty = inv.getQuantity() != null ? inv.getQuantity() : 0;
            inv.setQuantity(Math.max(0, currentQty - quantity));
            inventoryRepository.save(inv);
        }

        // 8. Tăng tổng số lượng đã bán (totalSold) của sách
        book.setTotalSold((book.getTotalSold() != null ? book.getTotalSold() : 0) + quantity);
        bookRepository.save(book);

        // Ghi chú cho Cường: Tuyệt đối KHÔNG xóa hay can thiệp giỏ hàng (không gọi cartService.clearCart)

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
