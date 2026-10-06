# ĐỒ ÁN CUỐI KỲ MÔN LẬP TRÌNH WEB (WEBPR330479)
## TRƯỜNG ĐẠI HỌC SƯ PHẠM KỸ THUẬT TP. HỒ CHÍ MINH (HCMUTE)
### KHOA CÔNG NGHỆ THÔNG TIN - BỘ MÔN CÔNG NGHỆ PHẦN MỀM

---

## ĐỀ TÀI: XÂY DỰNG WEBSITE QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ (OLD BOOK STORE)
- **Giảng viên hướng dẫn:** ThS. Nguyễn Hữu Trung  
- **Thời gian báo cáo:** Học kỳ 1, Năm học 2026 - 2027 (Ngày 30/10/2026 tại Phòng A5-203)
- **Repository GitHub:** [https://github.com/Muoipc/DoAn_QuanLyChuoiSachCu](https://github.com/Muoipc/DoAn_QuanLyChuoiSachCu)

---

## 1. THÔNG TIN THÀNH VIÊN VÀ PHÂN CÔNG NHIỆM VỤ

| STT | Họ và tên | MSSV | Vai trò chính đảm nhận | Phân hệ chức năng phân công chi tiết |
| :---: | :--- | :---: | :--- | :--- |
| 1 | **Thái Duy Cường** | **24162013** | **Quản trị viên (Admin) & Quản lý chi nhánh (Store Manager)** | **1. Phân hệ Admin (Quản trị hệ thống):**<br>- Quản lý và tìm kiếm User, phân quyền tài khoản (Admin, Manager, Shipper, User).<br>- Quản lý chuỗi chi nhánh cửa hàng (Store CRUD, ảnh Cloudinary, thông tin liên hệ).<br>- Cấu hình chiết khấu sàn / app cho từng shop/chi nhánh (`commission_rate`).<br>- Quản lý danh mục thể loại sách toàn sàn (Category CRUD).<br>- Quản lý sản phẩm sách cũ của từng shop / chi nhánh.<br>- Quản lý khuyến mãi Voucher: giảm % sản phẩm, giảm tiền cố định, giảm phí vận chuyển (Freeship).<br>- Quản lý đơn vị vận chuyển: tên nhà vận chuyển, phí vận chuyển cơ bản (`baseFee`), trạng thái hoạt động.<br>- Báo cáo thống kê doanh thu toàn hệ thống, biểu đồ Chart.js, xuất file Excel/PDF.<br>- Thông báo đơn hàng thời gian thực qua WebSocket (STOMP).<br><br>**2. Phân hệ Manager (Quản lý cửa hàng / chi nhánh):**<br>- Quản lý từng cửa hàng được chỉ định phụ trách.<br>- Quản lý sản phẩm sách cũ tại kho của từng shop.<br>- Kiểm soát tồn kho chi nhánh (`Inventory`) và điều chuyển sách giữa các kho (`Stock Transfer`).<br>- Tìm kiếm và quản lý người dùng thuộc chi nhánh.<br>- Xử lý đơn hàng của shop (Duyệt đơn, Đóng gói, Điều phối giao hàng).<br>- Thẩm định sách cũ ký gửi từ khách hàng (định giá, duyệt vào kho bán). |
| 2 | **Nguyễn Song Hoàng Phúc** | **--** | **Khách (Guest), Khách hàng (User), Người bán (Vendor/Seller) & Shipper** | **1. Chức năng chung hệ thống:**<br>- Tìm kiếm và lọc sản phẩm đa tiêu chí.<br>- Đăng ký tài khoản gửi mã OTP qua Email, Đăng nhập Spring Security, Quên mật khẩu gửi OTP Email, mã hoá mật khẩu BCrypt.<br><br>**2. Phân hệ Khách vãng lai (Guest):**<br>- Giao diện Trang chủ hiển thị các sản phẩm bán trên 10 cuốn của các shop (sắp xếp giảm dần theo lượt bán/độ hot).<br>- Khám phá chi tiết sách cũ, xem tình trạng sách.<br><br>**3. Phân hệ Khách hàng thành viên (User):**<br>- Trang chủ & Trang sản phẩm theo danh mục.<br>- Phân loại 20 sản phẩm mới nhất, bán chạy, đánh giá cao, yêu thích nhất (phân trang / lazy loading).<br>- Trang cá nhân (Profile): Sổ đa địa chỉ nhận hàng linh hoạt.<br>- Giỏ hàng lưu trữ bền vững trên Database (`carts`, `cart_items`).<br>- Thanh toán tích hợp VNPAY Sandbox & COD.<br>- Lịch sử đơn hàng đa trạng thái: Đơn mới, Đã xác nhận, Đang giao, Đã giao, Hủy, Trả hàng - Hoàn tiền.<br>- Yêu thích sách (Wishlist), Sách đã xem gần đây (Recently Viewed).<br>- Đánh giá và bình luận sản phẩm đã mua (text $\ge$ 50 ký tự, đính kèm ảnh/video qua Cloudinary).<br>- Áp dụng mã giảm giá (Voucher) vào giỏ hàng và checkout.<br><br>**4. Phân hệ Người bán / Ký gửi (Vendor/Seller):**<br>- Toàn bộ quyền của User.<br>- Đăng ký mở shop / gian hàng sách.<br>- Quản lý trang chủ shop, quản lý sản phẩm của mình.<br>- Quản lý đơn hàng shop đa trạng thái.<br>- Tạo chương trình khuyến mãi riêng của shop.<br>- Thống kê & quản lý doanh thu shop.<br><br>**5. Phân hệ Giao hàng (Shipper - Phần mở rộng):**<br>- Quản lý danh sách đơn hàng được phân công đi giao.<br>- Cập nhật tiến độ giao nhận (Đã lấy hàng, Đang giao, Giao thành công, Giao thất bại).<br>- Báo cáo và thống kê số lượng đơn hàng đã giao, tiền thu hộ COD. |

---

## 2. NỘI DUNG ĐỀ TÀI VÀ YÊU CẦU ĐẶC TẢ HỆ THỐNG

### a. Khảo sát hiện trạng và phân tích thiết kế hệ thống
- **Tài liệu đặc tả khảo sát & phân tích thiết kế:**  
  [https://utexlms.hcmute.edu.vn/pluginfile.php/1976872/mod_page/content/17/KhaoSat_PhanTichHeThong.docx](https://utexlms.hcmute.edu.vn/pluginfile.php/1976872/mod_page/content/17/KhaoSat_PhanTichHeThong.docx)
- **Mô hình kiến trúc tổng thể:**
  - Hệ thống sàn thương mại điện tử kết hợp chuỗi cửa hàng sách cũ truyền thống (Mô hình O2O: Online-to-Offline).
  - Khách hàng có thể đặt mua online giao hàng tận nhà (Home Delivery) qua các đơn vị vận chuyển hoặc đặt giữ chỗ nhận trực tiếp tại chi nhánh (Click & Collect).
  - Tích hợp mạng lưới kho hàng phân tán giữa các chi nhánh tại TP. Hồ Chí Minh (Thủ Đức, Quận 1, Bình Thạnh, Gò Vấp, Quận 5).
  - Tích hợp mô hình ký gửi - thẩm định sách cũ: Khách hàng có thể ký gửi sách cũ cho hệ thống, Store Manager thẩm định chất lượng/định giá và đưa vào kho kinh doanh.

---

### b. Đặc tả chi tiết các phân hệ chức năng theo vai trò

#### 1. Chức năng chung (Hạ tầng hệ thống):
- **Tìm kiếm & Lọc sản phẩm:** Tìm theo tên sách, tác giả, nhà xuất bản, lọc theo danh mục, khoảng giá, chi nhánh còn hàng và độ mới (từ 80% đến 99%).
- **Xác thực OTP Email:** Đăng ký tài khoản gửi mã OTP 6 chữ số có hiệu lực 5 phút qua SMTP Gmail.
- **Đăng nhập & Đăng xuất:** Bảo mật với Spring Security, hỗ trợ tự động ghi nhớ `redirectURL` sau khi đăng nhập.
- **Quên mật khẩu:** Xác thực quyền sở hữu tài khoản qua mã OTP Email trước khi cho phép đặt lại mật khẩu mới.
- **Bảo mật mật khẩu:** Toàn bộ mật khẩu người dùng được mã hóa một chiều an toàn bằng thuật toán **BCrypt** (`BCryptPasswordEncoder`).

#### 2. Phân hệ Khách vãng lai (Guest) - Đảm nhận: Nguyễn Song Hoàng Phúc:
- Giao diện Trang chủ: Hiển thị nổi bật danh sách sản phẩm bán trên 10 sản phẩm của các shop/chi nhánh, sắp xếp theo thứ tự từ lớn đến nhỏ (`totalSold` giảm dần).
- Tra cứu kho sách cũ, xem chi tiết đánh giá sao, nhận xét của độc giả.
- Hỗ trợ xem thông tin các chi nhánh chuỗi cửa hàng sách cũ.

#### 3. Phân hệ Người dùng đã đăng nhập (User) - Đảm nhận: Nguyễn Song Hoàng Phúc:
- **Giao diện trang chủ & Trang danh mục:** Danh mục sách phong phú, giao diện chuẩn Bootstrap 5 hiện đại.
- **Top 20 sản phẩm theo các tiêu chí:** 20 sản phẩm mới nhất, bán chạy nhất, đánh giá cao nhất, yêu thích nhất có phân trang hoặc lazy loading.
- **Trang cá nhân (Profile):** Quản lý thông tin cá nhân và sổ địa chỉ nhận hàng linh hoạt (hỗ trợ nhiều địa chỉ nhận hàng khác nhau cho việc giao nhận).
- **Trang chi tiết sản phẩm:** Đầy đủ thông tin ấn bản, bộ ảnh chụp các góc cạnh của sách cũ (bìa, gáy, trang sách) lưu trên Cloudinary, tỷ lệ độ mới %.
- **Giỏ hàng lưu trữ Database:** Giỏ hàng đồng bộ trực tiếp vào cơ sở dữ liệu (`carts` và `cart_items`), không bị mất khi đổi trình duyệt hoặc thiết bị.
- **Thanh toán đa phương thức:** Tích hợp thanh toán khi nhận hàng (COD) và Cổng thanh toán trực tuyến **VNPAY Sandbox** an toàn.
- **Quản lý lịch sử đơn hàng theo trạng thái:** Theo dõi trực quan theo 6 trạng thái: *Đơn hàng mới, Đã xác nhận, Đang giao, Đã giao, Hủy đơn, Trả hàng - hoàn tiền*.
- **Tương tác sản phẩm:** Thích sản phẩm (Wishlist), Lịch sử sách đã xem gần đây (Recently Viewed).
- **Đánh giá & Bình luận sản phẩm:** Chỉ những khách hàng đã mua sách mới được đánh giá sao, viết nhận xét với độ dài tối thiểu 50 ký tự, đính kèm hình ảnh/video thực tế qua Cloudinary.
- **Mã giảm giá (Vouchers):** Xem kho voucher, lưu voucher vào ví cá nhân và áp dụng mã giảm giá khi tiến hành thanh toán.

#### 4. Phân hệ Người bán / Ký gửi (Vendor / Seller) - Đảm nhận: Nguyễn Song Hoàng Phúc:
- Kế thừa toàn bộ quyền hạn của User.
- **Đăng ký gian hàng/shop:** Khách hàng có thể đăng ký tài khoản Seller để kinh doanh hoặc gửi yêu cầu ký gửi sách cũ.
- **Quản lý trang chủ shop:** Tùy biến thông tin cửa hàng, logo, banner, mô tả giới thiệu.
- **Quản lý sản phẩm của mình:** Thêm mới, chỉnh sửa thông tin sách cũ, cập nhật hình ảnh và giá bán.
- **Quản lý đơn hàng shop:** Xử lý đơn hàng của shop qua các trạng thái: Đơn mới, Đã xác nhận, Đã lấy hàng, Đang giao, Đã giao, Hủy, Trả hàng - hoàn tiền.
- **Tạo chương trình khuyến mãi:** Thiết lập mã voucher giảm giá riêng cho các sản phẩm của shop.
- **Quản lý doanh thu shop:** Báo cáo doanh số bán hàng, tiền chiết khấu app và doanh thu thực nhận.

#### 5. Phân hệ Quản lý chi nhánh (Store Manager) - Đảm nhận: Thái Duy Cường:
- **Quản lý từng cửa hàng được phân công:**
  - **Quản lý người dùng:** Tra cứu, tìm kiếm danh sách user, hỗ trợ xem thông tin khách hàng và kích hoạt/khóa tài khoản.
  - **Quản lý sản phẩm của từng shop:** Thêm mới sách cũ vào chi nhánh, cập nhật thông tin độ mới, tác giả, nhà xuất bản, mô tả chi tiết.
  - **Quản lý tồn kho & Điều chuyển kho:** Theo dõi số lượng tồn kho từng đầu sách tại chi nhánh (`Inventory`), tạo phiếu điều chuyển sách giữa các kho chi nhánh (`Stock Transfer`).
  - **Quản lý danh mục:** Quản lý cây danh mục thể loại sách.
  - **Quản lý chiết khấu app cho các shop:** Quản lý tỷ lệ chiết khấu app/sàn (`commission_rate`) áp dụng cho chi nhánh hoặc các shop thành viên.
  - **Quản lý chương trình khuyến mãi (Voucher):**
    + Giảm theo phần trăm sản phẩm (`PERCENT`): VD giảm 10%, 15%, 20% (có chặn mức giảm tối đa `maxDiscount`).
    + Giảm số tiền cố định (`FIXED_AMOUNT`): VD giảm 20.000₫, 50.000₫.
    + Giảm phí vận chuyển (`FREE_SHIPPING`): Miễn phí ship toàn quốc hoặc giảm trừ tiền ship.
  - **Quản lý nhà vận chuyển:** Quản lý danh sách đối tác giao nhận hàng (Giao Hàng Nhanh, Viettel Post, Shopee Xpress...), thiết lập phí vận chuyển cơ bản (`baseFee`) và thời gian giao dự kiến.
  - **Xử lý đơn hàng chi nhánh:** Duyệt đơn hàng của shop, chuẩn bị đóng gói sách cũ, điều phối đơn cho shipper giao hàng hoặc lưu giữ tại quầy Click & Collect.
  - **Thẩm định & Thu mua sách cũ ký gửi:** Xem xét các yêu cầu ký gửi từ khách hàng, định giá sách cũ và chuyển thành hàng bán trong kho.

#### 6. Phân hệ Quản trị viên tối cao (Admin) - Đảm nhận: Thái Duy Cường:
- **Quản trị toàn diện chuỗi hệ thống:**
  - **Tìm kiếm và Quản lý người dùng, Phân quyền:** Tra cứu tìm kiếm user đa tiêu chí, phân quyền hạn tài khoản theo các vai trò: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_SHIPPER`, `ROLE_USER`. Khóa hoặc mở khóa tài khoản.
  - **Quản lý chuỗi chi nhánh cửa hàng (Stores):** Quản lý toàn bộ 5 chi nhánh tại TP.HCM, thêm chi nhánh mới, cập nhật địa chỉ, hotline, thời gian mở cửa, ảnh mặt tiền Cloudinary, bổ nhiệm Store Manager phụ trách.
  - **Quản lý sản phẩm của toàn bộ các shop:** Kiểm duyệt sách đăng tải, quản lý kho tổng, kiểm soát tình trạng sách toàn chuỗi.
  - **Quản lý danh mục thể loại (Categories):** Thêm, sửa, xóa, kích hoạt danh mục cha - con.
  - **Cấu hình chiết khấu app cho các shop:** Thiết lập chính sách phần trăm hoa hồng / chiết khấu app áp dụng trên doanh số của các cửa hàng thành viên.
  - **Quản lý chiến dịch khuyến mãi toàn hệ thống:** Tạo và kích hoạt voucher giảm % sản phẩm, giảm tiền trực tiếp, miễn phí vận chuyển trên toàn sàn.
  - **Quản lý nhà vận chuyển:** Quản lý và cấu hình các đối tác vận chuyển toàn sàn.
  - **Báo cáo & Thống kê doanh thu chuyên sâu:** Biểu đồ tương tác thời gian thực bằng Chart.js (doanh thu theo ngày/tháng, doanh thu theo từng chi nhánh, tỷ trọng đơn hàng, top sách bán chạy nhất), hỗ trợ xuất báo cáo định dạng Excel và PDF.
  - **Hệ thống WebSocket (STOMP):** Tự động phát âm thanh và hiển thị thông báo popup thời gian thực khi có đơn hàng mới phát sinh trong chuỗi.

#### 7. Phân hệ Nhân viên giao hàng (Shipper - Phần mở rộng) - Đảm nhận: Nguyễn Song Hoàng Phúc:
- **Quản lý đơn hàng được phân công đi giao:** Xem danh sách đơn hàng được Store Manager gán quyền vận chuyển, xem địa chỉ chi tiết và số điện thoại người nhận.
- **Cập nhật tiến trình giao hàng:** Chuyển trạng thái đơn hàng (Đã lấy hàng từ shop $\rightarrow$ Đang đi giao $\rightarrow$ Giao thành công / Giao thất bại / Hoàn hàng).
- **Thống kê đơn hàng được phân công:** Báo cáo số đơn hàng đã hoàn thành, thống kê tổng số tiền mặt thu hộ (COD) cần bàn giao lại cho cửa hàng.

---

## 3. CÔNG NGHỆ VÀ GIAO DIỆN HỆ THỐNG

- **Backend:** Spring Boot 3.4.x, Java 21, Spring Data JPA (Hibernate), Spring Security, Spring Mail (SMTP OTP).
- **Frontend:** HTML5, CSS3, Thymeleaf, Thymeleaf Layout Dialect, **Bootstrap 5** (áp dụng đồng bộ 01 template chủ đạo phong cách Apple Liquid Glass kết hợp Wood & Vintage Bookshelf ấm áp), FontAwesome 6, jQuery AJAX, Chart.js.
- **Cơ sở dữ liệu:** MySQL 8.x với utf8mb4 full unicode.
- **Lưu trữ đám mây (Cloud Storage):** Cloudinary API (quản lý ảnh bìa sách, ảnh chụp chi tiết tình trạng sách cũ, ảnh mặt tiền chi nhánh và ảnh/video đánh giá).
- **Thời gian thực (Real-time):** Spring WebSocket kết hợp giao thức STOMP phục vụ thông báo đơn hàng trực tiếp tới Admin & Manager.
- **Cổng thanh toán:** VNPay Payment Gateway Sandbox (hỗ trợ thuật toán mã hoá HMAC-SHA512).
- **Trí tuệ nhân tạo (AI Assistant):** Tích hợp Gemini API hỗ trợ tư vấn chọn sách cũ theo tâm trạng và sở thích.

---

## 4. HƯỚNG DẪN CÀI ĐẶT & CHẠY ỨNG DỤNG (LOCAL)

### 1. Chuẩn bị môi trường:
- **Java Development Kit (JDK):** Phiên bản JDK 21 trở lên.
- **Apache Maven:** Phiên bản 3.9.x trở lên.
- **MySQL Server:** Phiên bản 8.0 trở lên.

### 2. Thiết lập Cơ sở dữ liệu:
Mở MySQL Workbench hoặc phpMyAdmin, chạy lệnh tạo database:
```sql
CREATE DATABASE old_book_store_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Cấu hình biến môi trường và chạy ứng dụng:
Mở PowerShell tại thư mục dự án và khởi chạy với mật khẩu MySQL của bạn:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:DB_PASS = "mat_khau_mysql_cua_ban"
& "D:\Study\MAVEN\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
```

Ứng dụng khởi chạy thành công tại: [http://localhost:8080/](http://localhost:8080/)

---

## 5. TÀI KHOẢN VÀ CÁC ĐƯỜNG DẪN DEMO CHÍNH

### Tài khoản thử nghiệm có sẵn trong hệ thống:
| Vai trò | Tên đăng nhập | Mật khẩu | Phân quyền truy cập |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `admin123` | Toàn quyền phân hệ Admin & Quản lý |
| **Quản lý chi nhánh (Manager)** | `manager` | `manager123` | Quản lý kho, sách và đơn hàng chi nhánh |
| **Nhân viên giao hàng (Shipper)** | `shipper` | `shipper123` | Nhận đơn và cập nhật trạng thái giao hàng |
| **Khách hàng (User)** | `user` | `user123` | Mua sách, giỏ hàng, thanh toán, đánh giá |

### Các đường dẫn demo phân hệ của Thái Duy Cường (Admin & Store Manager):
- **Quản lý Sách cũ toàn sàn & chi nhánh:** [http://localhost:8080/admin/books](http://localhost:8080/admin/books)
- **Tồn kho sách từng chi nhánh:** [http://localhost:8080/admin/inventory](http://localhost:8080/admin/inventory)
- **Điều chuyển sách giữa các kho chi nhánh:** [http://localhost:8080/admin/inventory/transfer](http://localhost:8080/admin/inventory/transfer)
- **Quản lý chuỗi chi nhánh & Chiết khấu App:** [http://localhost:8080/admin/stores](http://localhost:8080/admin/stores)
- **Quản lý danh mục thể loại sách:** [http://localhost:8080/admin/categories](http://localhost:8080/admin/categories)
- **Quản lý khuyến mãi (% sản phẩm, freeship):** [http://localhost:8080/admin/vouchers](http://localhost:8080/admin/vouchers)
- **Quản lý nhà vận chuyển & Phí giao hàng:** [http://localhost:8080/admin/shipping-units](http://localhost:8080/admin/shipping-units)
- **Xử lý đơn hàng đa trạng thái:** [http://localhost:8080/admin/orders](http://localhost:8080/admin/orders)
- **Thẩm định sách cũ ký gửi:** [http://localhost:8080/admin/consignments](http://localhost:8080/admin/consignments)
- **Quản lý người dùng & Phân quyền tài khoản:** [http://localhost:8080/admin/users](http://localhost:8080/admin/users)
- **Báo cáo thống kê doanh thu & Xuất Excel/PDF:** [http://localhost:8080/admin/reports](http://localhost:8080/admin/reports)
