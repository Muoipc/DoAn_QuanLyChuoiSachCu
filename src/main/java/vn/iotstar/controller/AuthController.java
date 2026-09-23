package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.auth.ForgotPasswordDTO;
import vn.iotstar.dto.auth.RegisterDTO;
import vn.iotstar.dto.auth.ResetPasswordDTO;
import vn.iotstar.dto.auth.VerifyOtpDTO;
import vn.iotstar.service.IUserService;

/**
 * Controller điều hướng và xử lý toàn bộ luồng xác thực (Authentication):
 * - Đăng nhập (Username hoặc Email)
 * - Đăng ký tài khoản mới kèm gửi mã OTP xác thực email
 * - Kích hoạt tài khoản qua OTP
 * - Quên mật khẩu và đặt lại mật khẩu bằng mã xác minh OTP
 */
@Controller
public class AuthController {

    private final IUserService userService;

    public AuthController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị trang đăng nhập.
     * Spring Security sẽ tự động chặn các request POST /login để xác thực.
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * Hiển thị form đăng ký tài khoản khách hàng mới.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "auth/register";
    }

    /**
     * Tiếp nhận dữ liệu form đăng ký, kiểm tra tính hợp lệ (Validation).
     * Nếu hợp lệ -> Gọi UserService lưu tài khoản ở trạng thái chờ kích hoạt (enabled=false)
     * và gửi mã OTP qua email người dùng -> Chuyển hướng sang trang nhập OTP.
     */
    @PostMapping("/register")
    public String handleRegister(
        @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        // Kiểm tra lỗi validation theo các annotation (@NotBlank, @Email, @Size,...)
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Mã OTP kích hoạt đã được gửi tới email của bạn.");
            return "redirect:/verify-otp?email=" + dto.getEmail();
        } catch (IllegalArgumentException ex) {
            // Bắt lỗi trùng username hoặc email từ tầng Service
            bindingResult.rejectValue("email", "error.registerDTO", ex.getMessage());
            return "auth/register";
        }
    }

    /**
     * Hiển thị giao diện nhập mã OTP kích hoạt tài khoản.
     */
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam(value = "email", required = false) String email, Model model) {
        VerifyOtpDTO dto = new VerifyOtpDTO();
        dto.setEmail(email);
        model.addAttribute("verifyOtpDTO", dto);
        return "auth/verify-otp";
    }

    /**
     * Xử lý xác thực OTP:
     * - Nếu mã đúng và còn hạn (5 phút) -> Kích hoạt tài khoản (enabled=true) và tạo giỏ hàng DB
     * - Nếu sai/hết hạn -> Báo lỗi ngay trên ô nhập OTP
     */
    @PostMapping("/verify-otp")
    public String handleVerifyOtp(
        @Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO dto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        boolean verified = userService.verifyRegistration(dto.getEmail(), dto.getOtpCode());
        if (!verified) {
            bindingResult.rejectValue("otpCode", "error.verifyOtpDTO", "Mã OTP không chính xác hoặc đã hết hạn.");
            return "auth/verify-otp";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Tài khoản đã kích hoạt thành công! Mời bạn đăng nhập.");
        return "redirect:/login?verified=true";
    }

    /**
     * Gửi lại mã OTP kích hoạt mới khi người dùng chưa nhận được hoặc mã cũ hết hạn.
     */
    @GetMapping("/resend-otp")
    public String handleResendOtp(
        @RequestParam("email") String email,
        RedirectAttributes redirectAttributes
    ) {
        try {
            userService.resendRegistrationOtp(email);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP mới đã được gửi lại vào email của bạn.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/verify-otp?email=" + email;
    }

    /**
     * Hiển thị trang yêu cầu quên mật khẩu.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }
        return "auth/forgot-password";
    }

    /**
     * Xử lý gửi OTP đặt lại mật khẩu vào email của người dùng.
     */
    @PostMapping("/forgot-password")
    public String handleForgotPassword(
        @Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            userService.requestForgotPassword(dto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Mã OTP đặt lại mật khẩu đã được gửi vào email của bạn.");
            return "redirect:/reset-password?email=" + dto.getEmail();
        } catch (Exception ex) {
            bindingResult.rejectValue("email", "error.forgotPasswordDTO", ex.getMessage());
            return "auth/forgot-password";
        }
    }

    /**
     * Hiển thị giao diện nhập mã OTP và mật khẩu mới.
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "email", required = false) String email, Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail(email);
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

    /**
     * Xác nhận đổi mật khẩu:
     * - Kiểm tra khớp mật khẩu xác nhận
     * - Kiểm tra OTP hợp lệ
     * - Mã hóa mật khẩu mới bằng BCrypt và lưu xuống DB
     */
    @PostMapping("/reset-password")
    public String handleResetPassword(
        @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.resetPasswordDTO", "Mật khẩu xác nhận không trùng khớp.");
            return "auth/reset-password";
        }

        boolean reset = userService.resetPassword(dto.getEmail(), dto.getOtpCode(), dto.getNewPassword());
        if (!reset) {
            bindingResult.rejectValue("otpCode", "error.resetPasswordDTO", "Mã OTP không chính xác hoặc đã hết hạn.");
            return "auth/reset-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Mời bạn đăng nhập lại.");
        return "redirect:/login?reset=true";
    }
}
