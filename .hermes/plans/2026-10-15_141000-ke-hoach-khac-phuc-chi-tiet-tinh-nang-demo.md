# KẾ HOẠCH CHI TIẾT KHẮC PHỤC 100% CÁC TÍNH NĂNG DEMO & HOÀN THIỆN ĐỒ ÁN REBOOK

## 1. Goal (Mục Tiêu)
Hoàn thiện toàn bộ các tính năng còn mang tính chất demo, gán dữ liệu tĩnh và thiếu liên kết nghiệp vụ trên hệ thống ReBook (chuẩn hóa dữ liệu người nhận Checkout, động hóa trung tâm thông báo thời gian thực phía khách hàng, kích hoạt chu trình bắn thông báo tự động theo sự kiện, bổ sung trang theo dõi phản hồi khiếu nại của bạn đọc, và đồng bộ nhận diện thương hiệu).

---

## 2. Current Context / Assumptions
- **Dự án:** `DoAn_QuanLyChuoiSachCu` (Spring Boot 3.4.3, Spring Security 6, Thymeleaf, MySQL `old_book_store_db`).
- **Hiện trạng:**
  - `CheckoutController.java` (dòng 198-202): Fallback thông tin người nhận khi chưa có địa chỉ mặc định đang gán cứng tên "Nguyễn Song Hoàng Phúc" và SĐT "0912345678".
  - `header.html` (dòng 1685-1708): Popover chuông thông báo đang render 3 thông báo HTML viết cứng (mock).
  - `INotificationService`: Chỉ mới kích hoạt cho sự kiện gửi ký gửi (`notifyConsignmentSubmitted`), chưa tự động phát sinh thông báo khi khách đặt đơn, đổi trạng thái đơn, duyệt mở shop hoặc giải quyết khiếu nại.
  - `user/profile.html`: Chưa có tab xem lại các phiếu hỗ trợ/khiếu nại đã gửi và phản hồi từ Admin; avatar mẫu đang trỏ link ngoài Cloudinary demo.
  - `store-detail.html`: Nút "Theo Dõi" chưa cập nhật trực quan số lượng follower ngay tại client.
  - `footer.html`, `home.html`, `search.html`: Còn sót tên thương hiệu cũ và định dạng tiền tệ chưa đồng bộ đuôi `... đ`.

---

## 3. Architecture / Proposed Approach
- **Trung tâm thông báo khách hàng (Client Notification Engine):** Mở rộng `NotificationRepository` để truy vấn thông báo theo `recipient.id`; bổ sung `NotificationRestController` (`/api/notifications`) trả về danh sách thông báo và số lượng chưa đọc; cập nhật JavaScript trong `header.html` để tải và render thông báo thật theo thời gian thực.
- **Tự động hóa thông báo theo sự kiện (Event-driven Notifications):** Tích hợp gọi `INotificationService` tại các điểm chuyển trạng thái nghiệp vụ trong `OrderServiceImpl` (tạo đơn, cập nhật thanh toán), `AdminOrderController` (chuyển trạng thái giao hàng), `AdminStoreRegistrationController` (duyệt/từ chối gian hàng) và `AdminSupportTicketController` (phản hồi khiếu nại).
- **Đồng bộ hóa dữ liệu & trải nghiệm khách hàng:** Khắc phục fallback Checkout lấy đúng tên/SĐT của tài khoản đang đăng nhập; bổ sung tab `Phiếu Hỗ Trợ` trong Profile cá nhân; đồng bộ tiền tệ `... đ` và thương hiệu ReBook toàn diện.

---

## 4. Step-by-Step Tasks (Chi Tiết Từng Bước Triển Khai)

### Task 1: Sửa Fallback Dữ Liệu Người Nhận Trên Checkout
- **Mục tiêu:** Xóa bỏ tên cứng "Nguyễn Song Hoàng Phúc" và SĐT "0912345678", lấy thông tin thật từ `User` đang đăng nhập.
- **File:** `src/main/java/vn/iotstar/controller/CheckoutController.java`
- **Thay đổi:**
```java
// Dòng 198-203 hiện tại:
// String formattedAddress = defaultAddress != null ? ... : "Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh";
// String receiverName = defaultAddress != null ? defaultAddress.getReceiverName() : "Nguyễn Song Hoàng Phúc";
// String receiverPhone = defaultAddress != null ? defaultAddress.getPhone() : "0912345678";

// Thay bằng:
User currentUser = userRepository.findById(userId).orElse(null);
String receiverName = defaultAddress != null ? defaultAddress.getReceiverName() 
        : (currentUser != null && currentUser.getFullName() != null && !currentUser.getFullName().isBlank() 
            ? currentUser.getFullName() : (currentUser != null ? currentUser.getUsername() : ""));
String receiverPhone = defaultAddress != null ? defaultAddress.getPhone() 
        : (currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "");
String formattedAddress = defaultAddress != null 
        ? (defaultAddress.getStreetAddress() + ", " + defaultAddress.getWard() + ", " + defaultAddress.getDistrict() + ", " + defaultAddress.getProvince())
        : "";
```
- **Xác minh:**
  - Chạy `curl` đăng nhập tài khoản `cuong_customer` (chưa có địa chỉ).
  - Truy cập `/checkout`, kiểm tra trường `receiverName` hiển thị `Thái Duy Cường`, không còn là `Nguyễn Song Hoàng Phúc`.

---

### Task 2: Mở Rộng `NotificationRepository` Truy Vấn Thông Báo Khách Hàng
- **Mục tiêu:** Cho phép truy vấn danh sách thông báo và đếm số lượng chưa đọc của từng khách hàng cụ thể (`recipient.id`).
- **File:** `src/main/java/vn/iotstar/repository/NotificationRepository.java`
- **Mã nguồn bổ sung:**
```java
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    long countUnreadForUser(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    void markAllReadForUser(@Param("userId") Long userId);
```
- **Xác minh:** Chạy `mvn test-compile` đảm bảo biên dịch thành công.

---

### Task 3: Bổ Sung Các Phương Thức Thông Báo Sự Kiện Vào `INotificationService` & `NotificationServiceImpl`
- **Mục tiêu:** Hỗ trợ tạo thông báo cho khách hàng khi đặt đơn, đổi trạng thái đơn, duyệt mở gian hàng và phản hồi khiếu nại.
- **File 1:** `src/main/java/vn/iotstar/service/INotificationService.java`
  - Thêm chữ ký hàm:
```java
    void notifyOrderCreated(vn.iotstar.entity.Order order);
    void notifyOrderStatusChanged(vn.iotstar.entity.Order order, vn.iotstar.entity.Order.OrderStatus oldStatus, vn.iotstar.entity.Order.OrderStatus newStatus);
    void notifySupportTicketResolved(vn.iotstar.entity.SupportTicket ticket, vn.iotstar.entity.User recipient);
    void notifyStoreRegistrationResult(vn.iotstar.entity.StoreRegistration registration);
    List<Notification> getNotificationsForUser(Long userId);
    long countUnreadForUser(Long userId);
    void markAllReadForUser(Long userId);
```
- **File 2:** `src/main/java/vn/iotstar/service/impl/NotificationServiceImpl.java`
  - Triển khai cụ thể:
```java
    @Override
    @Transactional
    public void notifyOrderCreated(vn.iotstar.entity.Order order) {
        if (order == null) return;
        // 1. Thông báo cho khách hàng
        if (order.getUser() != null) {
            Notification userNotif = new Notification(
                "Đặt hàng thành công",
                "Đơn hàng #" + order.getOrderCode() + " đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.",
                Notification.NotificationType.ORDER,
                "/orders/" + order.getOrderCode(),
                order.getStore(),
                order.getUser(),
                "ROLE_USER"
            );
            notificationRepository.save(userNotif);
        }
        // 2. Thông báo cho Quản lý chi nhánh
        if (order.getStore() != null) {
            Notification managerNotif = new Notification(
                "Đơn hàng mới tại chi nhánh",
                "Có đơn hàng mới #" + order.getOrderCode() + " (" + order.getItems().size() + " cuốn sách). Vui lòng xác nhận và đóng gói.",
                Notification.NotificationType.ORDER,
                "/admin/orders/" + order.getId(),
                order.getStore(),
                order.getStore().getManager(),
                "ROLE_MANAGER"
            );
            notificationRepository.save(managerNotif);
        }
    }

    @Override
    @Transactional
    public void notifyOrderStatusChanged(vn.iotstar.entity.Order order, vn.iotstar.entity.Order.OrderStatus oldStatus, vn.iotstar.entity.Order.OrderStatus newStatus) {
        if (order == null || order.getUser() == null) return;
        String statusDesc = switch (newStatus) {
            case CONFIRMED -> "đã được chi nhánh xác nhận và đang đóng gói.";
            case SHIPPING -> "đang trên đường giao tới bạn.";
            case DELIVERED -> "đã được giao thành công. Chúc bạn có những phút giây đọc sách thú vị!";
            case CANCELLED -> "đã được hủy.";
            case RETURNED -> "đã được hoàn trả về kho chi nhánh.";
            default -> "vừa được cập nhật trạng thái.";
        };
        Notification notif = new Notification(
            "Cập nhật đơn hàng #" + order.getOrderCode(),
            "Đơn hàng #" + order.getOrderCode() + " " + statusDesc,
            Notification.NotificationType.ORDER,
            "/orders/" + order.getOrderCode(),
            order.getStore(),
            order.getUser(),
            "ROLE_USER"
        );
        notificationRepository.save(notif);
    }

    @Override
    @Transactional
    public void notifySupportTicketResolved(vn.iotstar.entity.SupportTicket ticket, vn.iotstar.entity.User recipient) {
        if (ticket == null || recipient == null) return;
        Notification notif = new Notification(
            "Phản hồi yêu cầu hỗ trợ #" + ticket.getId(),
            "Ban quản trị ReBook đã phản hồi yêu cầu về [" + ticket.getCategory() + "]: " + 
                (ticket.getAdminReply() != null ? ticket.getAdminReply() : "Đã xử lý hoàn tất."),
            Notification.NotificationType.SUPPORT_TICKET,
            "/user/account/profile?tab=support",
            null,
            recipient,
            "ROLE_USER"
        );
        notificationRepository.save(notif);
    }

    @Override
    @Transactional
    public void notifyStoreRegistrationResult(vn.iotstar.entity.StoreRegistration registration) {
        if (registration == null || registration.getUser() == null) return;
        boolean approved = registration.getStatus() == vn.iotstar.entity.StoreRegistration.Status.APPROVED;
        String title = approved ? "Chúc mừng! Gian hàng đã được duyệt" : "Thông báo kết quả mở gian hàng";
        String msg = approved 
            ? "Hồ sơ mở gian hàng '" + registration.getStoreName() + "' đã được phê duyệt. Bạn đã là Quản lý chi nhánh!"
            : "Hồ sơ mở gian hàng '" + registration.getStoreName() + "' chưa đạt: " + registration.getRejectReason();
        String url = approved ? "/admin/inventory" : "/seller/register";

        Notification notif = new Notification(
            title,
            msg,
            Notification.NotificationType.STORE_REGISTRATION,
            url,
            null,
            registration.getUser(),
            "ROLE_USER"
        );
        notificationRepository.save(notif);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        if (userId == null) return Collections.emptyList();
        return notificationRepository.findForUser(userId, PageRequest.of(0, 10));
    }

    @Override
    public long countUnreadForUser(Long userId) {
        if (userId == null) return 0;
        return notificationRepository.countUnreadForUser(userId);
    }

    @Override
    @Transactional
    public void markAllReadForUser(Long userId) {
        if (userId != null) {
            notificationRepository.markAllReadForUser(userId);
        }
    }
```
- **Xác minh:** Viết Unit Test `NotificationServiceTest.java` kiểm thử các hàm trên chạy pass 100%.

---

### Task 4: Xây Dựng REST API Thông Báo Khách Hàng (`NotificationRestController.java`)
- **Mục tiêu:** Cung cấp API cho thanh Header client lấy thông báo thực và đánh dấu đã đọc.
- **File:** `src/main/java/vn/iotstar/controller/api/NotificationRestController.java`
- **Mã nguồn:**
```java
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
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") Long id) {
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
```
- **Xác minh:** Kiểm thử gọi `curl http://localhost:8080/api/notifications` trả về JSON cấu trúc chuẩn.

---

### Task 5: Động Hóa Popover Thông Báo Trên Header (`fragments/header.html`)
- **Mục tiêu:** Xóa sạch các thẻ HTML thông báo giả lập; nạp thông báo thật từ `/api/notifications`.
- **File:** `src/main/resources/templates/fragments/header.html`
- **Thay đổi:**
  1. Trong `.noti-popover-list`, thay khối HTML tĩnh bằng container động:
     `<div class="noti-popover-list" id="headerNotiList"></div>`
  2. Bổ sung script tự động nạp khi rê chuột hoặc click:
```javascript
function loadHeaderUserNotifications() {
    fetch('/api/notifications')
        .then(r => r.json())
        .then(data => {
            const listEl = document.getElementById('headerNotiList');
            const badgeEl = document.getElementById('headerNotiBadge');
            if (!listEl) return;
            
            if (badgeEl) {
                if (data.unreadCount > 0) {
                    badgeEl.style.display = 'block';
                    badgeEl.innerText = data.unreadCount > 9 ? '9+' : data.unreadCount;
                } else {
                    badgeEl.style.display = 'none';
                }
            }

            if (!data.authenticated) {
                listEl.innerHTML = '<div style="padding:24px; text-align:center; color:#78716C; font-size:12.5px;">' +
                    '<i class="fa-solid fa-lock fa-2x mb-2 d-block opacity-50"></i>' +
                    'Đăng nhập để xem cập nhật đơn hàng &amp; ưu đãi.' +
                    '<div class="mt-2"><a href="/login" class="btn-chotot-login" style="padding:4px 14px; font-size:12px;">Đăng Nhập</a></div></div>';
                return;
            }

            if (!data.notifications || data.notifications.length === 0) {
                listEl.innerHTML = '<div style="padding:28px 16px; text-align:center; color:#78716C; font-size:12.5px;">' +
                    '<i class="fa-regular fa-bell-slash fa-2x mb-2 d-block opacity-50"></i>' +
                    'Chưa có thông báo mới nào. Cập nhật đơn hàng sẽ xuất hiện tại đây!</div>';
                return;
            }

            let html = '';
            data.notifications.forEach(n => {
                const unreadClass = !n.isRead ? 'unread' : '';
                html += '<a href="' + n.targetUrl + '" class="noti-item ' + unreadClass + '" onclick="markNotiRead(' + n.id + ')">' +
                    '<div class="noti-icon-box"><i class="fa-solid fa-box text-danger"></i></div>' +
                    '<div class="noti-body">' +
                        '<h4 class="noti-title">' + n.title + '</h4>' +
                        '<p class="noti-desc">' + n.message + '</p>' +
                        '<span class="noti-time" style="font-size:10.5px; color:#A8A29E;">' + n.time + '</span>' +
                    '</div>' +
                '</a>';
            });
            listEl.innerHTML = html;
        });
}
```
- **Xác minh:** Mở giao diện trên trình duyệt, rê chuột vào chuông thông báo, xác nhận hiển thị thông báo thật từ API.

---

### Task 6: Kích Hoạt Tự Động Tạo Thông Báo Tại Các Service Nghiệp Vụ
- **Mục tiêu:** Kết nối `notificationService` vào các luồng xử lý:
  - **`OrderServiceImpl.java`:**
    - Trong `createOrderFromCart()` và `createOrderDirect()`: Gọi `notificationService.notifyOrderCreated(order);`
  - **`AdminOrderController.java`:**
    - Trong `updateStatus()`: Gọi `notificationService.notifyOrderStatusChanged(order, oldStatus, newStatus);`
  - **`AdminSupportTicketController.java`:**
    - Trong `resolveTicket()`: Tìm `User` theo email và gọi `notificationService.notifySupportTicketResolved(ticket, user);`
  - **`StoreRegistrationServiceImpl.java`:**
    - Trong `approveRegistration()` và `rejectRegistration()`: Gọi `notificationService.notifyStoreRegistrationResult(reg);`
- **Xác minh:** Đặt thử 1 đơn hàng -> Kiểm tra bảng `notifications` trong MySQL có ngay 2 bản ghi mới (1 cho khách, 1 cho quản lý).

---

### Task 7: Bổ Sung Tab Lịch Sử Khiếu Nại & Thay Avatar Demo Trong `Profile`
- **Mục tiêu:** Cho phép khách hàng xem lại các phiếu hỗ trợ đã gửi và câu trả lời của Admin; dọn dẹp link Cloudinary demo.
- **File 1:** `src/main/java/vn/iotstar/controller/ProfileController.java`
  - Bổ sung nạp danh sách `userTickets`:
```java
if ("support".equalsIgnoreCase(tab)) {
    User user = userRepository.findById(userId).orElse(null);
    if (user != null && user.getEmail() != null) {
        model.addAttribute("userTickets", supportTicketRepository.findByEmailOrderByCreatedAtDesc(user.getEmail()));
    }
}
```
- **File 2:** `src/main/resources/templates/user/profile.html`
  - Thêm mục `Phiếu Hỗ Trợ` vào sidebar.
  - Bổ sung nội dung tab hiển thị bảng danh sách các phiếu hỗ trợ (Mã phiếu, chủ đề, mã đơn, nội dung, phản hồi từ Admin, trạng thái).
  - Thay toàn bộ các đường dẫn `https://res.cloudinary.com/demo/image/upload/v1/samples/people/jazz-singer.jpg` bằng `/images/avatars/avatar-default.png`.
- **Xác minh:** Truy cập `/user/account/profile?tab=support`, xác nhận bảng phiếu hỗ trợ hiển thị phản hồi của Admin.

---

### Task 8: Chuẩn Hóa Tiền Tệ `... đ`, Lượt Theo Dõi & Nhận Diện ReBook
- **File 1:** `src/main/resources/templates/store-detail.html`
  - Nâng cấp `toggleFollowShop()`: Tự động cộng/trừ 1 vào thẻ `#storeFollowersCount` để hiển thị tương tác thời gian thực tức thì.
- **File 2:** `src/main/resources/templates/home.html` & `src/main/resources/templates/search.html`
  - Thay đổi hiển thị tiền tệ sang chuẩn `... đ`.
- **File 3:** `src/main/resources/templates/fragments/footer.html`
  - Đổi tiêu đề chân trang thành `ReBook — Chuỗi Cửa Hàng Sách Cũ TP.HCM`.
- **Xác minh:** F5 trang chủ và trang tìm kiếm, kiểm tra giá tiền và chân trang.

---

## 5. Tests / Validation (Chiến Lược Kiểm Thử)

1. **Unit Test TDD (`NotificationServiceTest.java`):**
   ```bash
   mvn test -Dtest=NotificationServiceTest
   ```
   - Kỳ vọng: 100% pass toàn bộ test tạo thông báo đơn hàng, đổi trạng thái và đếm thông báo chưa đọc.
2. **Kiểm thử tích hợp hệ thống (Full Suite):**
   ```bash
   mvn test
   ```
   - Kỳ vọng: 15/15 tests PASS không có lỗi hồi quy.
3. **Kịch bản kiểm thử trực quan E2E:**
   - **Kịch bản 1:** Đăng nhập `cuong_customer` -> Vào `/checkout` -> Kiểm tra tên người nhận hiển thị đúng `Thái Duy Cường` và SĐT, không bị gán cứng tên Phúc.
   - **Kịch bản 2:** Đặt 1 đơn hàng -> Chuông thông báo trên Header lập tức hiện thông báo thật: *"Đặt hàng thành công: Đơn hàng #ORD... đã được ghi nhận"*.
   - **Kịch bản 3:** Quản lý đổi trạng thái sang `SHIPPING` -> Chuông thông báo khách nhận ngay: *"Đơn hàng #ORD... đang trên đường giao tới bạn"*.
   - **Kịch bản 4:** Khách vào `/user/account/profile?tab=support` -> Xem đầy đủ lịch sử các phiếu khiếu nại kèm phản hồi xử lý của Admin.

---

## 6. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi xóa thông báo tĩnh trên Header, khách vãng lai (Guest) chưa đăng nhập mở chuông sẽ thấy gì?
  - *Giải pháp:* Hiển thị banner mời đăng nhập tinh gọn kèm nút `[Đăng Nhập]` để nhận thông báo đơn hàng.
- **Rủi ro:** Tải thông báo quá nhiều gây chậm trang?
  - *Giải pháp:* Giới hạn `PageRequest.of(0, 10)` chỉ lấy 10 thông báo mới nhất cho popover; nút "Xem tất cả" dẫn về `/orders`.
