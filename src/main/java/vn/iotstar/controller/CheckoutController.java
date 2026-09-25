package vn.iotstar.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.config.VNPayConfig;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ICartService;
import vn.iotstar.service.IOrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý luồng Đặt hàng chuỗi sách cũ & Thanh toán VNPay Sandbox / COD.
 * Ghi chú cho Cường:
 * 1. Phân luồng 2 phương thức nhận sách:
 *    - Click & Collect: Khách nhận trực tiếp tại 1 trong 5 chi nhánh TP.HCM (Miễn phí 100% tiền ship).
 *    - Giao tận nơi (Home Delivery): Chọn đơn vị vận chuyển (GHTK, GHN, Viettel Post) hoặc miễn phí nếu đơn >= 150.000₫.
 * 2. Tích hợp thanh toán trực tuyến qua Cổng VNPay Sandbox với mã băm HMAC-SHA512.
 * 3. Hỗ trợ tài khoản đăng nhập hoặc chế độ khách mẫu (Phúc - ID: 4).
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Long DEFAULT_GUEST_USER_ID = 4L;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ShippingUnitRepository shippingUnitRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private BookRepository bookRepository;

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        return DEFAULT_GUEST_USER_ID;
    }

    /**
     * Màn hình thanh toán & chọn hình thức nhận sách.
     * Ghi chú cho Cường:
     * - Hỗ trợ cả 2 chế độ:
     *   1. Thanh toán toàn bộ giỏ hàng (mặc định buyNow=false).
     *   2. Mua Ngay trực tiếp 1 cuốn sách (buyNow=true, có bookId): Tạo CartItem transient hiển thị ngay trên UI mà không cần thêm vào giỏ DB.
     * URL: /checkout
     */
    @GetMapping
    public String checkoutView(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "buyNow", required = false, defaultValue = "false") boolean buyNow,
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "storeId", required = false, defaultValue = "1") Long storeId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            Model model) {

        Long userId = resolveUserId(userDetails);
        List<CartItem> cartItems;
        BigDecimal subtotal;
        BigDecimal totalSavings;
        int totalQuantity;

        if (buyNow && bookId != null) {
            // Chế độ Mua Ngay: Tìm sách từ BookRepository, tạo CartItem tạm thời (transient)
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isEmpty()) {
                return "redirect:/cart";
            }
            Book book = bookOpt.get();
            int directQty = (quantity != null && quantity > 0) ? quantity : 1;

            Store selectedStore = storeRepository.findById(storeId).orElse(null);

            CartItem directItem = new CartItem();
            directItem.setBook(book);
            directItem.setQuantity(directQty);
            directItem.setStore(selectedStore);

            cartItems = List.of(directItem);
            subtotal = directItem.getItemTotal();
            totalSavings = directItem.getSavings();
            totalQuantity = directQty;

            model.addAttribute("buyNow", true);
            model.addAttribute("directBookId", bookId);
            model.addAttribute("directStoreId", storeId);
            model.addAttribute("directQuantity", directQty);
        } else {
            // Chế độ thanh toán từ Giỏ hàng thông thường
            Cart cart = cartService.getOrCreateCartForUser(userId);
            cartItems = cartService.getCartItemsWithDetails(cart.getId());

            if (cartItems.isEmpty()) {
                return "redirect:/cart";
            }

            // Tính toán tổng tiền
            subtotal = cartItems.stream()
                    .map(CartItem::getItemTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalSavings = cartItems.stream()
                    .map(CartItem::getSavings)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalQuantity = cartItems.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();

            model.addAttribute("buyNow", false);
            model.addAttribute("directBookId", null);
            model.addAttribute("directStoreId", null);
            model.addAttribute("directQuantity", null);
        }

        // Nạp danh sách 5 chi nhánh TP.HCM, đơn vị giao hàng, địa chỉ và voucher
        List<Store> stores = storeRepository.findByIsActiveTrue();
        List<ShippingUnit> shippingUnits = shippingUnitRepository.findByIsActiveTrue();
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        List<Voucher> vouchers = voucherRepository.findByIsActiveTrue();

        Address defaultAddress = addresses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .orElse(addresses.isEmpty() ? null : addresses.get(0));

        String formattedAddress = defaultAddress != null 
                ? (defaultAddress.getStreetAddress() + ", " + defaultAddress.getWard() + ", " + defaultAddress.getDistrict() + ", " + defaultAddress.getProvince())
                : "Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh";
        String receiverName = defaultAddress != null ? defaultAddress.getReceiverName() : "Nguyễn Song Hoàng Phúc";
        String receiverPhone = defaultAddress != null ? defaultAddress.getPhone() : "0912345678";

        model.addAttribute("formattedAddress", formattedAddress);
        model.addAttribute("receiverName", receiverName);
        model.addAttribute("receiverPhone", receiverPhone);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("stores", stores);
        model.addAttribute("shippingUnits", shippingUnits);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("isGuestMode", userDetails == null);
        model.addAttribute("pageTitle", "Thanh Toán Đơn Hàng - Chuỗi Sách Cũ TP.HCM");

        return "checkout";
    }

    /**
     * Xử lý xác nhận đặt hàng từ form thanh toán.
     * Ghi chú cho Cường:
     * - Nếu buyNow=true và bookId!=null: Gọi orderService.createOrderDirect tạo đơn trực tiếp cho cuốn sách đó, không đụng vào giỏ hàng.
     * - Nếu buyNow=false: Gọi orderService.createOrderFromCart chuyển toàn bộ giỏ hàng thành đơn hàng.
     * - Cả hai đều tích hợp VNPay Sandbox và COD.
     * URL: POST /checkout/place-order
     */
    @PostMapping("/place-order")
    public String placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("deliveryMethod") String deliveryMethodStr,
            @RequestParam("storeId") Long storeId,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam(value = "receiverAddress", required = false) String receiverAddress,
            @RequestParam(value = "shippingUnitId", required = false) Integer shippingUnitId,
            @RequestParam("paymentMethod") String paymentMethodStr,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam(value = "customerNotes", required = false) String customerNotes,
            @RequestParam(value = "buyNow", required = false, defaultValue = "false") boolean buyNow,
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Long userId = resolveUserId(userDetails);

        Order.DeliveryMethod deliveryMethod = "STORE_PICKUP".equalsIgnoreCase(deliveryMethodStr)
                ? Order.DeliveryMethod.STORE_PICKUP : Order.DeliveryMethod.HOME_DELIVERY;

        Order.PaymentMethod paymentMethod = "VNPAY".equalsIgnoreCase(paymentMethodStr)
                ? Order.PaymentMethod.VNPAY : Order.PaymentMethod.COD;

        // Nếu nhận tại chi nhánh, lấy địa chỉ của chính chi nhánh đó
        if (deliveryMethod == Order.DeliveryMethod.STORE_PICKUP) {
            Store pickupStore = storeRepository.findById(storeId).orElse(null);
            if (pickupStore != null) {
                receiverAddress = "Nhận trực tiếp tại: " + pickupStore.getStoreName() + " (" + pickupStore.getAddress() + ")";
            }
        }

        try {
            Order order;
            if (buyNow && bookId != null) {
                int finalQty = (quantity != null && quantity > 0) ? quantity : 1;
                order = orderService.createOrderDirect(
                        userId,
                        bookId,
                        storeId,
                        finalQty,
                        deliveryMethod,
                        receiverName,
                        receiverPhone,
                        receiverAddress != null ? receiverAddress : "Tại chi nhánh",
                        shippingUnitId,
                        paymentMethod,
                        voucherCode,
                        customerNotes
                );
            } else {
                order = orderService.createOrderFromCart(
                        userId,
                        deliveryMethod,
                        storeId,
                        receiverName,
                        receiverPhone,
                        receiverAddress != null ? receiverAddress : "Tại chi nhánh",
                        shippingUnitId,
                        paymentMethod,
                        voucherCode,
                        customerNotes
                );
            }

            // Nếu chọn thanh toán trực tuyến qua VNPay Sandbox
            if (paymentMethod == Order.PaymentMethod.VNPAY) {
                long amountVnd = order.getFinalAmount().longValue();
                String paymentUrl = VNPayConfig.createPaymentUrl(order.getOrderCode(), amountVnd, request);
                return "redirect:" + paymentUrl;
            }

            // Thanh toán khi nhận sách (COD)
            return "redirect:/checkout/success?orderCode=" + order.getOrderCode();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo đơn hàng: " + e.getMessage());
            if (buyNow && bookId != null) {
                return "redirect:/checkout?buyNow=true&bookId=" + bookId + "&storeId=" + storeId + "&quantity=" + quantity;
            }
            return "redirect:/checkout";
        }
    }

    /**
     * Xử lý phản hồi Return URL từ Cổng thanh toán VNPay Sandbox sau khi khách hoàn tất giao dịch.
     * URL: GET /checkout/vnpay-return
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(
            @RequestParam("vnp_TxnRef") String orderCode,
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam(value = "vnp_TransactionNo", required = false) String transactionNo,
            RedirectAttributes redirectAttributes) {

        // Mã phản hồi 00 = Giao dịch thành công
        if ("00".equals(responseCode)) {
            orderService.updatePaymentSuccess(orderCode, transactionNo);
            redirectAttributes.addFlashAttribute("paymentSuccess", true);
            return "redirect:/checkout/success?orderCode=" + orderCode + "&vnpay=success";
        } else {
            redirectAttributes.addFlashAttribute("paymentFailed", true);
            return "redirect:/checkout/success?orderCode=" + orderCode + "&vnpay=failed";
        }
    }

    /**
     * Màn hình thông báo đặt hàng thành công.
     * URL: GET /checkout/success
     */
    @GetMapping("/success")
    public String checkoutSuccess(@RequestParam("orderCode") String orderCode, Model model) {
        Optional<Order> orderOpt = orderService.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            return "redirect:/";
        }

        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Đặt Hàng Thành Công #" + order.getOrderCode() + " - Chuỗi Sách Cũ");

        return "checkout-success";
    }
}
