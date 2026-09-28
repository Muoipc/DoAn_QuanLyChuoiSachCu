package vn.iotstar.config;

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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          CustomAuthenticationFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình chuỗi lọc bảo mật SecurityFilterChain:
     * 1. URL công khai của khách: /, /books/**, /categories/**, /stores/**, /search/**, /cart/**, /vouchers/**, /api/**
     * 2. URL yêu cầu đăng nhập: /checkout, /orders/**, /user/**, /profile/** (tự động lưu redirectURL)
     * 3. Phân quyền phân hệ nội bộ: Admin, Quản lý chi nhánh (Store Manager), Shipper
     * 4. Custom AuthenticationEntryPoint & SuccessHandler để hỗ trợ luồng redirectURL mượt mà
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/api/**", "/ws/**", "/cart/**", "/checkout/**", "/orders/**", 
                "/reviews/**", "/consignments/**", "/ai-assistant/**", "/help/**", 
                "/user/**", "/profile/**", "/login", "/register", "/logout",
                "/vouchers", "/vouchers/**", "/api/vouchers/**"
            ))
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/error", "/favicon.ico"
                ).permitAll()
                // Xác thực & Quản lý tài khoản
                .requestMatchers(
                    "/login", "/register", "/register/**", "/verify-otp", "/verify-otp/**",
                    "/resend-otp", "/resend-otp/**", "/forgot-password", "/forgot-password/**",
                    "/reset-password", "/reset-password/**"
                ).permitAll()
                // Route công khai cho khách hàng vãng lai (Public Storefront)
                .requestMatchers(
                    "/", "/home", "/books/**", "/categories/**", "/stores/**",
                    "/search/**", "/cart", "/cart/**",
                    "/vouchers", "/vouchers/**", "/api/**",
                    "/checkout/vnpay-return", "/checkout/success",
                    "/reviews/**", "/consignments", "/consignments/**",
                    "/ai-assistant", "/ai-assistant/**", "/help", "/help/**"
                ).permitAll()
                // Phân quyền phân hệ nghiệp vụ nội bộ
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/store-manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
                // Route yêu cầu người dùng phải đăng nhập (Bảo mật tài khoản & Đơn mua)
                .requestMatchers(
                    "/checkout", "/checkout/**",
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
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
