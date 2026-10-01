package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.BookAdminDTO;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Category;

import java.util.List;

public interface IBookService {
    Page<Book> searchAdminBooks(String keyword, Integer categoryId, Integer minCondition, Boolean isActive, Pageable pageable);
    Book findById(Long id);
    Book createBook(BookAdminDTO dto);
    Book updateBook(Long id, BookAdminDTO dto);
    void deleteBook(Long id);
    void toggleActive(Long id);
    void toggleFeatured(Long id);
    void deleteBookImage(Long imageId);
    List<Category> findAllCategories();
    long countTotalBooks();
}
