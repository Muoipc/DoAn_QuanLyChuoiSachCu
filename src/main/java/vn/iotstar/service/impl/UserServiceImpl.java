package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.auth.RegisterDTO;
import vn.iotstar.entity.Cart;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.*;
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

    @Autowired(required = false)
    private CartItemRepository cartItemRepository;

    @Autowired(required = false)
    private WishlistRepository wishlistRepository;

    @Autowired(required = false)
    private ViewedBookRepository viewedBookRepository;

    @Autowired(required = false)
    private StoreFollowerRepository storeFollowerRepository;

    @Autowired(required = false)
    private UserVoucherRepository userVoucherRepository;

    @Autowired(required = false)
    private AddressRepository addressRepository;

    @Autowired(required = false)
    private ChatMessageRepository chatMessageRepository;

    @Autowired(required = false)
    private SupportTicketRepository supportTicketRepository;

    @Autowired(required = false)
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private BookConsignmentRepository bookConsignmentRepository;

    @Autowired(required = false)
    private StoreRepository storeRepository;

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

    /**
     * Xóa tài khoản người dùng an toàn:
     * - Kiểm tra các ràng buộc tài chính (Đơn hàng, Ký gửi sách, Quản lý cửa hàng).
     * - Nếu có dữ liệu lịch sử quan trọng, ném ngoại lệ hướng dẫn Khóa tài khoản thay vì xóa cứng.
     * - Dọn dẹp dữ liệu phụ trợ liên quan: Giỏ hàng, Yêu thích, Lịch sử xem, Đang theo dõi, Voucher, Địa chỉ, Thông báo.
     */
    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        // 1. Chặn xóa nếu là Admin hệ thống
        if (user.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalStateException("Không được phép xóa tài khoản Quản trị viên hệ thống (Admin)!");
        }

        // 2. Chặn xóa nếu đang phụ trách làm Quản lý chi nhánh cửa hàng
        if (storeRepository != null) {
            boolean isManager = storeRepository.findAll().stream()
                    .anyMatch(s -> s.getManager() != null && s.getManager().getId().equals(userId));
            if (isManager) {
                throw new IllegalStateException("Người dùng này đang làm Quản lý chi nhánh cửa hàng. Vui lòng bàn giao chi nhánh trước khi xóa!");
            }
        }

        // 3. Chặn xóa nếu đã có lịch sử giao dịch mua hàng hoặc nhận giao hàng
        if (orderRepository != null) {
            boolean hasOrders = !orderRepository.findByUserIdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged()).isEmpty();
            if (hasOrders) {
                throw new IllegalStateException("Tài khoản đã có lịch sử phát sinh Đơn hàng. Để bảo toàn hóa đơn và dữ liệu kế toán, vui lòng sử dụng tính năng 'Khóa tài khoản' thay vì xóa vĩnh viễn!");
            }
        }

        // 4. Chặn xóa nếu đang có phiếu Ký gửi sách
        if (bookConsignmentRepository != null) {
            boolean hasConsignments = !bookConsignmentRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty();
            if (hasConsignments) {
                throw new IllegalStateException("Tài khoản đang có hồ sơ Ký gửi sách cũ. Vui lòng thanh lý hoặc tất toán phiếu ký gửi trước khi xóa!");
            }
        }

        // 5. Dọn dẹp các bảng phụ trợ độc lập (Cascade cleanup)
        if (cartRepository != null) {
            cartRepository.findByUserId(userId).ifPresent(cart -> {
                if (cartItemRepository != null) {
                    cartItemRepository.deleteByCartId(cart.getId());
                }
                cartRepository.delete(cart);
            });
        }

        if (wishlistRepository != null) {
            wishlistRepository.findAll().stream()
                    .filter(w -> w.getUser() != null && w.getUser().getId().equals(userId))
                    .forEach(wishlistRepository::delete);
        }

        if (viewedBookRepository != null) {
            viewedBookRepository.findAll().stream()
                    .filter(v -> v.getUser() != null && v.getUser().getId().equals(userId))
                    .forEach(viewedBookRepository::delete);
        }

        if (storeFollowerRepository != null) {
            storeFollowerRepository.findAll().stream()
                    .filter(sf -> sf.getUser() != null && sf.getUser().getId().equals(userId))
                    .forEach(storeFollowerRepository::delete);
        }

        if (userVoucherRepository != null) {
            userVoucherRepository.findAll().stream()
                    .filter(uv -> uv.getUser() != null && uv.getUser().getId().equals(userId))
                    .forEach(userVoucherRepository::delete);
        }

        if (addressRepository != null) {
            addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .forEach(addressRepository::delete);
        }

        if (chatMessageRepository != null) {
            chatMessageRepository.findAll().stream()
                    .filter(m -> m.getUser() != null && m.getUser().getId().equals(userId))
                    .forEach(chatMessageRepository::delete);
        }

        if (supportTicketRepository != null) {
            supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .forEach(supportTicketRepository::delete);
        }

        if (notificationRepository != null) {
            notificationRepository.findAll().stream()
                    .filter(n -> n.getRecipient() != null && n.getRecipient().getId().equals(userId))
                    .forEach(notificationRepository::delete);
        }

        // Cuối cùng: Xóa người dùng khỏi bảng users
        userRepository.delete(user);
    }
}
