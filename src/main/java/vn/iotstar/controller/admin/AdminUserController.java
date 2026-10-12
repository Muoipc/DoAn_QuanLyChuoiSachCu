package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Danh sách tài khoản người dùng và phân quyền (CHỈ DÀNH RIÊNG CHO QUẢN TRỊ VIÊN - ADMIN)
     */
    @GetMapping({"", "/"})
    public String listUsers(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect,
            Model model
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên hệ thống (Admin) mới có quyền truy cập Quản lý Người dùng & Phân quyền!");
            return "redirect:/admin";
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.searchUsers(keyword.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());

        return "admin/users/list";
    }

    /**
     * Đổi vai trò người dùng (CHỈ DÀNH RIÊNG CHO ADMIN)
     */
    @PostMapping("/{id}/change-role")
    public String changeRole(
            @PathVariable("id") Long id,
            @RequestParam("roleId") Integer roleId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có quyền phân quyền vai trò người dùng!");
            return "redirect:/admin";
        }

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò"));
            user.setRole(role);
            userRepository.save(user);
            redirect.addFlashAttribute("successMessage", "Cập nhật vai trò tài khoản '" + user.getUsername() + "' thành: " + role.getName());
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Kích hoạt hoặc Khóa tài khoản (CHỈ DÀNH RIÊNG CHO ADMIN)
     */
    @PostMapping("/{id}/toggle-enabled")
    public String toggleEnabled(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có quyền khóa hoặc mở khóa tài khoản!");
            return "redirect:/admin";
        }

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
            user.setEnabled(!Boolean.TRUE.equals(user.getEnabled()));
            userRepository.save(user);
            redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái hoạt động của '" + user.getUsername() + "' thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Tạo tài khoản nhân viên mới (Manager, Shipper) trực tiếp từ Admin
     * POST /admin/users/create-staff
     */
    @PostMapping("/create-staff")
    public String createStaff(
            @RequestParam("fullName") String fullName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("roleId") Integer roleId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Không có quyền thực hiện!");
            return "redirect:/admin";
        }

        try {
            if (userRepository.existsByUsername(username.trim().toLowerCase())) {
                redirect.addFlashAttribute("errorMessage", "Tên đăng nhập '" + username + "' đã tồn tại trên hệ thống!");
                return "redirect:/admin/users";
            }

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ"));

            User user = new User();
            user.setFullName(fullName.trim());
            user.setUsername(username.trim().toLowerCase());
            user.setEmail(email.trim());
            user.setPhone(phone.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);

            userRepository.save(user);
            redirect.addFlashAttribute("successMessage", "Đã cấp tài khoản nhân viên '" + username + "' (" + role.getName() + ") thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi tạo tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Đặt lại mật khẩu nhân viên về mặc định (123456)
     * POST /admin/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Không có quyền thực hiện!");
            return "redirect:/admin";
        }

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirect.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
                return "redirect:/admin/users";
            }

            user.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(user);
            redirect.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu cho tài khoản '" + user.getUsername() + "' về mặc định (123456)!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi reset mật khẩu: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}
