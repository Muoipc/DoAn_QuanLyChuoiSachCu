package vn.iotstar.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.SellerApplyFormDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.ICloudinaryService;
import vn.iotstar.service.IStoreRegistrationService;

import java.util.Map;

@Controller
public class SellerRegistrationController {

    @Autowired
    private IStoreRegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ICloudinaryService cloudinaryService;

    /**
     * Màn hình Đăng ký mở gian hàng đối tác ReBook
     * Cho phép truy cập công khai (hỗ trợ cả /stores/register và /seller/register)
     */
    @GetMapping({"/seller/register", "/stores/register"})
    public String showRegisterPage(Model model) {
        SellerApplyFormDTO formDTO = new SellerApplyFormDTO();
        formDTO.setProvince("TP. Hồ Chí Minh");
        formDTO.setOpenTime("08:00");
        formDTO.setCloseTime("21:30");
        model.addAttribute("form", formDTO);
        model.addAttribute("activeTab", "seller");
        return "seller/register";
    }

    /**
     * Tiếp nhận hồ sơ đăng ký mở gian hàng (Tạo tài khoản quản lý đối tác riêng biệt)
     */
    @PostMapping({"/seller/register", "/stores/register"})
    public String handleRegistrationSubmit(
            @ModelAttribute("form") SellerApplyFormDTO form,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpServletRequest request,
            RedirectAttributes redirect
    ) {
        String newUsername = form.getNewUsername() != null ? form.getNewUsername().trim() : "";
        String newPassword = form.getNewPassword() != null ? form.getNewPassword().trim() : "";
        String confirmPassword = form.getConfirmPassword() != null ? form.getConfirmPassword().trim() : "";
        String email = form.getEmail() != null ? form.getEmail().trim() : "";

        String redirectUrl = (request.getRequestURI() != null && request.getRequestURI().contains("/stores")) 
                ? "/stores/register" : "/seller/register";

        if (newUsername.isBlank() || newPassword.isBlank()) {
            redirect.addFlashAttribute("errorMessage", "Vui lòng nhập Tên đăng nhập và Mật khẩu cho tài khoản Quản lý quầy!");
            return "redirect:" + redirectUrl;
        }

        if (!newPassword.equals(confirmPassword)) {
            redirect.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:" + redirectUrl;
        }

        if (userRepository.findByUsername(newUsername).isPresent()) {
            redirect.addFlashAttribute("errorMessage", "Tên đăng nhập '" + newUsername + "' đã tồn tại! Vui lòng chọn tên khác.");
            return "redirect:" + redirectUrl;
        }

        if (!email.isBlank() && userRepository.findByEmail(email).isPresent()) {
            redirect.addFlashAttribute("errorMessage", "Email '" + email + "' đã tồn tại trong hệ thống!");
            return "redirect:" + redirectUrl;
        }

        // Tạo tài khoản Người bán đối tác riêng biệt (ROLE_USER, khi duyệt sẽ nâng quyền thành ROLE_MANAGER)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(3, "ROLE_USER", "Khách hàng")));

        User sellerUser = new User();
        sellerUser.setUsername(newUsername);
        sellerUser.setPassword(passwordEncoder.encode(newPassword));
        sellerUser.setFullName(form.getOwnerName() != null && !form.getOwnerName().isBlank() ? form.getOwnerName() : form.getStoreName());
        sellerUser.setEmail(!email.isBlank() ? email : newUsername + "@rebook.local");
        sellerUser.setPhone(form.getPhone());
        sellerUser.setRole(userRole);
        sellerUser.setEnabled(true);
        User savedSellerUser = userRepository.save(sellerUser);

        try {
            StoreRegistration registration = form.toEntity();

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    Map<String, String> upload = cloudinaryService.uploadFile(imageFile, "old_book_store/registrations");
                    if (upload.containsKey("url")) {
                        registration.setImage(upload.get("url"));
                    }
                } catch (Exception ignored) {
                    registration.setImage("/images/hero-books-vintage.jpg");
                }
            } else if (registration.getImage() == null || registration.getImage().isBlank()) {
                registration.setImage("/images/hero-books-vintage.jpg");
            }

            StoreRegistration savedReg = registrationService.submitRegistration(registration, savedSellerUser);
            redirect.addFlashAttribute("submittedReg", savedReg);
            redirect.addFlashAttribute("submittedUser", savedSellerUser);
            redirect.addFlashAttribute("successMessage", 
                    "🎉 Chúc mừng bạn! Hồ sơ đăng ký mở gian hàng '" + savedReg.getStoreName() + "' đã được nộp thành công với mã #" + savedReg.getId() + ".");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi nộp hồ sơ: " + e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }

    /**
     * Xem trạng thái xét duyệt
     */
    @GetMapping({"/seller/status", "/stores/register/status"})
    public String viewStatus(HttpServletRequest request) {
        String target = (request.getRequestURI() != null && request.getRequestURI().contains("/stores")) 
                ? "/stores/register" : "/seller/register";
        return "redirect:" + target;
    }
}
