package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.StockTransfer;

import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    List<StockTransfer> findAllByOrderByCreatedAtDesc();

    List<StockTransfer> findByStatusOrderByCreatedAtDesc(StockTransfer.TransferStatus status);

    long countByStatus(StockTransfer.TransferStatus status);

    @Query("SELECT st FROM StockTransfer st " +
           "LEFT JOIN FETCH st.fromStore " +
           "LEFT JOIN FETCH st.toStore " +
           "LEFT JOIN FETCH st.book " +
           "LEFT JOIN FETCH st.requestedBy " +
           "LEFT JOIN FETCH st.approvedBy " +
           "WHERE (:status IS NULL OR st.status = :status) " +
           "AND (:storeId IS NULL OR st.fromStore.id = :storeId OR st.toStore.id = :storeId) " +
           "ORDER BY st.createdAt DESC")
    List<StockTransfer> searchTransfers(
            @Param("status") StockTransfer.TransferStatus status,
            @Param("storeId") Long storeId
    );
}
