package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.BookConsignment;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.BookConsignmentRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * CONTROLLER: CỔNG KÝ GỬI & THANH LÝ SÁCH CŨ CHO KHÁCH HÀNG (CONSIGNMENT)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đối tác kiểm tra: Cường (24162013) - Nhánh: feature/admin-cuong
 *
 * Ý nghĩa nghiệp vụ:
 * 1. Khách hàng có sách cũ ở nhà không còn đọc muốn bán lại hoặc gửi chuỗi cửa hàng
 *    bán hộ để nhận tiền mặt hoặc đổi voucher mua sách mới.
 * 2. Khách điền thông tin sách, độ mới (80-99%), tải ảnh các góc chụp và chọn
 *    1 trong 5 chi nhánh TP.HCM để gửi sách thẩm định.
 * 3. Tích hợp AI gợi ý định giá tức thì dựa trên năm xuất bản và độ mới %, giúp
 *    khách không bị bỡ ngỡ về giá thị trường.
 * 4. Theo dõi hành trình phiếu ký gửi: Chờ duyệt (PENDING) -> Đã duyệt giá (APPROVED)
 *    -> Đã nhập kho bày bán (STORED) -> Bán thành công & nhận hoa hồng.
 * ============================================================================
 */
@Controller
@RequestMapping("/consignments")
public class ConsignmentController {

    @Autowired
    private BookConsignmentRepository consignmentRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper nạp User hiện tại (mặc định lấy user test ID 4 nếu chưa đăng nhập Spring Security)
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            Optional<User> uOpt = userRepository.findByUsername(authentication.getName());
            if (uOpt.isPresent()) return uOpt.get();
        }
        return userRepository.findById(4L).orElse(null);
    }

    /**
     * GET /consignments: Trang giới thiệu quy trình ký gửi và Form đăng ký định giá sách cũ
     */
    @GetMapping
    public String index(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Store> stores = storeRepository.findByIsActiveTrue();
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        model.addAttribute("user", currentUser);
        model.addAttribute("stores", stores);
        model.addAttribute("categories", categories);

        // Số lượng phiếu hiện có của user
        if (currentUser != null) {
            long myPendingCount = consignmentRepository.countByUserIdAndStatus(currentUser.getId(), BookConsignment.ConsignmentStatus.PENDING);
            long myApprovedCount = consignmentRepository.countByUserIdAndStatus(currentUser.getId(), BookConsignment.ConsignmentStatus.APPROVED);
            model.addAttribute("myPendingCount", myPendingCount);
            model.addAttribute("myApprovedCount", myApprovedCount);
        }

        return "consignment";
    }

    /**
     * POST /consignments/submit: Tiếp nhận phiếu đăng ký ký gửi sách từ khách hàng
     */
    @PostMapping("/submit")
    public String submitConsignment(
            @RequestParam("bookTitle") String bookTitle,
            @RequestParam("author") String author,
            @RequestParam(value = "publisher", required = false) String publisher,
            @RequestParam(value = "publishYear", required = false) Integer publishYear,
            @RequestParam("conditionPercent") Integer conditionPercent,
            @RequestParam("conditionDescription") String conditionDescription,
            @RequestParam("storeId") Long storeId,
            @RequestParam("proposedPrice") BigDecimal proposedPrice,
            @RequestParam(value = "photosJson", required = false) String photosJson,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chi nhánh tiếp nhận không hợp lệ!");
            return "redirect:/consignments";
        }

        BookConsignment consignment = new BookConsignment();
        consignment.setUser(currentUser);
        consignment.setStore(store);
        consignment.setBookTitle(bookTitle.trim());
        consignment.setAuthor(author.trim());
        consignment.setPublisher(publisher != null ? publisher.trim() : "");
        consignment.setPublishYear(publishYear != null ? publishYear : 2020);
        consignment.setConditionPercent(conditionPercent);
        consignment.setConditionDescription(conditionDescription.trim());
        consignment.setProposedPrice(proposedPrice);
        consignment.setStatus(BookConsignment.ConsignmentStatus.PENDING);

        // Gán ảnh mẫu hoặc ảnh upload
        if (photosJson != null && !photosJson.trim().isEmpty()) {
            consignment.setPhotosJson(photosJson);
        } else {
            consignment.setPhotosJson("/images/books/book_1.jpg");
        }

        BookConsignment saved = consignmentRepository.save(consignment);

        redirectAttributes.addFlashAttribute("successMessage", 
                "Gửi phiếu ký gửi #" + saved.getId() + " thành công! Chi nhánh " + store.getStoreName() + " sẽ liên hệ thẩm định trong 24h.");
        return "redirect:/consignments/my-list";
    }

    /**
     * GET /consignments/my-list: Trang theo dõi tiến độ các phiếu ký gửi của khách hàng
     */
    @GetMapping("/my-list")
    public String myList(
            @RequestParam(value = "tab", defaultValue = "ALL") String tab,
            Model model,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<BookConsignment> consignments;
        if ("ALL".equalsIgnoreCase(tab)) {
            consignments = consignmentRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        } else {
            try {
                BookConsignment.ConsignmentStatus status = BookConsignment.ConsignmentStatus.valueOf(tab.toUpperCase());
                consignments = consignmentRepository.findByUserIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), status);
            } catch (Exception e) {
                consignments = consignmentRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
            }
        }

        // Đếm số lượng theo trạng thái
        long countAll = consignmentRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).size();
        long countPending = consignmentRepository.countByUserIdAndStatus(currentUser.getId(), BookConsignment.ConsignmentStatus.PENDING);
        long countApproved = consignmentRepository.countByUserIdAndStatus(currentUser.getId(), BookConsignment.ConsignmentStatus.APPROVED);
        long countStored = consignmentRepository.countByUserIdAndStatus(currentUser.getId(), BookConsignment.ConsignmentStatus.STORED);
        long countRejected = consignmentRepository.countByUserIdAndStatus(currentUser.getId(), BookConsignment.ConsignmentStatus.REJECTED);

        model.addAttribute("user", currentUser);
        model.addAttribute("consignments", consignments);
        model.addAttribute("currentTab", tab.toUpperCase());
        model.addAttribute("countAll", countAll);
        model.addAttribute("countPending", countPending);
        model.addAttribute("countApproved", countApproved);
        model.addAttribute("countStored", countStored);
        model.addAttribute("countRejected", countRejected);

        return "consignment-list";
    }

    /**
     * POST /consignments/{id}/accept-price: Khách hàng đồng ý với mức giá shop thẩm định
     */
    @PostMapping("/{id}/accept-price")
    public String acceptPrice(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<BookConsignment> opt = consignmentRepository.findById(id);
        if (opt.isPresent()) {
            BookConsignment c = opt.get();
            if (c.getStatus() == BookConsignment.ConsignmentStatus.APPROVED) {
                c.setAdminNotes((c.getAdminNotes() != null ? c.getAdminNotes() + "\n" : "") + "[Khách hàng đã đồng ý mức giá thẩm định]");
                consignmentRepository.save(c);
                redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đồng ý giá thẩm định. Vui lòng mang sách tới " + c.getStore().getStoreName() + " trong vòng 3 ngày!");
            }
        }
        return "redirect:/consignments/my-list";
    }

    /**
     * POST /consignments/{id}/cancel: Khách hàng hủy phiếu ký gửi khi đang ở trạng thái PENDING
     */
    @PostMapping("/{id}/cancel")
    public String cancelConsignment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<BookConsignment> opt = consignmentRepository.findById(id);
        if (opt.isPresent()) {
            BookConsignment c = opt.get();
            if (c.getStatus() == BookConsignment.ConsignmentStatus.PENDING) {
                consignmentRepository.delete(c);
                redirectAttributes.addFlashAttribute("successMessage", "Đã hủy phiếu ký gửi #" + id + " thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy phiếu đã được chi nhánh duyệt!");
            }
        }
        return "redirect:/consignments/my-list";
    }

    /**
     * POST /consignments/api/estimate-price: API AI Định giá sách cũ tự động
     * Giúp khách hàng nhập vào giá gốc, năm xuất bản và độ mới để hệ thống gợi ý mức giá thu mua chuẩn
     */
    @PostMapping("/api/estimate-price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> estimatePrice(
            @RequestParam(value = "originalPrice", defaultValue = "100000") BigDecimal originalPrice,
            @RequestParam(value = "conditionPercent", defaultValue = "90") int conditionPercent,
            @RequestParam(value = "publishYear", defaultValue = "2020") int publishYear,
            @RequestParam(value = "isRare", defaultValue = "false") boolean isRare) {

        Map<String, Object> resp = new HashMap<>();

        BigDecimal baseRate;
        if (isRare || publishYear < 1975) {
            // Sách xưa / hiếm trước 1975: giá trị sưu tầm cao hơn giá bìa
            baseRate = BigDecimal.valueOf(1.8);
            resp.put("categoryNote", "Sách cổ / Sách xuất bản trước 1975 có giá trị sưu tầm cao");
        } else {
            // Sách phổ thông: tỉ lệ theo độ mới (80-99%) x hệ số chiết khấu 0.55 - 0.70
            double condFactor = conditionPercent / 100.0;
            double discountRate = 0.50 + (condFactor * 0.20); // 0.66 đến 0.70
            baseRate = BigDecimal.valueOf(discountRate);
            resp.put("categoryNote", "Định giá dựa trên tình trạng sách " + conditionPercent + "% và niên hạn");
        }

        BigDecimal estimated = originalPrice.multiply(baseRate).setScale(-3, RoundingMode.HALF_UP);
        BigDecimal minPrice = estimated.multiply(BigDecimal.valueOf(0.85)).setScale(-3, RoundingMode.HALF_UP);
        BigDecimal maxPrice = estimated.multiply(BigDecimal.valueOf(1.15)).setScale(-3, RoundingMode.HALF_UP);

        resp.put("success", true);
        resp.put("estimatedPrice", estimated);
        resp.put("minPrice", minPrice);
        resp.put("maxPrice", maxPrice);

        return ResponseEntity.ok(resp);
    }
}
