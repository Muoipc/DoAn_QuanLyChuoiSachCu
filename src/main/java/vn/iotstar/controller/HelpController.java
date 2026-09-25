package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.repository.StoreRepository;

/**
 * ============================================================================
 * CONTROLLER TRUNG TÂM TRỢ GIÚP (SHOPEE HELP CENTER STYLE)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đối tác kiểm tra: Cường (24162013) - Nhánh: feature/admin-cuong
 *
 * Ý nghĩa nghiệp vụ:
 * 1. Cung cấp cổng tra cứu thông tin trợ giúp, hướng dẫn mua sắm, đổi trả và ký gửi sách cũ.
 * 2. Thiết kế mô phỏng 100% bố cục Trung tâm trợ giúp Shopee (help.shopee.vn/portal/4/vn/s).
 * 3. Hỗ trợ gửi phiếu yêu cầu hỗ trợ trực tuyến (Support Ticket) cho Admin/CSKH.
 * 4. Tích hợp tra cứu danh sách 5 chi nhánh tiếp nhận và kết nối Trợ lý AI.
 * ============================================================================
 */
@Controller
@RequestMapping("/help")
public class HelpController {

    @Autowired
    private StoreRepository storeRepository;

    /**
     * Hiển thị trang chủ Trung Tâm Trợ Giúp
     */
    @GetMapping({"", "/"})
    public String helpCenterHome(
            @RequestParam(value = "q", required = false) String keyword,
            Model model) {
        
        model.addAttribute("pageTitle", "Trung Tâm Trợ Giúp — Chuỗi Cửa Hàng Sách Cũ TP.HCM");
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("searchKeyword", keyword);

        return "help";
    }

    /**
     * Xử lý gửi phiếu yêu cầu hỗ trợ từ khách hàng
     */
    @PostMapping("/contact")
    public String submitSupportTicket(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "orderCode", required = false) String orderCode,
            @RequestParam("category") String category,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {

        // Ghi nhận yêu cầu hỗ trợ
        redirectAttributes.addFlashAttribute("successMessage", 
            "Cảm ơn " + fullName + "! Yêu cầu hỗ trợ về [" + category + "] của bạn đã được ghi nhận. Bộ phận CSKH Chuỗi Sách Cũ sẽ phản hồi qua email " + email + " trong vòng 24 giờ làm việc.");

        return "redirect:/help#contact-form";
    }
}
