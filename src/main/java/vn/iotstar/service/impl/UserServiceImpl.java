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

        otpService.generateAndSendOtp(user.getEmail(), OtpToken.TokenType.REGISTER);
    }

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

        if (cartRepository.findByUserId(user.getId()).isEmpty()) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        return true;
    }

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

    @Override
    @Transactional
    public void requestForgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        otpService.generateAndSendOtp(user.getEmail(), OtpToken.TokenType.FORGOT_PASSWORD);
    }

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

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }
}
