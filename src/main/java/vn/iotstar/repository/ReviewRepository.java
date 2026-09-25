package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Review;

import java.util.List;

/**
 * Repository Quản lý dữ liệu Đánh giá & Nhận xét sách (Reviews).
 * Ghi chú cho Cường:
 * - Hỗ trợ đếm tổng số đánh giá và đếm riêng theo từng mức sao (1, 2, 3, 4, 5 sao) phục vụ bộ lọc chuẩn Shopee.
 * - Hỗ trợ lọc danh sách nhận xét theo số sao hoặc lấy tất cả mới nhất.
 * - Kiểm tra lịch sử mua hàng của khách để hiển thị huy hiệu 'Đã mua tại cửa hàng' và chống spam review.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Lấy danh sách toàn bộ đánh giá của 1 cuốn sách sắp xếp từ mới nhất đến cũ nhất.
     */
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

    /**
     * Lấy danh sách đánh giá có phân trang phục vụ lazy-loading nếu sách có quá nhiều review.
     */
    Page<Review> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

    /**
     * Lấy danh sách đánh giá của 1 cuốn sách theo đúng số sao đánh giá (1 - 5 sao).
     * Phục vụ bộ lọc Pill Filter Chips chuẩn Shopee trên giao diện.
     */
    List<Review> findByBookIdAndRatingOrderByCreatedAtDesc(Long bookId, Integer rating);

    /**
     * Đếm số lượng đánh giá của 1 cuốn sách theo số sao cụ thể (Ví dụ: đếm xem có bao nhiêu lượt 5 sao).
     */
    long countByBookIdAndRating(Long bookId, Integer rating);

    /**
     * Đếm tổng số lượt đánh giá của 1 cuốn sách.
     */
    long countByBookId(Long bookId);

    /**
     * Tính điểm đánh giá trung bình (1.0 -> 5.0) của một cuốn sách.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);

    /**
     * Kiểm tra người dùng đã đánh giá cuốn sách này trong một đơn hàng cụ thể chưa (tránh spam nhiều lần trên cùng 1 đơn).
     */
    boolean existsByUserIdAndBookIdAndOrderId(Long userId, Long bookId, Long orderId);

    /**
     * Kiểm tra người dùng đã từng gửi đánh giá cho cuốn sách này chưa.
     */
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Lấy toàn bộ đánh giá do một người dùng đã viết.
     */
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Lấy danh sách đánh giá thuộc về một đơn hàng cụ thể.
     */
    List<Review> findByOrderId(Long orderId);

    /**
     * Tìm danh sách đơn hàng ở trạng thái ĐÃ GIAO (DELIVERED) của người dùng có chứa cuốn sách này.
     * Dùng để xác thực người dùng đã mua sách thực tế tại chuỗi cửa hàng trước khi đánh giá.
     */
    @Query("SELECT o FROM Order o JOIN o.items i WHERE o.user.id = :userId AND i.book.id = :bookId AND o.orderStatus = vn.iotstar.entity.Order.OrderStatus.DELIVERED ORDER BY o.createdAt DESC")
    List<Order> findDeliveredOrdersByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
