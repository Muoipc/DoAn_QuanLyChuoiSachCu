package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.BookAdminDTO;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.BookImage;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.BookImageRepository;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.IBookService;
import vn.iotstar.service.ICloudinaryService;
import vn.iotstar.service.IInventoryService;
import vn.iotstar.util.SlugUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BookServiceImpl implements IBookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookImageRepository bookImageRepository;

    @Autowired
    private ICloudinaryService cloudinaryService;

    @Autowired
    private IInventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<Book> searchAdminBooks(String keyword, Integer categoryId, Integer minCondition, Boolean isActive, Pageable pageable) {
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return bookRepository.searchAdminBooks(kw, categoryId, minCondition, isActive, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public Book createBook(BookAdminDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle().trim());

        // Tạo slug thân thiện và đảm bảo duy nhất
        String baseSlug = SlugUtil.toSlug(dto.getTitle());
        String slug = baseSlug;
        if (bookRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + System.currentTimeMillis() % 10000;
        }
        book.setSlug(slug);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại sách"));
        book.setCategory(category);

        book.setAuthor(dto.getAuthor().trim());
        book.setPublisher(dto.getPublisher());
        book.setPublishYear(dto.getPublishYear());
        book.setIsbn(dto.getIsbn());
        book.setConditionPercent(dto.getConditionPercent());
        book.setConditionNotes(dto.getConditionNotes());
        book.setDescription(dto.getDescription());
        book.setOriginalPrice(dto.getOriginalPrice());
        book.setPrice(dto.getPrice());
        book.setDiscountPrice(dto.getDiscountPrice());
        book.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        book.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        book.setTotalSold(0);
        book.setViewsCount(0);

        // Lưu trước để có ID
        book = bookRepository.save(book);

        // Upload ảnh bìa chính lên Cloudinary
        if (dto.getPrimaryImage() != null && !dto.getPrimaryImage().isEmpty()) {
            Map<String, String> uploadRes = cloudinaryService.uploadFile(dto.getPrimaryImage(), "old_book_store/books");
            if (uploadRes.containsKey("url")) {
                BookImage primaryImg = new BookImage();
                primaryImg.setBook(book);
                primaryImg.setImageUrl(uploadRes.get("url"));
                primaryImg.setPublicId(uploadRes.get("public_id"));
                primaryImg.setAngleDescription("Ảnh bìa chính");
                primaryImg.setIsPrimary(true);
                primaryImg.setDisplayOrder(1);
                book.getImages().add(primaryImg);
            }
        }

        // Upload các ảnh góc cạnh (bìa sau, gáy sách, ruột sách, con dấu...)
        if (dto.getAngleImages() != null && !dto.getAngleImages().isEmpty()) {
            int order = 2;
            for (int i = 0; i < dto.getAngleImages().size(); i++) {
                MultipartFile file = dto.getAngleImages().get(i);
                if (file != null && !file.isEmpty()) {
                    Map<String, String> uploadRes = cloudinaryService.uploadFile(file, "old_book_store/books/angles");
                    if (uploadRes.containsKey("url")) {
                        BookImage angleImg = new BookImage();
                        angleImg.setBook(book);
                        angleImg.setImageUrl(uploadRes.get("url"));
                        angleImg.setPublicId(uploadRes.get("public_id"));

                        String desc = "Góc chụp chi tiết";
                        if (dto.getAngleDescriptions() != null && i < dto.getAngleDescriptions().size()) {
                            String customDesc = dto.getAngleDescriptions().get(i);
                            if (customDesc != null && !customDesc.isBlank()) {
                                desc = customDesc;
                            }
                        }
                        angleImg.setAngleDescription(desc);
                        angleImg.setIsPrimary(false);
                        angleImg.setDisplayOrder(order++);
                        book.getImages().add(angleImg);
                    }
                }
            }
        }

        book = bookRepository.save(book);

        // Khởi tạo số lượng tồn kho cho các chi nhánh
        if (dto.getStoreQuantities() != null) {
            for (Map.Entry<Long, Integer> entry : dto.getStoreQuantities().entrySet()) {
                Long storeId = entry.getKey();
                Integer qty = entry.getValue();
                if (storeId != null && qty != null && qty >= 0) {
                    inventoryService.updateQuantity(storeId, book.getId(), qty);
                }
            }
        }

        return book;
    }

    @Override
    public Book updateBook(Long id, BookAdminDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + id));

        book.setTitle(dto.getTitle().trim());
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(book.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại"));
            book.setCategory(category);
        }

        book.setAuthor(dto.getAuthor().trim());
        book.setPublisher(dto.getPublisher());
        book.setPublishYear(dto.getPublishYear());
        book.setIsbn(dto.getIsbn());
        book.setConditionPercent(dto.getConditionPercent());
        book.setConditionNotes(dto.getConditionNotes());
        book.setDescription(dto.getDescription());
        book.setOriginalPrice(dto.getOriginalPrice());
        book.setPrice(dto.getPrice());
        book.setDiscountPrice(dto.getDiscountPrice());
        book.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        book.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);

        // Nếu tải ảnh bìa chính mới
        if (dto.getPrimaryImage() != null && !dto.getPrimaryImage().isEmpty()) {
            Map<String, String> uploadRes = cloudinaryService.uploadFile(dto.getPrimaryImage(), "old_book_store/books");
            if (uploadRes.containsKey("url")) {
                // Đổi cờ isPrimary của ảnh cũ thành false
                for (BookImage img : book.getImages()) {
                    if (Boolean.TRUE.equals(img.getIsPrimary())) {
                        img.setIsPrimary(false);
                    }
                }
                BookImage newPrimary = new BookImage();
                newPrimary.setBook(book);
                newPrimary.setImageUrl(uploadRes.get("url"));
                newPrimary.setPublicId(uploadRes.get("public_id"));
                newPrimary.setAngleDescription("Ảnh bìa chính");
                newPrimary.setIsPrimary(true);
                newPrimary.setDisplayOrder(1);
                book.getImages().add(0, newPrimary);
            }
        }

        // Nếu tải thêm ảnh góc chụp mới
        if (dto.getAngleImages() != null && !dto.getAngleImages().isEmpty()) {
            int maxOrder = book.getImages().size() + 1;
            for (int i = 0; i < dto.getAngleImages().size(); i++) {
                MultipartFile file = dto.getAngleImages().get(i);
                if (file != null && !file.isEmpty()) {
                    Map<String, String> uploadRes = cloudinaryService.uploadFile(file, "old_book_store/books/angles");
                    if (uploadRes.containsKey("url")) {
                        BookImage angleImg = new BookImage();
                        angleImg.setBook(book);
                        angleImg.setImageUrl(uploadRes.get("url"));
                        angleImg.setPublicId(uploadRes.get("public_id"));

                        String desc = "Góc chụp chi tiết";
                        if (dto.getAngleDescriptions() != null && i < dto.getAngleDescriptions().size()) {
                            String customDesc = dto.getAngleDescriptions().get(i);
                            if (customDesc != null && !customDesc.isBlank()) {
                                desc = customDesc;
                            }
                        }
                        angleImg.setAngleDescription(desc);
                        angleImg.setIsPrimary(false);
                        angleImg.setDisplayOrder(maxOrder++);
                        book.getImages().add(angleImg);
                    }
                }
            }
        }

        // Cập nhật số lượng tồn kho theo chi nhánh nếu được truyền
        if (dto.getStoreQuantities() != null && !dto.getStoreQuantities().isEmpty()) {
            for (Map.Entry<Long, Integer> entry : dto.getStoreQuantities().entrySet()) {
                Long storeId = entry.getKey();
                Integer qty = entry.getValue();
                if (storeId != null && qty != null && qty >= 0) {
                    inventoryService.updateQuantity(storeId, book.getId(), qty);
                }
            }
        }

        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setIsActive(!Boolean.TRUE.equals(book.getIsActive()));
            bookRepository.save(book);
        });
    }

    @Override
    public void toggleFeatured(Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setIsFeatured(!Boolean.TRUE.equals(book.getIsFeatured()));
            bookRepository.save(book);
        });
    }

    @Override
    public void deleteBookImage(Long imageId) {
        Optional<BookImage> opt = bookImageRepository.findById(imageId);
        if (opt.isPresent()) {
            BookImage img = opt.get();
            if (img.getPublicId() != null) {
                cloudinaryService.deleteFile(img.getPublicId());
            }
            bookImageRepository.delete(img);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalBooks() {
        return bookRepository.count();
    }
}
