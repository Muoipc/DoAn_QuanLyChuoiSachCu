package vn.iotstar.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Xử lý điều hướng sau khi người dùng đăng nhập thành công:
 * 1. Ưu tiên tham số redirectURL hoặc redirect được truyền từ form/query parameter.
 * 2. Nếu không có, kiểm tra SavedRequest do Spring Security lưu trữ khi chặn truy cập trước đó.
 * 3. Nếu không có trang đích hợp lệ, chuyển hướng về trang chủ ("/").
 * 4. Chống lỗi bảo mật Open Redirect (chỉ cho phép các relative URL nội bộ bắt đầu bằng "/").
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. Kiểm tra tham số redirectURL hoặc redirect từ request (POST body hoặc query string)
        String redirectUrl = request.getParameter("redirectURL");
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = request.getParameter("redirect");
        }

        if (redirectUrl != null && !redirectUrl.isBlank() && isSafeUrl(redirectUrl)) {
            requestCache.removeRequest(request, response);
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            return;
        }

        // 2. Nếu không có param trong request, kiểm tra SavedRequest do Spring Security lưu trong session
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            if (targetUrl != null && !targetUrl.isBlank() && isSafeUrl(targetUrl)) {
                requestCache.removeRequest(request, response);
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
        }

        // 3. Phân luồng điều hướng theo vai trò khi không có URL chỉ định
        clearAuthenticationAttributes(request);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isManager = authorities.stream().anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()));
        boolean isAdmin = authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isShipper = authorities.stream().anyMatch(a -> "ROLE_SHIPPER".equals(a.getAuthority()));

        if (isManager) {
            getRedirectStrategy().sendRedirect(request, response, "/admin/inventory?storeId=1");
            return;
        }
        if (isAdmin) {
            getRedirectStrategy().sendRedirect(request, response, "/admin/books");
            return;
        }
        if (isShipper) {
            getRedirectStrategy().sendRedirect(request, response, "/shipper/orders");
            return;
        }

        // Mặc định khách hàng chuyển về trang chủ "/"
        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    private boolean isSafeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        // Chỉ chấp nhận relative URL nội bộ, loại bỏ Open Redirect (như //evil.com hoặc http://...)
        if (url.startsWith("/") && !url.startsWith("//")) {
            return !url.contains("/login");
        }
        return false;
    }
}
