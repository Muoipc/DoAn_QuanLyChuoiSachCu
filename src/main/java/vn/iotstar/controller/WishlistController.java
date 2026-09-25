package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Wishlist;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.WishlistRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý tính năng Yêu thích / Wishlist của người dùng.
 * Phân hệ: Khách hàng (Hoàng Phúc - MSSV: 24162096).
 */
@Controller
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        return null;
    }

    /**
     * API AJAX bật/tắt yêu thích cuốn sách (Like / Unlike).
     * URL: POST /api/wishlist/toggle
     */
    @PostMapping("/api/wishlist/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("bookId") Long bookId) {

        Map<String, Object> response = new HashMap<>();
        Long userId = resolveUserId(userDetails);

        if (userId == null) {
            response.put("success", false);
            response.put("requireLogin", true);
            response.put("message", "Vui lòng đăng nhập để lưu sách vào danh sách yêu thích!");
            return ResponseEntity.ok(response);
        }

        Book book = bookRepository.findById(bookId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (book == null || user == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy thông tin cuốn sách hoặc người dùng!");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isLiked;
        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
            isLiked = false;
            response.put("message", "Đã bỏ yêu thích cuốn sách!");
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setBook(book);
            wishlistRepository.save(wishlist);
            isLiked = true;
            response.put("message", "Đã thêm cuốn sách vào danh sách yêu thích!");
        }

        long actualLikes = wishlistRepository.countByBookId(bookId);
        // Base seed cộng thêm để số lượt thích luôn thực tế và đẹp mắt
        long totalDisplayLikes = 120 + actualLikes;

        response.put("success", true);
        response.put("liked", isLiked);
        response.put("likesCount", totalDisplayLikes);

        return ResponseEntity.ok(response);
    }

    /**
     * Trang xem danh sách các cuốn sách đã lưu yêu thích.
     * URL: GET /user/wishlist
     */
    @GetMapping("/user/wishlist")
    public String myWishlistView(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        List<Wishlist> wishlistItems = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId, null).getContent();
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("pageTitle", "Sách Yêu Thích Của Tôi - Chuỗi Sách Cũ");

        return "user/wishlist";
    }
}
