package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Review;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.ReviewRepository;
import vn.iotstar.repository.WishlistRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý chi tiết sách và xem tồn kho chuỗi 5 chi nhánh TP.HCM.
 * Ghi chú cho Cường: Controller này phụ trách:
 * 1. Nạp chi tiết cuốn sách (ảnh chụp thật, độ mới %, tình trạng bìa/gáy/ruột sách theo thẩm định).
 * 2. Nạp tình trạng tồn kho thời gian thực tại 5 chi nhánh TP.HCM từ bảng inventory.
 * 3. Nạp danh sách đánh giá của độc giả và gợi ý các cuốn sách cùng thể loại.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    /**
     * Xem chi tiết sách theo ID.
     * URL: /books/{id} (Ví dụ: /books/1)
     */
    @GetMapping("/{id}")
    public String bookDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        // 1. Lấy thông tin sách kèm ảnh và danh mục
        Optional<Book> bookOpt = bookRepository.findDetailById(id);
        if (bookOpt.isEmpty()) {
            bookOpt = bookRepository.findById(id);
        }

        if (bookOpt.isEmpty()) {
            return "redirect:/";
        }

        Book book = bookOpt.get();

        // 2. Tăng lượt xem (views_count)
        book.setViewsCount(book.getViewsCount() != null ? book.getViewsCount() + 1 : 1);
        bookRepository.save(book);

        // 3. Lấy thông tin tồn kho tại 5 chi nhánh TP.HCM
        List<Inventory> inventoryList = inventoryRepository.findByBookIdWithStore(id);
        int totalStock = inventoryList.stream().mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0).sum();

        // 4. Lấy danh sách đánh giá & điểm đánh giá trung bình
        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(id);
        Double avgRating = reviewRepository.getAverageRatingByBookId(id);
        if (avgRating == null) {
            avgRating = 5.0;
        }
        long reviewCount = reviewRepository.countByBookId(id);

        long count5Star = reviewRepository.countByBookIdAndRating(id, 5);
        long count4Star = reviewRepository.countByBookIdAndRating(id, 4);
        long count3Star = reviewRepository.countByBookIdAndRating(id, 3);
        long count2Star = reviewRepository.countByBookIdAndRating(id, 2);
        long count1Star = reviewRepository.countByBookIdAndRating(id, 1);

        // 5. Lấy sách liên quan cùng danh mục
        List<Book> relatedBooks = List.of();
        if (book.getCategory() != null) {
            relatedBooks = bookRepository.findRelatedBooks(
                    book.getCategory().getId(),
                    book.getId(),
                    PageRequest.of(0, 6)
            );
        }

        // 6. Kiểm tra trạng thái yêu thích (Wishlist) và lịch sử mua sách của người dùng hiện tại
        boolean isLiked = false;
        boolean hasPurchased = false;
        boolean hasReviewed = false;
        if (userDetails != null && userDetails.getId() != null) {
            Long currentUserId = userDetails.getId();
            isLiked = wishlistRepository.existsByUserIdAndBookId(currentUserId, id);
            hasPurchased = !reviewRepository.findDeliveredOrdersByUserIdAndBookId(currentUserId, id).isEmpty();
            hasReviewed = reviewRepository.existsByUserIdAndBookId(currentUserId, id);
        }
        long actualLikes = wishlistRepository.countByBookId(id);
        long likesCount = 120 + actualLikes;

        // 7. Đưa dữ liệu vào Model cho Thymeleaf render
        model.addAttribute("book", book);
        model.addAttribute("inventoryList", inventoryList);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", String.format(java.util.Locale.US, "%.1f", avgRating));
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("count5Star", count5Star);
        model.addAttribute("count4Star", count4Star);
        model.addAttribute("count3Star", count3Star);
        model.addAttribute("count2Star", count2Star);
        model.addAttribute("count1Star", count1Star);
        model.addAttribute("hasPurchased", hasPurchased);
        model.addAttribute("hasReviewed", hasReviewed);
        model.addAttribute("isLoggedIn", userDetails != null);
        model.addAttribute("relatedBooks", relatedBooks);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likesCount", likesCount);
        model.addAttribute("pageTitle", book.getTitle() + " - Chuỗi Sách Cũ");

        return "book-detail";
    }

    /**
     * Xem chi tiết sách theo Slug thân thiện SEO.
     * URL: /books/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public String bookDetailBySlug(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        Optional<Book> bookOpt = bookRepository.findDetailBySlug(slug);
        if (bookOpt.isPresent()) {
            return bookDetail(bookOpt.get().getId(), userDetails, model);
        }
        return "redirect:/";
    }
}
