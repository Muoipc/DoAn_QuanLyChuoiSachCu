package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Notification;
import vn.iotstar.entity.Store;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.INotificationService;
import vn.iotstar.service.IStoreService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationController {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private IStoreService storeService;

    /**
     * API đánh dấu 1 thông báo là đã đọc
     */
    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        if (userDetails == null) {
            res.put("success", false);
            res.put("message", "Chưa xác thực");
            return ResponseEntity.status(401).body(res);
        }
        notificationService.markAsRead(id);
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    /**
     * API đánh dấu toàn bộ thông báo là đã đọc
     */
    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        if (userDetails == null) {
            res.put("success", false);
            return ResponseEntity.status(401).body(res);
        }
        Store managedStore = storeService.findByManagerId(userDetails.getId());
        notificationService.markAllAsRead(userDetails.getId(), userDetails.getRoleName(), managedStore);
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    /**
     * API lấy danh sách thông báo và số lượng chưa đọc mới nhất
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotificationsApi(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        if (userDetails == null) {
            res.put("unreadCount", 0);
            res.put("items", List.of());
            return ResponseEntity.ok(res);
        }
        Store managedStore = storeService.findByManagerId(userDetails.getId());
        List<Notification> list = notificationService.getRecentNotifications(userDetails.getId(), userDetails.getRoleName(), managedStore);
        long unread = notificationService.countUnreadNotifications(userDetails.getId(), userDetails.getRoleName(), managedStore);

        res.put("unreadCount", unread);
        res.put("items", list);
        return ResponseEntity.ok(res);
    }
}
