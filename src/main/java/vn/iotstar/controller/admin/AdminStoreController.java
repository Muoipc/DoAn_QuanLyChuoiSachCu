package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ICloudinaryService;
import vn.iotstar.service.IStoreService;
import vn.iotstar.util.SlugUtil;

import java.util.Collections;
import java.util.List;
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
     * Danh sách chi nhánh:
     * - Admin: Xem toàn bộ các chi nhánh trong chuỗi.
     * - Store Manager: Chỉ xem chi nhánh do mình trực tiếp quản lý.
     */
    @GetMapping({"", "/"})
    public String listStores(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        List<Store> stores;
        if (isAdmin) {
            stores = storeService.findAll();
        } else {
            stores = (managedStore != null) ? List.of(managedStore) : Collections.emptyList();
        }

        model.addAttribute("stores", stores);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);
        return "admin/stores/list";
    }

    /**
     * Trang thêm mới chi nhánh (CHỈ DÀNH CHO ADMIN)
     */
    @GetMapping("/create")
    public String createForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect,
            Model model
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có quyền tạo mới chi nhánh!");
            return "redirect:/admin/stores";
        }

        Store store = new Store();
        store.setOpenTime("08:00");
        store.setCloseTime("21:30");
        store.setIsActive(true);

        model.addAttribute("store", store);
        model.addAttribute("managers", userRepository.findAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("isAdmin", true);
        return "admin/stores/form";
    }

    /**
     * Trang chỉnh sửa thông tin chi nhánh:
     * - Admin: Được sửa bất kỳ chi nhánh nào.
     * - Manager: Chỉ được sửa chi nhánh của chính mình.
     */
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (!isAdmin) {
            if (managedStore == null || !managedStore.getId().equals(id)) {
                redirect.addFlashAttribute("errorMessage", "Bạn chỉ có quyền xem và quản lý thông tin chi nhánh của mình!");
                return "redirect:/admin/stores";
            }
        }

        Store store = storeService.findById(id);
        if (store == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy chi nhánh với mã ID: " + id);
            return "redirect:/admin/stores";
        }

        model.addAttribute("store", store);
        model.addAttribute("managers", userRepository.findAll());
        model.addAttribute("isEdit", true);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);
        return "admin/stores/form";
    }

    /**
     * Xử lý lưu thông tin chi nhánh
     */
    @PostMapping("/save")
    public String saveStore(
            @ModelAttribute("store") Store store,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (!isAdmin) {
            if (managedStore == null || store.getId() == null || !managedStore.getId().equals(store.getId())) {
                redirect.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa chi nhánh này!");
                return "redirect:/admin/stores";
            }

            // Bảo toàn thông tin quản lý và chiết khấu do Manager không được tự ý đổi
            Store existing = storeService.findById(store.getId());
            if (existing != null) {
                store.setManager(existing.getManager());
                store.setCommissionRate(existing.getCommissionRate());
                store.setIsActive(existing.getIsActive());
            }
        }

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
     * Đổi trạng thái hoạt động của chi nhánh (CHỈ DÀNH CHO ADMIN)
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có quyền khóa hoặc mở chi nhánh!");
            return "redirect:/admin/stores";
        }

        storeService.toggleActive(id);
        redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái chi nhánh thành công!");
        return "redirect:/admin/stores";
    }
}
