package vn.iotstar.service;

import vn.iotstar.entity.BookConsignment;
import vn.iotstar.entity.Notification;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;

import java.util.List;

public interface INotificationService {
    /**
     * Tạo thông báo lưu cơ sở dữ liệu và bắn WebSocket STOMP thời gian thực tới Admin & Quản lý chi nhánh
     * khi có khách hàng gửi phiếu ký gửi sách mới.
     */
    void notifyConsignmentSubmitted(BookConsignment consignment);

    /**
     * Lấy danh sách thông báo gần đây của người dùng (Admin xem toàn bộ, Manager xem theo chi nhánh phụ trách).
     */
    List<Notification> getRecentNotifications(Long userId, String roleName, Store managedStore);

    /**
     * Đếm số lượng thông báo chưa đọc.
     */
    long countUnreadNotifications(Long userId, String roleName, Store managedStore);

    /**
     * Đánh dấu 1 thông báo là đã đọc.
     */
    void markAsRead(Long notificationId);

    /**
     * Đánh dấu tất cả thông báo của người dùng là đã đọc.
     */
    void markAllAsRead(Long userId, String roleName, Store managedStore);

    void notifyOrderCreated(vn.iotstar.entity.Order order);
    void notifyOrderStatusChanged(vn.iotstar.entity.Order order, vn.iotstar.entity.Order.OrderStatus oldStatus, vn.iotstar.entity.Order.OrderStatus newStatus);
    void notifySupportTicketResolved(vn.iotstar.entity.SupportTicket ticket, vn.iotstar.entity.User recipient);
    void notifyStoreRegistrationResult(vn.iotstar.entity.StoreRegistration registration);
    List<Notification> getNotificationsForUser(Long userId);
    long countUnreadForUser(Long userId);
    void markAllReadForUser(Long userId);
}
