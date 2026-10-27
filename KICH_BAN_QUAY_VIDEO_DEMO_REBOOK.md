# KỊCH BẢN QUAY VIDEO DEMO BẢO VỆ ĐỒ ÁN CUỐI KỲ
## HỆ THỐNG QUẢN LÝ VÀ BÁN SÁCH CŨ CHUỖI CỬA HÀNG (REBOOK)

- **Môn học:** Lập trình Web (WEBPR330479)
- **Giảng viên hướng dẫn:** ThS. Nguyễn Hữu Trung
- **Đơn vị:** Trường Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE) – Khoa Công nghệ Thông tin
- **Nhóm thực hiện:**
  1. **Thái Duy Cường** – MSSV: 22110014 (Trưởng nhóm: Backend, Admin, Kho đa chi nhánh, Shipper, Security)
  2. **Nguyễn Song Hoàng Phúc** – MSSV: 22110058 (Thành viên: Frontend, Client, Giỏ hàng, Checkout VNPAY, Chat AI)
- **Thời lượng dự kiến:** 13 – 15 phút
- **Quy chuẩn ghi hình:**
  - Phần mềm khuyến nghị: **OBS Studio** (bố cục màn hình chia đôi hoặc màn hình máy tính kèm **Webcam mặt của 2 thành viên** ở góc trên/dưới bên phải).
  - Độ phân giải: **Full HD 1080p (1920x1080)**, tốc độ khung hình 30fps hoặc 60fps.
  - Âm thanh: Micro rõ ràng, lọc tạp âm, giọng điệu tự tin, rành mạch, tốc độ vừa phải.
  - Trạng thái hệ thống khi quay: Máy chủ Spring Boot đang chạy tại cổng `8080` (`http://localhost:8080`), CSDL MySQL `old_book_store_db` đã nạp sẵn dữ liệu mẫu.

---

## TỔNG QUAN PHÂN BỔ THỜI LƯỢNG KỊCH BẢN

| Phân đoạn | Thời lượng | Người thực hiện | Nội dung chính |
|:---|:---:|:---:|:---|
| **Phần 1: Giới thiệu chung** | 00:00 – 01:30 (1.5p) | **Cường & Phúc** | Xuất hiện trước webcam, chào GVHD, giới thiệu bản thân & phân công nhiệm vụ. |
| **Phần 2: Phân hệ Client & User** | 01:30 – 06:00 (4.5p) | **Hoàng Phúc** | Guest, Lọc sách độ mới, Đăng ký OTP Email, Đặt hàng VNPAY, Đánh giá $\ge$ 50 ký tự, Chatbot AI. |
| **Phần 3: Ký gửi Sách cũ (C2B)** | 06:00 – 07:30 (1.5p) | **Phúc $\to$ Cường** | Khách đăng ký ký gửi sách $\to$ Admin thẩm định ảnh thật, chốt giá thỏa thuận. |
| **Phần 4: Phân hệ Admin & Manager** | 07:30 – 11:30 (4.0p) | **Duy Cường** | Dashboard Chart.js, Quản lý kho đa chi nhánh, Điều chuyển kho (Stock Transfer), Quản lý người dùng, Voucher. |
| **Phần 5: Phân hệ Shipper** | 11:30 – 13:00 (1.5p) | **Duy Cường** | Nhận đơn giao hàng, Cập nhật trạng thái "Giao thành công", Thu COD và đối soát tiền mặt. |
| **Phần 6: Kiến trúc Code & Lời kết** | 13:00 – 14:30 (1.5p) | **Cường & Phúc** | Tóm tắt mô hình 3 tầng, bảo mật Spring Security 6, báo cáo ứng dụng AI có trách nhiệm, chào kết thúc. |

---

## KỊCH BẢN CHI TIẾT TỪNG PHÂN ĐOẠN (SCENE-BY-SCENE)

### PHẦN 1: MỞ ĐẦU & GIỚI THIỆU THÀNH VIÊN (00:00 – 01:30)
- **Thiết lập khung hình:** Bật Webcam toàn màn hình (Full-screen webcam) hoặc chia đôi màn hình có mặt rõ ràng của cả 2 bạn Thái Duy Cường và Nguyễn Song Hoàng Phúc. Mở slide/trang chủ ReBook làm hình nền phía sau.

> **[00:00 – 00:35] Lời thoại - Thái Duy Cường:**
> *"Kính chào thầy ThS. Nguyễn Hữu Trung và quý thầy cô trong bộ môn Lập trình Web!  
> Em tên là **Thái Duy Cường**, mã số sinh viên **22110014**, là trưởng nhóm thực hiện đề tài cuối kỳ: **Xây dựng Hệ thống Quản lý và Bán sách cũ chuỗi cửa hàng ReBook**.  
> Trong dự án này, em đảm nhiệm vai trò Tech Lead và phụ trách toàn bộ phân hệ Backend: Thiết kế cơ sở dữ liệu 21 bảng quan hệ, xây dựng cấu hình bảo mật Spring Security 6, mã hóa BCrypt, cùng toàn bộ nghiệp vụ Quản trị hệ thống (Admin), Quản lý cửa hàng chi nhánh (Manager), Vận hành kho đa cơ sở và phân hệ dành cho Shipper giao vận."*

> **[00:35 – 01:10] Lời thoại - Nguyễn Song Hoàng Phúc:**
> *"Em xin kính chào thầy! Em tên là **Nguyễn Song Hoàng Phúc**, mã số sinh viên **22110058**.  
> Trong đồ án ReBook, em đảm nhận vai trò thiết kế và hiện thực phân hệ Khách hàng (Client Tier): Xây dựng giao diện đồng bộ theo phong cách ReBook Vintage Leather với Bootstrap 5, lập trình chức năng tìm kiếm và lọc sách đa tiêu chí, quy trình đăng ký kích hoạt mã OTP qua Email, giỏ hàng đồng bộ Database, tích hợp cổng thanh toán trực tuyến VNPAY Sandbox, lịch sử mua hàng, hệ thống đánh giá sản phẩm tối thiểu 50 ký tự kèm upload ảnh Cloudinary, và kênh hỗ trợ trực tuyến tích hợp Trợ lý AI tra cứu tồn kho."*

> **[01:10 – 01:30] Lời thoại - Thái Duy Cường:**
> *"Sau đây, nhóm chúng em xin phép thu nhỏ webcam về góc phải màn hình để bắt đầu thao tác demo trực tiếp toàn bộ các chức năng của hệ thống ReBook đang chạy trên môi trường thực tế!"*

---

### PHẦN 2: DEMO PHÂN HỆ KHÁCH HÀNG (GUEST & USER) (01:30 – 06:00)
- **Người thao tác & thuyết minh:** **Nguyễn Song Hoàng Phúc**
- **Giao diện quay:** Trình duyệt mở `http://localhost:8080/`

#### Cảnh 2.1: Trang chủ và Bộ lọc sách cũ (01:30 – 02:30)
- **Thao tác màn hình:**
  1. Cuộn trang chủ từ trên xuống: Banner, Danh mục sách nổi bật, Top sách bán chạy (> 10 cuốn).
  2. Bấm vào nút "Xem thêm sách cũ" (Trình diễn AJAX Infinite Loading tự tải thêm sách không giật lag).
  3. Bấm vào Bộ lọc sách: Chọn lọc sách có độ mới từ **90% trở lên**, chọn mức giá 50k - 100k.
  4. Bấm vào một cuốn sách (ví dụ: *Số Đỏ - 1996* hoặc *Nhà Giả Kim*).
- **Lời thoại Hoàng Phúc:**
  > *"Trước hết là giao diện Trang chủ dành cho khách vãng lai (Guest). Website được nhóm em thiết kế đồng bộ theo phong cách Vintage Leather với tông màu da thuộc cổ điển, tạo cảm giác mộc mạc và hoài niệm đúng tinh thần của sách cũ.  
  > Trang chủ hiển thị danh sách các đầu sách bán chạy nhất của các chi nhánh, sắp xếp theo lượt bán giảm dần. Hệ thống hỗ trợ nút 'Xem thêm' tải bất đồng bộ bằng AJAX rất mượt mà.  
  > Điểm đặc biệt của ReBook là bộ lọc theo **Độ mới của sách cũ (% Condition)**. Khách hàng có thể lọc các cuốn sách nguyên vẹn 90%, 95% hoặc sách sưu tầm hiếm. Khi bấm vào trang chi tiết sản phẩm, người mua có thể xem được ảnh chụp bìa thực tế, đọc ghi chú chi tiết về tình trạng gáy sách, mép giấy, và đặc biệt là xem được **tồn kho thực tế tại từng chi nhánh** trong chuỗi."*

#### Cảnh 2.2: Đăng ký tài khoản xác thực mã OTP qua Email (02:30 – 03:30)
- **Thao tác màn hình:**
  1. Bấm vào nút **Đăng ký** trên thanh Header.
  2. Điền thông tin: Họ tên, Số điện thoại, Email cá nhân (mở sẵn tab Gmail để minh chứng), Mật khẩu.
  3. Bấm nút "Đăng ký tài khoản" -> Hệ thống chuyển sang trang Nhập mã xác thực OTP.
  4. Chuyển sang tab Gmail -> Nhận email từ ReBook với tiêu đề *[ReBook] Mã OTP Kích hoạt tài khoản*. Mở email cho thấy mã 6 số và giao diện email HTML chỉn chu.
  5. Copy mã OTP -> Dán vào ô xác thực trên ReBook -> Bấm "Xác thực".
  6. Màn hình báo thành công và tự động chuyển về trang Đăng nhập.
- **Lời thoại Hoàng Phúc:**
  > *"Để ngăn chặn triệt để tình trạng tạo tài khoản ảo và spam hệ thống, nhóm em đã tích hợp cơ chế kích hoạt 2 lớp qua Email.  
  > Khi em bấm đăng ký, hệ thống phía Backend sử dụng JavaMailSender để gửi một mã OTP gồm 6 chữ số ngẫu nhiên vào email của em. Mã này có thời hạn sống trong 5 phút được lưu vết trong bảng `otp_tokens`.  
  > Em chuyển sang tab Gmail, email kích hoạt đã về tức thì với template HTML chuyên nghiệp. Em nhập mã OTP này vào hệ thống và bấm xác nhận. Lúc này tài khoản mới chính thức được chuyển trạng thái `is_active = 1` và em có thể đăng nhập an toàn."*

#### Cảnh 2.3: Giỏ hàng, Áp Voucher & Thanh toán VNPAY Sandbox (03:30 – 05:00)
- **Thao tác màn hình:**
  1. Đăng nhập tài khoản vừa tạo (hoặc tài khoản `user@rebook.vn`).
  2. Vào trang chi tiết sách -> Bấm **"Thêm vào giỏ hàng"** (Hiện Toast thông báo realtime).
  3. Bấm vào biểu tượng Giỏ hàng trên header -> Xem danh sách sách trong giỏ (Lưu trong Database).
  4. Bấm "Tiến hành đặt hàng" -> Chuyển sang trang Checkout.
  5. Chọn địa chỉ giao hàng (hoặc thêm địa chỉ mới: Nhà riêng / Cơ quan).
  6. Áp dụng mã giảm giá **Voucher REBOOK20K** -> Tổng tiền giảm ngay 20.000đ.
  7. Chọn phương thức thanh toán **"Cổng thanh toán điện tử VNPAY"** -> Bấm "Thanh toán ngay".
  8. Hệ thống chuyển hướng sang Cổng thanh toán VNPAY Sandbox.
  9. Chọn thanh toán bằng Thẻ nội địa / Quét mã QR -> Nhập thông tin thẻ test NCB (`9704198526191432198`, Tên: `NGUYEN VAN A`, Ngày: `07/15`, OTP: `123456`).
  10. Bấm xác nhận -> Cổng VNPAY thông báo thành công -> Tự động chuyển về trang `checkout-success` của ReBook với mã giao dịch rõ ràng.
- **Lời thoại Hoàng Phúc:**
  > *"Tiếp theo là quy trình mua hàng và thanh toán trực tuyến – một trong những tính năng sáng tạo nổi bật của đồ án.  
  > Khi người dùng đã đăng nhập, giỏ hàng được đồng bộ trực tiếp vào cơ sở dữ liệu. Tại trang thanh toán, khách hàng có thể quản lý nhiều địa chỉ nhận hàng khác nhau. Em chọn áp dụng Voucher giảm 20.000đ, hệ thống kiểm tra điều kiện đơn hàng và trừ tiền trực tiếp trên hóa đơn.  
  > Khi chọn thanh toán qua VNPAY, Server ReBook sẽ đóng gói đơn hàng và tạo mã băm bảo mật HMAC-SHA512 để chuyển hướng sang Cổng VNPAY Sandbox. Em tiến hành nhập thẻ ngân hàng thử nghiệm để thanh toán.  
  > Ngay khi giao dịch hoàn tất, VNPAY phản hồi mã kết quả về Server, hệ thống tự động kiểm tra chữ ký số chống giả mạo, cập nhật đơn hàng sang trạng thái `PAID` và tự động trừ số lượng tồn kho của chi nhánh tương ứng."*

#### Cảnh 2.4: Đánh giá sản phẩm $\ge 50$ ký tự & Trợ lý AI (05:00 – 06:00)
- **Thao tác màn hình:**
  1. Vào trang **Lịch sử đơn hàng** (`/user/orders`) -> Cho thấy đơn hàng có trạng thái chi tiết.
  2. Bấm vào một đơn hàng đã giao (`DELIVERED`) -> Mở form viết đánh giá.
  3. Gõ thử 10 ký tự -> Bấm gửi -> Hệ thống báo lỗi Validation: *Bình luận phải đạt tối thiểu 50 ký tự*.
  4. Gõ bình luận chuẩn: *"Cuốn sách Số Đỏ ấn bản 1996 này được bảo quản rất kỹ, mép giấy có ố vàng nhẹ theo năm tháng đúng chất sách cổ, gáy sách chắc chắn không bị rách trang nào!"*
  5. Chọn tệp ảnh thật tải lên -> Bấm gửi -> Lưu thành công (Ảnh được đẩy lên Cloudinary CDN).
  6. Bấm mở Widget Chat ở góc dưới màn hình: Nhắn tin cho AI *"Tìm giúp mình cuốn Nhà Giả Kim còn ở chi nhánh nào?"* -> AI trả lời bằng tiếng Việt phân tích rõ cơ sở nào còn sách và số lượng tồn.
- **Lời thoại Hoàng Phúc:**
  > *"Sau khi nhận hàng, khách hàng có thể để lại đánh giá. Để đảm bảo độ tin cậy cho cộng đồng đọc sách cũ, ReBook bắt buộc bình luận phải đạt tối thiểu 50 ký tự thực tế và hỗ trợ upload ảnh chụp tình trạng sách thật lên Cloudinary.  
  > Ngoài ra, ở góc màn hình có Trợ lý AI Sách cũ. AI có khả năng nhận diện tiếng Việt không dấu, tự động truy vấn tồn kho đa cơ sở trong CSDL và phản hồi bằng ngôn ngữ tự nhiên, giúp người mua biết chính xác cơ sở nào đang còn cuốn sách mình tìm kiếm."*

---

### PHẦN 3: DEMO KÝ GỬI SÁCH CŨ TRỰC TUYẾN (C2B PLATFORM) (06:00 – 07:30)
- **Người thao tác:** **Hoàng Phúc (Khách gửi) $\to$ Thái Duy Cường (Admin duyệt)**
- **Giao diện:** Trang Ký gửi Sách cũ (`/consignments/create`)

- **Thao tác màn hình:**
  1. Hoàng Phúc vào tab **"Ký gửi sách cũ"** trên Header.
  2. Điền thông tin: Tiêu đề sách (*"Mắt Biếc - Bản in đầu 1990"*), Tác giả (*Nguyễn Nhật Ánh*), Năm xuất bản (*1990*), Độ mới (*85%*), Mô tả (*"Sách giữ kỹ, gáy đẹp, giấy ngả màu thời gian"*).
  3. Tải lên 2 ảnh chụp thực tế của sách.
  4. Nhập giá mong muốn (*120.000 VNĐ*) -> Chọn chi nhánh muốn gửi (*Cơ sở 1: Thủ Đức*).
  5. Bấm **"Gửi yêu cầu ký gửi"** -> Màn hình thông báo gửi thành công và hiển thị mã đơn ký gửi.
- **Lời thoại Hoàng Phúc:**
  > *"Điểm cốt lõi tạo nên nguồn hàng cho ReBook chính là mô hình ký gửi sách cũ C2B (tương tự như Chợ Tốt). Khách hàng có sách cũ ở nhà muốn thanh lý chỉ cần chụp ảnh các góc sách, điền tình trạng thực tế và giá mong muốn rồi gửi lên hệ thống.  
  > Bây giờ, em xin nhường quyền điều khiển lại cho bạn Thái Duy Cường để tiếp tục phần thẩm định của Admin và điều hành chuỗi chi nhánh!"*

---

### PHẦN 4: DEMO PHÂN HỆ QUẢN TRỊ (ADMIN & MANAGER) (07:30 – 11:30)
- **Người thao tác & thuyết minh:** **Thái Duy Cường**
- **Giao diện:** Đăng nhập tài khoản `admin@rebook.vn` -> Mở `/admin/dashboard`

#### Cảnh 4.1: Dashboard Thống kê & Báo cáo doanh thu (07:30 – 08:30)
- **Thao tác màn hình:**
  1. Đăng xuất tài khoản khách -> Đăng nhập tài khoản `admin@rebook.vn`.
  2. Hiển thị Dashboard quản trị: 4 thẻ KPI doanh thu, Tổng đơn hàng, Số sách cũ đang bán, Số đơn ký gửi chờ duyệt.
  3. Rê chuột trên Biểu đồ Chart.js doanh thu theo tháng và Biểu đồ tỷ lệ cơ cấu sách theo độ mới (% Condition).
- **Lời thoại Duy Cường:**
  > *"Chào thầy, em là Thái Duy Cường. Em xin tiếp tục phần trình bày với phân hệ Quản trị Admin và Quản lý chuỗi chi nhánh.  
  > Ngay khi đăng nhập vào trang Quản trị, Admin sẽ được tiếp cận Dashboard tổng quan với 4 thẻ KPI tài chính và hoạt động được đồng bộ trực tiếp từ CSDL. Biểu đồ Chart.js trực quan hóa doanh thu theo thời gian thực và phân loại cơ cấu sách theo từng phân khúc độ mới."*

#### Cảnh 4.2: Thẩm định & Duyệt đơn Ký gửi sách cũ (08:30 – 09:30)
- **Thao tác màn hình:**
  1. Vào menu **"Quản lý ký gửi"** (`/admin/consignments`).
  2. Thấy đơn ký gửi cuốn *Mắt Biếc* bạn Phúc vừa gửi với trạng thái `PENDING`.
  3. Bấm **"Xem chi tiết & Thẩm định"** -> Mở modal xem ảnh chụp thực tế phóng to sắc nét.
  4. Đánh giá chất lượng sách -> Nhập ghi chú thẩm định: *"Hình ảnh bìa và gáy đạt chuẩn 85%, hẹn bạn mang sách ra cơ sở Thủ Đức"*.
  5. Chốt giá thỏa thuận (`agreed_price`): *110.000 VNĐ*.
  6. Bấm nút **"Chấp thuận ký gửi (Approve)"** -> Trạng thái đổi thành `APPROVED`.
- **Lời thoại Duy Cường:**
  > *"Bây giờ em vào mục Quản lý ký gửi. Đơn ký gửi cuốn 'Mắt Biếc' mà bạn Phúc vừa gửi đã lập tức hiển thị. Admin có thể soi chiếu hình ảnh chụp thực tế ở độ phân giải cao.  
  > Sau khi đánh giá tình trạng sách đạt yêu cầu, Admin đưa ra mức giá thỏa thuận là 110.000đ và phê duyệt đơn. Khách hàng sẽ nhận được thông báo lịch hẹn mang sách ra chi nhánh Thủ Đức để nhân viên quầy kiểm tra lần cuối, thanh toán tiền và đưa sách vào kho bán."*

#### Cảnh 4.3: Quản lý Kho đa chi nhánh & Lệnh Điều chuyển kho (09:30 – 10:45)
- **Thao tác màn hình:**
  1. Vào menu **"Quản lý tồn kho"** (`/admin/inventory/list`).
  2. Bấm chọn Segmented Dock xem tồn kho của từng chi nhánh: Chi nhánh Thủ Đức, Chi nhánh Quận 1, Chi nhánh Bình Thạnh.
  3. Nhận thấy cuốn *Nhà Giả Kim* tại Chi nhánh Quận 1 chỉ còn 0 cuốn, trong khi Chi nhánh Thủ Đức còn 4 cuốn.
  4. Bấm nút **"Lập phiếu điều chuyển kho"** (`/admin/inventory/transfer`).
  5. Chọn Kho nguồn: *Thủ Đức*, Kho đích: *Quận 1*, Chọn sách: *Nhà Giả Kim*, Số lượng: *2 cuốn*.
  6. Bấm **"Tạo lệnh điều chuyển"** -> Phiếu chuyển sang trạng thái `IN_TRANSIT`.
  7. Bấm **"Xác nhận đã nhận hàng (Complete Transfer)"** -> Hệ thống tự động trừ 2 cuốn ở Thủ Đức và cộng 2 cuốn vào tồn kho Quận 1.
- **Lời thoại Duy Cường:**
  > *"Đây là nghiệp vụ Quản lý tồn kho chuỗi đa chi nhánh – giải quyết bài toán thực tế của các chuỗi sách. Admin và Manager có thể nắm bắt chính xác số lượng từng đầu sách tại từng cửa hàng.  
  > Khi phát hiện chi nhánh Quận 1 hết sạch cuốn 'Nhà Giả Kim' trong khi chi nhánh Thủ Đức đang còn hàng, em tạo một Phiếu điều chuyển kho. Toàn bộ quá trình xuất kho, vận chuyển và nhập kho đều được bao bọc trong giao dịch `@Transactional(isolation = Isolation.READ_COMMITTED)`. Khi em bấm xác nhận đã nhận, kho Thủ Đức tự động trừ 2 cuốn và kho Quận 1 tự động tăng thêm 2 cuốn, đảm bảo số liệu luôn chính xác tuyệt đối."*

#### Cảnh 4.4: Quản lý Người dùng, Voucher & Nhà vận chuyển (10:45 – 11:30)
- **Thao tác màn hình:**
  1. Vào menu **"Quản lý Người dùng"** (`/admin/users`): Tìm kiếm user, phân quyền tài khoản (chuyển đổi giữa `USER`, `MANAGER`, `SHIPPER`).
  2. Vào menu **"Nhà vận chuyển"** (`/admin/shipping-units`): Cho thấy cấu hình cước phí ship của GHTK, GHN, ViettelPost.
  3. Vào menu **"Mã giảm giá"** (`/admin/vouchers`): Cho thấy các voucher giảm %, giảm tiền cố định và điều kiện áp dụng.
- **Lời thoại Duy Cường:**
  > *"Bên cạnh đó, phân hệ Admin cung cấp đầy đủ các công cụ quản trị người dùng, phân quyền Role chặt chẽ, quản lý danh mục đa cấp, quản lý đối tác vận chuyển với các mức phí giao hàng khác nhau, và quản lý các chiến dịch khuyến mãi Voucher trên toàn hệ thống."*

---

### PHẦN 5: DEMO PHÂN HỆ GIAO VẬN SHIPPER (11:30 – 13:00)
- **Người thao tác & thuyết minh:** **Thái Duy Cường**
- **Giao diện:** Đăng xuất Admin -> Đăng nhập tài khoản `shipper@rebook.vn` -> Mở giao diện `/shipper/orders`

- **Thao tác màn hình:**
  1. Đăng nhập bằng tài khoản `shipper@rebook.vn` (Mật khẩu: `Shipper@123`).
  2. Giao diện tối ưu riêng cho điện thoại/tablet của Shipper hiện ra.
  3. Xem danh sách các đơn hàng được điều phối đi giao.
  4. Chọn một đơn hàng đang ở trạng thái `ASSIGNED` -> Bấm nút **"Bắt đầu đi giao"** -> Trạng thái chuyển sang `SHIPPING`.
  5. Bấm nút **"Xác nhận giao thành công"** -> Hệ thống hiển thị số tiền mặt COD cần thu -> Bấm xác nhận -> Đơn chuyển sang `DELIVERED`.
  6. Xem bảng thống kê: Tổng số đơn đã hoàn thành trong ngày và Tổng tiền COD đang giữ cần nộp về cho thủ quỹ.
- **Lời thoại Duy Cường:**
  > *"Để hoàn thiện chu trình khép kín của một website thương mại điện tử, nhóm em đã xây dựng riêng phân hệ Shipper tại đường dẫn `/shipper`.  
  > Nhân viên giao hàng có giao diện riêng biệt, chỉ nhìn thấy các đơn hàng được phân công cho mình. Shipper có thể chuyển đổi trạng thái từ 'Đang giao' sang 'Giao thành công' và xác nhận số tiền COD đã thu từ khách. Mọi dữ liệu thu hộ tiền mặt đều được thống kê minh bạch để đối soát với cửa hàng vào cuối ngày."*

---

### PHẦN 6: TỔNG KẾT, KIẾN TRÚC MÃ NGUỒN & LỜI KẾT (13:00 – 14:30)
- **Thiết lập khung hình:** Bật lại Webcam toàn cảnh xuất hiện cả 2 bạn Thái Duy Cường và Nguyễn Song Hoàng Phúc. Mở cửa sổ VS Code / IntelliJ hiển thị cấu trúc package `vn.iotstar` và lịch sử Git log 168 commits.

> **[13:00 – 13:45] Lời thoại - Thái Duy Cường:**
> *"Kính thưa thầy, về mặt kỹ thuật mã nguồn, dự án ReBook tuân thủ nghiêm ngặt mô hình 3 tầng (3-Tier Architecture) của Spring Boot:  
> - Tầng Presentation với 31 Controllers phân định rõ ràng giữa Admin, Client, Shipper và REST API.  
> - Tầng Business Logic gồm 18 Service Interfaces và 18 Implementations xử lý trọn vẹn nghiệp vụ.  
> - Tầng Data Access với 22 Repositories kế thừa Spring Data JPA và Hibernate 6.  
> Toàn bộ quá trình code được nhóm quản lý chặt chẽ trên GitHub với hơn 168 commits, phân nhánh tính năng độc lập giữa nhánh `feature/admin-cuong`, `feature/client-phuc` và nhánh `main`."*

> **[13:45 – 14:15] Lời thoại - Nguyễn Song Hoàng Phúc:**
> *"Đặc biệt, trong quá trình thực hiện đồ án, nhóm chúng em đã ứng dụng AI một cách có trách nhiệm theo mô hình **AI-Assisted Pair Programming**.  
> Chúng em hoàn toàn làm chủ kiến trúc hệ thống, trực tiếp thiết kế CSDL và quy trình bảo mật. AI chỉ đóng vai trò là một trợ lý hỗ trợ sinh mã nguồn mẫu và gợi ý cú pháp. Nhóm đã thực hiện rà soát, phản biện và tinh chỉnh lại từng dòng code của AI để đảm bảo tính an toàn, không có lỗi bảo mật và đáp ứng chính xác 100% các tiêu chí đánh giá của môn học."*

> **[14:15 – 14:30] Lời thoại - Cả 2 thành viên:**
> *(Cả 2 bạn cùng cúi chào)*  
> *"Nhóm chúng em xin chân thành cảm ơn thầy ThS. Nguyễn Hữu Trung đã tận tâm giảng dạy và đồng hành cùng chúng em trong suốt học kỳ qua! Chúng em rất mong nhận được những góp ý quý báu từ thầy để hệ thống ngày càng hoàn thiện hơn. Chúng em xin chân thành cảm ơn thầy ạ!"*

---

## CẨM NANG HƯỚNG DẪN KỸ THUẬT KHI QUAY VIDEO

1. **Chuẩn bị dữ liệu trước khi bấm quay:**
   - Khởi động sẵn ứng dụng: `mvn clean package -DskipTests` -> `java -jar target/DoAn_QuanLyChuoiSachCu-1.0.0.jar`.
   - Mở sẵn 3 trình duyệt / cửa sổ ẩn danh:
     - Cửa sổ 1: Khách hàng (Trang chủ `http://localhost:8080/`).
     - Cửa sổ 2: Gmail cá nhân để nhận mã OTP.
     - Cửa sổ 3: Admin đã đăng nhập sẵn (`http://localhost:8080/admin/dashboard`).
2. **Kỹ thuật bố cục Webcam trong OBS Studio:**
   - **Cảnh 1 (Intro):** Toàn màn hình Webcam 2 người (Full Screen).
   - **Cảnh 2 (Demo):** Màn hình máy tính hiển thị web ở trung tâm (chiếm 80%), Webcam 2 người đặt ở góc trên bên phải (chiếm 20% khung hình).
   - **Cảnh 3 (Outro):** Chuyển lại toàn màn hình Webcam 2 người để chào kết thúc.
3. **Mẹo nói lưu loát:**
   - Một bạn thao tác chuột, bạn còn lại tập trung nói theo kịch bản để tránh bị phân tâm.
   - Nếu nói nhầm 1 câu, hãy dừng lại 2 giây rồi nói lại câu đó, sau đó dùng phần mềm CapCut hoặc Premiere cắt bỏ đoạn lỗi cực kỳ nhanh chóng.
