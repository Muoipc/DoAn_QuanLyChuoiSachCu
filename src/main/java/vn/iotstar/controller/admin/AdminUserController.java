package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
}
