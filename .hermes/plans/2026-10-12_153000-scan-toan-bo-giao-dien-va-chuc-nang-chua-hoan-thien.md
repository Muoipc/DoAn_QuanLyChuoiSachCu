# Báo Cáo Rà Soát Chi Tiết Toàn Bộ Giao Diện & Kế Hoạch Hoàn Thiện Các Chức Năng Còn Thiếu (Comprehensive Public UI Audit & Completion Plan)

## 1. Goal
Rà soát toàn diện 100% giao diện phía người dùng (Client-facing UI & HTML templates) của hệ thống ReBook; lập danh mục chi tiết các tính năng có hiển thị trên giao diện nhưng chưa có xử lý backend, bị gán dữ liệu cứng (hardcoded/mock) hoặc chỉ là hiệu ứng thị giác (cosmetic); đồng thời xây dựng lộ trình từng bước để hoàn thiện, chuẩn hóa và đưa toàn bộ các tính năng này vào hoạt động thực tế.

---

## 2. Current Context & Assumptions
- **Quy mô khảo sát:** Toàn bộ 25 template giao diện người dùng công khai, bao gồm:
  - Header & Footer toàn cục (`fragments/header.html`, `fragments/footer.html`).
  - Trang chủ (`home.html`), Tìm kiếm & Lọc (`search.html`).
  - Trang chi tiết sách (`book-detail.html`), Cửa hàng & Chi nhánh (`stores.html`, `store-detail.html`).
  - Giỏ hàng (`cart.html`), Thanh toán & Kết quả (`checkout.html`, `checkout-success.html`).
  - Quản lý đơn mua (`order-history.html`, `order-detail.html`).
  - Ký gửi sách (`consignment.html`, `consignment-list.html`).
  - Kho mã giảm giá (`vouchers.html`), Trợ lý AI (`ai-assistant.html`), Trung tâm trợ giúp (`help.html`).
  - Hồ sơ cá nhân (`user/profile.html`, `user/wishlist.html`).
  - Xác thực (`auth/login.html`, `register.html`, `forgot-password.html`, `reset-password.html`, `verify-otp.html`).
- **Kết quả đối soát với Backend:** 170 controller route ánh xạ qua các Controller Spring Boot (`SearchController`, `CartController`, `CheckoutController`, `OrderHistoryController`, `ReviewController`, `WishlistController`, `VoucherController`, `HelpController`, `StoreController`...).

---

## 3. Báo Cáo Rà Soát Chi Tiết: Các Thành Phần UI "Có Nhưng Chưa Hoàn Thiện"

Qua quá trình quét mã nguồn HTML/JS và đối chiếu với Spring Controller, phát hiện **15 điểm tồn đọng** được phân loại thành 4 nhóm:

### Nhóm I: Lỗi Logic Nghiệp Vụ & Nút Bấm Không Chạy (Action Broken / Unhandled)
1. **Lỗi Sắp Xếp Tab Trang Chủ (`home.html`):**
   - *Hiện trạng:* Trên thanh tab feed "Dành cho bạn / Mới nhất / Bán chạy nhất", hai tab sau dẫn link:
     - `th:href="@{/search(sort='newest')}"`
     - `th:href="@{/search(sort='bestseller')}"`
   - *Vấn đề:* `SearchController.java` chỉ tiếp nhận các tham số `sort="latest"` và `sort="sales"`. Khi nhận `newest` hoặc `bestseller`, controller nhảy vào nhánh `else` (sắp xếp theo lượt xem `popular`). Do đó, hai nút lọc này **hoàn toàn không sắp xếp đúng** như tiêu đề người dùng bấm.
2. **Nút "Mua Lại" (Re-order) Không Hoạt Động (`order-history.html`, `order-detail.html`):**
   - *Hiện trạng:* Trên lịch sử đơn hàng, nút "Mua Lại" dẫn về `@{/}` (trang chủ); trên chi tiết đơn hàng dẫn về `@{/cart}` (giỏ hàng).
   - *Vấn đề:* Không có bất kỳ đoạn mã nào tự động thêm lại các cuốn sách từ đơn hàng cũ vào giỏ hàng. Người dùng bấm nút chỉ bị chuyển trang mà giỏ hàng vẫn trống trơn.
3. **Nút "Yêu Cầu Trả Hàng / Hoàn Tiền" Không Có Quy Trình (`order-detail.html`):**
   - *Hiện trạng:* Nút bấm mang nhãn `<i class="fa-solid fa-rotate-left"></i> Yêu Cầu Trả Hàng / Hoàn Tiền` dẫn thẳng link về `@{/help}` (trang trợ giúp).
   - *Vấn đề:* Không có modal chọn lý do trả hàng (sách rách, giao sai chi nhánh, không đúng mô tả), không có bảng lưu yêu cầu đổi trả để Store Manager duyệt.
4. **Các Nút Chia Sẻ Mạng Xã Hội Bị Bỏ Quên (`book-detail.html`):**
   - *Hiện trạng:* Hàng nút chia sẻ hiển thị 4 icon: Facebook, Messenger, Pinterest, X/Twitter.
   - *Vấn đề:* Trong JavaScript `shareSocial(network)`, chỉ có điều kiện `if (network === 'facebook')` mở cửa sổ share. Ba nút Messenger, Pinterest, Twitter hoàn toàn không có code xử lý, người dùng bấm vào không có phản ứng gì.
5. **Form Gửi Yêu Cầu Trợ Giúp Không Lưu Vào CSDL (`help.html`):**
   - *Hiện trạng:* Khối "Gửi Phiếu Yêu Cầu Hỗ Trợ Trực Tuyến" có form đầy đủ (Họ tên, Email, SĐT, Mã đơn, Thể loại, Nội dung) gửi `POST /help/contact`.
   - *Vấn đề:* `HelpController.submitSupportTicket()` chỉ gắn một dòng thông báo Flash Message `successMessage` rồi redirect, hoàn toàn không lưu vào bảng nào trong cơ sở dữ liệu, Admin/Manager không hề nhận được ticket này.

---

### Nhóm II: Dữ Liệu Tĩnh Giả Lập Cứng (Hardcoded Mock Data)
6. **Trung Tâm Thông Báo Header Giả Lập (`fragments/header.html`):**
   - *Hiện trạng:* Chuông thông báo có badge cố định số `5`. Popover chứa 3 thông báo tĩnh viết cứng (Mã giảm 50k, đơn hàng mẫu `#ORD259683735`, giáo trình CTDL).
   - *Vấn đề:* Dù tài khoản chưa từng mua hàng hay vừa đăng ký mới, chuông luôn báo có 5 thông báo và hiện mã đơn hàng `#ORD259683735` của người khác. Nút "Xem tất cả" dẫn về `/help` thay vì trang thông báo thực.
7. **"Điểm Tích Lũy Sách Cũ" (Shopee Coins) Giả Lập (`cart.html`):**
   - *Hiện trạng:* Dòng checkbox "Điểm Tích Lũy Sách Cũ (Dùng 2.000 Điểm tiết kiệm ₫2.000)" cho phép tích chọn để trừ 2.000₫.
   - *Vấn đề:* Đây là đoạn mã JS tính nhẩm phía client. Hệ thống backend hoàn toàn không có thực thể `UserPoints` hay bảng điểm tích lũy, số tiền này cũng không được trừ khi chuyển sang trang Thanh toán (`checkout.html`).
8. **Mục "Có Thể Bạn Cũng Thích" Bị Cố Định 5 Cuốn Sách Cũ (`cart.html`):**
   - *Hiện trạng:* 5 thẻ sách đề xuất dưới giỏ hàng được gán cứng HTML tĩnh (`/books/2`, `/books/3`, `/books/4`, `/books/5`, `/books/6`) kèm giá và ảnh tĩnh.
   - *Vấn đề:* Nếu cơ sở dữ liệu xóa các cuốn này hoặc cập nhật giá mới, phần đề xuất này sẽ bị link hỏng hoặc hiển thị sai giá. Cần nạp động từ `bookRepository.findTopSoldWithImages()`.
9. **Ngày Giao Hàng Dự Kiến Bị Cố Định Cả Năm (`checkout.html`):**
   - *Hiện trạng:* Dòng thông báo vận chuyển hiển thị cứng: `Nhận hàng dự kiến: 26 Th09 - 28 Th09`.
   - *Vấn đề:* Dù khách hàng mua vào tháng 10, tháng 11 hay năm sau, dòng chữ vẫn hiển thị ngày cố định của tháng 9 trong quá khứ.
10. **Không Cho Chọn Đơn Vị Vận Chuyển Tại Trang Thanh Toán (`checkout.html`):**
    - *Hiện trạng:* Phương thức vận chuyển bị khóa cứng là "Nhanh" (ID 1, phí ₫0).
    - *Vấn đề:* Phân hệ Admin đã có đầy đủ quản lý Đơn Vị Vận Chuyển (`/admin/shipping-units` với Viettel Post, Giao Hàng Nhanh, Hỏa Tốc...), nhưng giao diện Checkout phía người dùng lại không render danh sách đơn vị vận chuyển để khách chọn lựa.
11. **Chỉ Số Thống Kê Chi Nhánh Cửa Hàng Bị Gán Cứng (`book-detail.html`, `store-detail.html`):**
    - *Hiện trạng:* Box thông tin cửa hàng hiển thị: `Người Theo Dõi: 12.8k`, `Đánh Giá: 4.9k`, `Tỉ Lệ Phản Hồi: 98%`, `Tham Gia: 2 năm trước`, `Sản Phẩm: 120`.
    - *Vấn đề:* Toàn bộ các con số này là text cứng, không lấy từ dữ liệu thực của từng chi nhánh trong database.

---

### Nhóm III: Thao Tác Ảo Phía Client (Cosmetic Only / No Persistence)
12. **Nút "Theo Dõi Cửa Hàng" (Follow Shop) Bấm Xong F5 Là Mất (`store-detail.html`):**
    - *Hiện trạng:* Khách hàng bấm nút "Theo Dõi", nút đổi icon và đổi chữ thành "Đã Theo Dõi" kèm thông báo.
    - *Vấn đề:* Hàm JavaScript chỉ sửa `classList`, không hề gọi API lưu quan hệ Follow vào CSDL. F5 tải lại trang là trạng thái mất hoàn toàn.
13. **Chức Năng Đa Ngôn Ngữ Bị Nửa Vời (`static/js/i18n.js`):**
    - *Hiện trạng:* Nút chọn Tiếng Anh / Tiếng Việt chỉ có ~30 từ khóa tĩnh trong JavaScript.
    - *Vấn đề:* Khi chọn Tiếng Anh, chỉ đổi được vài chữ tiêu đề ngoài header. Toàn bộ sách, danh mục, mô tả, thông báo và các trang con vẫn 100% tiếng Việt, tạo cảm giác tính năng dang dở.

---

### Nhóm IV: Lệch Phong Cách Thiết Kế Chưa Đồng Bộ
14. **Thẻ Sách Flash Sale Trang Chủ Còn Dùng Kiểu Cũ (`home.html`):**
    - *Hiện trạng:* 5 thẻ sách trong khối Flash Sale (`.flash-book-card`) vẫn còn viền kính mờ, badge vàng/xanh neon che góc ảnh, chưa được tối giản hóa đồng bộ với thẻ sách mới (`shopee-product-card`, `book-card`, `shelf-book-card`).
15. **Nút "Xem Thêm" Chân Trang Chủ Bị Đổi Thành Link (`home.html`):**
    - *Hiện trạng:* Nút "Xem Thêm" cuối trang chủ thực chất là thẻ `<a>` chuyển hướng sang `/search`.
    - *Vấn đề:* Trải nghiệm cuộn trang hiện đại nên tải thêm 12-18 cuốn sách kế tiếp bằng AJAX thay vì bắt khách chuyển trang.

---

## 4. Kế Hoạch Triển Khai Hoàn Thiện Từng Bước (Implementation Roadmap)

### Giai Đoạn 1: Sửa Triệt Để Các Lỗi Nghiệp Vụ & Nút Bấm Không Hoạt Động (Ưu tiên cao nhất)

#### Task 1.1: Sửa Lỗi Sắp Xếp Tab Trang Chủ (`home.html` & `SearchController.java`)
- **Tệp chỉnh sửa:**
  - `src/main/resources/templates/home.html`: Sửa link tab `newest` -> `latest` và `bestseller` -> `sales`.
  - `src/main/java/vn/iotstar/controller/SearchController.java`: Bổ sung chấp nhận các alias: `newest` (tương đương `latest`), `bestseller` (tương đương `sales`).
- **Code thay đổi (`SearchController.java`):**
```java
if ("sales".equalsIgnoreCase(sort) || "bestseller".equalsIgnoreCase(sort)) {
    // Sắp xếp theo số lượng đã bán
} else if ("latest".equalsIgnoreCase(sort) || "newest".equalsIgnoreCase(sort)) {
    // Sắp xếp theo ngày tạo mới nhất
}
```

#### Task 1.2: Triển Khai Tính Năng "Mua Lại" Thực Tế (`OrderHistoryController.java`, `order-history.html`, `order-detail.html`)
- **Tệp chỉnh sửa:**
  - `src/main/java/vn/iotstar/controller/OrderHistoryController.java`: Bổ sung endpoint `POST /orders/reorder/{orderCode}` lặp qua danh sách `OrderItem` của đơn cũ và thêm lại vào giỏ hàng (`cartItemService.addToCart()`).
  - `templates/order-history.html` & `templates/order-detail.html`: Đổi thẻ link thành form/nút bấm submit `POST /orders/reorder/{orderCode}` kèm thông báo chuyển thẳng vào giỏ hàng.

#### Task 1.3: Xây Dựng Quy Trình "Yêu Cầu Trả Hàng / Hoàn Tiền" (`order-detail.html` & `OrderHistoryController.java`)
- **Tệp chỉnh sửa:**
  - `src/main/java/vn/iotstar/controller/OrderHistoryController.java`: Bổ sung endpoint `POST /orders/request-return/{orderCode}` nhận lý do trả hàng và ảnh chụp bằng chứng, cập nhật trạng thái đơn sang `RETURN_REQUESTED` kèm ghi chú khách hàng.
  - `templates/order-detail.html`: Bổ sung modal thân thiện cho phép khách chọn lý do (Sách không đúng tình trạng cam kết, Hư hỏng gáy sách, Giao nhầm tựa sách) và gửi yêu cầu.

#### Task 1.4: Hoàn Thiện Đầy Đủ Các Kênh Chia Sẻ Mạng Xã Hội (`book-detail.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/book-detail.html`
- **Code bổ sung trong hàm `shareSocial(network)`:**
```javascript
if (network === 'facebook') {
    window.open('https://www.facebook.com/sharer/sharer.php?u=' + encodeURIComponent(url), 'fbShare', 'width=640,height=480');
} else if (network === 'twitter') {
    window.open('https://twitter.com/intent/tweet?url=' + encodeURIComponent(url) + '&text=' + encodeURIComponent(title), 'twShare', 'width=640,height=480');
} else if (network === 'pinterest') {
    window.open('https://pinterest.com/pin/create/button/?url=' + encodeURIComponent(url) + '&media=' + encodeURIComponent(img) + '&description=' + encodeURIComponent(title), 'pinShare', 'width=750,height=550');
} else if (network === 'messenger') {
    window.open('fb-messenger://share/?link=' + encodeURIComponent(url), '_blank');
}
```

#### Task 1.5: Lưu Phiếu Hỗ Trợ Vào Database / Gửi Thông Báo Cho Admin (`HelpController.java`)
- **Tệp chỉnh sửa:**
  - Tạo entity `SupportTicket.java` hoặc lưu vào bảng tin nhắn hỗ trợ của hệ thống.
  - Cập nhật `HelpController.java`: Lưu thông tin khách hàng và nội dung cần giúp đỡ vào cơ sở dữ liệu để Admin có thể xem và phản hồi tại phân hệ quản trị.

---

### Giai Đoạn 2: Thay Thế Dữ Liệu Tĩnh Bằng Dữ Liệu Thực & Cải Tiến Trải Nghiệm Mua Sắm

#### Task 2.1: Đồng Bộ Hóa Đơn Vị Vận Chuyển Thực Tế Tại Checkout (`checkout.html` & `CheckoutController.java`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/checkout.html`
- **Mục tiêu:**
  - Thay khung vận chuyển cố định 1 dòng bằng danh sách các đơn vị vận chuyển (`th:each="unit : ${shippingUnits}"`).
  - Tính ngày giao hàng dự kiến động theo ngày hiện tại: `Hôm nay + unit.estimatedDays` (ví dụ: đặt hàng ngày 12/10 -> dự kiến giao 14 Th10 - 15 Th10).
  - Tự động cộng phí vận chuyển của đơn vị được chọn vào tổng tiền thanh toán.

#### Task 2.2: Thay Thẻ Sách Đề Xuất Giỏ Hàng Bằng Dữ Liệu Động (`CartController.java` & `cart.html`)
- **Tệp chỉnh sửa:**
  - `src/main/java/vn/iotstar/controller/CartController.java`: Nạp danh sách `recommendedBooks` (Top sách bán chạy hoặc sách cùng thể loại với sách trong giỏ) vào model.
  - `templates/cart.html`: Thay thế 5 thẻ tĩnh bằng vòng lặp `th:each="book : ${recommendedBooks}"` dùng chung class thẻ sách tối giản hoài cổ.

#### Task 2.3: Xử Lý Trung Tâm Thông Báo Header Thực Tế (`header.html` & `NotificationController.java`)
- **Tệp chỉnh sửa:**
  - Ẩn badge `5` cứng nếu người dùng chưa đăng nhập.
  - Khi đã đăng nhập: Nếu user có đơn hàng gần đây, hiển thị thông báo trạng thái đơn hàng thực tế của user (Đang chuẩn bị, Đang giao, Đã giao); nếu không có thì hiển thị lời chào mừng và các mã voucher hiện có.

#### Task 2.4: Loại Bỏ / Ẩn Dòng "Điểm Tích Lũy" Ảo Khỏi Giỏ Hàng (`cart.html`)
- **Tệp chỉnh sửa:** `templates/cart.html`
- **Mục tiêu:** Bỏ dòng `Điểm Tích Lũy Sách Cũ (-2.000₫)` ảo để tránh gây hiểu lầm cho khách hàng khi số tiền giảm không được tính thật vào đơn hàng.

#### Task 2.5: Đồng Bộ Thiết Kế 5 Thẻ Sách Flash Sale Trang Chủ (`home.html`)
- **Tệp chỉnh sửa:** `templates/home.html`
- **Mục tiêu:** Tái thiết kế `.flash-book-card` theo chuẩn tối giản hoài cổ ReBook: gỡ bỏ viền kính mờ đục và badge neon chói, dùng pill mờ kính nhẹ nhàng và font số giá nâu da bò `#8C4A27`.

---

## 5. Tests / Validation Strategy
- **Kiểm thử Unit Test (TDD):**
  - Viết test kiểm tra `SearchControllerTest`: đảm bảo cả `sort=latest`, `sort=newest`, `sort=sales`, `sort=bestseller` đều trả về đúng thứ tự danh sách.
  - Viết test `OrderReorderTest`: đảm bảo khi gọi endpoint mua lại, toàn bộ sách trong đơn cũ được thêm đầy đủ vào giỏ hàng của user.
- **Kiểm thử Trực Quan & Thao Tác (Playwright):**
  - Kịch bản 1: Mở trang chủ, bấm tab "Mới nhất" và "Bán chạy nhất" -> xác minh kết quả sắp xếp chuẩn xác.
  - Kịch bản 2: Vào chi tiết đơn hàng cũ, bấm "Mua Lại" -> xác minh giỏ hàng nhận đủ sách và chuyển trang mượt mà.
  - Kịch bản 3: Vào trang Checkout -> xác minh ngày nhận hàng dự kiến hiển thị đúng theo ngày thực tế (không còn tháng 9 cũ), đổi đơn vị vận chuyển cập nhật phí ship lập tức.
  - Kịch bản 4: Bấm chia sẻ Twitter/Pinterest tại trang chi tiết sách -> xác minh mở đúng cửa sổ popup chia sẻ.

---

## 6. Risks, Tradeoffs & Open Questions
- **Đánh đổi:** Việc bỏ dòng "Điểm Tích Lũy" ảo trong giỏ hàng sẽ làm giao diện bớt giống sàn Shopee một chút, nhưng đổi lại tính minh bạch và uy tín của ReBook tăng 100%, không bị khách hàng thắc mắc tại sao tick chọn trừ 2k mà thanh toán không thấy trừ.
- **Câu hỏi mở:** Khách hàng có muốn phát triển tính năng tích điểm thực tế (mua 100k được 2k điểm) trong CSDL luôn không? (Nếu muốn, có thể bổ sung cột `points` vào bảng `users` trong pha tiếp theo).
