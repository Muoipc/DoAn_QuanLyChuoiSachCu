package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByStoreIdAndBookId(Long storeId, Long bookId);
    List<Inventory> findByBookId(Long bookId);
    List<Inventory> findByStoreId(Long storeId);

    /**
     * Ghi chú cho Cường: Lấy toàn bộ danh sách tồn kho của cuốn sách tại 5 chi nhánh TP.HCM,
     * FETCH JOIN store để nạp sẵn thông tin chi nhánh (tên, địa chỉ, giờ mở cửa, hotline)
     * giúp hiển thị tình trạng còn hàng từng kho trên trang chi tiết sách.
     */
    @Query("SELECT i FROM Inventory i JOIN FETCH i.store s WHERE i.book.id = :bookId AND s.isActive = true ORDER BY s.id ASC")
    List<Inventory> findByBookIdWithStore(@Param("bookId") Long bookId);
}
