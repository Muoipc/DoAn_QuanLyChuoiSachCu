package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);
    Page<Review> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);
    boolean existsByUserIdAndBookIdAndOrderId(Long userId, Long bookId, Long orderId);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Review> findByOrderId(Long orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);

    long countByBookId(Long bookId);
}
