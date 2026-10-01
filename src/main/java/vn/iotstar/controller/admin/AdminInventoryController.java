package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.service.IInventoryService;
import vn.iotstar.service.IStoreService;

import java.util.*;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private IStoreService storeService;

    @Autowired
    private BookRepository bookRepository;

    /**
     * Quản lý tồn kho theo từng chi nhánh trong chuỗi
     */
    @GetMapping({"", "/"})
    public String viewInventory(
            @RequestParam(name = "storeId", required = false) Long storeId,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model
    ) {
        List<Store> stores = storeService.findActiveStores();
        if (stores.isEmpty()) {
            stores = storeService.findAll();
        }

        Long currentStoreId = storeId;
        if (currentStoreId == null && !stores.isEmpty()) {
            currentStoreId = stores.get(0).getId();
        }

        List<Inventory> inventoryList = new ArrayList<>();
        if (currentStoreId != null) {
            inventoryList = inventoryService.findByStoreId(currentStoreId);
        }

        // Lọc theo từ khóa tìm kiếm
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            inventoryList = inventoryList.stream()
                    .filter(inv -> (inv.getBook() != null && (
                            inv.getBook().getTitle().toLowerCase().contains(kw) ||
                            inv.getBook().getAuthor().toLowerCase().contains(kw) ||
                            (inv.getBook().getIsbn() != null && inv.getBook().getIsbn().contains(kw))
                    )))
                    .toList();
        }

        model.addAttribute("stores", stores);
        model.addAttribute("currentStoreId", currentStoreId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("inventoryList", inventoryList);

        // Đếm số lượng cảnh báo hết hàng / sắp hết
        long outOfStock = inventoryList.stream().filter(i -> i.getQuantity() <= 0).count();
        long lowStock = inventoryList.stream().filter(i -> i.getQuantity() > 0 && i.getQuantity() <= 3).count();
        long normalStock = inventoryList.stream().filter(i -> i.getQuantity() > 3).count();

        model.addAttribute("outOfStockCount", outOfStock);
        model.addAttribute("lowStockCount", lowStock);
        model.addAttribute("normalStockCount", normalStock);

        return "admin/inventory/list";
    }

    /**
     * Cập nhật nhanh số lượng tồn kho của một cuốn sách tại chi nhánh
     */
    @PostMapping("/update-quantity")
    public String updateQuantity(
            @RequestParam("storeId") Long storeId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirect
    ) {
        try {
            inventoryService.updateQuantity(storeId, bookId, quantity);
            redirect.addFlashAttribute("successMessage", "Cập nhật số lượng tồn kho thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/inventory?storeId=" + storeId;
    }

    /**
     * Trang điều chuyển sách giữa các kho chi nhánh
     */
    @GetMapping("/transfer")
    public String transferPage(
            @RequestParam(name = "fromStoreId", required = false) Long fromStoreId,
            Model model
    ) {
        List<Store> stores = storeService.findActiveStores();
        if (fromStoreId == null && !stores.isEmpty()) {
            fromStoreId = stores.get(0).getId();
        }

        List<Inventory> availableBooks = new ArrayList<>();
        if (fromStoreId != null) {
            availableBooks = inventoryService.findByStoreId(fromStoreId);
        }

        model.addAttribute("stores", stores);
        model.addAttribute("fromStoreId", fromStoreId);
        model.addAttribute("availableBooks", availableBooks);

        return "admin/inventory/transfer";
    }

    /**
     * Xử lý thực hiện điều chuyển sách giữa các chi nhánh
     */
    @PostMapping("/transfer")
    public String doTransfer(
            @RequestParam("fromStoreId") Long fromStoreId,
            @RequestParam("toStoreId") Long toStoreId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            @RequestParam(name = "notes", required = false) String notes,
            RedirectAttributes redirect
    ) {
        try {
            inventoryService.transferStock(fromStoreId, toStoreId, bookId, quantity, notes);
            redirect.addFlashAttribute("successMessage", "Điều chuyển thành công " + quantity + " cuốn sách sang chi nhánh nhận!");
            return "redirect:/admin/inventory?storeId=" + toStoreId;
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Không thể điều chuyển: " + e.getMessage());
            return "redirect:/admin/inventory/transfer?fromStoreId=" + fromStoreId;
        }
    }

    /**
     * API lấy danh sách sách có trong kho để nạp động AJAX vào dropdown điều chuyển
     */
    @GetMapping("/api/books-by-store")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getBooksByStore(@RequestParam("storeId") Long storeId) {
        List<Inventory> inventories = inventoryService.findByStoreId(storeId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Inventory inv : inventories) {
            if (inv.getBook() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("bookId", inv.getBook().getId());
                map.put("title", inv.getBook().getTitle());
                map.put("quantity", inv.getQuantity());
                map.put("condition", inv.getBook().getConditionPercent());
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }
}
