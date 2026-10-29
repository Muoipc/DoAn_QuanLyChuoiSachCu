package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.impl.BookServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử nghiệp vụ quản lý sách cũ (BookService).
 */
@ExtendWith(MockitoExtension.class)
class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("Tìm kiếm sách theo ID thành công khi sách tồn tại")
    void testFindByIdSuccess() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Số Đỏ");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.findById(1L);
        assertNotNull(result);
        assertEquals("Số Đỏ", result.getTitle());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Trả về null khi không tìm thấy sách theo ID")
    void testFindByIdNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        Book result = bookService.findById(999L);
        assertNull(result);
        verify(bookRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Đếm tổng số sách đang có trong hệ thống")
    void testCountTotalBooks() {
        when(bookRepository.count()).thenReturn(70L);

        long count = bookService.countTotalBooks();
        assertEquals(70L, count);
        verify(bookRepository, times(1)).count();
    }

    @Test
    @DisplayName("Lấy danh sách tất cả danh mục thể loại sách")
    void testFindAllCategories() {
        Category c1 = new Category();
        c1.setId(1);
        c1.setCategoryName("Văn Học");

        Category c2 = new Category();
        c2.setId(2);
        c2.setCategoryName("Kinh Tế");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Category> categories = bookService.findAllCategories();
        assertNotNull(categories);
        assertEquals(2, categories.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Đổi trạng thái hiển thị hoạt động của sách (toggleActive)")
    void testToggleActive() {
        Book book = new Book();
        book.setId(1L);
        book.setIsActive(true);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookService.toggleActive(1L);

        assertFalse(book.getIsActive());
        verify(bookRepository, times(1)).save(book);
    }
}
