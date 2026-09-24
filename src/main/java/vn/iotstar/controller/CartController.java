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
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ICartService;

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

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("isGuestMode", userDetails == null);
        model.addAttribute("pageTitle", "Giỏ Hàng (" + totalQuantity + " cuốn sách) - Chuỗi Sách Cũ");

        return "cart";
    }

    /**
     * Thêm sách cũ vào giỏ hàng.
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

        Long userId = resolveUserId(userDetails);
        cartService.addToCart(userId, bookId, storeId, quantity);

        if ("buynow".equalsIgnoreCase(action)) {
            return "redirect:/cart";
        }

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
}
