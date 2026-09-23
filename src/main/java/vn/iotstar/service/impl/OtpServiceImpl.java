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

@Service
public class OtpServiceImpl implements IOtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository, JavaMailSender mailSender) {
        this.otpTokenRepository = otpTokenRepository;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public String generateAndSendOtp(String email, OtpToken.TokenType tokenType) {
        String otpCode = String.format("%06d", random.nextInt(900000) + 100000);

        OtpToken otpToken = new OtpToken(email, otpCode, tokenType, LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(otpToken);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Mã xác thực OTP - Chuỗi Cửa Hàng Sách Cũ");
            message.setText("Xin chào,\n\nMã xác thực OTP của bạn là: " + otpCode + 
                           "\nMã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Không gửi được email thật (kiểm tra cấu hình SMTP): " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.println(">>> OTP TOKEN (" + tokenType + ") cho " + email + ": " + otpCode);
        System.out.println("==================================================");

        return otpCode;
    }

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
