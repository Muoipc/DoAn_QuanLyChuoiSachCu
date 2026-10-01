package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.security.JwtTokenProvider;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    /**
     * API Đăng nhập cấp phát JWT Token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập đầy đủ username và password"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

            String roleName = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";
            String jwt = tokenProvider.generateToken(user.getUsername(), roleName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", jwt);
            response.put("tokenType", "Bearer");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", roleName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Sai tên đăng nhập hoặc mật khẩu!"));
        }
    }

    /**
     * API Lấy thông tin tài khoản hiện tại từ Bearer Token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("id", userDetails.getId());
        res.put("username", userDetails.getUsername());
        res.put("email", userDetails.getEmail());
        res.put("fullName", userDetails.getFullName());
        res.put("role", userDetails.getRoleName());
        res.put("avatar", userDetails.getAvatar());

        return ResponseEntity.ok(res);
    }
}
