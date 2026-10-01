package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Danh sách tài khoản người dùng và phân quyền (Admin, Store Manager, Customer, Shipper)
     */
    @GetMapping({"", "/"})
    public String listUsers(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
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
     * Đổi vai trò người dùng (Phân quyền Admin, Store Manager, Shipper, Customer)
     */
    @PostMapping("/{id}/change-role")
    public String changeRole(
            @PathVariable("id") Long id,
            @RequestParam("roleId") Integer roleId,
            RedirectAttributes redirect
    ) {
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
     * Kích hoạt hoặc Khóa tài khoản
     */
    @PostMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable("id") Long id, RedirectAttributes redirect) {
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
