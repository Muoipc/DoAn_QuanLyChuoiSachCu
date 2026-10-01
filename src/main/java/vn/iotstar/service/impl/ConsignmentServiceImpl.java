package vn.iotstar.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.*;
import vn.iotstar.repository.BookImageRepository;
import vn.iotstar.repository.BookConsignmentRepository;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.IConsignmentService;
import vn.iotstar.service.IInventoryService;
import vn.iotstar.util.SlugUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ConsignmentServiceImpl implements IConsignmentService {

    @Autowired
    private BookConsignmentRepository consignmentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookImageRepository bookImageRepository;

    @Autowired
    private IInventoryService inventoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public Page<BookConsignment> searchAdminConsignments(BookConsignment.ConsignmentStatus status, Long storeId, Pageable pageable) {
        return consignmentRepository.searchAdminConsignments(status, storeId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BookConsignment findById(Long id) {
        return consignmentRepository.findById(id).orElse(null);
    }

    @Override
    public void approveConsignment(Long id, BigDecimal agreedPrice, String adminNotes) {
        BookConsignment c = consignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu ký gửi mã #" + id));
        c.setStatus(BookConsignment.ConsignmentStatus.APPROVED);
        if (agreedPrice != null && agreedPrice.compareTo(BigDecimal.ZERO) > 0) {
            c.setAgreedPrice(agreedPrice);
        } else {
            c.setAgreedPrice(c.getProposedPrice());
        }
        if (adminNotes != null && !adminNotes.isBlank()) {
            c.setAdminNotes(adminNotes);
        }
        consignmentRepository.save(c);
    }

    @Override
    public void rejectConsignment(Long id, String adminNotes) {
        BookConsignment c = consignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu ký gửi mã #" + id));
        c.setStatus(BookConsignment.ConsignmentStatus.REJECTED);
        if (adminNotes != null && !adminNotes.isBlank()) {
            c.setAdminNotes(adminNotes);
        }
        consignmentRepository.save(c);
    }

    @Override
    public Book convertToBookAndStock(Long id, Integer categoryId, BigDecimal sellingPrice, String adminNotes) {
        BookConsignment c = consignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu ký gửi mã #" + id));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Vui lòng chọn danh mục sách hợp lệ"));

        // 1. Tạo đối tượng Book mới để bày bán
        Book book = new Book();
        book.setTitle(c.getBookTitle());
        String baseSlug = SlugUtil.toSlug(c.getBookTitle());
        String slug = baseSlug;
        if (bookRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + System.currentTimeMillis() % 10000;
        }
        book.setSlug(slug);
        book.setCategory(category);
        book.setAuthor(c.getAuthor());
        book.setPublisher(c.getPublisher());
        book.setPublishYear(c.getPublishYear());
        book.setConditionPercent(c.getConditionPercent());
        book.setConditionNotes(c.getConditionDescription());
        book.setOriginalPrice(c.getProposedPrice());

        BigDecimal finalPrice = (sellingPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) > 0)
                ? sellingPrice
                : (c.getAgreedPrice() != null ? c.getAgreedPrice() : c.getProposedPrice());
        book.setPrice(finalPrice);
        book.setIsActive(true);
        book.setIsFeatured(false);
        book.setTotalSold(0);
        book.setViewsCount(0);

        Book savedBook = bookRepository.save(book);

        // 2. Chuyển ảnh từ photosJson thành BookImage
        List<String> photoUrls = new ArrayList<>();
        if (c.getPhotosJson() != null && !c.getPhotosJson().isBlank()) {
            try {
                photoUrls = objectMapper.readValue(c.getPhotosJson(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                // Thử tách bằng dấu phẩy
                String[] parts = c.getPhotosJson().replace("[", "").replace("]", "").replace("\"", "").split(",");
                for (String p : parts) {
                    if (!p.trim().isEmpty()) {
                        photoUrls.add(p.trim());
                    }
                }
            }
        }

        if (photoUrls.isEmpty()) {
            // Ảnh mặc định
            BookImage img = new BookImage();
            img.setBook(savedBook);
            img.setImageUrl("/images/books/book-1.jpg");
            img.setAngleDescription("Ảnh bìa thực tế");
            img.setIsPrimary(true);
            img.setDisplayOrder(1);
            bookImageRepository.save(img);
        } else {
            boolean first = true;
            int order = 1;
            for (String url : photoUrls) {
                BookImage img = new BookImage();
                img.setBook(savedBook);
                img.setImageUrl(url);
                img.setAngleDescription(first ? "Ảnh bìa thực tế" : "Góc ảnh kiểm định");
                img.setIsPrimary(first);
                img.setDisplayOrder(order++);
                bookImageRepository.save(img);
                first = false;
            }
        }

        // 3. Nhập tồn kho 1 cuốn vào chi nhánh mà khách đã gửi ký gửi
        Store store = c.getStore();
        if (store != null) {
            inventoryService.updateQuantity(store.getId(), savedBook.getId(), 1);
        }

        // 4. Cập nhật phiếu ký gửi thành STORED
        c.setStatus(BookConsignment.ConsignmentStatus.STORED);
        if (adminNotes != null && !adminNotes.isBlank()) {
            c.setAdminNotes(adminNotes);
        }
        consignmentRepository.save(c);

        return savedBook;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(BookConsignment.ConsignmentStatus status) {
        return consignmentRepository.countByStatus(status);
    }
}
