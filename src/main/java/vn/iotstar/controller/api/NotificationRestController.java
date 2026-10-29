package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Notification;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.INotificationService;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    @Autowired
    private INotificationService notificationService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM");

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> resp = new HashMap<>();
        if (userDetails == null || userDetails.getId() == null) {
            resp.put("authenticated", false);
            resp.put("unreadCount", 0);
            resp.put("notifications", Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        Long userId = userDetails.getId();
        List<Notification> list = notificationService.getNotificationsForUser(userId);
        long unread = notificationService.countUnreadForUser(userId);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Notification n : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", n.getId());
            item.put("title", n.getTitle());
            item.put("message", n.getMessage());
            item.put("type", n.getType() != null ? n.getType().name() : "SYSTEM");
            item.put("targetUrl", n.getTargetUrl() != null ? n.getTargetUrl() : "/orders");
            item.put("isRead", Boolean.TRUE.equals(n.getIsRead()));
            item.put("time", n.getCreatedAt() != null ? n.getCreatedAt().format(TIME_FMT) : "");
            items.add(item);
        }

        resp.put("authenticated", true);
        resp.put("unreadCount", unread);
        resp.put("notifications", items);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Yêu cầu đăng nhập!"));
        }
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            notificationService.markAllReadForUser(userDetails.getId());
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
