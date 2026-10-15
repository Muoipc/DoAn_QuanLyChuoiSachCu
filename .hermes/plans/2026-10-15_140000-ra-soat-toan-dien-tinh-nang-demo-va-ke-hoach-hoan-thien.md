# KẾ HOẠCH RÀ SOÁT TOÀN DIỆN & HOÀN THIỆN 100% CÁC CHỨC NĂNG CÒN THIẾU / CÒN MANG TÍNH CHẤT DEMO TRÊN REBOOK

## 1. Goal
Rà soát toàn diện 100% các thành phần mã nguồn, giao diện, API và cơ sở dữ liệu của dự án ReBook; lập danh mục chi tiết mọi điểm còn gán dữ liệu tĩnh (hardcoded/mock), nút bấm chưa có logic xử lý, hoặc luồng nghiệp vụ bị đứt đoạn; và xây dựng kế hoạch kỹ thuật từng bước để hoàn thiện, đưa toàn bộ hệ thống vào vận hành thực tế.

---

## 2. Báo Cáo Rà Soát Chi Tiết: Các Tính Năng Còn Thiếu / Mang Tính Chất Demo

Qua quá trình rà soát toàn bộ 25 template giao diện người dùng, 14 controller quản trị, 10 controller khách hàng và cơ sở dữ liệu MySQL `old_book_store_db`, hệ thống đã đạt độ hoàn thiện cao (~92%), tuy nhiên vẫn còn tồn tại **7 điểm cụ thể** còn mang tính chất demo hoặc dữ liệu gán cứng cần khắc phục:

### Nhóm 1: Dữ Liệu Gán Cứng / Fallback Cần Khắc Phục Triệt Để (Critical Hardcoded Data)
1. **Thông tin người nhận mặc định trên Checkout (`CheckoutController.java` dòng 198-202):**
   - *Hiện trạng:* Khi tài khoản đăng nhập chưa lưu địa chỉ trong sổ địa chỉ (`defaultAddress == null`), controller đang gán cứng:
     - `receiverName = "Nguyễn Song Hoàng Phúc";`
     - `receiverPhone = "0912345678";`
     - `formattedAddress = "Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh";`
   - *Hệ quả:* Bất kỳ người dùng nào mới đăng ký hoặc tài khoản khác khi vào trang Thanh toán đều bị hiển thị tên và số điện thoại của Phúc thay vì thông tin thật của chính họ.
   - *Khắc phục:* Lấy thông tin thật từ `currentUser.getFullName()` và `currentUser.getPhone()`. Nếu chưa có địa chỉ, hiển thị thông báo yêu cầu nhập hoặc chọn địa chỉ nhận hàng.

2. **Trung tâm thông báo khách hàng Header là HTML tĩnh (`fragments/header.html` dòng 1685-1708):**
   - *Hiện trạng:* Chuông thông báo trên Header hiển thị 3 thông báo mẫu viết cứng bằng HTML (Mã giảm giá, đơn hàng giả lập, giáo trình CTDL).
   - *Hệ quả:* Dù khách hàng có đặt bao nhiêu đơn, hủy đơn, hay được duyệt ký gửi, chuông thông báo không bao giờ cập nhật dữ liệu thật.
   - *Khắc phục:* Viết API `GET /api/notifications` lấy danh sách thông báo thật của người dùng từ bảng `notifications` trong CSDL, nạp động vào popover và tự động cập nhật chấm đỏ khi có thông báo mới.

3. **Avatar mẫu dùng link Cloudinary Demo ngoài Internet (`user/profile.html` dòng 487 & 981):**
   - *Hiện trạng:* Khi người dùng chưa có avatar, hệ thống fallback về ảnh demo: `https://res.cloudinary.com/demo/image/upload/v1/samples/people/jazz-singer.jpg`.
   - *Hệ quả:* Phụ thuộc mạng ngoài, không đồng bộ với ảnh nội bộ ReBook `/images/avatars/avatar-default.png`.
   - *Khắc phục:* Thay toàn bộ các đường dẫn fallback sang `/images/avatars/avatar-default.png`.

4. **Tên thương hiệu cũ tại `fragments/footer.html`:**
   - *Hiện trạng:* Dòng tiêu đề vẫn để `CHUỖI CỬA HÀNG SÁCH CŨ` trong khi toàn bộ hệ thống đã chuyển sang thương hiệu `ReBook`.
   - *Khắc phục:* Chuẩn hóa thành `ReBook — Chuỗi Cửa Hàng Sách Cũ TP.HCM`.

---

### Nhóm 2: Luồng Nghiệp Vụ Tự Động Bị Đứt Đoạn (Event-Driven Gaps)
5. **Thiếu cơ chế tự động tạo Thông Báo (Event Notifications) trong vòng đời đơn hàng & nghiệp vụ:**
   - *Hiện trạng:* `NotificationServiceImpl` hiện tại chỉ mới có hàm `notifyConsignmentSubmitted`. Chưa có cơ chế bắn thông báo khi:
     - Khách đặt đơn mới -> Tạo thông báo cho Store Manager chi nhánh phụ trách & Khách hàng.
     - Đơn đổi trạng thái (`CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`) -> Tạo thông báo cập nhật hành trình cho khách hàng.
     - Admin duyệt / từ chối mở gian hàng (`StoreRegistration`) -> Tạo thông báo cho người đăng ký.
     - Admin phản hồi phiếu hỗ trợ (`SupportTicket`) -> Tạo thông báo cho khách hàng.
   - *Khắc phục:* Mở rộng `INotificationService` với các phương thức tạo thông báo tự động và gọi tại các thời điểm nghiệp vụ tương ứng trong `OrderServiceImpl`, `AdminOrderController`, `AdminStoreRegistrationController`, `AdminSupportTicketController`.

6. **Theo dõi phản hồi Phiếu Trợ Giúp phía Khách Hàng:**
   - *Hiện trạng:* Khách gửi phiếu khiếu nại qua `/help/contact`, Admin tiếp nhận và phản hồi qua `/admin/support-tickets`, nhưng khách hàng chưa có nơi xem lại các phiếu của mình và câu trả lời của Admin.
   - *Khắc phục:* Bổ sung tab **Hỗ Trợ & Khiếu Nại** (`tab=support`) trong trang Hồ sơ cá nhân (`/user/account/profile?tab=support`) để khách theo dõi các phiếu đã gửi kèm câu trả lời từ Ban quản trị.

---

### Nhóm 3: Đồng Bộ Trải Nghiệm & Tương Tác Thị Giác (UI Consistency & Reactivity)
7. **Đồng bộ hiển thị tiền tệ và tương tác Follower:**
   - *Hiện trạng:* 
     - Lưới sách trên `home.html` và `search.html` còn hiển thị `₫68.000` (ký hiệu ₫ ở đầu) trong khi PDP, giỏ hàng và checkout dùng chuẩn `68.000 đ`.
     - Nút "Theo Dõi" tại `store-detail.html` khi bấm chỉ đổi trạng thái nút mà chưa tự động tăng/giảm con số lượt theo dõi hiển thị trên giao diện.
   - *Khắc phục:* Chuẩn hóa toàn bộ tiền tệ sang hậu tố `... đ` và cập nhật DOM số lượng follower ngay khi click.

---

## 3. Kiến Trúc Kỹ Thuật & Giải Pháp Triển Khai

1. **Hệ Thống Thông Báo Thời Gian Thực (Unified Notification System):**
   - Entity `Notification` đã có sẵn các type: `ORDER`, `CONSIGNMENT`, `SUPPORT_TICKET`, `STORE_REGISTRATION`.
   - Bổ sung `NotificationRepository.findByRecipientIdOrderByCreatedAtDesc(Long userId)`.
   - Tạo REST Controller `NotificationRestController.java` (`GET /api/notifications`, `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all`).
   - Cập nhật JavaScript trong `header.html` gọi API này khi mở popover thông báo.
2. **Sửa Đổi Fallback Địa Chỉ Checkout An Toàn:**
   - Trong `CheckoutController.java`, nếu `defaultAddress == null`:
     - `receiverName = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank()) ? currentUser.getFullName() : currentUser.getUsername();`
     - `receiverPhone = (currentUser.getPhone() != null) ? currentUser.getPhone() : "";`
     - `formattedAddress = "";` (yêu cầu người dùng nhập địa chỉ giao hàng hoặc chọn nhận tại quầy).
3. **Tab Lịch Sử Phiếu Khiếu Nại:**
   - Trong `ProfileController.java`, nạp danh sách `SupportTicket` theo email của user đăng nhập vào model khi `tab == 'support'`.

---

## 4. Kế Hoạch Triển Khai Từng Bước (Bite-Sized Tasks)

### Task 1: Mở rộng `NotificationRepository` & `INotificationService`
- **File 1:** `src/main/java/vn/iotstar/repository/NotificationRepository.java`
  - Bổ sung:
    ```java
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    ```
- **File 2:** `src/main/java/vn/iotstar/service/INotificationService.java`
  - Bổ sung các phương thức:
    ```java
    void notifyOrderCreated(Order order);
    void notifyOrderStatusChanged(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus);
    void notifySupportTicketResolved(SupportTicket ticket);
    void notifyStoreRegistrationResult(StoreRegistration registration);
    List<Notification> getNotificationsForUser(Long userId);
    long countUnreadForUser(Long userId);
    ```
- **File 3:** `src/main/java/vn/iotstar/service/impl/NotificationServiceImpl.java`
  - Triển khai logic lưu bản ghi `Notification` vào DB và gửi WebSocket STOMP (nếu có kết nối).

---

### Task 2: Xây Dựng REST API Thông Báo Người Dùng (`NotificationRestController.java`)
- **File:** `src/main/java/vn/iotstar/controller/api/NotificationRestController.java`
- **Routes:**
  - `GET /api/notifications`: Trả về danh sách 10 thông báo mới nhất kèm `unreadCount`.
  - `POST /api/notifications/{id}/read`: Đánh dấu 1 thông báo đã đọc.
  - `POST /api/notifications/read-all`: Đánh dấu tất cả là đã đọc.

---

### Task 3: Động Hóa Popover Thông Báo Trên Header (`fragments/header.html`)
- **File:** `src/main/resources/templates/fragments/header.html`
- **Thay đổi:**
  - Xóa bỏ 3 thông báo HTML viết cứng trong `.noti-popover-list`.
  - Viết hàm JavaScript `fetchUserNotifications()`:
    - Tự động gọi `GET /api/notifications` khi rê chuột hoặc click vào chuông thông báo.
    - Render danh sách thông báo thật (mã đơn, link xem đơn, trạng thái).
    - Cập nhật số đếm badge đỏ trên chuông.

---

### Task 4: Sửa Fallback Dữ Liệu Thông Tin Người Nhận Trên Checkout (`CheckoutController.java`)
- **File:** `src/main/java/vn/iotstar/controller/CheckoutController.java`
- **Thay đổi:**
  - Xóa bỏ tên cứng "Nguyễn Song Hoàng Phúc", SĐT "0912345678" và địa chỉ "Số 48 Võ Văn Ngân...".
  - Lấy trực tiếp thông tin từ đối tượng `User` trong cơ sở dữ liệu:
    ```java
    User currentUser = userRepository.findById(userId).orElse(null);
    String receiverName = defaultAddress != null ? defaultAddress.getReceiverName() 
            : (currentUser != null && currentUser.getFullName() != null ? currentUser.getFullName() : "");
    String receiverPhone = defaultAddress != null ? defaultAddress.getPhone() 
            : (currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "");
    String formattedAddress = defaultAddress != null 
            ? (defaultAddress.getStreetAddress() + ", " + defaultAddress.getWard() + ", " + defaultAddress.getDistrict() + ", " + defaultAddress.getProvince())
            : "";
    ```

---

### Task 5: Bổ Sung Tab Lịch Sử Hỗ Trợ & Khiếu Nại Trong Hồ Sơ Người Dùng
- **File 1:** `src/main/java/vn/iotstar/controller/ProfileController.java`
  - Khi `tab.equals("support")`: Truy vấn danh sách phiếu hỗ trợ theo email của user `supportTicketRepository.findByEmailOrderByCreatedAtDesc(user.getEmail())` và đưa vào Model.
- **File 2:** `src/main/resources/templates/user/profile.html`
  - Thêm mục `Hỗ Trợ & Khiếu Nại` vào Sidebar bên trái của Profile.
  - Bổ sung view hiển thị bảng các phiếu khiếu nại đã gửi, trạng thái (`Chờ xử lý` / `Đã giải quyết`) và nội dung phản hồi từ Admin (`adminReply`).
  - Thay thế các link avatar Cloudinary demo cũ sang `/images/avatars/avatar-default.png`.

---

### Task 6: Tự Động Tạo Thông Báo Tại Các Điểm Nghiệp Vụ
- **File 1:** `src/main/java/vn/iotstar/service/impl/OrderServiceImpl.java`
  - Khi tạo đơn thành công: Gọi `notificationService.notifyOrderCreated(order)`.
  - Khi cập nhật thanh toán VNPay thành công: Tạo thông báo thanh toán.
- **File 2:** `src/main/java/vn/iotstar/controller/admin/AdminOrderController.java`
  - Khi đổi trạng thái đơn hàng: Gọi `notificationService.notifyOrderStatusChanged(order, oldStatus, newStatus)`.
- **File 3:** `src/main/java/vn/iotstar/controller/admin/AdminSupportTicketController.java`
  - Khi Admin bấm giải quyết phiếu: Gọi `notificationService.notifySupportTicketResolved(ticket)`.
- **File 4:** `src/main/java/vn/iotstar/service/impl/StoreRegistrationServiceImpl.java`
  - Khi duyệt hoặc từ chối gian hàng: Gọi `notificationService.notifyStoreRegistrationResult(reg)`.

---

### Task 7: Đồng Bộ Định Dạng Tiền Tệ & Tương Tác Lượt Theo Dõi Chi Nhánh
- **File 1:** `src/main/resources/templates/home.html` & `search.html`
  - Đổi định dạng hiển thị giá tiền sang chuẩn đồng nhất `... đ`.
- **File 2:** `src/main/resources/templates/store-detail.html`
  - Cập nhật hàm `toggleFollowShop()`: Tự động cộng/trừ 1 vào thẻ `#storeFollowersCount` khi bấm Theo Dõi / Hủy Theo Dõi.
- **File 3:** `src/main/resources/templates/fragments/footer.html`
  - Đổi tên thương hiệu chân trang thành `ReBook — Chuỗi Cửa Hàng Sách Cũ TP.HCM`.

---

## 5. Kiểm Thử & Nghiệm Thu (Tests & Validation)

1. **Unit Test TDD:**
   - Viết `NotificationServiceTest.java`: Kiểm thử các hàm tạo thông báo đơn hàng, đổi trạng thái và đếm số lượng chưa đọc.
   - Chạy kiểm thử:
     ```bash
     mvn test -Dtest=NotificationServiceTest
     ```
2. **Kiểm Thử Thực Tế End-to-End:**
   - **Kịch bản 1:** Đăng nhập tài khoản `cuong_customer` (chưa có địa chỉ) -> Vào `/checkout` -> Kiểm tra tên người nhận hiển thị đúng `Thái Duy Cường` và SĐT của Cường, không còn bị gán thành `Nguyễn Song Hoàng Phúc`.
   - **Kịch bản 2:** Đặt 1 đơn hàng -> Chuông thông báo trên Header lập tức hiện thông báo thật: *"Đơn hàng #ORD... đã được tạo thành công!"*.
   - **Kịch bản 3:** Dùng tài khoản `admin` đổi trạng thái đơn sang `SHIPPING` -> Tài khoản khách nhận thông báo: *"Đơn hàng #ORD... đang được giao tận nơi!"*.
   - **Kịch bản 4:** Gửi phiếu khiếu nại qua `/help` -> Admin phản hồi trong `/admin/support-tickets` -> Khách vào `/user/account/profile?tab=support` thấy câu trả lời của Admin.

---

## 6. Rủi Ro & Giải Pháp (Risks & Tradeoffs)
- **Rủi ro:** Khi xóa thông báo tĩnh trên header, nếu người dùng mới chưa có đơn hàng nào thì popover thông báo bị rỗng.
  - *Giải pháp:* Thiết kế trạng thái Empty State tinh tế: icon chuông nhạt kèm dòng chữ *"Bạn chưa có thông báo mới nào. Các cập nhật về đơn hàng và ưu đãi sẽ xuất hiện tại đây!"*.
- **Rủi ro:** Người dùng chưa đăng nhập bấm vào chuông thông báo.
  - *Giải pháp:* Hiển thị thông báo chào mừng và nút mời đăng nhập để nhận thông báo đơn hàng.
