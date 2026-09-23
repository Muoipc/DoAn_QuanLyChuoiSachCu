package vn.iotstar.service;

import vn.iotstar.dto.auth.RegisterDTO;
import vn.iotstar.entity.User;

import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ người dùng:
 * - Đăng ký, kích hoạt tài khoản
 * - Quản lý mật khẩu qua mã OTP
 * - Tìm kiếm người dùng theo username/email cho Spring Security
 */
public interface IUserService {
    void register(RegisterDTO dto);
    boolean verifyRegistration(String email, String otpCode);
    void resendRegistrationOtp(String email);
    void requestForgotPassword(String email);
    boolean resetPassword(String email, String otpCode, String newPassword);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
}
