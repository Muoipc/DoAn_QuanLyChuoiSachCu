package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartIdAndBookIdAndStoreId(Long cartId, Long bookId, Long storeId);
    void deleteByCartId(Long cartId);

    /**
     * Ghi chú cho Cường: Truy vấn danh sách mục trong giỏ hàng kèm Book (và ảnh) và Store (chi nhánh lấy sách)
     * sử dụng JOIN FETCH để tránh lỗi LazyInitializationException và tối ưu thành 1 câu truy vấn SQL duy nhất.
     */
    @Query("SELECT DISTINCT ci FROM CartItem ci JOIN FETCH ci.book b LEFT JOIN FETCH b.images JOIN FETCH ci.store s WHERE ci.cart.id = :cartId ORDER BY ci.addedAt DESC")
    List<CartItem> findByCartIdWithDetails(@Param("cartId") Long cartId);
}
