package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.AdminNotificationDTO;
import vn.iotstar.entity.BookConsignment;
import vn.iotstar.entity.Notification;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.NotificationRepository;
import vn.iotstar.service.INotificationService;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");
    private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,###");

    @Override
    @Transactional
    public void notifyConsignmentSubmitted(BookConsignment consignment) {
        if (consignment == null) return;

        String customerName = "Khách vãng lai";
        if (consignment.getUser() != null) {
            customerName = (consignment.getUser().getFullName() != null && !consignment.getUser().getFullName().isBlank())
                    ? consignment.getUser().getFullName()
                    : consignment.getUser().getUsername();
        }

        String storeName = (consignment.getStore() != null) ? consignment.getStore().getStoreName() : "Chuỗi Cửa Hàng";
        Long storeId = (consignment.getStore() != null) ? consignment.getStore().getId() : null;
        String formattedPrice = (consignment.getProposedPrice() != null)
                ? PRICE_FORMATTER.format(consignment.getProposedPrice()) + " đ"
                : "Thỏa thuận";

        String targetUrl = "/admin/consignments/" + consignment.getId();

        // 1. Lưu thông báo cho Quản trị viên (Admin)
        Notification adminNotif = new Notification(
                "Sách ký gửi mới cần thẩm định",
                "Khách hàng " + customerName + " vừa gửi ký gửi cuốn sách '" + consignment.getBookTitle() + 
                "' (Độ mới " + consignment.getConditionPercent() + "%) tại Chi nhánh " + storeName + 
                ". Giá đề xuất: " + formattedPrice + ".",
                Notification.NotificationType.CONSIGNMENT,
                targetUrl,
                consignment.getStore(),
                null,
                "ROLE_ADMIN"
        );
        notificationRepository.save(adminNotif);

        // 2. Lưu thông báo riêng cho Quản lý chi nhánh phụ trách (Store Manager)
        Notification managerNotif = new Notification(
                "Chi nhánh có sách ký gửi mới",
                "Khách hàng " + customerName + " vừa gửi phiếu ký gửi sách '" + consignment.getBookTitle() + 
                "' (Độ mới " + consignment.getConditionPercent() + "%) tại chi nhánh của bạn. Hãy tiến hành kiểm tra hình ảnh và định giá.",
                Notification.NotificationType.CONSIGNMENT,
                targetUrl,
                consignment.getStore(),
                (consignment.getStore() != null) ? consignment.getStore().getManager() : null,
                "ROLE_MANAGER"
        );
        notificationRepository.save(managerNotif);

        // 3. Bắn WebSocket STOMP thời gian thực tới tất cả Admin và Manager đang online
        if (messagingTemplate != null) {
            try {
                AdminNotificationDTO dto = new AdminNotificationDTO(
                        consignment.getId(),
                        "NEW_CONSIGNMENT",
                        "Yêu Cầu Ký Gửi Mới!",
                        "Khách hàng " + customerName + " vừa gửi ký gửi sách '" + consignment.getBookTitle() + "' (" + consignment.getConditionPercent() + "%)",
                        storeId,
                        storeName,
                        consignment.getBookTitle(),
                        consignment.getAuthor(),
                        customerName,
                        consignment.getProposedPrice(),
                        consignment.getConditionPercent(),
                        targetUrl,
                        LocalDateTime.now().format(TIME_FORMATTER)
                );

                // Broadcast trên các topic liên quan
                messagingTemplate.convertAndSend("/topic/admin-notifications", dto);
                messagingTemplate.convertAndSend("/topic/admin-consignments", dto);
            } catch (Exception e) {
                // Log and continue gracefully
                System.err.println("Lỗi gửi WebSocket notification: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Notification> getRecentNotifications(Long userId, String roleName, Store managedStore) {
        if (userId == null) return Collections.emptyList();
        PageRequest pageable = PageRequest.of(0, 15);

        if ("ROLE_ADMIN".equals(roleName)) {
            return notificationRepository.findForAdmin(userId, pageable);
        } else {
            Long storeId = (managedStore != null) ? managedStore.getId() : -1L;
            return notificationRepository.findForManager(userId, storeId, pageable);
        }
    }

    @Override
    public long countUnreadNotifications(Long userId, String roleName, Store managedStore) {
        if (userId == null) return 0;
        if ("ROLE_ADMIN".equals(roleName)) {
            return notificationRepository.countUnreadForAdmin(userId);
        } else {
            Long storeId = (managedStore != null) ? managedStore.getId() : -1L;
            return notificationRepository.countUnreadForManager(userId, storeId);
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        if (notificationId != null) {
            notificationRepository.findById(notificationId).ifPresent(n -> {
                n.setIsRead(true);
                notificationRepository.save(n);
            });
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId, String roleName, Store managedStore) {
        if (userId == null) return;
        if ("ROLE_ADMIN".equals(roleName)) {
            notificationRepository.markAllReadForAdmin(userId);
        } else {
            Long storeId = (managedStore != null) ? managedStore.getId() : -1L;
            notificationRepository.markAllReadForManager(userId, storeId);
        }
    }
}
