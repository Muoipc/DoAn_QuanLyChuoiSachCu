package vn.iotstar.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.auth.RegisterDTO;
import vn.iotstar.entity.Cart;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CartRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.IOtpService;
import vn.iotstar.service.IUserService;

import java.util.Optional;

/**
 * Hiện thực nghiệp vụ quản lý người dùng:
 * Tích hợp mã hóa BCrypt, phân quyền mặc định ROLE_USER, gửi OTP kích hoạt
 * và khởi tạo giỏ hàng DB khi kích hoạt thành công.
 */
@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final IOtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
        UserRepository userRepository,
        RoleRepository roleRepository,
        CartRepository cartRepository,
        IOtpService otpService,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Đăng ký tài khoản:
     * 1. Kiểm tra tính trùng khớp mật khẩu và sự tồn tại của username/email trong DB
     * 2. Gán quyền mặc định ROLE_USER
     * 3. Mã hóa mật khẩu bằng BCrypt
     * 4. Lưu user ở trạng thái enabled = false
     * 5. Sinh và gửi mã OTP qua email
     */
    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trên hệ thống");
        }
        if (userRepository.existsByEmail(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Địa chỉ email đã được sử dụng");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> roleRepository.findById(3)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy vai trò ROLE_USER")));

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim());
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(userRole);
        user.setEnabled(false);

        userRepository.save(user);

        // Sinh mã OTP kích hoạt và gửi email
        otpService.generateAndSendOtp(user.getEmail(), OtpToken.TokenType.REGISTER);
    }

    /**
     * Xác thực mã OTP kích hoạt:
     * - Nếu mã đúng -> chuyển trạng thái user sang enabled = true
     * - Khởi tạo 1 giỏ hàng rỗng trong DB cho user (nếu chưa có)
     */
    @Override
    @Transactional
    public boolean verifyRegistration(String email, String otpCode) {
        boolean valid = otpService.verifyOtp(email, otpCode, OtpToken.TokenType.REGISTER);
        if (!valid) {
            return false;
        }

        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        user.setEnabled(true);
        userRepository.save(user);

        // Đảm bảo người dùng luôn có giỏ hàng lưu trữ trên Database
        if (cartRepository.findByUserId(user.getId()).isEmpty()) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        return true;
    }

    /**
     * Gửi lại mã OTP kích hoạt mới
     */
    @Override
    @Transactional
    public void resendRegistrationOtp(String email) {
        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalStateException("Tài khoản này đã được kích hoạt trước đó");
        }

        otpService.generateAndSendOtp(user.getEmail(), OtpToken.TokenType.REGISTER);
    }

    /**
     * Yêu cầu đặt lại mật khẩu: kiểm tra email tồn tại rồi sinh mã OTP
     */
    @Override
    @Transactional
    public void requestForgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        otpService.generateAndSendOtp(user.getEmail(), OtpToken.TokenType.FORGOT_PASSWORD);
    }

    /**
     * Đặt lại mật khẩu mới sau khi xác thực OTP thành công
     */
    @Override
    @Transactional
    public boolean resetPassword(String email, String otpCode, String newPassword) {
        boolean valid = otpService.verifyOtp(email, otpCode, OtpToken.TokenType.FORGOT_PASSWORD);
        if (!valid) {
            return false;
        }

        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    /**
     * Tìm kiếm user theo Username hoặc Email (dùng cho Spring Security)
     */
    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }
}
