package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.SupportTicket;
import vn.iotstar.entity.User;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.SupportTicketRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.Map;

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

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Hiển thị trang chủ Trung Tâm Trợ Giúp
     */
    @GetMapping({"", "/"})
    public String helpCenterHome(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "orderCode", required = false) String orderCodeParam,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        model.addAttribute("pageTitle", "Trung Tâm Trợ Giúp — Chuỗi Cửa Hàng Sách Cũ TP.HCM");
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("prefilledOrderCode", orderCodeParam);

        if (userDetails != null) {
            userRepository.findById(userDetails.getId()).ifPresent(u -> {
                model.addAttribute("currentUser", u);
            });
        }

        return "help";
    }

    /**
     * Xử lý gửi phiếu yêu cầu hỗ trợ từ khách hàng (Form HTML chuẩn)
     */
    @PostMapping("/contact")
    public String submitSupportTicket(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "orderCode", required = false) String orderCode,
            @RequestParam("category") String category,
            @RequestParam("message") String message,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // Ghi nhận và lưu phiếu hỗ trợ vào Database
        SupportTicket ticket = new SupportTicket();
        ticket.setFullName(fullName.trim());
        ticket.setEmail(email.trim());
        ticket.setPhone(phone != null ? phone.trim() : null);
        ticket.setOrderCode(orderCode != null ? orderCode.trim() : null);
        ticket.setCategory(category.trim());
        ticket.setMessage(message.trim());
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        if (userDetails != null && userDetails.getId() != null) {
            userRepository.findById(userDetails.getId()).ifPresent(ticket::setUser);
        }

        supportTicketRepository.save(ticket);

        redirectAttributes.addFlashAttribute("successMessage", 
            "Cảm ơn " + fullName + "! Yêu cầu hỗ trợ về [" + category + "] của bạn đã được ghi nhận trong hệ thống (Mã phiếu #" + ticket.getId() + "). Bộ phận CSKH Chuỗi Sách Cũ sẽ phản hồi qua email " + email + " trong vòng 24 giờ làm việc.");

        return "redirect:/help#contact-form";
    }

    /**
     * API Gửi yêu cầu hỗ trợ nhanh (AJAX từ Floating CSKH Widget trên toàn sàn)
     */
    @PostMapping("/api/quick-submit")
    @ResponseBody
    public ResponseEntity<?> quickSubmitTicket(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String fullName = payload.get("fullName");
        String email = payload.get("email");
        String phone = payload.get("phone");
        String orderCode = payload.get("orderCode");
        String category = payload.get("category");
        String message = payload.get("message");

        if (fullName == null || fullName.isBlank() || email == null || email.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng điền họ tên, email và nội dung cần hỗ trợ!"));
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setFullName(fullName.trim());
        ticket.setEmail(email.trim());
        ticket.setPhone(phone != null ? phone.trim() : null);
        ticket.setOrderCode(orderCode != null ? orderCode.trim() : null);
        ticket.setCategory(category != null ? category.trim() : "Tư vấn chung");
        ticket.setMessage(message.trim());
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        if (userDetails != null && userDetails.getId() != null) {
            userRepository.findById(userDetails.getId()).ifPresent(ticket::setUser);
        }

        supportTicketRepository.save(ticket);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "ticketId", ticket.getId(),
                "message", "Yêu cầu hỗ trợ #" + ticket.getId() + " đã được gửi tới Ban CSKH! Chúng tôi sẽ phản hồi trong 24h."
        ));
    }
}
