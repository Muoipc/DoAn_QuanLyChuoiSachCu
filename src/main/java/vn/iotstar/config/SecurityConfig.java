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
                // WebSocket & REST API công khai
                .requestMatchers(
                    "/ws/**", "/api/auth/**", "/api/books/**", "/admin/ai/**"
                ).permitAll()
                // Route công khai cho khách hàng vãng lai (Public Storefront)
                .requestMatchers(
                    "/", "/home", "/books/**", "/categories/**", "/stores/**",
                    "/search/**", "/cart", "/cart/**",
                    "/vouchers", "/vouchers/**", "/api/**",
                    "/checkout", "/checkout/vnpay-return", "/checkout/success",
                    "/reviews/**", "/consignments", "/consignments/**",
                    "/ai-assistant", "/ai-assistant/**", "/help", "/help/**"
                ).permitAll()
                // Phân quyền phân hệ nghiệp vụ nội bộ
                .requestMatchers("/admin/users", "/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/store-manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
                // Route yêu cầu người dùng phải đăng nhập (Bảo mật tài khoản & Đơn mua)
                .requestMatchers(
                    "/checkout/place-order", "/checkout/process",
                    "/orders", "/orders/**",
                    "/user/**", "/profile/**"
                ).authenticated()
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
