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
