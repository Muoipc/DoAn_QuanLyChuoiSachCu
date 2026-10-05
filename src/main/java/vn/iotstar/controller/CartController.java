package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Cart;
import vn.iotstar.entity.CartItem;
import vn.iotstar.entity.Inventory;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ICartService;
import vn.iotstar.repository.InventoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller điều hướng và xử lý giỏ hàng lưu CSDL MySQL.
 * Ghi chú cho Cường:
 * 1. Hỗ trợ cả người dùng đã đăng nhập (lấy userId từ CustomUserDetails) và khách trải nghiệm nhanh (mặc định userId = 4 của Phúc).
 * 2. Mỗi mục trong giỏ ghi nhận chính xác cuốn sách nào và chi nhánh nào trong 5 chi nhánh TP.HCM mà khách chọn.
 * 3. Hỗ trợ cả tương tác submit form chuẩn lẫn gọi AJAX để Header cập nhật số lượng trực tiếp.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    // User ID demo mặc định khi khách chưa đăng nhập (Nguyễn Song Hoàng Phúc - ID: 4)
    private static final Long DEFAULT_GUEST_USER_ID = 4L;

    @Autowired
    private ICartService cartService;

    @Autowired
    private vn.iotstar.repository.VoucherRepository voucherRepository;

    /** Repository tồn kho — dùng để kiểm tra số lượng có sẵn trước khi thêm giỏ hàng */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Xác định userId của phiên hiện tại: ưu tiên tài khoản đã đăng nhập Spring Security.
     */
    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        return DEFAULT_GUEST_USER_ID;
    }

    /**
     * Hiển thị trang giỏ hàng chuẩn sàn Shopee.
     * URL: /cart
     */
    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = resolveUserId(userDetails);
        Cart cart = cartService.getOrCreateCartForUser(userId);
        List<CartItem> cartItems = cartService.getCartItemsWithDetails(cart.getId());

        // Tính toán tổng tiền, tiết kiệm và tổng số lượng
        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSavings = cartItems.stream()
                .map(CartItem::getSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        List<vn.iotstar.entity.Voucher> vouchers = voucherRepository.findByIsActiveTrue();

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("isGuestMode", userDetails == null);
        model.addAttribute("pageTitle", "Giỏ Hàng (" + totalQuantity + " cuốn sách) - Chuỗi Sách Cũ");

        return "cart";
    }

    /**
     * Thêm sách cũ vào giỏ hàng hoặc Mua Ngay.
     * Ghi chú cho Cường:
     * - Nếu action là "buy_now" hoặc "buynow": Điều hướng thẳng đến trang /checkout kèm param buyNow=true, bookId, storeId, quantity mà không lưu vào giỏ hàng CSDL.
     * - Nếu action là thêm vào giỏ thông thường: Gọi cartService.addToCart và quay lại trang chi tiết sách.
     * URL: POST /cart/add
     */
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("bookId") Long bookId,
            @RequestParam(value = "storeId", defaultValue = "1") Long storeId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            @RequestParam(value = "action", defaultValue = "add") String action,
            RedirectAttributes redirectAttributes) {

        // Tính năng Mua Ngay (Direct Buy Now / Instant Checkout): Không lưu CSDL cart, chuyển hướng ngay đến checkout
        if ("buy_now".equalsIgnoreCase(action) || "buynow".equalsIgnoreCase(action)) {
            return "redirect:/checkout?buyNow=true&bookId=" + bookId + "&storeId=" + storeId + "&quantity=" + quantity;
        }

        Long userId = resolveUserId(userDetails);
        cartService.addToCart(userId, bookId, storeId, quantity);

        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sách vào giỏ hàng thành công!");
        return "redirect:/books/" + bookId;
    }

    /**
     * Cập nhật số lượng sách trong giỏ.
     * URL: POST /cart/update
     */
    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam("itemId") Long itemId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        cartService.updateQuantity(itemId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng sách!");
        return "redirect:/cart";
    }

    /**
     * Xóa một cuốn sách khỏi giỏ hàng.
     * URL: GET/POST /cart/remove/{itemId}
     */
    @GetMapping("/remove/{itemId}")
    public String removeItem(@PathVariable("itemId") Long itemId, RedirectAttributes redirectAttributes) {
        cartService.removeCartItem(itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách khỏi giỏ hàng!");
        return "redirect:/cart";
    }

    /**
     * Xóa toàn bộ sản phẩm khỏi giỏ hàng.
     * URL: GET/POST /cart/clear
     */
    @RequestMapping(value = "/clear", method = {RequestMethod.GET, RequestMethod.POST})
    public String clearCart(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        Long userId = resolveUserId(userDetails);
        Cart cart = cartService.getOrCreateCartForUser(userId);
        cartService.clearCart(cart.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ sản phẩm khỏi giỏ hàng!");
        return "redirect:/cart";
    }

    /**
     * API AJAX lấy số lượng sách hiện có trong giỏ hàng để cập nhật Badge đỏ cam trên Header.
     * URL: GET /cart/api/count
     */
    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        int count = cartService.getCartTotalCount(userId);
        return ResponseEntity.ok(Map.of("count", count, "success", true));
    }

    /**
     * API AJAX thêm sách vào giỏ hàng phục vụ hiệu ứng bay vào giỏ hàng không tải lại trang.
     * URL: POST /cart/api/add
     */
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCartAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("bookId") Long bookId,
            @RequestParam(value = "storeId", defaultValue = "1") Long storeId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        Long userId = resolveUserId(userDetails);

        // Kiểm tra tồn kho: tổng số lượng có sẵn tại tất cả chi nhánh
        List<Inventory> inventories = inventoryRepository.findByBookId(bookId);
        int totalStock = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        if (totalStock > 0 && quantity > totalStock) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Số lượng vượt quá tồn kho! Chỉ còn " + totalStock + " sản phẩm có sẵn."
            ));
        }

        cartService.addToCart(userId, bookId, storeId, quantity);
        int newTotal = cartService.getCartTotalCount(userId);
        return ResponseEntity.ok(Map.of("count", newTotal, "success", true, "message", "Đã thêm vào giỏ hàng thành công!"));
    }

    /**
     * API AJAX lấy danh sách sản phẩm mới thêm vào giỏ hàng (tối đa 5 cuốn)
     * phục vụ Shopee Cart Popover khi rê chuột vào icon giỏ hàng trên Header.
     * URL: GET /cart/api/preview
     */
    @GetMapping("/api/preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartPreview(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        Cart cart = cartService.getOrCreateCartForUser(userId);
        List<CartItem> allItems = cartService.getCartItemsWithDetails(cart.getId());

        int totalCount = allItems.stream().mapToInt(CartItem::getQuantity).sum();
        int displayLimit = 5;
        List<Map<String, Object>> itemsList = new java.util.ArrayList<>();

        for (int i = 0; i < Math.min(displayLimit, allItems.size()); i++) {
            CartItem ci = allItems.get(i);
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", ci.getId());
            map.put("bookId", ci.getBook() != null ? ci.getBook().getId() : 1);
            map.put("title", ci.getBook() != null ? ci.getBook().getTitle() : "Sách cũ");
            map.put("price", ci.getBook() != null && ci.getBook().getPrice() != null ? ci.getBook().getPrice() : 0);
            map.put("quantity", ci.getQuantity());
            map.put("imageUrl", ci.getBook() != null && ci.getBook().getPrimaryImageUrl() != null 
                    ? ci.getBook().getPrimaryImageUrl() : "/images/books/book_1.jpg");
            map.put("conditionPercent", ci.getBook() != null ? ci.getBook().getConditionPercent() : 90);
            itemsList.add(map);
        }

        int displayedCount = itemsList.stream().mapToInt(m -> (int) m.get("quantity")).sum();
        int moreCount = Math.max(0, totalCount - displayedCount);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("totalCount", totalCount);
        response.put("moreCount", moreCount);
        response.put("items", itemsList);

        return ResponseEntity.ok(response);
    }
}
