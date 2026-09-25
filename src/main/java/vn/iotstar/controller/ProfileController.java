package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Address;
import vn.iotstar.entity.User;
import vn.iotstar.repository.AddressRepository;
import vn.iotstar.repository.BookConsignmentRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.List;

/**
 * ============================================================================
 * CONTROLLER QUẢN LÝ HỒ SƠ TÀI KHOẢN KHÁCH HÀNG (SHOPEE USER PROFILE STYLE)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đối tác kiểm tra: Cường (24162013) - Nhánh: feature/admin-cuong
 *
 * Ý nghĩa nghiệp vụ:
 * 1. Hiện thực hóa trang quản lý thông tin tài khoản chuẩn Shopee:
 *    - URL: /user/account/profile (hoặc /profile, /user/profile)
 * 2. Cho phép xem và cập nhật: Họ tên, Số điện thoại, Email, Giới tính, Ngày sinh, Avatar.
 * 3. Quản lý Sổ địa chỉ nhận hàng (Addresses) và Đổi mật khẩu tài khoản.
 * 4. Tích hợp thanh Sidebar bên trái hiển thị tổng quan: Số đơn mua, Phiếu ký gửi sách.
 * ============================================================================
 */
@Controller
@RequestMapping({"/user/account", "/user", "/profile"})
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookConsignmentRepository consignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Xác định userId của phiên hiện tại:
     * Ưu tiên tài khoản đăng nhập Spring Security, fallback ID 4 (Nguyễn Song Hoàng Phúc).
     */
    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userDetails.getId();
        }
        return 4L;
    }

    /**
     * Hiển thị trang Hồ Sơ Của Tôi chuẩn Shopee
     */
    @GetMapping({"/profile", ""})
    public String viewProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "tab", defaultValue = "profile") String tab,
            Model model) {

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        // Lấy danh sách địa chỉ nhận hàng của khách
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

        // Số lượng đơn hàng & số phiếu ký gửi
        int totalOrders = orderRepository.findByUserIdWithItems(userId).size();
        int totalConsignments = consignmentRepository.findByUserIdOrderByCreatedAtDesc(userId).size();

        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalConsignments", totalConsignments);
        model.addAttribute("activeTab", tab);
        model.addAttribute("pageTitle", "Hồ Sơ Của Tôi — Chuỗi Cửa Hàng Sách Cũ TP.HCM");

        return "user/profile";
    }

    /**
     * Xử lý cập nhật thông tin hồ sơ
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "avatar", required = false) String avatar,
            RedirectAttributes redirectAttributes) {

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setFullName(fullName.trim());
            user.setPhone(phone.trim());
            if (avatar != null && !avatar.trim().isEmpty()) {
                user.setAvatar(avatar.trim());
            }
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
        }

        return "redirect:/user/account/profile?tab=profile";
    }

    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/user/account/profile?tab=password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/user/account/profile?tab=password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/account/profile?tab=password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu tài khoản thành công!");
        return "redirect:/user/account/profile?tab=password";
    }

    /**
     * Xử lý thêm địa chỉ nhận hàng mới
     */
    @PostMapping("/address/add")
    public String addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("phone") String phone,
            @RequestParam("province") String province,
            @RequestParam("district") String district,
            @RequestParam("ward") String ward,
            @RequestParam("streetAddress") String streetAddress,
            @RequestParam(value = "isDefault", defaultValue = "false") boolean isDefault,
            RedirectAttributes redirectAttributes) {

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            Address address = new Address();
            address.setUser(user);
            address.setReceiverName(receiverName.trim());
            address.setPhone(phone.trim());
            address.setProvince(province.trim());
            address.setDistrict(district.trim());
            address.setWard(ward.trim());
            address.setStreetAddress(streetAddress.trim());
            address.setIsDefault(isDefault);

            addressRepository.save(address);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ giao hàng mới thành công!");
        }

        return "redirect:/user/account/profile?tab=address";
    }
}
