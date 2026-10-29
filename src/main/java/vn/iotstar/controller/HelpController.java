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

    @Autowired
    private vn.iotstar.service.IOpenRouterService openRouterService;

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

    /**
     * API Chat trực tuyến với Trợ Lý AI Chăm Sóc Khách Hàng ReBook 24/7
     * URL: POST /help/api/chat
     */
    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<?> chatWithSupportAi(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String message = payload.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nội dung tin nhắn không được để trống!"));
        }

        User user = null;
        if (userDetails != null && userDetails.getId() != null) {
            user = userRepository.findById(userDetails.getId()).orElse(null);
        }

        // Chỉ trò chuyện trực tuyến với CSKH AI, không tự động tạo phiếu khiếu nại (SupportTicket)
        String aiReply = generateSupportAiReply(message.trim(), user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "reply", aiReply
        ));
    }

    private String generateSupportAiReply(String userMessage, User user) {
        if (openRouterService != null && openRouterService.isConfigured()) {
            try {
                String userName = user != null && user.getFullName() != null ? user.getFullName() : "Quý khách";
                String systemPrompt =
                        "Bạn là Trợ lý Chăm Sóc Khách Hàng chuyên nghiệp của 'ReBook — Hệ Thống Chuỗi Cửa Hàng Sách Cũ TP.HCM'.\n" +
                        "Khách hàng đang nhắn tin với bạn là: " + userName + ".\n\n" +
                        "### HỒ SƠ & QUY ĐỊNH VẬN HÀNH TOÀN CHUỖI REBOOK:\n" +
                        "1. Chính sách Đổi Trả & Hoàn Tiền: Miễn phí đổi trả trong 7 ngày nếu sách bị rách nát, bung gáy nặng, thiếu trang hoặc không khớp % độ mới đã cam kết. Khách có thể mang đến bất kỳ chi nhánh nào trong 5 shop hoặc shipper đến thu hồi.\n" +
                        "2. Chính sách Ký Gửi Sách Cũ: Hỗ trợ khách mang sách tới quầy hoặc gửi phiếu online. Ban kiểm định định giá trong 24H. Sau khi bán được sách, khách nhận tiền chuyển khoản hoặc tiền mặt ngay tại chi nhánh.\n" +
                        "3. Chính sách Vận Chuyển: Đội ngũ Shipper ReBook giao hàng nhanh 1-2 ngày trong TP.HCM, đồng kiểm trước khi nhận, freeship đơn từ 200.000đ.\n" +
                        "4. Hệ thống 5 Chi Nhánh Chuỗi Sách Cũ tại TP.HCM:\n" +
                        "   - Chi Nhánh 1 (Tổng kho): 48 Võ Văn Ngân, TP. Thủ Đức (Hotline: 0988.111.001)\n" +
                        "   - Chi Nhánh 2: Đường Sách Nguyễn Văn Bình, Q.1 (Hotline: 0988.111.002)\n" +
                        "   - Chi Nhánh 3: Khu Đô Thị ĐHQG-HCM, TP. Thủ Đức (Hotline: 0988.111.003)\n" +
                        "   - Chi Nhánh 4: 120 Trần Huy Liệu, Q. Phú Nhuận (Hotline: 0988.111.004)\n" +
                        "   - Chi Nhánh 5: 280 An Dương Vương, Q.5 (Hotline: 0988.111.005)\n" +
                        "5. Hotline CSKH miễn cước 24/7: 1900 2416.\n\n" +
                        "### NGUYÊN TẮC PHỤC VỤ:\n" +
                        "- Xưng 'Em' hoặc 'ReBook', gọi khách là 'Anh/Chị' hoặc '" + userName + "'. Luôn giữ thái độ niềm nở, ân cần, lịch sự và chu đáo.\n" +
                        "- Trả lời đúng trọng tâm. Nếu khách hàng phản ánh khiếu nại (giao chậm, sách lỗi, thái độ phục vụ), hãy chân thành xin lỗi, cam kết hỗ trợ tối đa và hướng dẫn cách xử lý ngay.\n" +
                        "- In đậm (**...**) các từ khóa quan trọng, hotline, hoặc địa chỉ chi nhánh. Trình bày ngắt dòng thoáng, ngắn gọn, súc tích.";

                String aiReply = openRouterService.generateReply(userMessage, systemPrompt);
                if (aiReply != null && !aiReply.isBlank()) {
                    return aiReply.trim();
                }
            } catch (Exception e) {
                System.err.println("[SupportChat AI] Lỗi LLM CSKH: " + e.getMessage());
            }
        }

        return generateFallbackSupportReply(userMessage);
    }

    private String generateFallbackSupportReply(String userMessage) {
        String msg = userMessage.toLowerCase();

        if (msg.contains("đổi") || msg.contains("trả") || msg.contains("hoàn tiền") || msg.contains("lỗi") || msg.contains("rách")) {
            return "Dạ chào bạn, **ReBook cam kết bảo hành đổi trả miễn phí trong vòng 7 ngày** nếu sách gặp lỗi rách trang, bung gáy hoặc không đúng với mô tả độ mới. Bạn có thể mang sách đến bất kỳ chi nhánh nào trong 5 shop hoặc liên hệ Hotline **1900 2416** để shipper tới hỗ trợ nhé!";
        }

        if (msg.contains("ký gửi") || msg.contains("bán lại") || msg.contains("thanh lý") || msg.contains("định giá")) {
            return "Dạ về chính sách ký gửi: Bạn có thể mang sách cũ trực tiếp tới quầy hoặc tạo phiếu ký gửi trực tuyến tại mục **'Ký Gửi Sách'** trên website. Đội ngũ thẩm định sẽ định giá trong vòng **24 giờ**. Khi sách được bán, tiền sẽ chuyển thẳng vào tài khoản của bạn ạ!";
        }

        if (msg.contains("giao") || msg.contains("ship") || msg.contains("vận chuyển") || msg.contains("bao lâu")) {
            return "Dạ đơn hàng tại ReBook được đội ngũ Shipper riêng giao hỏa tốc từ **1 - 2 ngày làm việc** trong nội thành TP.HCM. Bạn hoàn toàn được **đồng kiểm tra sách** trước khi thanh toán. Đơn từ **200.000đ** sẽ được miễn phí giao hàng ạ!";
        }

        if (msg.contains("chi nhánh") || msg.contains("địa chỉ") || msg.contains("ở đâu") || msg.contains("cửa hàng")) {
            return "Dạ ReBook hiện có **5 chi nhánh tại TP.HCM** phục vụ từ 8h00 - 21h30:\n" +
                   "1. **Thủ Đức**: 48 Võ Văn Ngân, TP. Thủ Đức\n" +
                   "2. **Quận 1**: Đường Sách Nguyễn Văn Bình, Q.1\n" +
                   "3. **Làng ĐH**: Khu Đô Thị ĐHQG-HCM, TP. Thủ Đức\n" +
                   "4. **Phú Nhuận**: 120 Trần Huy Liệu, Q. Phú Nhuận\n" +
                   "5. **Quận 5**: 280 An Dương Vương, Q.5\n" +
                   "Hotline hỗ trợ toàn chuỗi: **1900 2416** ạ!";
        }

        if (msg.contains("hotline") || msg.contains("số điện thoại") || msg.contains("liên hệ") || msg.contains("sđt")) {
            return "Dạ tổng đài Chăm sóc khách hàng của ReBook hoạt động 24/7 qua Hotline miễn phí: **1900 2416**. Em luôn sẵn sàng hỗ trợ anh/chị ạ!";
        }

        return "Dạ chào anh/chị, em là Trợ lý CSKH ReBook! Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần hỗ trợ gấp về đơn hàng, đổi trả hoặc ký gửi sách cũ, anh/chị có thể gọi ngay Hotline miễn cước **1900 2416** hoặc để lại lời nhắn chi tiết tại đây để em giải đáp ngay nhé!";
    }
}
