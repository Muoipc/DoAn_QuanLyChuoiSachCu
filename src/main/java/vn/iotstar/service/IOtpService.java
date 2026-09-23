package vn.iotstar.service;

import vn.iotstar.entity.OtpToken;

public interface IOtpService {
    String generateAndSendOtp(String email, OtpToken.TokenType tokenType);
    boolean verifyOtp(String email, String otpCode, OtpToken.TokenType tokenType);
}
