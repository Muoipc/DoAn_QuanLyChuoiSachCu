package vn.iotstar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình chuỗi lọc bảo mật SecurityFilterChain:
     * - Phân quyền truy cập các URL công khai (Trang chủ, Tìm kiếm, Đăng nhập, Đăng ký, OTP)
     * - Phân quyền các phân hệ: Admin, Quản lý chi nhánh (Store Manager), Nhân viên giao hàng (Shipper)
     * - Tùy biến trang đăng nhập (/login) và đăng xuất (/logout)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/ws/**", "/cart/**", "/checkout/**", "/orders/**", "/reviews/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home", "/books/**", "/stores/**", "/categories/**",
                    "/search/**", "/cart", "/cart/**", "/checkout", "/checkout/**", 
                    "/orders", "/orders/**", "/reviews/**",
                    "/api/**", "/login", "/register/**", "/verify-otp/**",
                    "/resend-otp/**", "/forgot-password/**", "/reset-password/**",
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/error"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/store-manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
