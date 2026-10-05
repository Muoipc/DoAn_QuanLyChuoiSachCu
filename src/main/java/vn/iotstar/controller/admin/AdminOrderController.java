package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IOrderService;
import vn.iotstar.service.IStoreService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IStoreService storeService;

    /**
     * Danh sách đơn hàng toàn hệ thống hoặc theo chi nhánh:
     * - Admin: Xem và lọc theo tất cả các chi nhánh.
     * - Store Manager: Chỉ được xem các đơn hàng thuộc chi nhánh của mình.
     */
    @GetMapping({"", "/"})
    public String listOrders(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Order.OrderStatus status,
            @RequestParam(name = "storeId", required = false) Long storeId,
            @RequestParam(name = "deliveryMethod", required = false) Order.DeliveryMethod deliveryMethod,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        Long effectiveStoreId;
        List<Store> availableStores;

        if (isAdmin) {
            effectiveStoreId = storeId;
            availableStores = storeService.findActiveStores();
        } else {
            // Manager: Chỉ xem đơn hàng của chi nhánh mình
            effectiveStoreId = (managedStore != null) ? managedStore.getId() : -1L;
            availableStores = (managedStore != null) ? List.of(managedStore) : Collections.emptyList();
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderService.searchAdminOrders(keyword, status, effectiveStoreId, deliveryMethod, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("stores", availableStores);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStoreId", effectiveStoreId);
        model.addAttribute("selectedDeliveryMethod", deliveryMethod);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);

        // Đếm theo từng trạng thái đơn hàng phục vụ bộ đếm Badge
        model.addAttribute("countNew", orderService.countByStatus(Order.OrderStatus.NEW));
        model.addAttribute("countConfirmed", orderService.countByStatus(Order.OrderStatus.CONFIRMED));
        model.addAttribute("countShipping", orderService.countByStatus(Order.OrderStatus.SHIPPING));
        model.addAttribute("countDelivered", orderService.countByStatus(Order.OrderStatus.DELIVERED));
        model.addAttribute("countCancelled", orderService.countByStatus(Order.OrderStatus.CANCELLED));
        model.addAttribute("countReturned", orderService.countByStatus(Order.OrderStatus.RETURNED));

        return "admin/orders/list";
    }

    /**
     * Xem chi tiết đơn hàng, dòng sản phẩm, lịch trình và phân công Shipper:
     * - Manager chỉ được xem đơn hàng thuộc chi nhánh của mình.
     */
    @GetMapping("/{id}")
    public String orderDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        Order order = orderService.findById(id);
        if (order == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng mã #" + id);
            return "redirect:/admin/orders";
        }

        if (!isAdmin) {
            if (managedStore == null || order.getStore() == null || !managedStore.getId().equals(order.getStore().getId())) {
                redirect.addFlashAttribute("errorMessage", "Bạn không có quyền xem chi tiết đơn hàng của chi nhánh khác!");
                return "redirect:/admin/orders";
            }
        }

        List<User> shippers = orderService.findAllShippers();

        model.addAttribute("order", order);
        model.addAttribute("shippers", shippers);
        model.addAttribute("allStatuses", Order.OrderStatus.values());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);

        return "admin/orders/detail";
    }

    /**
     * Cập nhật trạng thái đơn hàng (NEW -> CONFIRMED -> SHIPPING -> DELIVERED / CANCELLED / RETURNED)
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Order.OrderStatus status,
            @RequestParam(name = "note", required = false) String note,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        Order order = orderService.findById(id);
        if (order == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/admin/orders";
        }

        if (!isAdmin) {
            if (managedStore == null || order.getStore() == null || !managedStore.getId().equals(order.getStore().getId())) {
                redirect.addFlashAttribute("errorMessage", "Bạn không có quyền cập nhật đơn hàng của chi nhánh khác!");
                return "redirect:/admin/orders";
            }
        }

        try {
            orderService.updateOrderStatus(id, status, note);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng thành: " + status.name());
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Phân công nhân viên giao hàng (Shipper) cho đơn hàng
     */
    @PostMapping("/{id}/assign-shipper")
    public String assignShipper(
            @PathVariable("id") Long id,
            @RequestParam("shipperId") Long shipperId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        Order order = orderService.findById(id);
        if (order == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/admin/orders";
        }

        if (!isAdmin) {
            if (managedStore == null || order.getStore() == null || !managedStore.getId().equals(order.getStore().getId())) {
                redirect.addFlashAttribute("errorMessage", "Bạn không có quyền phân công shipper cho chi nhánh khác!");
                return "redirect:/admin/orders";
            }
        }

        try {
            orderService.assignShipper(id, shipperId);
            redirect.addFlashAttribute("successMessage", "Phân công Shipper thành công! Đơn hàng đã chuyển sang trạng thái ĐANG GIAO (SHIPPING).");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
