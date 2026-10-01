package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping({"", "/"})
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Category c = new Category();
        c.setIsActive(true);
        c.setIcon("fa-book");

        model.addAttribute("category", c);
        model.addAttribute("parentCategories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/categories/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirect) {
        Category c = categoryService.findById(id);
        if (c == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy danh mục thể loại!");
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", c);
        model.addAttribute("parentCategories", categoryService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category, RedirectAttributes redirect) {
        try {
            categoryService.save(category);
            redirect.addFlashAttribute("successMessage", "Lưu thể loại sách '" + category.getCategoryName() + "' thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirect) {
        try {
            categoryService.deleteById(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa danh mục thể loại!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: Danh mục này đang chứa các đầu sách, không thể xóa trực tiếp.");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") Integer id, RedirectAttributes redirect) {
        categoryService.toggleActive(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái danh mục thành công!");
        return "redirect:/admin/categories";
    }
}
