package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IStoreRegistrationService;

import java.util.List;

@Controller
@RequestMapping({"/admin/store-registrations", "/admin/stores/registrations"})
public class AdminStoreRegistrationController {

    @Autowired
    private IStoreRegistrationService registrationService;

    /**
     * Danh sách hồ sơ đăng ký mở gian hàng (Chỉ dành cho ADMIN)
     */
    @GetMapping({"", "/"})
    public String listRegistrations(
            @RequestParam(value = "status", required = false) String statusFilter,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có quyền duyệt mở gian hàng!");
            return "redirect:/admin";
        }

        List<StoreRegistration> list;
        if ("PENDING".equalsIgnoreCase(statusFilter)) {
            list = registrationService.findByStatus(StoreRegistration.Status.PENDING);
        } else if ("APPROVED".equalsIgnoreCase(statusFilter)) {
            list = registrationService.findByStatus(StoreRegistration.Status.APPROVED);
        } else if ("REJECTED".equalsIgnoreCase(statusFilter)) {
            list = registrationService.findByStatus(StoreRegistration.Status.REJECTED);
        } else {
            list = registrationService.findAll();
        }

        long countPending = registrationService.countPending();

        model.addAttribute("registrations", list);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter.toUpperCase() : "ALL");
        model.addAttribute("countPending", countPending);
        model.addAttribute("pageTitle", "Duyệt Đăng Ký Mở Gian Hàng");

        return "admin/stores/registrations";
    }

    /**
     * Duyệt kích hoạt gian hàng
     */
    @PostMapping("/{id}/approve")
    public String approveRegistration(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Admin mới có quyền duyệt gian hàng!");
            return "redirect:/admin";
        }

        try {
            Store createdStore = registrationService.approveRegistration(id);
            redirect.addFlashAttribute("successMessage", 
                    "Duyệt thành công! Gian hàng '" + createdStore.getStoreName() + "' đã được kích hoạt và phân quyền Quản lý chi nhánh.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi duyệt hồ sơ: " + e.getMessage());
        }

        return "redirect:/admin/store-registrations";
    }

    /**
     * Từ chối hồ sơ kèm lý do
     */
    @PostMapping("/{id}/reject")
    public String rejectRegistration(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Admin mới có quyền từ chối gian hàng!");
            return "redirect:/admin";
        }

        try {
            registrationService.rejectRegistration(id, reason);
            redirect.addFlashAttribute("successMessage", "Đã từ chối hồ sơ #" + id + " và ghi nhận lý do phản hồi.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi từ chối hồ sơ: " + e.getMessage());
        }

        return "redirect:/admin/store-registrations";
    }
}
