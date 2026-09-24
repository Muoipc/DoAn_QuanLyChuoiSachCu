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
    Page<BookConsignment> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);
    Page<BookConsignment> findByStatusOrderByCreatedAtDesc(BookConsignment.ConsignmentStatus status, Pageable pageable);
    List<BookConsignment> findByStoreIdAndStatus(Long storeId, BookConsignment.ConsignmentStatus status);
}
