# Kế Hoạch Đánh Giá & Tối Ưu Hóa Phân Hệ Quản Trị (Admin & Store Manager)

## 1. Goal
Rà soát toàn diện phân hệ Admin/Manager của chuỗi sách cũ ReBook: lược bỏ các menu/chức năng phân mảnh thừa thãi (YAGNI), bổ sung các nghiệp vụ cốt lõi còn thiếu (In phiếu giao hàng chuẩn in ấn, Quản lý & cấp tài khoản nhân viên, Cảnh báo tồn kho thấp), và nâng cấp trải nghiệm vận hành liền mạch theo triết lý tối giản (Minimalist ERP).

---

## 2. Current Context & Assumptions
- **Hiện trạng hệ thống:**
  - Spring Boot 3.4.3, Spring Security 6, Thymeleaf, MySQL 8.x.
  - Phân quyền: `ROLE_ADMIN` (quản trị toàn hệ thống) và `ROLE_MANAGER` (quản lý chi nhánh).
  - Có 13 Controller quản trị (`AdminBookController`, `AdminOrderController`, `AdminInventoryController`, `AdminReportController`, `AdminUserController`, `AdminStoreController`, `AdminConsignmentController`, `AdminVoucherController`, `AdminShippingUnitController`, `AdminCategoryController`, `AdminAiController`, `AdminNotificationController`, `AdminLayoutAdvice`).
- **Các vấn đề tồn đọng sau kiểm tra thực tế:**
  1. **Sidebar bị phân mảnh & quá tải:** Menu Admin đang có tới 14-15 mục, trong đó có những mục bị trùng lặp như `Tạo Chuyển Kho` tách riêng khỏi `Duyệt Điều Chuyển`, và có mục gây nhầm lẫn vai trò như `Phân Hệ Shipper` nằm trên sidebar Admin.
  2. **Thiếu CSS Print cho Phiếu giao hàng (`orders/detail.html`):** Nút "In Phiếu Giao Sách" đang gọi trực tiếp `window.print()` mà không có `@media print`, dẫn đến khi in sẽ in toàn bộ thanh sidebar tối màu, header, các nút bấm và màu nền của trang web thay vì phiếu in khổ A4/A5 sạch sẽ.
  3. **Quản lý người dùng bị thụ động (`users/list.html`):** Admin chỉ có thể đổi role cho người dùng đã tự đăng ký từ trước; không có tính năng tạo nhanh tài khoản nhân viên (Quản lý cửa hàng, Shipper) và không có chức năng Reset mật khẩu cho nhân viên.
  4. **Tồn kho thiếu cảnh báo thiếu hụt (`inventory/list.html`):** Thủ kho không có bộ lọc nhanh các sách có tồn kho bằng 0 hoặc sắp hết hàng (< 3 cuốn) để chủ động điều chuyển hoặc duyệt ký gửi.
  5. **Báo cáo thiếu lọc ngày tùy biến (`reports/dashboard.html`):** Báo cáo hiện cố định theo tháng của năm hiện tại, chưa cho phép chọn khoảng ngày (Từ ngày - Đến ngày) để đối soát doanh thu.

---

## 3. Architecture & Proposed Approach
- **Chiến lược Tinh gọn (Simplification & Consolidation):**
  - Gộp quy trình Chuyển kho: Đưa form "Tạo Chuyển Kho" thành Modal hoặc nút hành động nằm ngay trong trang "Điều Chuyển Kho" (`/admin/inventory/transfers`), loại bỏ 1 menu thừa trên Sidebar.
  - Loại bỏ menu `Phân Hệ Shipper` khỏi Sidebar của Admin (Admin điều phối Shipper trực tiếp trong màn hình chi tiết đơn hàng).
- **Bộ nhận diện In ấn Chuyên nghiệp (@media print Standard):**
  - Xây dựng template in hóa đơn/phiếu giao hàng ẩn toàn bộ chrome trình duyệt (sidebar, topbar, buttons), hiển thị layout chuẩn hóa đơn gồm: Logo ReBook, Mã vạch đơn hàng, Kho xuất, Người nhận, Bảng sách chi tiết và khu vực chữ ký khách hàng.
- **Mở rộng Quản trị Nhân sự & Tồn kho:**
  - Bổ sung modal "Cấp Tài Khoản Nhân Viên" & API Reset mật khẩu trong `AdminUserController`.
  - Bổ sung KPI & Chip lọc "Sắp hết hàng / Hết hàng" trong `AdminInventoryController`.

---

## 4. Chi Tiết Đánh Giá: Lược Bỏ - Thêm Mới - Cải Thiện

### Nhóm 1: NÊN LƯỢC BỎ (Remove / Consolidate - YAGNI)
1. **Lược bỏ menu "Tạo Chuyển Kho" (`/admin/inventory/transfer`) trên Sidebar:**
   - *Lý do:* Hai menu `Tạo Chuyển Kho` và `Duyệt Điều Chuyển` nằm cạnh nhau trên sidebar gây thừa thãi.
   - *Hành động:* Giữ URL route xử lý nhưng bỏ dòng menu `Tạo Chuyển Kho` trên sidebar; thay vào đó, đặt nút `+ Tạo Phiếu Chuyển Kho` nổi bật ở góc trên bên phải của trang `/admin/inventory/transfers`.
2. **Lược bỏ menu "Phân Hệ Shipper" (`/shipper/orders`) trên Sidebar Admin:**
   - *Lý do:* Shipper đã có portal riêng tại `/shipper/orders`. Admin là người phân công Shipper tại chi tiết đơn hàng (`/admin/orders/{id}`), không phải người nhận ca đi giao. Để menu này trên sidebar Admin gây nhầm lẫn nghiệp vụ.
3. **Lược bỏ trang Form riêng biệt cho Danh mục & Đơn vị vận chuyển (Khuyến nghị tương lai):**
   - *Lý do:* Bảng danh mục chỉ có 3 trường (Tên, Mô tả, Thứ tự). Chuyển hướng sang trang `/categories/create` rồi submit tải lại cả trang là trải nghiệm của web thập niên 2000. Dùng modal in-page giúp giao diện tinh gọn hơn nhiều.

---

### Nhóm 2: NÊN THÊM (New High-Value Features)
1. **Thêm Bản In Phiếu Giao Hàng Chuẩn Khổ Giấy (Printable Packing Slip):**
   - *Tệp tác động:* `src/main/resources/templates/admin/orders/detail.html`
   - *Mô tả:* Khi bấm nút "In Phiếu Giao Sách", CSS `@media print` tự động biến đổi trang thành hóa đơn trắng đen tối giản:
     - Ẩn toàn bộ: Sidebar, Topbar, Stepper Shopee, Form chọn Shipper, nút bấm.
     - Hiển thị: Tiêu đề "PHIẾU GIAO HÀNG & ĐỒNG KIỂM - REBOOK", Mã đơn hàng dạng Barcode text, Thông tin kho xuất, Địa chỉ khách, Bảng sản phẩm (Tên, Số lượng, Đơn giá, Thành tiền), Số tiền thu hộ COD nổi bật, Ô ký nhận của khách hàng.
2. **Thêm Chức năng Cấp Tài Khoản Nhân Viên & Reset Mật Khẩu:**
   - *Tệp tác động:* `AdminUserController.java`, `admin/users/list.html`
   - *Mô tả:*
     - Modal "Tạo Nhân Viên Mới": Cho phép Admin nhập Tên, Username, SĐT, Email, Mật khẩu ban đầu và chọn Role (`ROLE_MANAGER`, `ROLE_SHIPPER`).
     - Nút "Reset Mật Khẩu" trên từng dòng user: Đặt lại mật khẩu về mặc định `123456` kèm thông báo cho nhân viên.
3. **Thêm Cảnh Báo Tồn Kho Thấp (Low-Stock Alert) theo Chi Nhánh:**
   - *Tệp tác động:* `AdminInventoryController.java`, `admin/inventory/list.html`
   - *Mô tả:*
     - Bộ lọc "Chỉ xem sách sắp hết hàng" (`quantity <= 2`) hoặc "Hết hàng" (`quantity == 0`).
     - Badge màu đỏ cam cảnh báo ngay trên danh sách để Quản lý chi nhánh biết sách nào cần điều chuyển gấp từ Tổng Kho.

---

### Nhóm 3: NÊN CẢI THIỆN (Enhance UX & Performance)
1. **Cải thiện Báo Cáo Doanh Thu theo Khoảng Ngày (Date Range):**
   - *Tệp tác động:* `AdminReportController.java`, `IReportService.java`, `admin/reports/dashboard.html`
   - *Mô tả:* Cho phép chọn khoảng ngày bất kỳ (ví dụ: `startDate=2026-10-01` đến `endDate=2026-10-15`) thay vì chỉ xem theo tháng cố định.
2. **Cải thiện Xem Trước Ảnh Bìa Sách Nhanh (Image Hover Zoom):**
   - *Tệp tác động:* `admin/books/list.html`, `admin/inventory/list.html`
   - *Mô tả:* Rê chuột vào ảnh bìa sách sẽ hiển thị thumbnail lớn (200x260px) dạng tooltip nổi mượt mà, giúp thủ kho thẩm định đúng ấn bản sách mà không cần bấm vào trang sửa sách.

---

## 5. Step-by-Step Implementation Tasks

### Task 1: Tinh gọn Sidebar Menu (`admin-layout.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/layout/admin-layout.html`
- **Mục tiêu:**
  - Bỏ mục `Tạo Chuyển Kho` (`/admin/inventory/transfer`) khỏi sidebar.
  - Bỏ mục `Phân Hệ Shipper` (`/shipper/orders`) khỏi sidebar của Admin.
  - Đổi tên `Duyệt Điều Chuyển` thành `Điều Chuyển Kho` (`/admin/inventory/transfers`).
- **Mã nguồn thay đổi:**
```html
                <li class="sidebar-item" th:classappend="${#httpServletRequest != null and #httpServletRequest.requestURI.startsWith('/admin/inventory/transfers')} ? 'active' : ''">
                    <a th:href="@{/admin/inventory/transfers}">
                        <i class="fa-solid fa-dolly"></i>
                        <span>Điều Chuyển Kho</span>
                    </a>
                </li>
```
- **Tại trang `transfers.html`:** Thêm nút `+ Tạo Phiếu Chuyển Kho` trỏ đến `@{/admin/inventory/transfer}` ở thanh tiêu đề.

---

### Task 2: Hiện thực Bản In Phiếu Giao Hàng Chuẩn (@media print) (`orders/detail.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/orders/detail.html`
- **Mục tiêu:** Thiết kế khối in phiếu giao hàng A4/A5 sạch sẽ, hiển thị khi in và ẩn giao diện web.
- **Mã nguồn CSS `@media print`:**
```html
<style>
@media print {
    /* 1. Ẩn toàn bộ giao diện quản trị */
    .admin-sidebar, .admin-topbar, .admin-footer, .breadcrumb-wrapper, 
    .shopee-stepper-card, .btn, form, .status-update-card, 
    .toast-container, #shipperAssignmentCard, .sidebar-overlay {
        display: none !important;
    }

    body, .admin-content, .admin-main-wrap {
        background: #ffffff !important;
        padding: 0 !important;
        margin: 0 !important;
        color: #000000 !important;
        font-family: 'Times New Roman', serif !important;
    }

    /* 2. Hiển thị khối phiếu in giao sách */
    #printablePackingSlip {
        display: block !important;
        width: 100% !important;
        padding: 20px !important;
        border: 2px solid #000000 !important;
    }

    .print-table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 15px;
    }
    .print-table th, .print-table td {
        border: 1px solid #000000;
        padding: 8px;
        font-size: 13px;
    }
    .print-table th {
        background-color: #f2f2f2 !important;
        font-weight: bold;
    }
}

/* Mặc định trên màn hình web: ẩn khối in */
#printablePackingSlip {
    display: none;
}
</style>
```
- **Khối HTML `#printablePackingSlip` chèn vào cuối trang:**
```html
<div id="printablePackingSlip">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid #000; padding-bottom: 12px; margin-bottom: 16px;">
        <div>
            <h2 style="margin: 0; font-size: 20px; font-weight: bold; text-transform: uppercase;">CHUỖI SÁCH CŨ REBOOK</h2>
            <div style="font-size: 13px; margin-top: 4px;">Chi nhánh xuất kho: <strong th:text="${order.store.storeName}">Tổng Kho Thủ Đức</strong></div>
            <div style="font-size: 12px;" th:text="${order.store.address}">Số 1 Võ Văn Ngân, Thủ Đức</div>
            <div style="font-size: 12px;">Hotline: 1900 8888 • Email: support@rebook.vn</div>
        </div>
        <div style="text-align: right;">
            <h3 style="margin: 0; font-size: 18px; font-weight: bold;">PHIẾU GIAO HÀNG &amp; ĐỒNG KIỂM</h3>
            <div style="font-size: 14px; font-weight: bold; margin-top: 4px;" th:text="'MÃ ĐƠN: ' + ${order.orderCode}">MÃ ĐƠN: OBS-2026-001</div>
            <div style="font-size: 12px;" th:text="'Ngày in: ' + ${#temporals.format(#temporals.createNow(), 'dd/MM/yyyy HH:mm')}">Ngày in: 11/10/2026</div>
        </div>
    </div>

    <!-- Thông tin khách nhận -->
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px; font-size: 13px; line-height: 1.6;">
        <div style="width: 60%;">
            <div><strong>Người nhận:</strong> <span th:text="${order.receiverName}">Nguyễn Văn A</span></div>
            <div><strong>Số điện thoại:</strong> <span th:text="${order.receiverPhone}">0901234567</span></div>
            <div><strong>Địa chỉ giao:</strong> <span th:text="${order.receiverAddress}">Địa chỉ</span></div>
            <div><strong>Ghi chú đơn:</strong> <span th:text="${order.customerNotes != null ? order.customerNotes : 'Không có'}">Ghi chú</span></div>
        </div>
        <div style="width: 38%; text-align: right;">
            <div><strong>Hình thức giao:</strong> <span th:text="${order.deliveryMethod.name() == 'SHIPPING' ? 'Giao tận nơi' : 'Nhận tại cửa hàng'}">Giao tận nơi</span></div>
            <div><strong>Phương thức TT:</strong> <span th:text="${order.paymentMethod.name()}">COD</span></div>
            <div style="font-size: 15px; margin-top: 6px;">
                <strong>TIỀN THU COD:</strong> 
                <span style="font-size: 17px; font-weight: bold;" th:text="${order.paymentMethod.name() == 'COD' ? #numbers.formatDecimal(order.finalAmount, 0, 'COMMA', 0, 'POINT') + ' VNĐ' : '0 VNĐ (ĐÃ THANH TOÁN)'}">150.000 VNĐ</span>
            </div>
        </div>
    </div>

    <!-- Bảng sách chi tiết -->
    <table class="print-table">
        <thead>
            <tr>
                <th style="width: 40px; text-align: center;">STT</th>
                <th>Tựa Sách Cũ</th>
                <th style="width: 100px; text-align: center;">Tình Trạng</th>
                <th style="width: 60px; text-align: center;">SL</th>
                <th style="width: 100px; text-align: right;">Đơn Giá</th>
                <th style="width: 110px; text-align: right;">Thành Tiền</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="it, stat : ${order.items}">
                <td style="text-align: center;" th:text="${stat.count}">1</td>
                <td>
                    <strong th:text="${it.bookTitle}">Tựa sách</strong>
                    <div style="font-size: 11px; color: #444;" th:if="${it.book != null}" th:text="'Mã: #' + ${it.book.id}">Mã: #12</div>
                </td>
                <td style="text-align: center;" th:text="${it.book != null ? it.book.conditionPercent + '%' : '90%'}">90%</td>
                <td style="text-align: center;" th:text="${it.quantity}">1</td>
                <td style="text-align: right;" th:text="${#numbers.formatDecimal(it.price, 0, 'COMMA', 0, 'POINT')} + ' đ'">85.000 đ</td>
                <td style="text-align: right;" th:text="${#numbers.formatDecimal(it.subtotal, 0, 'COMMA', 0, 'POINT')} + ' đ'">85.000 đ</td>
            </tr>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="5" style="text-align: right; font-weight: bold;">Tổng tiền hàng:</td>
                <td style="text-align: right;" th:text="${#numbers.formatDecimal(order.totalAmount, 0, 'COMMA', 0, 'POINT')} + ' đ'">85.000 đ</td>
            </tr>
            <tr th:if="${order.discountAmount != null and order.discountAmount > 0}">
                <td colspan="5" style="text-align: right; font-weight: bold;">Giảm giá voucher:</td>
                <td style="text-align: right;" th:text="'-' + ${#numbers.formatDecimal(order.discountAmount, 0, 'COMMA', 0, 'POINT')} + ' đ'">-15.000 đ</td>
            </tr>
            <tr>
                <td colspan="5" style="text-align: right; font-weight: bold; font-size: 14px;">TỔNG CỘNG THANH TOÁN:</td>
                <td style="text-align: right; font-weight: bold; font-size: 14px;" th:text="${#numbers.formatDecimal(order.finalAmount, 0, 'COMMA', 0, 'POINT')} + ' đ'">70.000 đ</td>
            </tr>
        </tfoot>
    </table>

    <!-- Cam kết & Ký nhận -->
    <div style="margin-top: 25px; display: flex; justify-content: space-between; text-align: center; font-size: 13px;">
        <div style="width: 30%;">
            <strong>Người Lập Phiếu</strong>
            <div style="margin-top: 50px; font-style: italic;">(Ký &amp; ghi rõ họ tên)</div>
        </div>
        <div style="width: 30%;">
            <strong>Nhân Viên Giao Hàng</strong>
            <div style="margin-top: 50px; font-style: italic;" th:text="${order.shipper != null ? order.shipper.fullName : '(Ký & ghi rõ họ tên)'}">Shipper</div>
        </div>
        <div style="width: 30%;">
            <strong>Khách Hàng Ký Nhận</strong>
            <div style="font-size: 11px; margin-top: 2px;">(Xác nhận đồng kiểm sách đúng mô tả)</div>
            <div style="margin-top: 40px; font-style: italic;">(Ký &amp; ghi rõ họ tên)</div>
        </div>
    </div>
</div>
```

---

### Task 3: Bổ sung Quản lý Cấp Tài Khoản & Reset Mật Khẩu (`AdminUserController.java`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/admin/AdminUserController.java`
- **Mã nguồn bổ sung:**
```java
    /**
     * Tạo tài khoản nhân viên mới (Manager, Shipper, Staff) từ Admin
     * POST /admin/users/create-staff
     */
    @PostMapping("/create-staff")
    public String createStaff(
            @RequestParam("fullName") String fullName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("roleId") Integer roleId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Không có quyền thực hiện!");
            return "redirect:/admin";
        }

        try {
            if (userRepository.existsByUsername(username)) {
                redirect.addFlashAttribute("errorMessage", "Tên đăng nhập '" + username + "' đã tồn tại!");
                return "redirect:/admin/users";
            }

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ"));

            User user = new User();
            user.setFullName(fullName.trim());
            user.setUsername(username.trim().toLowerCase());
            user.setEmail(email.trim());
            user.setPhone(phone.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setIsActive(true);

            userRepository.save(user);
            redirect.addFlashAttribute("successMessage", "Đã cấp tài khoản nhân viên '" + username + "' (" + role.getRoleName() + ") thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi tạo tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Reset mật khẩu nhân viên về mặc định (123456)
     * POST /admin/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect
    ) {
        if (userDetails == null || !"ROLE_ADMIN".equals(userDetails.getRoleName())) {
            redirect.addFlashAttribute("errorMessage", "Không có quyền thực hiện!");
            return "redirect:/admin";
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        redirect.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu cho tài khoản '" + user.getUsername() + "' về mặc định (123456)!");

        return "redirect:/admin/users";
    }
```
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/admin/users/list.html`
  - Thêm nút `+ Cấp Tài Khoản Nhân Viên` mở Modal tạo nhân viên.
  - Thêm nút `Reset Mật Khẩu` dạng key icon cạnh nút khóa tài khoản.

---

### Task 4: Cảnh báo Tồn Kho Thấp / Sắp Hết Hàng (`AdminInventoryController.java` & `inventory/list.html`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/admin/AdminInventoryController.java`
- **Mã nguồn bổ sung:**
  - Bổ sung tham số `@RequestParam(name = "stockFilter", required = false) String stockFilter` (giá trị: `ALL`, `LOW_STOCK`, `OUT_OF_STOCK`).
  - Lọc danh sách tồn kho nếu `LOW_STOCK` (số lượng > 0 và <= 2) hoặc `OUT_OF_STOCK` (số lượng == 0).
  - Bổ sung đếm: `countOutOfStock`, `countLowStock`.
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/admin/inventory/list.html`
  - Thêm 2 chip lọc nhanh: `Cảnh Báo Hết Hàng (X)` (màu đỏ) và `Sắp Hết Hàng (Y)` (màu vàng cam).
  - Thêm nút tắt `Chuyển Kho Ngay` trên dòng sách sắp hết hàng để chuyển thẳng sang form chuyển kho với sách đó được chọn sẵn.

---

### Task 5: Xem Trước Bìa Sách Nhanh khi Hover (Hover Image Preview)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/books/list.html` & `src/main/resources/templates/admin/inventory/list.html`
- **Mã nguồn CSS/JS tối giản:**
```html
<style>
.book-thumb-hover {
    position: relative;
    cursor: zoom-in;
}
.book-thumb-hover:hover::after {
    content: '';
    position: absolute;
    left: 45px;
    top: -30px;
    width: 140px;
    height: 190px;
    background-image: inherit;
    background-size: cover;
    background-position: center;
    border-radius: 8px;
    box-shadow: 0 10px 25px rgba(0,0,0,0.25);
    border: 2px solid #fff;
    z-index: 1050;
    pointer-events: none;
}
</style>
```

---

## 6. Tests & Validation
1. **Kiểm thử In Ấn (Print Slip Test):**
   - Đăng nhập `admin` hoặc `manager_td`, truy cập `/admin/orders/1`.
   - Bấm "In Phiếu Giao Sách", xác minh hộp thoại in của trình duyệt:
     - Toàn bộ menu sidebar và topbar bị ẩn.
     - Khung phiếu in hiển thị đầy đủ thông tin: Mã đơn `ORD-20260920-001`, bảng sách, tiền COD, ô chữ ký 3 bên.
2. **Kiểm thử Quản lý Nhân viên (Staff Management Test):**
   - Bấm "+ Cấp Tài Khoản Nhân Viên" tạo tài khoản `manager_q7` (Role: `ROLE_MANAGER`).
   - Đăng nhập thử với `manager_q7` / mật khẩu vừa tạo -> Đăng nhập thành công.
   - Thử bấm "Reset Mật Khẩu" tài khoản -> Mật khẩu trở về `123456`.
3. **Kiểm thử Tồn kho thấp (Low Stock Alert Test):**
   - Truy cập `/admin/inventory`, bấm chip lọc "Sắp Hết Hàng" -> Lọc chính xác các sách có tồn kho <= 2 cuốn.

---

## 7. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi ẩn menu `Tạo Chuyển Kho` khỏi sidebar, người dùng cũ có thể bỡ ngỡ nếu chưa quen tìm nút bấm tại trang `transfers.html`.
  - *Giải pháp:* Đặt nút `+ Tạo Phiếu Chuyển Kho` to, nổi bật màu cam ở góc trên bên phải của bảng danh sách điều chuyển.
- **Đánh đổi:** Chưa thay thế hoàn toàn các trang form con của Danh mục & ĐVVC bằng Modal ngay trong đợt này để giữ phạm vi thay đổi tập trung vào các luồng quan trọng nhất (In phiếu giao, Cấp quyền nhân viên, Cảnh báo tồn kho).
