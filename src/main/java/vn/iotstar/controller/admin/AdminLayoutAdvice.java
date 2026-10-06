package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.iotstar.entity.BookConsignment;
import vn.iotstar.entity.Notification;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookConsignmentRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.INotificationService;
import vn.iotstar.service.IStoreService;

import java.util.Collections;
import java.util.List;

/**
 * Tự động cung cấp dữ liệu chuông thông báo và số lượng sách ký gửi chờ duyệt
 * cho thanh điều hướng Sidebar và Topbar trên tất cả các trang Quản trị (Admin & Manager).
 */
@ControllerAdvice(basePackages = "vn.iotstar.controller.admin")
public class AdminLayoutAdvice {

    @Autowired
    private BookConsignmentRepository consignmentRepository;

    @Autowired
    private IStoreService storeService;

    @Autowired
    private INotificationService notificationService;

    @ModelAttribute("pendingConsignmentCount")
    public long getPendingConsignmentCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return 0;
        boolean isAdmin = "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (isAdmin) {
            return consignmentRepository.countByStatus(BookConsignment.ConsignmentStatus.PENDING);
        } else {
            Store managedStore = storeService.findByManagerId(userDetails.getId());
            if (managedStore != null) {
                return consignmentRepository.countByStoreIdAndStatus(managedStore.getId(), BookConsignment.ConsignmentStatus.PENDING);
            }
            return 0;
        }
    }

    @ModelAttribute("adminNotifications")
    public List<Notification> getAdminNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return Collections.emptyList();
        Store managedStore = storeService.findByManagerId(userDetails.getId());
        return notificationService.getRecentNotifications(userDetails.getId(), userDetails.getRoleName(), managedStore);
    }

    @ModelAttribute("unreadNotifCount")
    public long getUnreadNotifCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return 0;
        Store managedStore = storeService.findByManagerId(userDetails.getId());
        return notificationService.countUnreadNotifications(userDetails.getId(), userDetails.getRoleName(), managedStore);
    }

    @ModelAttribute("currentManagedStore")
    public Store getCurrentManagedStore(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return null;
        return storeService.findByManagerId(userDetails.getId());
    }

    @ModelAttribute("isCurrentAdmin")
    public boolean isCurrentAdmin(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
    }
}
