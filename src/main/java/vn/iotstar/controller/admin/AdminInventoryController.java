package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.*;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IInventoryService;
import vn.iotstar.service.IStockTransferService;
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

    @Autowired
    private IStockTransferService stockTransferService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getId() == null) return null;
        return userRepository.findById(userDetails.getId()).orElse(null);
    }

    /**
     * Quản lý tồn kho theo từng chi nhánh trong chuỗi.
     * - Admin: Toàn quyền xem và chuyển đổi giữa tất cả chi nhánh.
     * - Store Manager: Chỉ được xem tồn kho tại chi nhánh của chính mình.
     */
    @GetMapping({"", "/"})
    public String viewInventory(
            @RequestParam(name = "storeId", required = false) Long storeId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        List<Store> stores;
        Long currentStoreId;

        if (isAdmin) {
            stores = storeService.findActiveStores();
            if (stores.isEmpty()) {
                stores = storeService.findAll();
            }
            currentStoreId = storeId;
            if (currentStoreId == null && !stores.isEmpty()) {
                currentStoreId = stores.get(0).getId();
            }
        } else {
            // Quản lý chi nhánh: Bắt buộc chỉ được xem cửa hàng của mình
            if (managedStore != null) {
                stores = List.of(managedStore);
                currentStoreId = managedStore.getId();
            } else {
                stores = Collections.emptyList();
                currentStoreId = null;
            }
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
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);

        // Đếm số lượng cảnh báo hết hàng / sắp hết
        long outOfStock = inventoryList.stream().filter(i -> i.getQuantity() <= 0).count();
        long lowStock = inventoryList.stream().filter(i -> i.getQuantity() > 0 && i.getQuantity() <= 3).count();
        long normalStock = inventoryList.stream().filter(i -> i.getQuantity() > 3).count();

        model.addAttribute("outOfStockCount", outOfStock);
        model.addAttribute("lowStockCount", lowStock);
        model.addAttribute("normalStockCount", normalStock);
        model.addAttribute("pendingTransferCount", stockTransferService.countPendingTransfers());

        return "admin/inventory/list";
    }

    /**
     * Cập nhật nhanh số lượng tồn kho của một cuốn sách tại chi nhánh.
     * Quản lý chỉ được cập nhật tại chi nhánh mình phụ trách.
     */
    @PostMapping("/update-quantity")
    public String updateQuantity(
            @RequestParam("storeId") Long storeId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (!isAdmin) {
            if (managedStore == null || !managedStore.getId().equals(storeId)) {
                redirect.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa tồn kho của chi nhánh khác!");
                return "redirect:/admin/inventory";
            }
        }

        try {
            inventoryService.updateQuantity(storeId, bookId, quantity);
            redirect.addFlashAttribute("successMessage", "Cập nhật số lượng tồn kho thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/inventory?storeId=" + storeId;
    }

    /**
     * Trang tạo yêu cầu điều chuyển sách giữa các kho chi nhánh.
     * - Manager: Tự động khóa kho xuất là chi nhánh của mình.
     * - Admin: Tự do chọn bất kỳ kho xuất và kho nhận.
     */
    @GetMapping("/transfer")
    public String transferPage(
            @RequestParam(name = "fromStoreId", required = false) Long fromStoreId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        List<Store> allStores = storeService.findActiveStores();
        Long effectiveFromStoreId;

        if (isAdmin) {
            effectiveFromStoreId = (fromStoreId != null) ? fromStoreId : (!allStores.isEmpty() ? allStores.get(0).getId() : null);
        } else {
            if (managedStore != null) {
                effectiveFromStoreId = managedStore.getId();
            } else {
                effectiveFromStoreId = null;
            }
        }

        List<Inventory> availableBooks = new ArrayList<>();
        if (effectiveFromStoreId != null) {
            availableBooks = inventoryService.findByStoreId(effectiveFromStoreId);
        }

        model.addAttribute("stores", allStores);
        model.addAttribute("fromStoreId", effectiveFromStoreId);
        model.addAttribute("availableBooks", availableBooks);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);

        return "admin/inventory/transfer";
    }

    /**
     * Xử lý gửi yêu cầu điều chuyển sách giữa các chi nhánh.
     * Quy trình mới: Tạo phiếu điều chuyển ở trạng thái PENDING để Quản trị viên (Admin) phê duyệt.
     */
    @PostMapping("/transfer")
    public String doTransfer(
            @RequestParam("fromStoreId") Long fromStoreId,
            @RequestParam("toStoreId") Long toStoreId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            @RequestParam(name = "notes", required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (!isAdmin) {
            if (managedStore == null || !managedStore.getId().equals(fromStoreId)) {
                redirect.addFlashAttribute("errorMessage", "Bạn chỉ có thể tạo yêu cầu điều chuyển xuất hàng từ chi nhánh bạn quản lý!");
                return "redirect:/admin/inventory/transfer";
            }
        }

        try {
            User currentUser = getCurrentUser(userDetails);
            StockTransfer transfer = stockTransferService.createTransferRequest(
                    fromStoreId, toStoreId, bookId, quantity, notes, currentUser
            );

            redirect.addFlashAttribute("successMessage", 
                    "Đã tạo yêu cầu điều chuyển kho thành công (Mã: #" + transfer.getTransferCode() + ")! " +
                    "Yêu cầu đang chờ Quản trị viên (Admin) phê duyệt trước khi xuất hàng.");
            return "redirect:/admin/inventory/transfers";
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Không thể gửi yêu cầu điều chuyển: " + e.getMessage());
            return "redirect:/admin/inventory/transfer?fromStoreId=" + fromStoreId;
        }
    }

    /**
     * TRANG MỚI: Danh sách và Phê duyệt các yêu cầu điều chuyển kho.
     * - Admin: Xem toàn bộ các yêu cầu trong hệ thống, thực hiện DUYỆT hoặc TỪ CHỐI phiếu.
     * - Manager: Chỉ xem các yêu cầu liên quan đến chi nhánh của mình (kho xuất hoặc kho nhận).
     */
    @GetMapping("/transfers")
    public String listTransfers(
            @RequestParam(name = "status", required = false) StockTransfer.TransferStatus status,
            @RequestParam(name = "storeId", required = false) Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        Long filterStoreId;
        List<Store> availableStores;

        if (isAdmin) {
            filterStoreId = storeId;
            availableStores = storeService.findActiveStores();
        } else {
            // Manager chỉ xem yêu cầu của chi nhánh mình
            filterStoreId = (managedStore != null) ? managedStore.getId() : -1L;
            availableStores = (managedStore != null) ? List.of(managedStore) : Collections.emptyList();
        }

        List<StockTransfer> transfers = stockTransferService.searchTransfers(status, filterStoreId);

        model.addAttribute("transfers", transfers);
        model.addAttribute("stores", availableStores);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStoreId", filterStoreId);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);
        model.addAttribute("pendingCount", stockTransferService.countPendingTransfers());

        return "admin/inventory/transfers";
    }

    /**
     * Action phê duyệt phiếu điều chuyển kho (CHỈ DÀNH CHO ADMIN)
     */
    @PostMapping("/transfers/{id}/approve")
    public String approveTransfer(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có thẩm quyền phê duyệt phiếu điều chuyển kho!");
            return "redirect:/admin/inventory/transfers";
        }

        try {
            User adminUser = getCurrentUser(userDetails);
            StockTransfer approved = stockTransferService.approveTransfer(id, adminUser);
            redirect.addFlashAttribute("successMessage", 
                    "Đã phê duyệt thành công phiếu điều chuyển #" + approved.getTransferCode() + "! " +
                    "Số lượng sách (" + approved.getQuantity() + " cuốn) đã được trừ khỏi kho '" + approved.getFromStore().getStoreName() + 
                    "' và nhập vào kho '" + approved.getToStore().getStoreName() + "'.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Không thể phê duyệt: " + e.getMessage());
        }
        return "redirect:/admin/inventory/transfers";
    }

    /**
     * Action từ chối phiếu điều chuyển kho (CHỈ DÀNH CHO ADMIN)
     */
    @PostMapping("/transfers/{id}/reject")
    public String rejectTransfer(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", defaultValue = "Không chấp thuận điều chuyển") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Chỉ Quản trị viên (Admin) mới có quyền từ chối phiếu điều chuyển!");
            return "redirect:/admin/inventory/transfers";
        }

        try {
            User adminUser = getCurrentUser(userDetails);
            StockTransfer rejected = stockTransferService.rejectTransfer(id, reason, adminUser);
            redirect.addFlashAttribute("successMessage", "Đã từ chối phiếu điều chuyển #" + rejected.getTransferCode() + ".");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/inventory/transfers";
    }

    /**
     * Action hủy phiếu điều chuyển khi còn ở trạng thái PENDING
     */
    @PostMapping("/transfers/{id}/cancel")
    public String cancelTransfer(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        try {
            User currentUser = getCurrentUser(userDetails);
            StockTransfer cancelled = stockTransferService.cancelTransfer(id, currentUser);
            redirect.addFlashAttribute("successMessage", "Đã hủy phiếu yêu cầu điều chuyển #" + cancelled.getTransferCode() + ".");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Không thể hủy: " + e.getMessage());
        }
        return "redirect:/admin/inventory/transfers";
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
