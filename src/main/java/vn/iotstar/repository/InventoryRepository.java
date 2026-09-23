package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByStoreIdAndBookId(Long storeId, Long bookId);
    List<Inventory> findByBookId(Long bookId);
    List<Inventory> findByStoreId(Long storeId);
}
