package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.entity.User;
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
    private ICloudinaryService cloudinaryService;

    /**
     * Màn hình Đăng ký mở gian hàng đối tác ReBook
     */
    @GetMapping("/register")
    public String showRegisterPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/seller/register";
        }

        // Nếu đã là Store Manager, đưa thẳng vào Kênh Quản Trị Cửa Hàng
        if ("ROLE_MANAGER".equals(userDetails.getRoleName())) {
            return "redirect:/admin/inventory?notice=already_manager";
        }

        User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
        StoreRegistration latestReg = registrationService.getLatestByUser(userDetails.getId());

        model.addAttribute("user", currentUser);
        model.addAttribute("latestReg", latestReg);
        model.addAttribute("activeTab", "seller");

        if (latestReg == null || latestReg.getStatus() == StoreRegistration.Status.REJECTED) {
            StoreRegistration newReg = new StoreRegistration();
            newReg.setOwnerName(currentUser != null ? currentUser.getFullName() : "");
            newReg.setPhone(currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "");
            newReg.setEmail(currentUser != null ? currentUser.getEmail() : "");
            newReg.setProvince("TP. Hồ Chí Minh");
            newReg.setOpenTime("08:00");
            newReg.setCloseTime("21:30");
            model.addAttribute("registration", newReg);
            model.addAttribute("viewMode", "FORM");
        } else {
            // Đã nộp và đang PENDING hoặc APPROVED
            model.addAttribute("registration", latestReg);
            model.addAttribute("viewMode", "STATUS");
        }

        return "seller/register";
    }

    /**
     * Tiếp nhận hồ sơ đăng ký mở gian hàng
     */
    @PostMapping("/register")
    public String handleRegistrationSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute StoreRegistration registration,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirect
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
        if (currentUser == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy thông tin tài khoản người dùng!");
            return "redirect:/seller/register";
        }

        try {
            // Upload ảnh đại diện gian hàng nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    Map<String, String> upload = cloudinaryService.uploadFile(imageFile, "old_book_store/registrations");
                    if (upload.containsKey("url")) {
                        registration.setImage(upload.get("url"));
                    }
                } catch (Exception ignored) {
                    // Nếu lỗi upload thì dùng ảnh mặc định
                    registration.setImage("/images/hero-books-vintage.jpg");
                }
            } else if (registration.getImage() == null || registration.getImage().isBlank()) {
                registration.setImage("/images/hero-books-vintage.jpg");
            }

            registrationService.submitRegistration(registration, currentUser);
            redirect.addFlashAttribute("successMessage", "Nộp hồ sơ mở gian hàng thành công! ReBook sẽ xét duyệt trong 24 giờ.");
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
