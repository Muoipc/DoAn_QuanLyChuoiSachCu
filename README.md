# ĐỒ ÁN CUỐI KỲ MÔN LẬP TRÌNH WEB (WEBPR330479)
## TRƯỜNG ĐẠI HỌC SƯ PHẠM KỸ THUẬT TP. HỒ CHÍ MINH (HCMUTE)
### KHOA CÔNG NGHỆ THÔNG TIN - BỘ MÔN CÔNG NGHỆ PHẦN MỀM

---

## ĐỀ TÀI 14: XÂY DỰNG WEBSITE QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ
**Giảng viên hướng dẫn:** ThS. Nguyễn Hữu Trung  
**Thời gian báo cáo:** 30/10/2026 tại Phòng A5-203

---

### THÀNH VIÊN NHÓM VÀ PHÂN CÔNG NHIỆM VỤ

| STT | Họ và tên | MSSV | Vai trò đảm nhận | Phân hệ chức năng chính |
| :---: | :--- | :---: | :--- | :--- |
| 1 | **Nguyễn Song Hoàng Phúc** | **24162096** | **Khách (Guest) & Khách hàng (Customer)** | - Xác thực: Đăng ký OTP qua Email, Đăng nhập Spring Security, Đổi/Quên mật khẩu OTP.<br>- Khám phá sách cũ: Bộ lọc đa tiêu chí (độ mới 80%-99%, tình trạng trang sách, tác giả, NXB, chi nhánh còn sách).<br>- Giỏ hàng DB, Đặt hàng chuỗi (Click & Collect nhận tại shop hoặc giao tận nơi).<br>- Thanh toán tích hợp VNPAY Sandbox & COD.<br>- Đánh giá sách (kèm ảnh/video Cloudinary, text $\ge$ 50 ký tự), Sổ đa địa chỉ.<br>- Ký gửi sách cũ: Gửi form bán lại/ký gửi sách cho hệ thống.<br>- **AI Shopping Assistant:** Trợ lý ảo tư vấn chọn sách thông minh. |
| 2 | **Cường** |  | **Quản lý chi nhánh (Store Manager) & Quản trị viên (Admin) & Shipper** | - Quản lý chuỗi chi nhánh (Store): Thông tin shop, kho hàng từng chi nhánh, điều chuyển sách giữa các kho.<br>- Quản lý Sách cũ: Thêm/sửa sách, phân loại độ mới, upload bộ ảnh góc cạnh qua Cloudinary.<br>- Duyệt yêu cầu ký gửi/thu mua sách cũ từ khách hàng.<br>- Xử lý đơn hàng đa trạng thái, phân công giao hàng cho Shipper.<br>- Quản trị toàn hệ thống (Admin): Quản lý Users, Vouchers, Đơn vị vận chuyển, Cấu hình chiết khấu sàn.<br>- Thống kê & Báo cáo doanh thu trực quan bằng Chart.js, xuất file Excel/PDF.<br>- **WebSocket (STOMP):** Thông báo đơn hàng & trạng thái thời gian thực.<br>- **AI Auto-Writer & Valuation:** AI viết mô tả sách và hỗ trợ định giá sách cũ. |

---

### CÔNG NGHỆ SỬ DỤNG

- **Backend:** Spring Boot 3.4.x, Java 21, Spring Data JPA, Spring Security, Spring Mail.
- **Frontend:** Thymeleaf, Thymeleaf Layout Dialect, Bootstrap 5, jQuery AJAX, FontAwesome.
- **Cơ sở dữ liệu:** MySQL 8.x.
- **Lưu trữ đám mây (Cloud Storage):** Cloudinary API (quản lý ảnh bìa và chi tiết tình trạng sách cũ).
- **Thời gian thực (Real-time):** WebSocket (STOMP Protocol) phục vụ thông báo và trạng thái đơn hàng.
- **Thanh toán trực tuyến:** VNPay Payment Gateway Sandbox.
- **Trí tuệ nhân tạo (AI Features):** Tích hợp Generative AI API (Tư vấn sách, Tóm tắt review, Định giá sách cũ).

---

### CẤU TRÚC PHÂN NHÁNH GIT (GIT WORKFLOW)

- `main`: Nhánh chính, mã nguồn ổn định sẵn sàng báo cáo và deploy.
- `feature/client-phuc`: Nhánh phát triển phân hệ Guest, Customer, Giỏ hàng, VNPay, AI Shopping Assistant.
- `feature/admin-cuong`: Nhánh phát triển phân hệ Store Manager, Admin, Shipper, Quản lý kho, WebSocket, Cloudinary.

---

### HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY (LOCAL)

1. **Clone repository:**
   ```bash
   git clone https://github.com/Muoipc/DoAn_QuanLyChuoiSachCu_24162096.git
   cd DoAn_QuanLyChuoiSachCu_24162096
   ```

2. **Cấu hình Cơ sở dữ liệu:**
   - Mở MySQL Workbench hoặc phpMyAdmin, tạo database:
     ```sql
     CREATE DATABASE old_book_store_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     ```
   - Cập nhật thông tin kết nối trong `src/main/resources/application.properties`.

3. **Cấu hình API Keys:**
   - Cập nhật thông tin Cloudinary, Mail SMTP và VNPay trong file cấu hình.

4. **Chạy ứng dụng:**
   ```bash
   mvn clean spring-boot:run
   ```
   Ứng dụng khởi chạy tại: `http://localhost:8080/`
