# Kế Hoạch Triển Khai: Tái Thiết Kế Giao Diện Ký Gửi Sách Cũ Tối Giản Hoài Cổ (`/consignments/create`)

## 1. Goal
Tái thiết kế toàn diện trang đăng ký ký gửi sách cũ (`/consignments/create` — `consignment.html`) theo phong cách **Tiệm Sách Ký Gửi Hoài Cổ Tối Giản (Minimalist Antique Bookhouse)**: loại bỏ banner 3D hoạt hình lòe loẹt, tinh gọn 4 bước quy trình thành các thẻ phẳng thanh nhã, chuẩn hóa form đăng ký nền trắng ngà viền be, gọt bỏ các tag viền đứt nét (dashed) và badge xanh/hồng neon, tối ưu cụm AI định giá và nút nộp phiếu chuẩn nhận diện ReBook.

---

## 2. Current Context & Assumptions
- **Trang mục tiêu:** URL `http://localhost:8080/consignments/create` (route `ConsignmentController.java`), điều khiển giao diện bởi template `src/main/resources/templates/consignment.html`.
- **Hiện trạng tồn tại qua phân tích trực quan Playwright & Vision:**
  1. **Banner Hero & 4 Bước Quy Trình:**
     - Nền ảnh banner 3D hoạt hình phong cách fintech (`consignment-hero-banner.jpg`) không ăn nhập với văn hóa sách cũ, bóng đổ chữ gắt (`text-shadow: 0 2px 10px rgba(0,0,0,0.45)`).
     - 4 thẻ quy trình sử dụng kính mờ đục kiểu futuristic (`background: rgba(255,255,255,0.14); backdrop-filter: blur(18px)`), bóng đổ đen nặng nề, khó đọc lướt.
  2. **Form Ký Gửi & Khung Tải Ảnh:**
     - Khung Form dùng kính đục nặng nề (`form-card-glass`), viền dày và bóng đổ nhiều lớp.
     - Vùng upload ảnh (`#consignmentDropZone`) dùng viền đứt nét thô (`border: 1.5px dashed rgba(140,74,39,0.35)`). Bên dưới lại tách thêm 1 ô dán URL link online gây rối mắt.
  3. **Bộ Chọn Độ Mới Sách:**
     - 5 nút chọn nhanh độ mới dùng viền đứt nét (`border: 1.5px dashed`), bên dưới lại trùng lặp với thanh trượt slider kèm badge xanh lá cây neon (`#065f46; background: rgba(16,185,129,0.12)`).
  4. **Khối AI Gợi Ý Định Giá:**
     - Chứa badge hồng "Miễn Phí" lệch tông, ô nhập giá bìa và nút "⚡ Tính Giá" chưa đồng đều baseline, nút "Áp Dụng" màu xanh lá cây chói gắt.
  5. **Nút Gửi Phiếu Ký Gửi:**
     - Nút submit dùng gradient đỏ nâu bóng loáng đục gắt (`linear-gradient(135deg, #8C4A27 0%, #6B3416 100%)`) kèm bóng đổ đậm.
  6. **Chân Trang:**
     - Thiếu khối cam kết minh bạch và chính sách ký gửi giúp khách hàng an tâm gửi gắm sách quý.

---

## 3. Architecture & Proposed Approach
- **Hệ Thống Màu Sắc & Ngôn Ngữ Thiết Kế Thư Tịch Hoài Cổ:**
  - Nền trang (Body canvas): Giấy ngà ấm `#F8F5EE`.
  - Khung thẻ và container: Trắng tinh khiết `#FFFFFF`, viền chỉ be giấy `#ECE7DE`, bo góc Squircle Apple 14px - 16px, bóng đổ khuếch tán nhẹ tự nhiên `0 1px 4px rgba(45, 24, 15, 0.03)`.
  - Màu chữ: Màu mực than `#1C1917` (tiêu đề, label) và màu xám chì `#57534E` (mô tả, phụ chú).
  - Màu nhấn chính (Brand Accent): Màu nâu da bò cổ điển `#8C4A27` và be ấm `#FAF8F5`.
- **Cải Tổ Từng Khu Vực Trọng Điểm:**
  1. *Hero & Quy Trình:* Thay nền 3D sặc sỡ bằng nền canvas ấm sang trọng (`background: linear-gradient(180deg, #F5EFEB 0%, #FAF8F5 100%)`), 4 thẻ bước chuyển thành thẻ trắng tối giản với số thứ tự `01, 02, 03, 04` màu nâu da bò `#8C4A27`.
  2. *Form Ký Gửi:* Làm phẳng form card, bỏ toàn bộ viền đứt (dashed), thay bằng viền liền mảnh `#ECE7DE`.
  3. *Tải Ảnh:* Hộp kéo thả ảnh thanh thoát, nền giấy ngà `#FAF8F5`, icon máy ảnh tinh gọn, tích hợp tùy chọn URL link gọn gàng.
  4. *Độ Mới Sách:* Thanh capsule pill mượt mà, loại bỏ slider rườm rà trùng lặp, chọn 1 chạm nhanh gọn (99%, 95%, 90%, 80%, 70%).
  5. *AI Định Giá & Nút Submit:* Canh thẳng hàng baseline, kết quả hiển thị trang nhã, nút bấm nộp phiếu phẳng màu nâu da bò `#8C4A27` tinh tế.
  6. *Cam Kết Dịch Vụ:* Bổ sung 3 thẻ cam kết uy tín (Thẩm định minh bạch, Nhận tiền tức thì, Bảo quản nâng niu).

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Tái Cấu Trúc CSS Cho Trang Ký Gửi (`src/main/resources/templates/consignment.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/consignment.html`
- **Mục tiêu:** Cập nhật các lớp CSS từ dòng 60 đến 335:
  - `.consignment-hero`, `.consignment-hero-content`, `.hero-title`, `.hero-desc`.
  - `.process-steps-grid`, `.step-item-card`, `.step-num-text`, `.step-title`, `.step-desc`.
  - `.consignment-form-card`, `.form-section-title`, `.form-input`, `.form-select`, `.form-textarea`.
  - `.photos-upload-zone`, `.condition-radio-chip`, `.ai-price-helper-card`, `.btn-submit-consignment`.
- **Mã nguồn CSS thay thế:**
```css
        /* ================= HERO BANNER HOÀI CỔ TỐI GIẢN ================= */
        .consignment-hero {
            background: linear-gradient(180deg, #F3ECE2 0%, #FAF8F5 100%);
            border-bottom: 1px solid #ECE7DE;
            padding: 44px 0 36px 0;
            margin-bottom: 32px;
        }
        .consignment-hero-content {
            max-width: 900px;
            margin: 0 auto;
            text-align: center;
        }
        .hero-tag {
            background: #FFFFFF;
            border: 1px solid #ECE7DE;
            color: #8C4A27;
            font-size: 11.5px;
            font-weight: 700;
            padding: 4px 14px;
            border-radius: 9999px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 14px;
            letter-spacing: 0.3px;
            box-shadow: 0 1px 3px rgba(45, 24, 15, 0.04);
        }
        .hero-title {
            font-size: 28px;
            font-weight: 800;
            margin-bottom: 10px;
            color: #1C1917;
            letter-spacing: -0.4px;
            line-height: 1.3;
        }
        .hero-desc {
            font-size: 14px;
            color: #57534E;
            max-width: 680px;
            margin: 0 auto 28px auto;
            line-height: 1.6;
            font-weight: 400;
        }

        /* 4 BƯỚC QUY TRÌNH PHẲNG TỐI GIẢN */
        .process-steps-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 14px;
            text-align: left;
        }
        .step-item-card {
            background: #FFFFFF;
            border: 1px solid #ECE7DE;
            border-radius: 14px;
            padding: 16px 18px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            min-height: 100px;
            box-shadow: 0 1px 4px rgba(45, 24, 15, 0.03);
            transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
        }
        .step-item-card:hover {
            transform: translateY(-3px);
            border-color: rgba(140, 74, 39, 0.35);
            box-shadow: 0 8px 20px rgba(45, 24, 15, 0.06);
        }
        .step-card-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            width: 100%;
        }
        .step-num-text {
            font-size: 16px;
            font-weight: 800;
            color: #8C4A27;
            font-variant-numeric: tabular-nums;
        }
        .step-card-icon {
            font-size: 14px;
            color: #A8A29E;
        }
        .step-title {
            font-size: 13.5px;
            font-weight: 700;
            color: #1C1917;
            margin-top: 10px;
            line-height: 1.35;
        }
        .step-desc {
            font-size: 11.5px;
            color: #78716C;
            line-height: 1.35;
            margin-top: 3px;
        }

        /* FORM KÝ GỬI REBOOK EDITORIAL */
        .consignment-form-card {
            background: #FFFFFF;
            border: 1px solid #ECE7DE;
            border-radius: 16px;
            box-shadow: 0 1px 4px rgba(45, 24, 15, 0.03);
            padding: 32px 36px;
            margin-bottom: 36px;
        }
        .form-section-title {
            font-size: 16px;
            font-weight: 800;
            color: #1C1917;
            margin-bottom: 24px;
            padding-bottom: 14px;
            border-bottom: 1px solid #F5F2EB;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .form-grid-2col {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 32px;
        }
        .form-group { margin-bottom: 20px; }
        .form-label {
            display: block;
            font-size: 13px;
            font-weight: 700;
            color: #1C1917;
            margin-bottom: 7px;
        }
        .form-label .required { color: #8C4A27; margin-left: 2px; }
        
        .form-input, .form-select, .form-textarea {
            width: 100%;
            border: 1px solid #E5DFD5;
            border-radius: 10px;
            padding: 10px 14px;
            font-size: 13.5px;
            font-family: inherit;
            color: #1C1917;
            background: #FAF8F5;
            transition: all 0.2s ease;
            outline: none;
        }
        .form-input:focus, .form-select:focus, .form-textarea:focus {
            border-color: #8C4A27;
            background: #FFFFFF;
            box-shadow: 0 0 0 3px rgba(140, 74, 39, 0.1);
        }

        /* KHU VỰC TẢI ẢNH THỰC TẾ */
        .photos-upload-zone {
            border-radius: 12px;
            border: 1px solid #E5DFD5;
            background: #FAF8F5;
            padding: 22px 16px;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .photos-upload-zone:hover {
            border-color: #8C4A27;
            background: #F5EFEB;
        }

        /* CHIP ĐỘ MỚI DẠNG CAPSULE PILL */
        .condition-chips-grid {
            display: flex;
            gap: 8px;
            margin-bottom: 12px;
            flex-wrap: wrap;
        }
        .condition-radio-chip {
            border-radius: 9999px;
            border: 1px solid #E5DFD5;
            background: #FAF8F5;
            padding: 7px 14px;
            font-size: 12px;
            font-weight: 700;
            color: #57534E;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .condition-radio-chip:hover {
            border-color: #8C4A27;
            color: #1C1917;
            background: #FFFFFF;
        }
        .condition-radio-chip.active {
            background: #1C1917;
            border-color: #1C1917;
            color: #FFFFFF;
        }

        /* KHỐI AI ĐỊNH GIÁ THỊ TRƯỜNG */
        .ai-price-helper-card {
            background: #FAF8F5;
            border: 1px solid #ECE7DE;
            border-radius: 12px;
            padding: 16px 18px;
            margin-bottom: 20px;
        }
        .ai-helper-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 10px;
            font-size: 13px;
            font-weight: 800;
            color: #8C4A27;
        }
        .ai-calc-row {
            display: flex;
            gap: 8px;
            align-items: center;
        }
        .btn-calc-ai {
            background: #8C4A27;
            color: #FFFFFF;
            border: none;
            border-radius: 10px;
            padding: 10px 18px;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            white-space: nowrap;
            transition: background 0.2s;
            height: 42px;
        }
        .btn-calc-ai:hover {
            background: #733615;
        }

        /* NÚT SUBMIT GỬI PHIẾU */
        .btn-submit-consignment {
            background: #8C4A27;
            color: #FFFFFF;
            border: none;
            border-radius: 12px;
            padding: 14px 28px;
            font-size: 14px;
            font-weight: 800;
            cursor: pointer;
            width: 100%;
            margin-top: 10px;
            letter-spacing: 0.3px;
            transition: all 0.2s ease;
        }
        .btn-submit-consignment:hover {
            background: #733615;
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(140, 74, 39, 0.25);
        }

        /* KHỐI 3 CAM KẾT DỊCH VỤ */
        .consignment-trust-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 18px;
            margin-bottom: 50px;
        }
        .trust-item-card {
            background: #FFFFFF;
            border: 1px solid #ECE7DE;
            border-radius: 14px;
            padding: 20px 22px;
            display: flex;
            align-items: flex-start;
            gap: 14px;
            box-shadow: 0 1px 4px rgba(45, 24, 15, 0.03);
        }
        .trust-icon-box {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            background: #FAF8F5;
            border: 1px solid #ECE7DE;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #8C4A27;
            font-size: 16px;
            flex-shrink: 0;
        }
        .trust-title {
            font-size: 13.5px;
            font-weight: 700;
            color: #1C1917;
            margin-bottom: 4px;
        }
        .trust-desc {
            font-size: 12px;
            color: #78716C;
            line-height: 1.45;
        }
```

---

### Task 2: Cập Nhật Cấu Trúc HTML Cho Banner & Form (`src/main/resources/templates/consignment.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/consignment.html`
- **Mục tiêu:**
  - Thay thế khối `<section class="consignment-hero">` bằng markup mới, bỏ ảnh banner 3D.
  - Tinh gọn 4 thẻ bước thành số thứ tự `01, 02, 03, 04`.
  - Cập nhật Form:
    - Loại bỏ viền đứt nét ở `#consignmentDropZone`.
    - Gộp ô dán link ảnh thành một liên kết nhỏ tinh tế dạng accordion / toggle, không chèn ngang nhiên một ô input thô.
    - Bỏ thanh trượt slider trùng lặp trong phần chọn độ mới, giữ 5 chip capsule chọn nhanh (`70%`, `80%`, `90%`, `95%`, `99%`).
    - Đồng bộ độ cao ô nhập giá bìa và nút "Tính Giá" (`height: 42px`).
    - Nút submit phẳng thanh lịch màu `#8C4A27`.
  - Bổ sung khối 3 cam kết minh bạch `.consignment-trust-grid` trước thẻ `</main>`.

- **Mã nguồn HTML cập nhật cho Hero & Quy Trình:**
```html
    <!-- HERO BANNER & 4 BƯỚC (PHONG CÁCH TIỆM SÁCH CŨ TỐI GIẢN) -->
    <section class="consignment-hero">
        <div class="container">
            <div class="consignment-hero-content">
                <span class="hero-tag">
                    <i class="fa-solid fa-book-bookmark"></i> Dịch Vụ Ký Gửi Sách ReBook
                </span>
                <h1 class="hero-title">Trao Lại Sách Hay, Nhận Tiền Ngay</h1>
                <p class="hero-desc">
                    Hồi sinh những cuốn sách không dùng đến cùng ReBook. Tiếp nhận tại 5 chi nhánh TP.HCM, thẩm định minh bạch bằng công cụ AI và thanh toán trong 24 giờ.
                </p>

                <div class="process-steps-grid">
                    <div class="step-item-card">
                        <div class="step-card-top">
                            <span class="step-num-text">01</span>
                            <i class="fa-solid fa-pen-nib step-card-icon"></i>
                        </div>
                        <div class="step-title">Khai Báo Sách</div>
                        <div class="step-desc">Điền tựa sách, tác giả &amp; AI gợi ý định giá</div>
                    </div>
                    <div class="step-item-card">
                        <div class="step-card-top">
                            <span class="step-num-text">02</span>
                            <i class="fa-solid fa-location-dot step-card-icon"></i>
                        </div>
                        <div class="step-title">Chọn Chi Nhánh</div>
                        <div class="step-desc">Gửi sách đến 1 trong 5 chi nhánh gần nhất</div>
                    </div>
                    <div class="step-item-card">
                        <div class="step-card-top">
                            <span class="step-num-text">03</span>
                            <i class="fa-solid fa-magnifying-glass step-card-icon"></i>
                        </div>
                        <div class="step-title">Thẩm Định 24 Giờ</div>
                        <div class="step-desc">Kiểm tra thực tế và xác nhận mức giá</div>
                    </div>
                    <div class="step-item-card">
                        <div class="step-card-top">
                            <span class="step-num-text">04</span>
                            <i class="fa-solid fa-money-bill-transfer step-card-icon"></i>
                        </div>
                        <div class="step-title">Nhận Thanh Toán</div>
                        <div class="step-desc">Chuyển khoản tức thì hoặc đổi voucher mua sách</div>
                    </div>
                </div>
            </div>
        </div>
    </section>
```

- **Mã nguồn HTML cập nhật cho Khối 3 Cam Kết Dịch Vụ:**
```html
        <!-- KHỐI 3 CAM KẾT DỊCH VỤ REBOOK -->
        <div class="consignment-trust-grid">
            <div class="trust-item-card">
                <div class="trust-icon-box">
                    <i class="fa-solid fa-scale-balanced"></i>
                </div>
                <div>
                    <div class="trust-title">Định Giá Minh Bạch</div>
                    <div class="trust-desc">Công cụ đối soát giá thị trường thời gian thực, đảm bảo quyền lợi tối ưu cho bạn đọc.</div>
                </div>
            </div>
            <div class="trust-item-card">
                <div class="trust-icon-box">
                    <i class="fa-solid fa-bolt"></i>
                </div>
                <div>
                    <div class="trust-title">Quy Trình 24 Giờ</div>
                    <div class="trust-desc">Thẩm định và giải ngân chuyển khoản hoặc tiền mặt ngay khi hoàn tất kiểm tra sách.</div>
                </div>
            </div>
            <div class="trust-item-card">
                <div class="trust-icon-box">
                    <i class="fa-solid fa-shield-halved"></i>
                </div>
                <div>
                    <div class="trust-title">Bảo Quản Chu Đáo</div>
                    <div class="trust-desc">Sách được bọc màng co, phân loại độ ẩm và trưng bày cẩn thận tại kệ sách chuỗi.</div>
                </div>
            </div>
        </div>
```

---

### Task 3: Tinh Chỉnh JavaScript Xử Lý Độ Mới & AI Tính Giá (`consignment.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/consignment.html`
- **Mục tiêu:**
  - Đồng bộ hàm `setCondition(val, btn)`: Cập nhật giá trị vào `input type="hidden" name="conditionPercent" value="90"`.
  - Cập nhật hàm `calculateAiPrice()` lấy trực tiếp `conditionPercent` từ giá trị chip đang kích hoạt.
  - Căn chỉnh giá trị mặc định và thông báo thành công thanh thoát.

---

### Task 4: Kiểm Thử Tự Động & Đồng Bộ Hot-Sync
- **Lệnh thực thi:**
  1. Sao chép template cập nhật sang thư mục `target/classes/templates/consignment.html`:
     ```bash
     cp src/main/resources/templates/consignment.html target/classes/templates/consignment.html
     ```
  2. Chạy test suite Maven đảm bảo không lỗi biên dịch:
     ```bash
     mvn test
     ```
     *Kỳ vọng:* `BUILD SUCCESS` (7/7 tests passed).

---

### Task 5: Kiểm Thử Trực Quan Với Playwright & Vision
- **Kịch bản kiểm thử:**
  1. Dùng Playwright điều hướng tới `http://localhost:8080/consignments/create`.
  2. Chụp ảnh màn hình các phân đoạn:
     - `/Users/phucdeptrai/consignment_redesign_top.png` (Hero & 4 bước).
     - `/Users/phucdeptrai/consignment_redesign_form.png` (Form đăng ký & AI tính giá).
     - `/Users/phucdeptrai/consignment_redesign_bottom.png` (3 cam kết dịch vụ).
  3. Sử dụng `vision_analyze` kiểm chứng:
     - Đã loại bỏ hoàn toàn banner 3D sặc sỡ và kính đục mờ tối.
     - 4 bước quy trình phẳng, số thứ tự `01-04` màu nâu da bò `#8C4A27` sắc nét.
     - Form phẳng, viền liền be giấy nhạt `#ECE7DE`, không còn viền đứt nét (dashed).
     - Nút nộp phiếu màu nâu da bò trầm `#8C4A27` đồng bộ nhận diện toàn hệ thống ReBook.

---

## 5. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi loại bỏ thanh trượt slider, người dùng có cần chọn độ mới lẻ (ví dụ 87% hay 93%) không?
  - *Giải pháp:* Trong thực tế nghiệp vụ sách cũ, tiêu chuẩn độ mới luôn được quy chuẩn thành 5 thang điểm rõ ràng: 99% (như mới), 95% (rất tốt), 90% (tốt), 80% (khá), 70% (đọc được). Việc dùng 5 chip bấm 1 chạm giúp người dùng thao tác nhanh hơn gấp 3 lần so với kéo thanh slider.
- **Tính tương thích:** Giữ nguyên 100% tên trường form (`bookTitle`, `author`, `publisher`, `publishYear`, `storeId`, `imageFile`, `conditionPercent`, `conditionDescription`, `proposedPrice`) nên `ConsignmentController.java` xử lý submit hoàn toàn bình thường, không gây ảnh hưởng đến backend.
