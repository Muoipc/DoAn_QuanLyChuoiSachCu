package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Voucher;
import vn.iotstar.service.IVoucherService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @Autowired
    private IVoucherService voucherService;

    @GetMapping({"", "/"})
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.findAll());
        return "admin/vouchers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Voucher v = new Voucher();
        v.setDiscountType(Voucher.DiscountType.PERCENT);
        v.setStartDate(LocalDateTime.now());
        v.setEndDate(LocalDateTime.now().plusMonths(1));
        v.setUsageLimit(100);
        v.setIsActive(true);

        model.addAttribute("voucher", v);
        model.addAttribute("isEdit", false);
        return "admin/vouchers/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        Voucher v = voucherService.findById(id);
        if (v == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy voucher mã #" + id);
            return "redirect:/admin/vouchers";
        }
        model.addAttribute("voucher", v);
        model.addAttribute("isEdit", true);
        return "admin/vouchers/form";
    }

    @PostMapping("/save")
    public String saveVoucher(@ModelAttribute("voucher") Voucher voucher, RedirectAttributes redirect) {
        try {
            voucherService.save(voucher);
            redirect.addFlashAttribute("successMessage", "Lưu mã giảm giá " + voucher.getCode() + " thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            voucherService.deleteById(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa voucher thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") Long id, RedirectAttributes redirect) {
        voucherService.toggleActive(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái voucher thành công!");
        return "redirect:/admin/vouchers";
    }
}
