package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.ShippingUnit;
import vn.iotstar.service.IShippingUnitService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/shipping-units")
public class AdminShippingUnitController {

    @Autowired
    private IShippingUnitService shippingUnitService;

    @GetMapping({"", "/"})
    public String listUnits(Model model) {
        model.addAttribute("units", shippingUnitService.findAll());
        return "admin/shipping-units/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        ShippingUnit u = new ShippingUnit();
        u.setBaseFee(new BigDecimal("25000.00"));
        u.setEstimatedDays("2-3 ngày");
        u.setIsActive(true);

        model.addAttribute("unit", u);
        model.addAttribute("isEdit", false);
        return "admin/shipping-units/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirect) {
        ShippingUnit u = shippingUnitService.findById(id);
        if (u == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy đơn vị vận chuyển!");
            return "redirect:/admin/shipping-units";
        }
        model.addAttribute("unit", u);
        model.addAttribute("isEdit", true);
        return "admin/shipping-units/form";
    }

    @PostMapping("/save")
    public String saveUnit(@ModelAttribute("unit") ShippingUnit unit, RedirectAttributes redirect) {
        try {
            shippingUnitService.save(unit);
            redirect.addFlashAttribute("successMessage", "Lưu thông tin đơn vị vận chuyển '" + unit.getName() + "' thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/shipping-units";
    }

    @GetMapping("/delete/{id}")
    public String deleteUnit(@PathVariable("id") Integer id, RedirectAttributes redirect) {
        try {
            shippingUnitService.deleteById(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa đơn vị vận chuyển!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi xóa: " + e.getMessage());
        }
        return "redirect:/admin/shipping-units";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") Integer id, RedirectAttributes redirect) {
        shippingUnitService.toggleActive(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái đối tác giao hàng thành công!");
        return "redirect:/admin/shipping-units";
    }
}
