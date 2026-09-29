package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.Address;
import vn.iotstar.entity.User;
import vn.iotstar.repository.AddressRepository;
import vn.iotstar.repository.BookConsignmentRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        throw new IllegalStateException("Yêu cầu đăng nhập trước khi thực hiện thao tác này!");
    }

    /**
     * Hiển thị trang Hồ Sơ Của Tôi chuẩn Shopee
     */
    @GetMapping({"/profile", ""})
    @Transactional(readOnly = true)
    public String viewProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "tab", defaultValue = "profile") String tab,
            HttpServletRequest request,
            Model model) {

        if (userDetails == null || userDetails.getId() == null) {
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String fullUrl = (qs != null && !qs.isBlank()) ? (uri + "?" + qs) : uri;
            return "redirect:/login?redirectURL=" + URLEncoder.encode(fullUrl, StandardCharsets.UTF_8);
        }

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "redirect:/login?redirectURL=/user/account/profile";
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
     * Xử lý cập nhật thông tin hồ sơ (Họ tên, Email, Số điện thoại, Avatar)
     */
    @PostMapping("/profile/update")
    @Transactional
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("phone") String phone,
            @RequestParam(value = "avatar", required = false) String avatar,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile";
        }

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setFullName(fullName.trim());
            user.setPhone(phone.trim());

            // Cho phép người dùng tự thay đổi email từ form hồ sơ
            if (email != null && !email.trim().isEmpty()) {
                String cleanEmail = email.trim().toLowerCase();
                if (!cleanEmail.equalsIgnoreCase(user.getEmail())) {
                    if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ email không đúng định dạng hợp lệ!");
                        return "redirect:/user/account/profile?tab=profile";
                    }
                    if (userRepository.existsByEmail(cleanEmail)) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Email " + cleanEmail + " đã được sử dụng bởi một tài khoản khác!");
                        return "redirect:/user/account/profile?tab=profile";
                    }
                    user.setEmail(cleanEmail);
                }
            }

            if (avatar != null && !avatar.trim().isEmpty()) {
                user.setAvatar(avatar.trim());
            }
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
        }

        return "redirect:/user/account/profile?tab=profile";
    }

    /**
     * Xử lý thay đổi Email riêng biệt từ Modal Cập Nhật Email
     */
    @PostMapping("/profile/change-email")
    @Transactional
    public String changeEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("newEmail") String newEmail,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile";
        }

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            if (newEmail == null || newEmail.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập địa chỉ email mới!");
                return "redirect:/user/account/profile?tab=profile";
            }

            String cleanEmail = newEmail.trim().toLowerCase();
            if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ email không đúng định dạng hợp lệ!");
                return "redirect:/user/account/profile?tab=profile";
            }

            if (!cleanEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(cleanEmail)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email " + cleanEmail + " đã được sử dụng bởi tài khoản khác!");
                return "redirect:/user/account/profile?tab=profile";
            }

            user.setEmail(cleanEmail);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật địa chỉ email mới thành công: " + cleanEmail);
        }

        return "redirect:/user/account/profile?tab=profile";
    }

    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/profile/change-password")
    @Transactional
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile?tab=password";
        }

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "redirect:/login?redirectURL=/user/account/profile?tab=password";
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
    @Transactional
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

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile?tab=address";
        }

        Long userId = resolveUserId(userDetails);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            if (isDefault) {
                List<Address> all = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
                for (Address a : all) {
                    if (Boolean.TRUE.equals(a.getIsDefault())) {
                        a.setIsDefault(false);
                        addressRepository.save(a);
                    }
                }
            }

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

    /**
     * Xử lý cập nhật thông tin địa chỉ nhận hàng
     */
    @PostMapping("/address/update")
    @Transactional
    public String updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("id") Long addressId,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("phone") String phone,
            @RequestParam("province") String province,
            @RequestParam("district") String district,
            @RequestParam("ward") String ward,
            @RequestParam("streetAddress") String streetAddress,
            @RequestParam(value = "isDefault", defaultValue = "false") boolean isDefault,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile?tab=address";
        }

        Long userId = resolveUserId(userDetails);
        Address address = addressRepository.findById(addressId).orElse(null);

        if (address != null && address.getUser() != null && address.getUser().getId().equals(userId)) {
            if (isDefault && !Boolean.TRUE.equals(address.getIsDefault())) {
                List<Address> all = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
                for (Address a : all) {
                    if (Boolean.TRUE.equals(a.getIsDefault())) {
                        a.setIsDefault(false);
                        addressRepository.save(a);
                    }
                }
            }

            address.setReceiverName(receiverName.trim());
            address.setPhone(phone.trim());
            address.setProvince(province.trim());
            address.setDistrict(district.trim());
            address.setWard(ward.trim());
            address.setStreetAddress(streetAddress.trim());
            address.setIsDefault(isDefault);

            addressRepository.save(address);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ nhận hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy địa chỉ hợp lệ hoặc bạn không có quyền sửa!");
        }

        return "redirect:/user/account/profile?tab=address";
    }

    /**
     * Xử lý thiết lập địa chỉ mặc định
     */
    @PostMapping("/address/set-default/{id}")
    @Transactional
    public String setDefaultAddress(
            @PathVariable("id") Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile?tab=address";
        }

        Long userId = resolveUserId(userDetails);
        Address targetAddress = addressRepository.findById(addressId).orElse(null);

        if (targetAddress != null && targetAddress.getUser() != null && targetAddress.getUser().getId().equals(userId)) {
            List<Address> all = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            for (Address a : all) {
                a.setIsDefault(a.getId().equals(addressId));
                addressRepository.save(a);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã thiết lập địa chỉ mặc định thành công!");
        }

        return "redirect:/user/account/profile?tab=address";
    }

    /**
     * Xử lý xóa địa chỉ nhận hàng
     */
    @PostMapping("/address/delete/{id}")
    @Transactional
    public String deleteAddress(
            @PathVariable("id") Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getId() == null) {
            return "redirect:/login?redirectURL=/user/account/profile?tab=address";
        }

        Long userId = resolveUserId(userDetails);
        Address address = addressRepository.findById(addressId).orElse(null);

        if (address != null && address.getUser() != null && address.getUser().getId().equals(userId)) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa địa chỉ đang là Mặc Định!");
            } else {
                addressRepository.delete(address);
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa chỉ nhận hàng thành công!");
            }
        }

        return "redirect:/user/account/profile?tab=address";
    }
}
