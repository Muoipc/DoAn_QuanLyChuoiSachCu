package vn.iotstar.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {
    @NotBlank(message = "Vui lòng nhập địa chỉ email đã đăng ký")
    @Email(message = "Email không đúng định dạng")
    private String email;

    public ForgotPasswordDTO() {}
    public ForgotPasswordDTO(String email) { this.email = email; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
