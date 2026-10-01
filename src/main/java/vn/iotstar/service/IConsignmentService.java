package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.BookConsignment;

import java.math.BigDecimal;

public interface IConsignmentService {
    Page<BookConsignment> searchAdminConsignments(BookConsignment.ConsignmentStatus status, Long storeId, Pageable pageable);
    BookConsignment findById(Long id);
    void approveConsignment(Long id, BigDecimal agreedPrice, String adminNotes);
    void rejectConsignment(Long id, String adminNotes);
    Book convertToBookAndStock(Long id, Integer categoryId, BigDecimal sellingPrice, String adminNotes);
    long countByStatus(BookConsignment.ConsignmentStatus status);
}
