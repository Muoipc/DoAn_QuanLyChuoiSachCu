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

@Controller
public class AuthController {

    private final IUserService userService;

    public AuthController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(
        @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Mã OTP kích hoạt đã được gửi tới email của bạn.");
            return "redirect:/verify-otp?email=" + dto.getEmail();
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "error.registerDTO", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam(value = "email", required = false) String email, Model model) {
        VerifyOtpDTO dto = new VerifyOtpDTO();
        dto.setEmail(email);
        model.addAttribute("verifyOtpDTO", dto);
        return "auth/verify-otp";
    }

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

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }
        return "auth/forgot-password";
    }

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

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "email", required = false) String email, Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail(email);
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

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
