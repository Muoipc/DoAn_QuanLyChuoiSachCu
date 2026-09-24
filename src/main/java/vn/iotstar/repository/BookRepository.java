package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findBySlug(String slug);

    /**
     * Ghi chú cho Cường: Truy vấn danh sách sách bán chạy trên mức số lượng quy định (minSold),
     * kết hợp FETCH JOIN để load sẵn danh mục và ảnh sách, tránh lỗi LazyInitializationException.
     */
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.images WHERE b.isActive = true AND b.totalSold >= :minSold ORDER BY b.totalSold DESC")
    List<Book> findTopSoldWithImages(@Param("minSold") int minSold);

    /**
     * Ghi chú cho Cường: Lấy toàn bộ sách cũ đang hoạt động để hiển thị trên trang chủ 'Gợi ý hôm nay'.
     */
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.images WHERE b.isActive = true ORDER BY b.totalSold DESC")
    List<Book> findAllActiveWithImages();

    List<Book> findTop10ByTotalSoldGreaterThanEqualAndIsActiveTrueOrderByTotalSoldDesc(int minSold);

    List<Book> findTop20ByIsActiveTrueOrderByCreatedAtDesc();

    List<Book> findTop20ByIsActiveTrueOrderByTotalSoldDesc();

    Page<Book> findByCategoryIdAndIsActiveTrue(Integer categoryId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "b.isbn LIKE CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true " +
           "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
           "AND (:minCondition IS NULL OR b.conditionPercent >= :minCondition) " +
           "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR b.price <= :maxPrice)")
    Page<Book> filterBooks(
        @Param("categoryId") Integer categoryId,
        @Param("minCondition") Integer minCondition,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}
