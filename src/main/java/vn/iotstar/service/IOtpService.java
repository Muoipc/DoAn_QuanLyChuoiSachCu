package vn.iotstar.service;

import vn.iotstar.entity.OtpToken;

/**
 * Interface quản lý sinh mã, lưu trữ và xác thực mã OTP
 */
public interface IOtpService {
    String generateAndSendOtp(String email, OtpToken.TokenType tokenType);
    boolean verifyOtp(String email, String otpCode, OtpToken.TokenType tokenType);
}
