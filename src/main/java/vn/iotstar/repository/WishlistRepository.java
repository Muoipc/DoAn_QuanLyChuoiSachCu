package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Wishlist;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Lấy toàn bộ sách yêu thích của người dùng kèm Book và hình ảnh để tránh LazyInitializationException.
     */
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.book b LEFT JOIN FETCH b.images WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdWithBook(@Param("userId") Long userId);

    Optional<Wishlist> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Transactional
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    long countByBookId(Long bookId);
}
