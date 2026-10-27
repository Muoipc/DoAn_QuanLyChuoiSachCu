# KẾ HOẠCH HOÀN THIỆN TÍNH NĂNG ĐĂNG KÝ MỞ GIAN HÀNG & HỖ TRỢ ĐA NGÔN NGỮ (MERCHANT ONBOARDING)

## 1. Goal (Mục Tiêu)
Xây dựng trọn vẹn quy trình đăng ký mở gian hàng sách cũ (Merchant / Store Onboarding) trên ReBook: từ giao diện giới thiệu & nộp hồ sơ trực tuyến, hỗ trợ song ngữ (Tiếng Việt & English), thẩm định danh tính & tài khoản nhận tiền, quy trình Admin duyệt hồ sơ & tự động nâng quyền tài khoản lên `ROLE_MANAGER`, đến việc phân quyền truy cập Kênh Quản Trị Cửa Hàng.

---

## 2. Trả Lời Người Dùng: "Nếu tôi muốn mở gian hàng trên trang này thì cần làm những gì?"

Quy trình chuẩn dành cho một đối tác / người bán sách cũ muốn mở gian hàng trên ReBook gồm 4 bước cụ thể:

### Bước 1: Chuẩn bị thông tin & Đăng nhập tài khoản ReBook
1. Đăng ký/Đăng nhập tài khoản thành viên ReBook thông thường (tài khoản người dùng cá nhân).
2. Chuẩn bị thông tin cửa hàng:
   - **Tên gian hàng / Chi nhánh:** (Ví dụ: *Hiệu Sách Cũ Bình Thạnh*, *Gác Sách Xưa Sài Gòn*...).
   - **Địa chỉ kinh doanh:** Số nhà, tên đường, Phường/Xã, Quận/Huyện tại TP.HCM (hoặc kho sách thực tế).
   - **Hotline & Email liên hệ:** Số điện thoại trực quầy và email tiếp nhận thông báo đơn hàng.
   - **Khung giờ mở cửa:** Khung giờ phục vụ khách (mặc định: `08:00 - 21:30`).
   - **Ảnh đại diện / Biển hiệu shop:** Ảnh chụp quầy sách hoặc logo gian hàng.
3. Chuẩn bị thông tin định danh & Thanh toán đối soát:
   - **Họ tên chủ quầy / Người đại diện hợp pháp.**
   - **Số CCCD / CMND.**
   - **Số tài khoản ngân hàng & Tên ngân hàng:** Dùng để ReBook đối soát và chuyển tiền bán sách (tiền thu hộ COD sau khi trừ chiết khấu sàn 10%) vào thứ 2 và thứ 5 hàng tuần.

### Bước 2: Nộp hồ sơ đăng ký mở gian hàng trực tuyến
1. Bấm vào nút **[Kênh Người Bán / Seller Centre]** trên thanh Header hoặc trong menu cá nhân (truy cập URL: `/seller/register`).
2. Điền biểu mẫu 3 phần:
   - *Phần 1 - Hồ sơ gian hàng:* Tên shop, địa chỉ chi tiết, hotline, giờ mở cửa, ảnh quầy sách.
   - *Phần 2 - Định danh & Đối soát:* CCCD, Ngân hàng, Số tài khoản, Tên chủ tài khoản.
   - *Phần 3 - Cam kết vận hành:* Tích chọn đồng ý các điều khoản (chất lượng sách thật, không bán sách lậu/rách nát, đồng kiểm trong 24h, chiết khấu nền tảng 10%).
3. Bấm **[Gửi Hồ Sơ Đăng Ký]**. Hệ thống lưu hồ sơ ở trạng thái `CHỜ XÉT DUYỆT (PENDING)` và cấp mã hồ sơ.

### Bước 3: Chờ Ban Quản Trị ReBook xét duyệt (trong 24 giờ)
- Admin ReBook nhận được thông báo hồ sơ mới tại màn hình `/admin/store-registrations`.
- Admin kiểm tra tính hợp lệ của địa chỉ và thông tin ngân hàng.
- Admin đưa ra quyết định:
  - **Duyệt (Approved):** Hệ thống tự động tạo Chi nhánh mới trong bảng `stores`, kích hoạt `isActive = true`, gán quyền Quản lý cửa hàng (`ROLE_MANAGER`) cho tài khoản của bạn, và gán bạn làm Manager của chi nhánh đó.
  - **Từ chối (Rejected):** Hệ thống ghi nhận lý do cụ thể (ví dụ: *Địa chỉ chưa rõ ràng, ảnh biển hiệu mờ...*) để bạn chỉnh sửa nộp lại.

### Bước 4: Nhận bàn giao gian hàng & Bắt đầu kinh doanh
- Sau khi được duyệt, khi bạn tải lại trang web hoặc đăng nhập lại:
  - Hệ thống tự nhận diện vai trò `ROLE_MANAGER`.
  - Trên Header xuất hiện nút **[Kênh Quản Trị Cửa Hàng]**.
  - Bạn được cấp quyền truy cập toàn diện phân hệ quản lý của chi nhánh mình:
    1. Đăng bán sách lên kệ chi nhánh (`/admin/books/create`).
    2. Quản lý tồn kho chi nhánh (`/admin/inventory`).
    3. Bật/Tắt Trợ lý AI trực chat tự động hoặc tự mình chat với khách (`/admin/chat`).
    4. Quản lý đơn hàng phát sinh từ quầy của mình (`/admin/orders`).
    5. Theo dõi báo cáo doanh thu độc lập của chi nhánh (`/admin/reports`).

---

## 3. Kiến Trúc Kỹ Thuật & Giải Pháp Triển Khai

1. **Entity `StoreRegistration` & Trạng Thái:**
   - Tạo bảng `store_registrations` độc lập để lưu toàn bộ hồ sơ đăng ký, CCCD, thông tin ngân hàng, trạng thái (`PENDING`, `APPROVED`, `REJECTED`) và lý do từ chối mà không làm bẩn bảng `stores` hiện có.
2. **Luồng Nâng Quyền Tự Động (Auto-Promotion):**
   - Khi Admin bấm `Approve`, Service thực hiện một Transaction:
     - Tạo bản ghi mới trong bảng `stores` với `manager_id = applicantUser.getId()`.
     - Tìm `Role` mang tên `ROLE_MANAGER` trong DB và gán cho `applicantUser.setRole(managerRole)`.
     - Lưu trạng thái đăng ký là `APPROVED`.
3. **Đa Ngôn Ngữ Song Ngữ (VI/EN) & Trạng Thái Chat:**
   - Bổ sung các từ khóa vào `i18n.js`: `Kênh Người Bán` / `Seller Centre`, `Mở Gian Hàng` / `Open a Store`, `Hồ Sơ Chờ Duyệt` / `Application Pending`, `Trực tiếp` / `Online`, `Vắng mặt` / `Offline`.
   - Script tự động đồng bộ nhãn dựa theo cookie `app_lang` hoặc tham số `?lang=en` / `?lang=vi`.

---

## 4. Kế Hoạch Triển Khai Từng Bước (Bite-sized Tasks)

### Task 1: Tạo Entity & Enum `StoreRegistration`
- **File:** `src/main/java/vn/iotstar/entity/StoreRegistration.java`
- **Mã nguồn:**
```java
package vn.iotstar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_registrations")
public class StoreRegistration {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String province = "TP. Hồ Chí Minh";

    @Column(nullable = false, length = 100)
    private String district;

    @Column(name = "open_time", length = 20)
    private String openTime = "08:00";

    @Column(name = "close_time", length = 20)
    private String closeTime = "21:30";

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "owner_id_card", nullable = false, length = 30)
    private String ownerIdCard;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "bank_account", nullable = false, length = 50)
    private String bankAccount;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StoreRegistration() {}

    // Getters and Setters đầy đủ
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerIdCard() { return ownerIdCard; }
    public void setOwnerIdCard(String ownerIdCard) { this.ownerIdCard = ownerIdCard; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

---

### Task 2: Tạo Repository & Service Xử Lý Nghiệp Vụ
- **File 1:** `src/main/java/vn/iotstar/repository/StoreRegistrationRepository.java`
```java
package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.StoreRegistration;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRegistrationRepository extends JpaRepository<StoreRegistration, Long> {
    List<StoreRegistration> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<StoreRegistration> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<StoreRegistration> findByStatusOrderByCreatedAtDesc(StoreRegistration.Status status);
    long countByStatus(StoreRegistration.Status status);
}
```

- **File 2:** `src/main/java/vn/iotstar/service/IStoreRegistrationService.java`
```java
package vn.iotstar.service;

import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.entity.User;
import java.util.List;

public interface IStoreRegistrationService {
    StoreRegistration submitRegistration(StoreRegistration registration, User user);
    StoreRegistration getLatestByUser(Long userId);
    List<StoreRegistration> findAll();
    List<StoreRegistration> findByStatus(StoreRegistration.Status status);
    StoreRegistration findById(Long id);
    Store approveRegistration(Long registrationId);
    StoreRegistration rejectRegistration(Long registrationId, String reason);
}
```

- **File 3:** `src/main/java/vn/iotstar/service/impl/StoreRegistrationServiceImpl.java`
  - Triển khai logic:
    + Khi `approveRegistration`: Tạo `Store`, sinh `slug` từ tên store qua `SlugUtil.toSlug()`, gán `manager = registration.getUser()`, nâng cấp role `user.setRole(roleManager)`, cập nhật status = `APPROVED`.
    + Khi `rejectRegistration`: Cập nhật status = `REJECTED`, gán `rejectReason`.

---

### Task 3: Viết Unit Test TDD Cho Quy Trình Duyệt Gian Hàng
- **File:** `src/test/java/vn/iotstar/service/StoreRegistrationServiceTest.java`
- **Mục tiêu test:**
  1. Nộp hồ sơ -> Lưu thành công trạng thái `PENDING`.
  2. Admin duyệt -> Sinh ra `Store` mới trong DB, tài khoản người dùng được đổi thành `ROLE_MANAGER`.
  3. Admin từ chối -> Lưu đúng lý do từ chối, tài khoản vẫn giữ `ROLE_USER`.
- **Lệnh chạy:**
```bash
mvn test -Dtest=StoreRegistrationServiceTest
```
- **Kỳ vọng:** `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`.

---

### Task 4: Xây Dựng Controller Đăng Ký Người Bán (`SellerRegistrationController.java`)
- **File:** `src/main/java/vn/iotstar/controller/SellerRegistrationController.java`
- **Route:**
  - `GET /seller/register`:
    + Kiểm tra nếu chưa đăng nhập -> Chuyển hướng về `/login?redirect=/seller/register`.
    + Nếu đã là `ROLE_MANAGER` -> Chuyển hướng sang `/admin/inventory` (bạn đã là chủ gian hàng).
    + Nếu đã có đơn `PENDING` -> Hiển thị màn hình chờ duyệt.
    + Nếu chưa có đơn hoặc đơn cũ bị `REJECTED` -> Hiển thị form đăng ký.
  - `POST /seller/register`:
    + Nhận dữ liệu form + ảnh đại diện gian hàng (upload Cloudinary hoặc ảnh mặc định).
    + Gọi `storeRegistrationService.submitRegistration(...)`.
    + Flash thông báo thành công và chuyển sang màn hình trạng thái.
  - `GET /seller/status`: Xem chi tiết trạng thái xét duyệt hồ sơ.

---

### Task 5: Xây Dựng Controller Quản Trị Xét Duyệt Admin (`AdminStoreRegistrationController.java`)
- **File:** `src/main/java/vn/iotstar/controller/admin/AdminStoreRegistrationController.java`
- **Route (Khóa quyền `ROLE_ADMIN`):**
  - `GET /admin/store-registrations`: Hiển thị danh sách hồ sơ xin mở quầy (có bộ lọc tab: `Tất Cả`, `Chờ Duyệt`, `Đã Duyệt`, `Từ Chối`).
  - `POST /admin/store-registrations/{id}/approve`: Duyệt hồ sơ, tự động tạo Store và cấp quyền Manager.
  - `POST /admin/store-registrations/{id}/reject`: Từ chối hồ sơ kèm lý do nhập từ modal.

---

### Task 6: Thiết Kế Giao Diện Đăng Ký Mở Gian Hàng (`templates/seller/register.html`)
- **File:** `src/main/resources/templates/seller/register.html`
- **Phong cách:** Minimalist Antique White kết hợp Apple Liquid Glass.
- **Bố cục:**
  1. **Hero Banner:** Giới thiệu quyền lợi mở gian hàng ReBook:
     - 🚀 Tiếp cận hàng chục ngàn bạn đọc tại TP.HCM.
     - 🤖 Được trang bị Trợ Lý AI tự động giải đáp tồn kho & tư vấn sách 24/7.
     - 🚚 Hệ thống điều phối Shipper giao hàng & thu COD đồng kiểm minh bạch.
     - 💼 Chiết khấu nền tảng chỉ 10%, đối soát thanh toán 2 lần/tuần.
  2. **Form 3 Bước Phẳng Tối Giản:**
     - *Bước 1: Thông tin quầy sách:* Tên quầy, Quận/Huyện, Địa chỉ, Hotline, Giờ hoạt động.
     - *Bước 2: Định danh & Ngân hàng:* Họ tên đại diện, CCCD/CMND, Tên ngân hàng, Số tài khoản.
     - *Bước 3: Cam kết:* Điều khoản quy định gian hàng.
  3. **Màn hình Chờ Duyệt (Pending View):**
     - Hiển thị khi người dùng đã nộp đơn: Thẻ kính mờ hiển thị trạng thái `Đang Chờ Duyệt ⏳`, thời gian dự kiến duyệt trong 24h, tóm tắt thông tin đã nộp.

---

### Task 7: Thiết Kế Màn Hình Quản Trị Duyệt Hồ Sơ (`templates/admin/stores/registrations.html`)
- **File:** `src/main/resources/templates/admin/stores/registrations.html`
- **Tích hợp vào `admin-layout.html`:** Thêm menu `Duyệt Mở Gian Hàng` trên Sidebar của Admin kèm badge số lượng hồ sơ đang chờ (`pendingCount`).
- **Tính năng giao diện:**
  - Bảng danh sách hồ sơ: Tên gian hàng, Người đăng ký, Hotline, Quận/Huyện, Thời gian nộp, Trạng thái.
  - Nút **[Xem Chi Tiết]**: Modal mở xem toàn bộ CCCD, STK Ngân hàng, Ảnh quầy.
  - Nút **[Duyệt]**: Xác nhận 1-chạm tạo store ngay lập tức.
  - Nút **[Từ Chối]**: Bật modal nhập lý do gửi lại cho người nộp đơn.

---

### Task 8: Tích Hợp Nút "Kênh Người Bán" Lên Header & Đa Ngôn Ngữ (`i18n.js`)
- **File 1:** `src/main/resources/templates/fragments/header.html`
  - Bổ sung nút truy cập Kênh Người Bán:
    + Ở Topbar và trong User Popover menu:
      ```html
      <!-- Kênh Người Bán / Mở Gian Hàng -->
      <a sec:authorize="hasRole('USER')" th:href="@{/seller/register}" class="shopee-popover-item">
          <i class="fa-solid fa-store" style="color:#8C4A27;"></i>
          <span class="i18n-text" data-i18n="Kênh Người Bán">Kênh Người Bán</span>
      </a>
      ```
  - Trên trang `/stores` (`stores.html`): Thêm banner mời gọi mở gian hàng kèm nút `[Đăng Ký Mở Gian Hàng >]` dẫn tới `/seller/register`.

- **File 2:** `src/main/resources/static/js/i18n.js`
  - Cập nhật từ điển song ngữ:
```javascript
// Bổ sung vào I18N_DICTIONARY.vi:
'Kênh Người Bán': 'Kênh Người Bán',
'Mở Gian Hàng': 'Mở Gian Hàng',
'Đăng Ký Mở Cửa Hàng': 'Đăng Ký Mở Cửa Hàng',
'Hồ Sơ Đang Chờ Duyệt': 'Hồ Sơ Đang Chờ Duyệt',
'Gian Hàng Đã Được Duyệt': 'Gian Hàng Đã Được Duyệt',
'Trực tiếp': 'Trực tiếp',
'Vắng mặt': 'Vắng mặt',

// Bổ sung vào I18N_DICTIONARY.en:
'Kênh Người Bán': 'Seller Centre',
'Mở Gian Hàng': 'Open a Store',
'Đăng Ký Mở Cửa Hàng': 'Store Registration',
'Hồ Sơ Đang Chờ Duyệt': 'Application Pending',
'Gian Hàng Đã Được Duyệt': 'Store Approved',
'Trực tiếp': 'Online',
'Vắng mặt': 'Offline',
```

---

### Task 9: Cập Nhật Phân Quyền Trong `SecurityConfig.java`
- **File:** `src/main/java/vn/iotstar/config/SecurityConfig.java`
- **Cấu hình:**
```java
// Cho phép xem giới thiệu hoặc bắt buộc đăng nhập để nộp đơn:
.requestMatchers("/seller/**").authenticated()
// Khóa quyền kiểm duyệt hồ sơ mở quầy cho riêng ADMIN:
.requestMatchers("/admin/store-registrations/**").hasRole("ADMIN")
```

---

## 5. Kiểm Thử & Nghiệm Thu (Validation & Testing)

1. **Unit Test TDD:**
   ```bash
   mvn test -Dtest=StoreRegistrationServiceTest
   ```
2. **Kịch bản Kiểm thử Đầu-Cuối (End-to-End):**
   - **Kịch bản 1 (Người dùng thường):**
     - Đăng nhập tài khoản khách hàng `phuc_test` (role `ROLE_USER`).
     - Bấm vào `Kênh Người Bán` trên Header -> Dẫn đến `/seller/register`.
     - Chuyển ngôn ngữ sang `English` -> Toàn bộ tiêu đề đổi thành `Seller Centre`, `Store Registration`.
     - Điền form: *Tiệm Sách Cũ Gò Vấp*, *0901234567*, *Số 123 Quang Trung, Gò Vấp*, CCCD *079204001234*, Vietcombank *0123456789*.
     - Bấm submit -> Chuyển sang màn hình `Pending Approval` với mã hồ sơ.
   - **Kịch bản 2 (Quản trị viên Admin):**
     - Đăng nhập tài khoản `admin` -> Truy cập `/admin/store-registrations`.
     - Danh sách hiện hồ sơ của *Tiệm Sách Cũ Gò Vấp*.
     - Bấm nút **[Duyệt Kích Hoạt]**.
     - Kiểm tra DB: Bảng `stores` sinh thêm chi nhánh mới, cột `manager_id` trỏ đến `phuc_test`, bảng `users` cập nhật quyền của `phuc_test` thành `ROLE_MANAGER`.
   - **Kịch bản 3 (Trở lại người dùng sau khi duyệt):**
     - Đăng nhập lại `phuc_test` -> Truy cập `/admin/inventory` hoặc `/admin/books` thành công mà không bị chặn `403`.
     - Xem đúng chi nhánh *Tiệm Sách Cũ Gò Vấp* vừa tạo.

---

## 6. Rủi Ro & Giải Pháp Phòng Ngừa (Risks & Tradeoffs)
1. **Trùng lặp Slug cửa hàng:** Nếu hai người dùng đăng ký cùng tên shop (ví dụ đều đặt *Tiệm Sách Cũ Sài Gòn*), hàm tạo slug tự động thêm hậu tố số ngẫu nhiên (`tiem-sach-cu-sai-gon-2`) để đảm bảo tính duy nhất.
2. **Người dùng nộp đơn nhiều lần liên tiếp:** Khi người dùng đã có một đơn ở trạng thái `PENDING`, form đăng ký sẽ khóa lại và hiển thị màn hình theo dõi tiến trình, tránh việc spam tạo nhiều đơn rác.
3. **Session Role sau khi duyệt:** Sau khi Admin duyệt, nếu người dùng vẫn đang giữ session cũ, token/session có thể chưa cập nhật role ngay. Cung cấp thông báo hướng dẫn người dùng đăng xuất và đăng nhập lại một lần để nạp đầy đủ quyền `ROLE_MANAGER`.
