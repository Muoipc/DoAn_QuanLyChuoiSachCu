package vn.iotstar.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ICloudinaryService;
import vn.iotstar.service.IStoreRegistrationService;

import java.util.Map;

@Controller
@RequestMapping("/seller")
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
     * Cho phép truy cập công khai (cả Guest chưa có tài khoản và User đã đăng nhập)
     */
    @GetMapping("/register")
    public String showRegisterPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "reapply", required = false) Boolean reapply,
            Model model
    ) {
        // Nếu đã là Store Manager, đưa thẳng vào Kênh Quản Trị Cửa Hàng
        if (userDetails != null && "ROLE_MANAGER".equals(userDetails.getRoleName())) {
            return "redirect:/admin/inventory?notice=already_manager";
        }

        User currentUser = null;
        StoreRegistration latestReg = null;

        if (userDetails != null) {
            currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            latestReg = registrationService.getLatestByUser(userDetails.getId());
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("latestReg", latestReg);
        model.addAttribute("activeTab", "seller");
        model.addAttribute("isLoggedIn", currentUser != null);

        // Xem chế độ STATUS nếu đã có đơn nộp đang PENDING/APPROVED (trừ khi cố tình bấm reapply)
        if (latestReg != null && Boolean.TRUE.equals(reapply)) {
            SellerApplyFormDTO formDTO = new SellerApplyFormDTO();
            fillFormFromExisting(formDTO, currentUser, latestReg);
            model.addAttribute("form", formDTO);
            model.addAttribute("viewMode", "FORM");
        } else if (latestReg != null && (latestReg.getStatus() == StoreRegistration.Status.PENDING || latestReg.getStatus() == StoreRegistration.Status.APPROVED)) {
            model.addAttribute("registration", latestReg);
            model.addAttribute("viewMode", "STATUS");
        } else {
            SellerApplyFormDTO formDTO = new SellerApplyFormDTO();
            fillFormFromExisting(formDTO, currentUser, latestReg);
            model.addAttribute("form", formDTO);
            model.addAttribute("viewMode", "FORM");
        }

        return "seller/register";
    }

    private void fillFormFromExisting(SellerApplyFormDTO form, User user, StoreRegistration reg) {
        if (reg != null) {
            form.setStoreName(reg.getStoreName());
            form.setPhone(reg.getPhone());
            form.setEmail(reg.getEmail());
            form.setProvince(reg.getProvince());
            form.setDistrict(reg.getDistrict());
            form.setAddress(reg.getAddress());
            form.setOpenTime(reg.getOpenTime());
            form.setCloseTime(reg.getCloseTime());
            form.setDescription(reg.getDescription());
            form.setImage(reg.getImage());
            form.setOwnerName(reg.getOwnerName());
            form.setOwnerIdCard(reg.getOwnerIdCard());
            form.setBankName(reg.getBankName());
            form.setBankAccount(reg.getBankAccount());
        } else if (user != null) {
            form.setOwnerName(user.getFullName() != null ? user.getFullName() : "");
            form.setPhone(user.getPhone() != null ? user.getPhone() : "");
            form.setEmail(user.getEmail() != null ? user.getEmail() : "");
        }
    }

    /**
     * Tiếp nhận hồ sơ đăng ký mở gian hàng (All-in-One: Tạo tài khoản nếu chưa có)
     */
    @PostMapping("/register")
    public String handleRegistrationSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("form") SellerApplyFormDTO form,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpServletRequest request,
            RedirectAttributes redirect
    ) {
        User targetUser = null;

        // 1. Nếu đã đăng nhập, lấy User hiện tại
        if (userDetails != null) {
            targetUser = userRepository.findById(userDetails.getId()).orElse(null);
        }

        // 2. Nếu là khách vãng lai chưa có tài khoản, tự động tạo tài khoản mới cho chủ quầy
        if (targetUser == null) {
            String newUsername = form.getNewUsername() != null ? form.getNewUsername().trim() : "";
            String newPassword = form.getNewPassword() != null ? form.getNewPassword().trim() : "";
            String confirmPassword = form.getConfirmPassword() != null ? form.getConfirmPassword().trim() : "";
            String email = form.getEmail() != null ? form.getEmail().trim() : "";

            if (newUsername.isBlank() || newPassword.isBlank()) {
                redirect.addFlashAttribute("errorMessage", "Vui lòng nhập Tên đăng nhập và Mật khẩu để khởi tạo tài khoản Quản lý quầy!");
                return "redirect:/seller/register";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirect.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp, vui lòng nhập lại!");
                return "redirect:/seller/register";
            }

            if (userRepository.findByUsername(newUsername).isPresent()) {
                redirect.addFlashAttribute("errorMessage", "Tên đăng nhập '" + newUsername + "' đã được sử dụng. Vui lòng chọn tên khác!");
                return "redirect:/seller/register";
            }

            if (!email.isBlank() && userRepository.findByEmail(email).isPresent()) {
                redirect.addFlashAttribute("errorMessage", "Email '" + email + "' đã tồn tại trong hệ thống. Nếu đã có tài khoản, hãy đăng nhập trước!");
                return "redirect:/seller/register";
            }

            // Tạo tài khoản User mới với quyền ROLE_USER (sẽ được nâng ROLE_MANAGER khi Admin duyệt)
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role(1, "ROLE_USER", "Khách hàng")));

            User newUser = new User();
            newUser.setUsername(newUsername);
            newUser.setPassword(passwordEncoder.encode(newPassword));
            newUser.setFullName(form.getOwnerName() != null && !form.getOwnerName().isBlank() ? form.getOwnerName() : form.getStoreName());
            newUser.setEmail(!email.isBlank() ? email : newUsername + "@rebook.local");
            newUser.setPhone(form.getPhone());
            newUser.setRole(userRole);
            newUser.setEnabled(true); // Kích hoạt luôn để đăng nhập sau khi duyệt
            targetUser = userRepository.save(newUser);

            // Tự động đăng nhập vào session để khách theo dõi trạng thái hồ sơ ngay lập tức
            CustomUserDetails newUserDetails = new CustomUserDetails(targetUser);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(newUserDetails, null, newUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        }

        try {
            StoreRegistration registration = form.toEntity();

            // Upload ảnh đại diện gian hàng nếu có
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

            registrationService.submitRegistration(registration, targetUser);
            redirect.addFlashAttribute("successMessage", 
                    "🎉 Chúc mừng bạn! Hồ sơ mở gian hàng đã được nộp thành công. Ban quản trị ReBook sẽ xét duyệt và kích hoạt quyền Quản lý chi nhánh cho bạn trong vòng 24 giờ.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi khi nộp hồ sơ: " + e.getMessage());
        }

        return "redirect:/seller/register";
    }

    /**
     * Xem trạng thái xét duyệt
     */
    @GetMapping("/status")
    public String viewStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return "redirect:/seller/register";
    }
}
