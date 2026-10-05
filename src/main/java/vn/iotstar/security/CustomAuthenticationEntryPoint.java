package vn.iotstar.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Xử lý khi người dùng chưa đăng nhập cố gắng truy cập các trang yêu cầu quyền (Protected routes).
 * Tự động chuyển hướng về trang /login kèm tham số redirectURL trỏ tới URL ban đầu người dùng muốn truy cập.
 */
public class CustomAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationEntryPoint() {
        super("/login");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Lưu SavedRequest vào session theo chuẩn Spring Security
        requestCache.saveRequest(request, response);

        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/login")) {
            super.commence(request, response, authException);
            return;
        }

        // Lấy full URL bao gồm cả query parameter (ví dụ: /checkout?buyNow=true&bookId=1&quantity=1)
        String queryString = request.getQueryString();
        String fullUrl = (queryString != null && !queryString.isBlank()) ? (uri + "?" + queryString) : uri;

        // Nếu là POST request (như submit form tạo đơn), chuyển hướng GET về trang nghiệp vụ cha
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (uri != null && uri.startsWith("/checkout")) {
                fullUrl = "/checkout";
            } else if (uri != null && uri.startsWith("/orders")) {
                fullUrl = "/orders";
            } else if (uri != null && uri.startsWith("/user")) {
                fullUrl = "/user/account/profile";
            }
        }

        String redirectUrl = "/login?redirectURL=" + URLEncoder.encode(fullUrl, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
