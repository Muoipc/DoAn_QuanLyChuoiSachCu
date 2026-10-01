package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.BookConsignment;

import java.util.List;

@Repository
public interface BookConsignmentRepository extends JpaRepository<BookConsignment, Long> {
    Page<BookConsignment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<BookConsignment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<BookConsignment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookConsignment.ConsignmentStatus status);
    long countByUserIdAndStatus(Long userId, BookConsignment.ConsignmentStatus status);
    List<BookConsignment> findByStoreIdAndStatus(Long storeId, BookConsignment.ConsignmentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM BookConsignment c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:storeId IS NULL OR c.store.id = :storeId) " +
           "ORDER BY c.createdAt DESC")
    Page<BookConsignment> searchAdminConsignments(
        @org.springframework.data.repository.query.Param("status") BookConsignment.ConsignmentStatus status,
        @org.springframework.data.repository.query.Param("storeId") Long storeId,
        Pageable pageable
    );

    long countByStatus(BookConsignment.ConsignmentStatus status);
}
