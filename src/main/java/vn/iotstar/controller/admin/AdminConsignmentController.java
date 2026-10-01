package vn.iotstar.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.BookConsignment;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.IConsignmentService;
import vn.iotstar.service.IStoreService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/consignments")
public class AdminConsignmentController {

    @Autowired
    private IConsignmentService consignmentService;

    @Autowired
    private IStoreService storeService;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Danh sách yêu cầu ký gửi / thu mua sách cũ từ khách hàng
     */
    @GetMapping({"", "/"})
    public String listConsignments(
            @RequestParam(name = "status", required = false) BookConsignment.ConsignmentStatus status,
            @RequestParam(name = "storeId", required = false) Long storeId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookConsignment> consignmentPage = consignmentService.searchAdminConsignments(status, storeId, pageable);

        model.addAttribute("consignmentPage", consignmentPage);
        model.addAttribute("stores", storeService.findActiveStores());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", consignmentPage.getTotalPages());
        model.addAttribute("totalElements", consignmentPage.getTotalElements());

        // Đếm theo trạng thái
        model.addAttribute("countPending", consignmentService.countByStatus(BookConsignment.ConsignmentStatus.PENDING));
        model.addAttribute("countApproved", consignmentService.countByStatus(BookConsignment.ConsignmentStatus.APPROVED));
        model.addAttribute("countStored", consignmentService.countByStatus(BookConsignment.ConsignmentStatus.STORED));
        model.addAttribute("countRejected", consignmentService.countByStatus(BookConsignment.ConsignmentStatus.REJECTED));

        return "admin/consignments/list";
    }

    /**
     * Xem chi tiết yêu cầu ký gửi, thẩm định hình ảnh và xét duyệt
     */
    @GetMapping("/{id}")
    public String consignmentDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        BookConsignment c = consignmentService.findById(id);
        if (c == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy phiếu ký gửi mã #" + id);
            return "redirect:/admin/consignments";
        }

        // Parse danh sách ảnh các góc chụp do khách gửi
        List<String> photos = new ArrayList<>();
        if (c.getPhotosJson() != null && !c.getPhotosJson().isBlank()) {
            try {
                photos = objectMapper.readValue(c.getPhotosJson(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                String[] parts = c.getPhotosJson().replace("[", "").replace("]", "").replace("\"", "").split(",");
                for (String p : parts) {
                    if (!p.trim().isEmpty()) photos.add(p.trim());
                }
            }
        }

        model.addAttribute("consignment", c);
        model.addAttribute("photos", photos);
        model.addAttribute("categories", categoryRepository.findAll());

        return "admin/consignments/detail";
    }

    /**
     * Duyệt yêu cầu ký gửi & thỏa thuận giá thu mua
     */
    @PostMapping("/{id}/approve")
    public String approveConsignment(
            @PathVariable("id") Long id,
            @RequestParam(name = "agreedPrice", required = false) BigDecimal agreedPrice,
            @RequestParam(name = "adminNotes", required = false) String adminNotes,
            RedirectAttributes redirect
    ) {
        try {
            consignmentService.approveConsignment(id, agreedPrice, adminNotes);
            redirect.addFlashAttribute("successMessage", "Đã duyệt phiếu ký gửi! Thông báo đã được gửi đến khách hàng.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/consignments/" + id;
    }

    /**
     * Từ chối thu mua / ký gửi
     */
    @PostMapping("/{id}/reject")
    public String rejectConsignment(
            @PathVariable("id") Long id,
            @RequestParam(name = "adminNotes", required = false) String adminNotes,
            RedirectAttributes redirect
    ) {
        try {
            consignmentService.rejectConsignment(id, adminNotes);
            redirect.addFlashAttribute("successMessage", "Đã từ chối phiếu ký gửi mã #" + id);
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/consignments/" + id;
    }

    /**
     * Nhập kho chính thức bày bán (Tạo Book & cộng vào Inventory chi nhánh)
     */
    @PostMapping("/{id}/store")
    public String storeConsignment(
            @PathVariable("id") Long id,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("sellingPrice") BigDecimal sellingPrice,
            @RequestParam(name = "adminNotes", required = false) String adminNotes,
            RedirectAttributes redirect
    ) {
        try {
            Book book = consignmentService.convertToBookAndStock(id, categoryId, sellingPrice, adminNotes);
            redirect.addFlashAttribute("successMessage", "Đã nhập sách '" + book.getTitle() + "' vào kho chi nhánh và sẵn sàng bày bán trên website!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi nhập kho: " + e.getMessage());
        }
        return "redirect:/admin/consignments/" + id;
    }
}
