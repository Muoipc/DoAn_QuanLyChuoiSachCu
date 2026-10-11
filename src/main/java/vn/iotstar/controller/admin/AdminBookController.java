package vn.iotstar.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.BookAdminDTO;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.BookConsignment;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookConsignmentRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IBookService;
import vn.iotstar.service.IInventoryService;
import vn.iotstar.service.IStoreService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    @Autowired
    private IBookService bookService;

    @Autowired
    private IStoreService storeService;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private BookConsignmentRepository consignmentRepository;

    private List<Store> resolveAllowedStores(CustomUserDetails userDetails) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (isAdmin) {
            return storeService.findActiveStores();
        }
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;
        return (managedStore != null) ? List.of(managedStore) : Collections.emptyList();
    }

    /**
     * Danh sách sách cũ trong hệ thống (Hỗ trợ tìm kiếm, lọc theo thể loại, độ mới %, trạng thái)
     */
    @GetMapping({"", "/"})
    public String listBooks(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "minCondition", required = false) Integer minCondition,
            @RequestParam(name = "isActive", required = false) Boolean isActive,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> bookPage = bookService.searchAdminBooks(keyword, categoryId, minCondition, isActive, pageable);

        // Ánh xạ các cuốn sách có nguồn gốc từ Tin Ký Gửi của khách hàng (Mô hình Chợ Tốt Recommerce)
        List<BookConsignment> allConsignments = consignmentRepository.findAll();
        Map<Long, BookConsignment> consignedBookMap = new HashMap<>();
        for (Book b : bookPage.getContent()) {
            if (b.getTitle() == null) continue;
            for (BookConsignment c : allConsignments) {
                if (c.getStatus() == BookConsignment.ConsignmentStatus.STORED
                        && c.getBookTitle() != null
                        && c.getBookTitle().trim().equalsIgnoreCase(b.getTitle().trim())) {
                    consignedBookMap.put(b.getId(), c);
                    break;
                }
            }
        }

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("consignedBookMap", consignedBookMap);
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCondition", minCondition);
        model.addAttribute("selectedIsActive", isActive);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalElements", bookPage.getTotalElements());

        return "admin/books/list";
    }

    /**
     * Trang hiển thị form thêm mới sách cũ
     */
    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        BookAdminDTO dto = new BookAdminDTO();
        dto.setConditionPercent(90);
        dto.setIsActive(true);
        dto.setIsFeatured(false);

        model.addAttribute("bookDto", dto);
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("stores", resolveAllowedStores(userDetails));
        model.addAttribute("isEdit", false);

        return "admin/books/form";
    }

    /**
     * Trang hiển thị form chỉnh sửa sách cũ
     */
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirect
    ) {
        Book book = bookService.findById(id);
        if (book == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy sách với mã ID: " + id);
            return "redirect:/admin/books";
        }

        BookAdminDTO dto = new BookAdminDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setCategoryId(book.getCategory() != null ? book.getCategory().getId() : null);
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublishYear(book.getPublishYear());
        dto.setIsbn(book.getIsbn());
        dto.setConditionPercent(book.getConditionPercent());
        dto.setConditionNotes(book.getConditionNotes());
        dto.setDescription(book.getDescription());
        dto.setOriginalPrice(book.getOriginalPrice());
        dto.setPrice(book.getPrice());
        dto.setDiscountPrice(book.getDiscountPrice());
        dto.setIsActive(book.getIsActive());
        dto.setIsFeatured(book.getIsFeatured());

        // Lấy số lượng tồn kho hiện tại tại các chi nhánh được phép quản lý
        List<Inventory> inventories = inventoryService.findByBookId(book.getId());
        Map<Long, Integer> storeQuantities = new HashMap<>();
        for (Inventory inv : inventories) {
            if (inv.getStore() != null) {
                storeQuantities.put(inv.getStore().getId(), inv.getQuantity());
            }
        }
        dto.setStoreQuantities(storeQuantities);

        model.addAttribute("bookDto", dto);
        model.addAttribute("book", book);
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("stores", resolveAllowedStores(userDetails));
        model.addAttribute("isEdit", true);

        return "admin/books/form";
    }

    /**
     * Xử lý lưu sách mới hoặc cập nhật sách
     */
    @PostMapping("/save")
    public String saveBook(
            @Valid @ModelAttribute("bookDto") BookAdminDTO bookDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirect
    ) {
        List<Store> allowedStores = resolveAllowedStores(userDetails);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", bookService.findAllCategories());
            model.addAttribute("stores", allowedStores);
            model.addAttribute("isEdit", bookDto.getId() != null);
            if (bookDto.getId() != null) {
                model.addAttribute("book", bookService.findById(bookDto.getId()));
            }
            return "admin/books/form";
        }

        // Nếu là Manager, chỉ cho phép cập nhật tồn kho của chi nhánh mình phụ trách
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin && bookDto.getStoreQuantities() != null) {
            Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;
            Map<Long, Integer> filtered = new HashMap<>();
            if (managedStore != null && bookDto.getStoreQuantities().containsKey(managedStore.getId())) {
                filtered.put(managedStore.getId(), bookDto.getStoreQuantities().get(managedStore.getId()));
            }
            bookDto.setStoreQuantities(filtered);
        }

        try {
            if (bookDto.getId() == null) {
                bookService.createBook(bookDto);
                redirect.addFlashAttribute("successMessage", "Thêm sách cũ mới vào hệ thống thành công!");
            } else {
                bookService.updateBook(bookDto.getId(), bookDto);
                redirect.addFlashAttribute("successMessage", "Cập nhật thông tin sách thành công!");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi xử lý: " + e.getMessage());
            return "redirect:/admin/books";
        }

        return "redirect:/admin/books";
    }

    /**
     * Xóa sách cũ
     */
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            bookService.deleteBook(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa sách mã #" + id + " thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Không thể xóa sách: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    /**
     * Bật / Tắt trạng thái hiển thị
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") Long id, RedirectAttributes redirect) {
        bookService.toggleActive(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái hiển thị thành công!");
        return "redirect:/admin/books";
    }

    /**
     * Bật / Tắt đánh dấu sách nổi bật
     */
    @PostMapping("/toggle-featured/{id}")
    public String toggleFeatured(@PathVariable("id") Long id, RedirectAttributes redirect) {
        bookService.toggleFeatured(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái nổi bật thành công!");
        return "redirect:/admin/books";
    }

    /**
     * Xóa ảnh góc chụp
     */
    @PostMapping("/images/delete/{id}")
    public String deleteImage(
            @PathVariable("id") Long imageId,
            @RequestParam("bookId") Long bookId,
            RedirectAttributes redirect
    ) {
        try {
            bookService.deleteBookImage(imageId);
            redirect.addFlashAttribute("successMessage", "Đã xóa ảnh góc chụp!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi khi xóa ảnh: " + e.getMessage());
        }
        return "redirect:/admin/books/edit/" + bookId;
    }
}
