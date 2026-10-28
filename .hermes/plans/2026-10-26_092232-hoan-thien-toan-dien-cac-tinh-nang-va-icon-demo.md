# Kế Hoạch Hoàn Thiện Toàn Diện Các Tính Năng & Xóa Bỏ Icon/Dữ Liệu Demo Trên Toàn Bộ Web ReBook

## 1. Goal
Rà soát và hoàn thiện triệt để toàn bộ các thành phần, nút bấm, icon và dòng chữ còn mang tính chất tĩnh/demo trên giao diện ReBook (PDP, Trang chi nhánh, Giỏ hàng, Thanh toán, Trang chủ), chuyển hóa thành các tính năng tương tác thực tế kết nối trực tiếp CSDL MySQL và backend Spring Boot 3.4.

---

## 2. Current Context & Assumptions
- **Hiện trạng các điểm nghẽn "mang tính chất demo":**
  1. **Trang Chi Tiết Sách (`book-detail.html`):**
     - Thẻ thông tin cửa hàng hiển thị text tĩnh `<span>Online 5 Phút Trước</span>`, chưa liên kết với trạng thái Presence Tracking thực tế (`isStoreOnline(store)`) của chi nhánh.
     - Các chỉ số quầy: *"Đang Theo: 3"*, *"Tỉ Lệ Phản Hồi: 100%"*, *"Thời Gian Phản Hồi: trong vài phút"* là các giá trị cố định (mock).
     - Icon chia sẻ Messenger đang dùng hàm `alert()` Javascript thô sơ thay vì hệ thống Toast hiện đại.
     - Khối đánh giá sản phẩm đôi khi hiển thị sao giả lập hoặc nhận xét mẫu khi sách chưa có đánh giá thực tế.
  2. **Trang Chi Nhánh Cửa Hàng (`store-detail.html`):**
     - Dòng trạng thái quầy hardcode `<div class="shop-status-line">Online 5 Phút Trước</div>`.
     - Nút "Theo Dõi Shop" (`btn-shop-follow`) chỉ lưu tạm vào HTTP Session (tắt trình duyệt là mất), số người theo dõi ghi cứng `850` (mock).
  3. **Trang Giỏ Hàng (`cart.html`):**
     - Icon mũi tên xổ xuống `fa-chevron-down` cạnh `"Phân loại: 95% Mới"` là icon giả, không bấm được, gây hiểu nhầm cho khách hàng là dropdown chọn độ mới khác.
     - Link `"Tìm tương tự <i class="fa-solid fa-caret-down"></i>"` trỏ về chính cuốn sách đó (`/books/{id}`) thay vì dẫn sang trang tìm kiếm các đầu sách cùng thể loại.
  4. **Trang Thanh Toán (`checkout.html`):**
     - Hàng checkbox *"Điểm Tích Lũy: Dùng 2.000 điểm [-2.000 đ]"* là phép tính trừ tiền tĩnh ở frontend, chưa kết nối với trường điểm tích lũy thực tế của User trong CSDL.
  5. **Trang Chủ (`home.html`):**
     - Bộ đếm ngược Flash Sale chạy lặp lại cục bộ ở client (`02:45:18`), chưa khớp với các phiên săn sách theo khung giờ vàng trong ngày.
     - Modal 5 Cửa Hàng (`#storeModal`) chưa hiển thị trạng thái `🟢 Trực tiếp` / `⚪ Vắng mặt` theo thời gian thực của từng shop.

---

## 3. Architecture & Proposed Approach
- **1. Đồng Bộ Presence Tracking Toàn Diện (Real-Time Presence):**
  - Tái sử dụng phương thức `StoreChatController.isStoreOnline(store)` trên cả trang Chi tiết sản phẩm (`book-detail.html`), trang Chi nhánh (`store-detail.html`) và Modal danh sách cửa hàng (`#storeModal`).
  - Hiển thị đồng bộ theo ngôn ngữ người dùng (`vi`: `🟢 Trực tiếp` / `⚪ Vắng mặt`; `en`: `🟢 Online` / `⚪ Offline`).
- **2. Bền Vững Hóa Tính Năng Theo Dõi Cửa Hàng (Store Followers):**
  - Tạo bảng `store_followers (id, user_id, store_id, created_at)` và entity `StoreFollower.java`.
  - API `POST /api/stores/{storeId}/follow-toggle`: Lưu trạng thái theo dõi vào CSDL, đếm số follower thực tế từ DB để hiển thị trên `store-detail.html` và `book-detail.html`.
- **3. Chuyển Đổi Các Icon Demo Thành Tính Năng Hữu Ích:**
  - Chuyển icon `fa-chevron-down` ở phân loại giỏ hàng thành nút bấm mở Modal/Tooltip *"Bảng Tiêu Chuẩn 4 Cấp Độ Mới Sách Cũ ReBook (80% - 99%)"*.
  - Chuyển nút *"Tìm tương tự"* trong giỏ hàng trỏ chính xác về `/search?cat={category_id}` kèm tên thể loại.
  - Tích hợp trường `loyalty_points` vào bảng `users` và entity `User.java`, tự động cộng 1% giá trị đơn hàng thành công và cho phép người dùng đổi điểm thật khi thanh toán tại `/checkout`.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: CSDL & Entity — Bảng `store_followers` & Cột `loyalty_points`
- **Tệp chỉnh sửa:** CSDL MySQL & `src/main/java/vn/iotstar/entity/`
- **Mục tiêu:** Lưu trữ người theo dõi shop bền vững và điểm tích lũy thành viên.
- **Lệnh SQL:**
```sql
-- 1. Bổ sung cột điểm tích lũy cho người dùng
ALTER TABLE users ADD COLUMN IF NOT EXISTS loyalty_points INT DEFAULT 0;

-- 2. Tạo bảng theo dõi chi nhánh
CREATE TABLE IF NOT EXISTS store_followers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sf_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_store_follow UNIQUE (user_id, store_id)
);
```
- **Tạo Entity `StoreFollower.java`:** `src/main/java/vn/iotstar/entity/StoreFollower.java`
```java
package vn.iotstar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_followers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_store_follow", columnNames = {"user_id", "store_id"})
})
public class StoreFollower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public StoreFollower() {}
    public StoreFollower(User user, Store store) {
        this.user = user;
        this.store = store;
        this.createdAt = LocalDateTime.now();
    }
    // Getters and Setters...
}
```
- **Cập nhật `User.java`:** Thêm `loyaltyPoints` (Integer, default 0).
- **Lệnh xác thực:** `mvn compile`

---

### Task 2: Repository & Service Theo Dõi Shop Thực Tế (`StoreFollowerRepository.java`)
- **Tệp tạo mới:** `src/main/java/vn/iotstar/repository/StoreFollowerRepository.java`
```java
package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.StoreFollower;
import java.util.Optional;

@Repository
public interface StoreFollowerRepository extends JpaRepository<StoreFollower, Long> {
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);
    void deleteByUserIdAndStoreId(Long userId, Long storeId);
    long countByStoreId(Long storeId);
}
```
- **Cập nhật `StoreController.java`:**
  - Endpoint `POST /api/stores/{id}/toggle-follow`: Toggle lưu/xóa trong DB thay vì lưu session tạm bợ.
  - Trả về JSON: `{ "success": true, "isFollowed": boolean, "followersCount": long }`.
  - Nạp `followersCount` thật và `isStoreOnline` vào Model của `/stores/{id}`.
- **Lệnh xác thực:** `mvn test-compile`

---

### Task 3: Nâng Cấp PDP `book-detail.html` — Xóa Bỏ Dữ Liệu Demo Thẻ Shop
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/BookController.java` & `src/main/resources/templates/book-detail.html`
- **Mục tiêu:**
  1. Trong `BookController.java`:
     - Truyền biến `isStoreOnline` tính toán từ `StoreChatController.isStoreOnline(store)`.
     - Lấy `followersCount` thật từ `storeFollowerRepository.countByStoreId(store.getId())`.
     - Lấy đánh giá thật từ `ReviewRepository.findByBookId(id)`.
  2. Trong `book-detail.html`:
     - Thay dòng chữ tĩnh `Online 5 Phút Trước` thành:
       ```html
       <div class="shop-online-status">
           <span class="online-indicator-dot" th:styleappend="${isStoreOnline} ? 'background:#10B981;' : 'background:#A8A29E;'"></span>
           <span th:text="${isStoreOnline ? 'Trực tiếp' : 'Vắng mặt'}">Trực tiếp</span>
           <span class="status-sep">|</span>
           <span th:text="${store != null ? store.district : 'TP. Thủ Đức'}">TP. Thủ Đức</span>
       </div>
       ```
     - Cập nhật số người theo dõi: `<span th:text="${followersCount}">12</span> Người theo dõi`.
     - Thay `alert()` của nút Messenger thành `showLiquidToast('Đã sao chép liên kết sách để gửi qua Messenger!', 'success')`.
- **Lệnh xác thực:** `mvn test-compile`

---

### Task 4: Nâng Cấp `store-detail.html` — Trạng Thái Online Thật & Follow Bền Vững
- **Tệp chỉnh sửa:** `src/main/resources/templates/store-detail.html`
- **Mục tiêu:**
  - Dòng trạng thái shop: Kết nối với `isStoreOnline`, hiển thị chấm xanh `Trực tiếp` (hoặc `Online`) / chấm xám `Vắng mặt` (hoặc `Offline`).
  - Nút "Theo Dõi Shop": Gọi AJAX đến `/api/stores/{id}/toggle-follow`, cập nhật số người theo dõi ngay lập tức trên giao diện không cần reload trang.
  - Chuyển tooltip "Tỉ lệ phản hồi chat" thành popover giải thích minh bạch: *"Dựa trên tốc độ giải đáp của Quản lý quầy và Trợ lý AI trong 30 ngày gần nhất"*.
- **Lệnh xác thực:** `mvn test-compile`

---

### Task 5: Hoàn Thiện Giỏ Hàng `cart.html` — Biến Icon Demo Thành Tính Năng Thật
- **Tệp chỉnh sửa:** `src/main/resources/templates/cart.html`
- **Mục tiêu:**
  1. **Khắc phục icon mũi tên giả `fa-chevron-down` cạnh độ mới:**
     - Thay thế bằng icon thông tin `<i class="fa-solid fa-circle-info text-muted ms-1" style="font-size:11px;" title="Xem tiêu chuẩn kiểm định"></i>`.
     - Bấm vào mở Modal giải thích tiêu chuẩn phân loại độ mới ReBook:
       - **99% (Like New):** Sách chưa qua sử dụng hoặc chỉ đọc 1 lần, không vết ố, nguyên gáy.
       - **95% (Rất Đẹp):** Bìa đẹp, ruột sách sạch sẽ, không quăn góc, không gạch xóa.
       - **90% (Tốt):** Giấy ngả vàng nhẹ theo thời gian, đủ 100% trang, gáy chắc chắn.
       - **80% (Khá):** Sách cũ có dấu vết thời gian, bìa sờn nhẹ, thích hợp đọc lấy kiến thức giá rẻ.
  2. **Khắc phục nút "Tìm tương tự":**
     - Sửa link `th:href="@{/search(cat=${item.book.category != null ? item.book.category.id : 1})}"`.
     - Bấm vào đưa người dùng đến đúng danh mục sách tương tự để tiếp tục mua sắm thay vì link ngược về chính cuốn sách đó!
- **Lệnh xác thực:** Kiểm tra DOM qua Playwright.

---

### Task 6: Hoàn Thiện Điểm Tích Lũy Trên `checkout.html` & `OrderServiceImpl.java`
- **Tệp chỉnh sửa:** 
  - `src/main/java/vn/iotstar/controller/CheckoutController.java`
  - `src/main/java/vn/iotstar/service/impl/OrderServiceImpl.java`
  - `src/main/resources/templates/checkout.html`
- **Mục tiêu:**
  1. Trong `CheckoutController.java`:
     - Lấy `user.getLoyaltyPoints()` thật từ tài khoản đang đăng nhập.
     - Cho phép giảm tối đa theo số điểm user đang có (1 điểm = 1 VNĐ, tối đa 10% giá trị đơn hàng).
     - Nếu user có 0 điểm -> Ẩn hoặc disable checkbox dùng điểm kèm ghi chú *"Bạn hiện có 0 điểm tích lũy"*.
  2. Trong `OrderServiceImpl.java`:
     - Khi đơn hàng hoàn tất (`DELIVERED`): Tự động cộng 1% giá trị đơn hàng vào `user.loyaltyPoints`.
     - Khi đặt hàng có dùng điểm: Trừ số điểm đã dùng khỏi `user.loyaltyPoints`.
- **Lệnh xác thực:** `mvn test`

---

### Task 7: Đồng Bộ Khung Giờ Flash Sale & Modal 5 Cửa Hàng Trên `home.html`
- **Tệp chỉnh sửa:** `src/main/resources/templates/home.html`
- **Mục tiêu:**
  1. **Phiên Flash Sale thời gian thực:**
     - Thiết lập 4 khung giờ vàng mỗi ngày: `09:00 - 12:00`, `12:00 - 15:00`, `18:00 - 21:00`, `21:00 - 24:00`.
     - Đồng hồ đếm ngược tính chính xác đến mốc kết thúc của phiên hiện tại thay vì đếm lùi lặp lại cục bộ.
  2. **Trạng thái chi nhánh trong `#storeModal`:**
     - Duyệt danh sách `stores` trong modal: Hiển thị chấm tròn xanh `🟢 Trực tiếp` hoặc xám `⚪ Vắng mặt` cạnh tên từng chi nhánh để khách biết cửa hàng nào đang mở cửa đón khách.
- **Lệnh xác thực:** `mvn test`

---

## 5. Tests / Validation (TDD Cycle)
1. **Unit Test `StoreFollowerTest.java`:**
   - Test toggle follow: Chưa follow -> Lưu vào DB -> `count = 1`.
   - Test toggle lần 2: Đã follow -> Xóa khỏi DB -> `count = 0`.
2. **Unit Test `UserLoyaltyPointsTest.java`:**
   - Test tích lũy điểm khi đơn hàng chuyển sang `DELIVERED`.
   - Test trừ điểm khi áp dụng điểm tại checkout.
3. **Kiểm thử Visual & Playwright:**
   - Truy cập `/books/1`, kiểm tra thẻ shop hiển thị trạng thái liveness thật `Trực tiếp / Vắng mặt`.
   - Truy cập `/cart`, kiểm tra nút "Tìm tương tự" dẫn đến `/search?cat=...` và icon kiểm định độ mới mở popover.
   - Truy cập `/stores/1`, bấm "Theo Dõi" -> Số follower tăng lên ngay lập tức và giữ nguyên sau khi reload trang.

---

## 6. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi xóa checkbox mock xu ở Checkout, nếu user không đăng nhập thì `loyaltyPoints = 0`.
  - *Giải pháp:* Kiểm tra `sec:authorize="isAuthenticated()"` để hiển thị dòng điểm tích lũy; khách vãng lai (Anonymous) sẽ thấy gợi ý đăng nhập để nhận hoàn điểm 1%.
- **Hiệu năng:** Bảng `store_followers` có unique constraint `(user_id, store_id)` và đánh index giúp truy vấn `exists` và `count` đạt tốc độ < 1ms.
