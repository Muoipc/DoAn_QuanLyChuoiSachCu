package vn.iotstar.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.IOtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service xử lý sinh mã OTP ngẫu nhiên 6 chữ số, thời hạn 5 phút.
 * Tích hợp JavaMailSender gửi email thật và in ra Console để thuận tiện kiểm thử.
 */
@Service
public class OtpServiceImpl implements IOtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository, JavaMailSender mailSender) {
        this.otpTokenRepository = otpTokenRepository;
        this.mailSender = mailSender;
    }

    /**
     * Sinh mã OTP 6 số ngẫu nhiên an toàn (100000 - 999999), lưu vào DB với hạn 5 phút
     * và gửi đến email đích.
     */
    @Override
    @Transactional
    public String generateAndSendOtp(String email, OtpToken.TokenType tokenType) {
        String otpCode = String.format("%06d", random.nextInt(900000) + 100000);

        OtpToken otpToken = new OtpToken(email, otpCode, tokenType, LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(otpToken);

        // Gửi email qua giao thức SMTP
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Mã xác thực OTP - Chuỗi Cửa Hàng Sách Cũ");
            message.setText("Xin chào,\n\nMã xác thực OTP của bạn là: " + otpCode + 
                           "\nMã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Chưa cấu hình SMTP hoặc lỗi gửi mail: " + e.getMessage());
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
