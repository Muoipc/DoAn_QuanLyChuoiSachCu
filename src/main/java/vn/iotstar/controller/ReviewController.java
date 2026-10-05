package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ReviewDTO;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Review;
import vn.iotstar.entity.User;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ReviewRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller Chuyên trách xử lý Đánh Giá Sao & Phản Hồi Sách Cũ (Star Rating & Reviews).
 * Ghi chú cho Cường:
 * 1. POST /reviews/create: Nhận submit từ form HTML truyền thống (order-history, order-detail, book-detail),
 *    tự động redirect về trang gọi với flash attribute thông báo thành công hoặc lỗi.
 * 2. POST /api/reviews/submit: API REST nhận đánh giá qua AJAX mượt mà, lưu vào CSDL, cập nhật điểm trung bình,
 *    trả về JSON cập nhật trực tiếp DOM mà không cần reload trang.
 * 3. GET /api/reviews/book/{bookId}: API REST lấy danh sách nhận xét có hỗ trợ bộ lọc theo số sao (1-5 sao hoặc tất cả),
 *    phục vụ các nút chip lọc sao chuẩn Shopee.
 */
@Controller
public class ReviewController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Long DEFAULT_GUEST_USER_ID = 4L;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Xác định User ID hiện tại: ưu tiên từ phiên đăng nhập Spring Security,
     * nếu là chế độ test cục bộ thì dùng DEFAULT_GUEST_USER_ID.
     */
    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        return DEFAULT_GUEST_USER_ID;
    }

    /**
     * Helper chuyển đổi từ Entity Review sang ReviewDTO an toàn cho JSON và View.
     */
    private ReviewDTO convertToDTO(Review review) {
        if (review == null) return null;

        String userName = "Khách Hàng";
        String userAvatar = null;
        String userInitial = "K";
        Long userId = null;

        if (review.getUser() != null) {
            userId = review.getUser().getId();
            userName = (review.getUser().getFullName() != null && !review.getUser().getFullName().isBlank())
                    ? review.getUser().getFullName()
                    : review.getUser().getUsername();
            userAvatar = review.getUser().getAvatar();
            if (userName != null && !userName.isBlank()) {
                userInitial = userName.trim().substring(0, 1).toUpperCase();
            }
        }

        String label = getRatingLabel(review.getRating());
        String createdAtStr = review.getCreatedAt() != null
                ? review.getCreatedAt().format(DATE_FORMATTER)
                : "";

        boolean isPurchased = (review.getOrder() != null);
        if (!isPurchased && userId != null && review.getBook() != null) {
            isPurchased = !reviewRepository.findDeliveredOrdersByUserIdAndBookId(userId, review.getBook().getId()).isEmpty();
        }

        return new ReviewDTO(
                review.getId(),
                review.getBook() != null ? review.getBook().getId() : null,
                userId,
                userName,
                userAvatar,
                userInitial,
                review.getRating(),
                label,
                review.getComment(),
                review.getMediaUrl(),
                review.getMediaType() != null ? review.getMediaType().name() : "NONE",
                createdAtStr,
                isPurchased
        );
    }

    private String getRatingLabel(Integer rating) {
        if (rating == null) return "Bình thường";
        return switch (rating) {
            case 1 -> "Tệ";
            case 2 -> "Chưa tốt";
            case 3 -> "Bình thường";
            case 4 -> "Hài lòng";
            case 5 -> "Tuyệt vời";
            default -> "Hài lòng";
        };
    }

    /**
     * Tiếp nhận đánh giá thông qua Form POST truyền thống.
     * URL: POST /reviews/create
     */
    @PostMapping("/reviews/create")
    public String createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("bookId") Long bookId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "mediaUrl", required = false) String mediaUrl,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {

        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để gửi đánh giá chất lượng sách!");
            return "redirect:/login";
        }

        // Kiểm tra xem đã đánh giá trong đơn hàng này chưa
        if (orderId != null && reviewRepository.existsByUserIdAndBookIdAndOrderId(userId, bookId, orderId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá cuốn sách này trong đơn hàng rồi!");
            return getSafeRedirect(redirectUrl, orderId, bookId);
        }

        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);
        Order order = (orderId != null) ? orderRepository.findById(orderId).orElse(null) : null;

        // Nếu không có orderId từ form, thử tìm đơn hàng DELIVERED gần nhất của user có sách này
        if (order == null && user != null && book != null) {
            List<Order> deliveredOrders = reviewRepository.findDeliveredOrdersByUserIdAndBookId(userId, bookId);
            if (!deliveredOrders.isEmpty()) {
                order = deliveredOrders.get(0);
            }
        }

        if (user != null && book != null) {
            Review review = new Review();
            review.setUser(user);
            review.setBook(book);
            review.setOrder(order);
            review.setRating(Math.max(1, Math.min(5, rating != null ? rating : 5)));
            review.setComment(comment != null && !comment.trim().isEmpty() ? comment.trim() : "Sách đúng cam kết mô tả của cửa hàng.");
            if (mediaUrl != null && !mediaUrl.isBlank()) {
                review.setMediaUrl(mediaUrl.trim());
                review.setMediaType(Review.MediaType.IMAGE);
            } else {
                review.setMediaType(Review.MediaType.NONE);
            }
            reviewRepository.save(review);

            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá chất lượng sách cũ! Đánh giá đã được ghi nhận thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin sách hoặc người dùng để lưu đánh giá.");
        }

        return getSafeRedirect(redirectUrl, orderId, bookId);
    }

    private String getSafeRedirect(String redirectUrl, Long orderId, Long bookId) {
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        if (orderId != null) {
            return "redirect:/orders";
        }
        return "redirect:/books/" + bookId;
    }

    /**
     * REST API tiếp nhận đánh giá sách gửi qua AJAX (JSON/FormData).
     * URL: POST /api/reviews/submit
     */
    @PostMapping("/api/reviews/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitReviewApi(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("bookId") Long bookId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "mediaUrl", required = false) String mediaUrl) {

        Map<String, Object> response = new HashMap<>();

        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để gửi đánh giá chất lượng sách!");
            return ResponseEntity.status(401).body(response);
        }

        // Kiểm tra xem đã đánh giá đơn hàng này chưa
        if (orderId != null && reviewRepository.existsByUserIdAndBookIdAndOrderId(userId, bookId, orderId)) {
            response.put("success", false);
            response.put("message", "Bạn đã đánh giá cuốn sách này trong đơn hàng rồi!");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);
        Order order = (orderId != null) ? orderRepository.findById(orderId).orElse(null) : null;

        if (order == null && user != null && book != null) {
            List<Order> deliveredOrders = reviewRepository.findDeliveredOrdersByUserIdAndBookId(userId, bookId);
            if (!deliveredOrders.isEmpty()) {
                order = deliveredOrders.get(0);
            }
        }

        if (user == null || book == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy cuốn sách hoặc tài khoản người dùng!");
            return ResponseEntity.badRequest().body(response);
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setOrder(order);
        review.setRating(Math.max(1, Math.min(5, rating != null ? rating : 5)));
        review.setComment(comment != null && !comment.trim().isEmpty() ? comment.trim() : "Sách đúng cam kết chất lượng.");
        if (mediaUrl != null && !mediaUrl.isBlank()) {
            review.setMediaUrl(mediaUrl.trim());
            review.setMediaType(Review.MediaType.IMAGE);
        } else {
            review.setMediaType(Review.MediaType.NONE);
        }
        Review saved = reviewRepository.save(review);

        // Lấy lại điểm trung bình và các mức sao mới nhất
        Double avgRating = reviewRepository.getAverageRatingByBookId(bookId);
        long totalReviews = reviewRepository.countByBookId(bookId);

        Map<String, Long> counts = new HashMap<>();
        counts.put("all", totalReviews);
        counts.put("star5", reviewRepository.countByBookIdAndRating(bookId, 5));
        counts.put("star4", reviewRepository.countByBookIdAndRating(bookId, 4));
        counts.put("star3", reviewRepository.countByBookIdAndRating(bookId, 3));
        counts.put("star2", reviewRepository.countByBookIdAndRating(bookId, 2));
        counts.put("star1", reviewRepository.countByBookIdAndRating(bookId, 1));

        response.put("success", true);
        response.put("message", "Gửi đánh giá sách thành công! Cảm ơn phản hồi quý báu của bạn.");
        response.put("avgRating", avgRating != null ? String.format(Locale.US, "%.1f", avgRating) : "5.0");
        response.put("reviewCount", totalReviews);
        response.put("counts", counts);
        response.put("review", convertToDTO(saved));

        return ResponseEntity.ok(response);
    }

    /**
     * REST API lấy danh sách nhận xét của 1 cuốn sách kèm bộ lọc theo mức sao (1-5 sao).
     * Phục vụ các chip lọc 'Tất Cả (N)', '5 Sao (N)', '4 Sao (N)'... chuẩn Shopee.
     * URL: GET /api/reviews/book/{bookId}?rating=5
     */
    @GetMapping("/api/reviews/book/{bookId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReviewsByBook(
            @PathVariable("bookId") Long bookId,
            @RequestParam(value = "rating", required = false) Integer rating) {

        Map<String, Object> response = new HashMap<>();

        List<Review> reviewList;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviewList = reviewRepository.findByBookIdAndRatingOrderByCreatedAtDesc(bookId, rating);
        } else {
            reviewList = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        }

        List<ReviewDTO> dtoList = reviewList.stream().map(this::convertToDTO).toList();

        Double avgRating = reviewRepository.getAverageRatingByBookId(bookId);
        long totalReviews = reviewRepository.countByBookId(bookId);

        Map<String, Long> counts = new HashMap<>();
        counts.put("all", totalReviews);
        counts.put("star5", reviewRepository.countByBookIdAndRating(bookId, 5));
        counts.put("star4", reviewRepository.countByBookIdAndRating(bookId, 4));
        counts.put("star3", reviewRepository.countByBookIdAndRating(bookId, 3));
        counts.put("star2", reviewRepository.countByBookIdAndRating(bookId, 2));
        counts.put("star1", reviewRepository.countByBookIdAndRating(bookId, 1));

        response.put("success", true);
        response.put("bookId", bookId);
        response.put("filterRating", rating != null ? rating : 0);
        response.put("totalReviews", totalReviews);
        response.put("filteredCount", dtoList.size());
        response.put("avgRating", avgRating != null ? String.format(Locale.US, "%.1f", avgRating) : "5.0");
        response.put("counts", counts);
        response.put("reviews", dtoList);

        return ResponseEntity.ok(response);
    }

    /**
     * REST API tiếp nhận tải lên hình ảnh thực tế của sách cũ cho bài đánh giá.
     * Hỗ trợ lưu trữ cục bộ phục vụ hiển thị tức thì chuẩn Shopee.
     * URL: POST /api/reviews/upload-image
     */
    @PostMapping("/api/reviews/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadReviewImage(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file == null || file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng chọn tệp hình ảnh hợp lệ!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            String filename = "review_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            // 1. Lưu vào thư mục target/classes để server đang chạy truy xuất ngay lập tức
            Path targetPath = Paths.get("target/classes/static/images/reviews", filename);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());

            // 2. Lưu vào thư mục src/main/resources để bền vững qua các lần biên dịch Maven
            try {
                Path srcPath = Paths.get("src/main/resources/static/images/reviews", filename);
                Files.createDirectories(srcPath.getParent());
                Files.write(srcPath, file.getBytes());
            } catch (Exception ignored) {}

            String imageUrl = "/images/reviews/" + filename;
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Tải ảnh thực tế thành công!");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi trong quá trình lưu trữ ảnh: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
