package vn.iotstar.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.IOtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service xử lý sinh mã OTP ngẫu nhiên 6 chữ số, thời hạn 5 phút.
 * Tích hợp JavaMailSender gửi email HTML chuyên nghiệp và in ra Console để thuận tiện kiểm thử.
 */
@Service
public class OtpServiceImpl implements IOtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.username:demo@gmail.com}")
    private String mailSenderUsername;

    public OtpServiceImpl(
        OtpTokenRepository otpTokenRepository,
        @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender
    ) {
        this.otpTokenRepository = otpTokenRepository;
        this.mailSender = mailSender;
    }

    /**
     * Sinh mã OTP 6 số ngẫu nhiên an toàn (100000 - 999999), lưu vào DB với hạn 5 phút
     * và gửi đến email đích qua HTML template.
     */
    @Override
    @Transactional
    public String generateAndSendOtp(String email, OtpToken.TokenType tokenType) {
        String otpCode = String.format("%06d", random.nextInt(900000) + 100000);

        OtpToken otpToken = new OtpToken(email, otpCode, tokenType, LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(otpToken);

        String typeLabel = tokenType == OtpToken.TokenType.FORGOT_PASSWORD 
                ? "Đặt Lại Mật Khẩu" : (tokenType == OtpToken.TokenType.CHANGE_PASSWORD ? "Đổi Mật Khẩu Tài Khoản" : "Kích Hoạt Tài Khoản");

        // Gửi email qua giao thức SMTP (HTML Formatted)
        try {
            if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl) {
                if (impl.getPassword() != null && impl.getPassword().contains(" ")) {
                    impl.setPassword(impl.getPassword().replace(" ", "").trim());
                }
                System.out.println(">>> [SMTP CONFIG CHECK] Host=" + impl.getHost() 
                        + ", Port=" + impl.getPort() 
                        + ", Username=" + impl.getUsername()
                        + ", PasswordLength=" + (impl.getPassword() != null ? impl.getPassword().length() : 0));
            }

            if (mailSender != null && !mailSenderUsername.contains("demo@gmail.com")) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(mailSenderUsername, "Chuỗi Cửa Hàng Sách Cũ TP.HCM");
                helper.setTo(email);
                helper.setSubject("Mã xác thực OTP (" + typeLabel + ") — Chuỗi Cửa Hàng Sách Cũ");

                String htmlContent = "<div style=\"max-width:540px; margin:0 auto; padding:28px; background:#F8F5EE; font-family:Arial,sans-serif; border-radius:18px;\">"
                        + "<div style=\"text-align:center; margin-bottom:20px;\">"
                        + "<h2 style=\"color:#8C4A27; margin:0 0 6px 0;\">CHUỖI CỬA HÀNG SÁCH CŨ TP.HCM</h2>"
                        + "<p style=\"color:#78716C; font-size:13px; margin:0;\">Hệ Thống 5 Chi Nhánh Tri Thức Cố Đô Sài Gòn</p>"
                        + "</div>"
                        + "<div style=\"background:#ffffff; padding:24px; border-radius:14px; box-shadow:0 4px 16px rgba(0,0,0,0.06); text-align:center;\">"
                        + "<p style=\"font-size:14px; color:#1C1917; margin-bottom:12px;\">Xin chào quý khách,</p>"
                        + "<p style=\"font-size:14px; color:#44403C; line-height:1.5;\">Bạn vừa gửi yêu cầu <strong>" + typeLabel + "</strong>. Vui lòng sử dụng mã OTP bên dưới để hoàn tất:</p>"
                        + "<div style=\"margin:24px auto; padding:14px 28px; background:#F5ECE4; border:2px dashed #8C4A27; border-radius:12px; display:inline-block;\">"
                        + "<span style=\"font-size:32px; font-weight:900; letter-spacing:8px; color:#8C4A27;\">" + otpCode + "</span>"
                        + "</div>"
                        + "<p style=\"font-size:12.5px; color:#78716C; margin:0;\"><i style=\"color:#dc2626;\">⚠️ Lưu ý:</i> Mã OTP có hiệu lực trong <strong>5 phút</strong>. Không chia sẻ mã này cho bất kỳ ai.</p>"
                        + "</div>"
                        + "<div style=\"text-align:center; margin-top:20px; font-size:11.5px; color:#a8a29e;\">"
                        + "<p>© 2026 Quản lý chuỗi cửa hàng sách cũ — Đề tài 14 (HCMUTE)</p>"
                        + "</div>"
                        + "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                System.out.println(">>> [EMAIL THẬT] Đã gửi thành công mã OTP tới hộp thư: " + email);
            } else {
                System.out.println(">>> [DEV MODE] Chưa cấu hình Gmail SMTP thật, bỏ qua gửi mạng.");
            }
        } catch (Exception e) {
            System.err.println(">>> [SMTP ERROR] Gửi mail thất bại: " + e.getMessage());
        }

        // In mã OTP ra màn hình Console để thành viên nhóm kiểm tra nhanh mà không cần mở hộp thư
        System.out.println("==================================================");
        System.out.println(">>> [KIỂM THỬ] MÃ OTP (" + tokenType + ") cho " + email + ": " + otpCode);
        System.out.println("==================================================");

        return otpCode;
    }

    /**
     * Xác thực mã OTP:
     * - Kiểm tra mã mới nhất theo email và mục đích (REGISTER / FORGOT_PASSWORD)
     * - Kiểm tra thời gian hết hạn (expiresAt > now)
     * - Đánh dấu mã đã dùng (isUsed = true) để chống tấn công Replay Attack
     */
    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode, OtpToken.TokenType tokenType) {
        return otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(email, tokenType)
            .map(token -> {
                if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                    return false;
                }
                if (token.getOtpCode().equals(otpCode.trim())) {
                    token.setIsUsed(true);
                    otpTokenRepository.save(token);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
