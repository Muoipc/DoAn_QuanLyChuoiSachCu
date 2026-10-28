package vn.iotstar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.iotstar.security.CustomAuthenticationEntryPoint;
import vn.iotstar.security.CustomAuthenticationFailureHandler;
import vn.iotstar.security.CustomAuthenticationSuccessHandler;
import vn.iotstar.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          CustomAuthenticationFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình chuỗi lọc bảo mật SecurityFilterChain:
     * 1. URL công khai của khách: /, /books/**, /categories/**, /stores/**, /search/**, /cart/**, /vouchers/**, /api/**
     * 2. URL yêu cầu đăng nhập: /checkout/place-order, /orders/**, /user/**, /profile/** (tự động lưu redirectURL)
     * 3. Phân quyền phân hệ nội bộ: Admin, Quản lý chi nhánh (Store Manager), Shipper
     * 4. Custom AuthenticationEntryPoint & SuccessHandler để hỗ trợ luồng redirectURL mượt mà
     * 5. Tích hợp JwtAuthenticationFilter cho REST API /api/** và WebSocket /ws/**
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/upload/**", "/uploads/**", "/webjars/**", "/error", "/favicon.ico"
                ).permitAll()
                // Xác thực & Quản lý tài khoản
                .requestMatchers(
                    "/login", "/register", "/register/**", "/verify-otp", "/verify-otp/**",
                    "/resend-otp", "/resend-otp/**", "/forgot-password", "/forgot-password/**",
                    "/reset-password", "/reset-password/**"
                ).permitAll()
                // WebSocket & REST API công khai tra cứu
                .requestMatchers(
                    "/ws/**", "/api/auth/**", "/api/books/**", "/api/reviews/book/**"
                ).permitAll()
                // Route công khai cho khách hàng vãng lai (Public Storefront)
                .requestMatchers(
                    "/", "/home", "/books/**", "/categories/**", "/stores/**",
                    "/search/**", "/vouchers", "/vouchers/**",
                    "/checkout/vnpay-return",
                    "/seller/register", "/seller/register/**",
                    "/ai-assistant", "/ai-assistant/**", "/help", "/help/**"
                ).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/reviews/**", "/consignments", "/consignments/").permitAll()
                // Phân quyền API nghiệp vụ nội bộ
                .requestMatchers("/api/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
                .requestMatchers("/api/chat/staff/**", "/api/chat/store/*/ai-config", "/api/chat/store/*/toggle-ai", "/api/chat/store/*/test-ai")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/orders/*/status").hasAnyRole("ADMIN", "MANAGER", "SHIPPER")
                .requestMatchers("/api/notifications/**", "/api/wishlist/**", "/api/reviews/submit", "/api/chat/**").authenticated()
                // Phân quyền phân hệ nghiệp vụ nội bộ
                // 1. Quản trị viên toàn hệ thống (ADMIN ONLY)
                .requestMatchers(
                    "/admin/users", "/admin/users/**",
                    "/admin/shipping-units", "/admin/shipping-units/**",
                    "/admin/stores/create", "/admin/stores/delete/**", "/admin/stores/toggle-active/**",
                    "/admin/store-registrations", "/admin/store-registrations/**",
                    "/admin/categories", "/admin/categories/**"
                ).hasRole("ADMIN")
                // 2. Phân hệ vận hành chi nhánh, gian hàng & chuỗi (ADMIN, MANAGER, VENDOR)
                .requestMatchers("/admin/ai/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/admin/vouchers", "/admin/vouchers/**").hasAnyRole("ADMIN", "MANAGER", "VENDOR")
                .requestMatchers(
                    "/admin", "/admin/", "/admin/dashboard",
                    "/admin/books", "/admin/books/**",
                    "/admin/inventory", "/admin/inventory/**",
                    "/admin/stores", "/admin/stores/edit/**", "/admin/stores/save",
                    "/admin/chat", "/admin/chat/**",
                    "/admin/orders", "/admin/orders/**",
                    "/admin/consignments", "/admin/consignments/**",
                    "/admin/reports", "/admin/reports/**",
                    "/admin/support-tickets", "/admin/support-tickets/**",
                    "/store-manager/**"
                ).hasAnyRole("ADMIN", "MANAGER", "VENDOR")
                // 3. Phân hệ đối tác người bán (VENDOR / SELLER)
                .requestMatchers("/seller/**").hasAnyRole("ADMIN", "MANAGER", "VENDOR")
                // 4. Phân hệ giao hàng (SHIPPER)
                .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
                // 5. Mọi route admin còn lại mặc định thuộc ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 6. Route yêu cầu người dùng phải đăng nhập (Bảo mật tài khoản & Đơn mua)
                .requestMatchers(
                    "/cart", "/cart/**",
                    "/checkout", "/checkout/**",
                    "/orders", "/orders/**",
                    "/user/**", "/profile/**",
                    "/reviews/create",
                    "/consignments/create", "/consignments/tracking", "/consignments/tracking/**"
                ).hasAnyRole("USER", "VENDOR", "MANAGER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    if (uri != null && uri.startsWith("/admin")) {
                        response.sendRedirect("/admin?error=access_denied");
                    } else {
                        response.sendRedirect("/?error=access_denied");
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
