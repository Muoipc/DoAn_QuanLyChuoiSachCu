package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.ICloudinaryService;
import vn.iotstar.service.IStoreService;
import vn.iotstar.util.SlugUtil;

import java.util.Map;

@Controller
@RequestMapping("/admin/stores")
public class AdminStoreController {

    @Autowired
    private IStoreService storeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ICloudinaryService cloudinaryService;

    /**
     * Danh sách 5 chi nhánh chuỗi cửa hàng sách cũ
     */
    @GetMapping({"", "/"})
    public String listStores(Model model) {
        model.addAttribute("stores", storeService.findAll());
        return "admin/stores/list";
    }

    /**
     * Trang thêm mới chi nhánh
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        Store store = new Store();
        store.setOpenTime("08:00");
        store.setCloseTime("21:30");
        store.setIsActive(true);

        model.addAttribute("store", store);
        model.addAttribute("managers", userRepository.findAll());
        model.addAttribute("isEdit", false);
        return "admin/stores/form";
    }

    /**
     * Trang chỉnh sửa thông tin chi nhánh
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        Store store = storeService.findById(id);
        if (store == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy chi nhánh với mã ID: " + id);
            return "redirect:/admin/stores";
        }
        model.addAttribute("store", store);
        model.addAttribute("managers", userRepository.findAll());
        model.addAttribute("isEdit", true);
        return "admin/stores/form";
    }

    /**
     * Xử lý lưu thông tin chi nhánh
     */
    @PostMapping("/save")
    public String saveStore(
            @ModelAttribute("store") Store store,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirect
    ) {
        try {
            if (store.getSlug() == null || store.getSlug().isBlank()) {
                store.setSlug(SlugUtil.toSlug(store.getStoreName()));
            }

            // Tải ảnh đại diện chi nhánh lên Cloudinary
            if (imageFile != null && !imageFile.isEmpty()) {
                Map<String, String> upload = cloudinaryService.uploadFile(imageFile, "old_book_store/stores");
                if (upload.containsKey("url")) {
                    store.setImage(upload.get("url"));
                }
            }

            storeService.save(store);
            redirect.addFlashAttribute("successMessage", "Lưu thông tin chi nhánh '" + store.getStoreName() + "' thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/stores";
    }

    /**
     * Đổi trạng thái hoạt động của chi nhánh
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") Long id, RedirectAttributes redirect) {
        storeService.toggleActive(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái chi nhánh thành công!");
        return "redirect:/admin/stores";
    }
}
