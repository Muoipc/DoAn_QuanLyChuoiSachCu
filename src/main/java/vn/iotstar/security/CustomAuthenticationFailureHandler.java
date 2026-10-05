package vn.iotstar.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Xử lý khi đăng nhập thất bại:
 * Bảo lưu tham số redirectURL để khi người dùng nhập lại mật khẩu đúng vẫn quay lại đúng trang mong muốn.
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthenticationFailureHandler() {
        setDefaultFailureUrl("/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String redirectUrl = request.getParameter("redirectURL");
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = request.getParameter("redirect");
        }

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            String failureUrl = "/login?error=true&redirectURL=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, failureUrl);
            return;
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}
