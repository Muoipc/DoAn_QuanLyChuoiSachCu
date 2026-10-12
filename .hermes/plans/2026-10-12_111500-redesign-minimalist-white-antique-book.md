# Kế Hoạch Triển Khai: Tái Thiết Kế Toàn Diện Hiệu Ứng Kính Mờ Bạch Ngọc Sách Cũ (White Apple Liquid Glass & Antique Book) cho Admin, Manager & Shipper

## 1. Goal
Chuyển đổi toàn diện giao diện các phân hệ quản trị Admin, Quản lý chi nhánh (Manager) và Cổng giao hàng (Shipper) sang ngôn ngữ thiết kế **Bạch Ngọc Kính Mờ Hoài Cổ (White Apple Liquid Glass on Warm Book Canvas)**: xóa bỏ triệt để các mảng nền đen sì/gỗ gụ thô ráp, bao bọc toàn bộ sidebar, navbar, bảng biểu và thẻ đơn hàng bằng các phiến kính mờ quang học trong suốt (`backdrop-filter: blur(28px) saturate(190%)`), vát mép phản quang thấu kính (`inset 0 1.5px 1.5px rgba(255,255,255,0.95)`), bo góc Squircle Apple mượt mà nổi bồng bềnh trên nền giấy sách cũ ngà sáng (`#F8F5EE`).

---

## 2. Current Context & Assumptions
- **Hệ thống hiện tại:** Spring Boot 3.4.3, Thymeleaf, Bootstrap 5.3, FontAwesome 6.4.
- **Yêu cầu cốt lõi từ người dùng:**
  - Vẫn **giữ và áp dụng toàn bộ hiệu ứng kính mờ (Liquid Glass / Frosted Glass)** xuyên suốt mọi phân hệ (Admin, Manager, Shipper).
  - Tông màu chuyển dịch sang **nền trắng ngà sáng sủa, thanh lịch**, đúng chất phong cách sách cũ (antique book aesthetic), triệt tiêu các mảng đen u tối và viền sọc thô kệch.
- **Các điểm bất cập cần khắc phục:**
  1. **Sidebar (`#adminSidebar`) bị u tối, nặng nề:** Đang dùng gradient đen gỗ gụ (`#2D180F` -> `#1A0D07`), tạo khối đen sì chiếm 1/5 màn hình, cần chuyển sang phiến kính mờ Bạch Ngọc mượt mà.
  2. **Kênh Chat Chi Nhánh (`admin/chat/index.html`) bị lạc quẻ:** Vẫn còn tàn dư các class nền đen `bg-dark text-white border-secondary` của Bootstrap, cần chuyển thành 2 phiến kính mờ trắng nổi bồng bềnh.
  3. **Kênh Shipper (`shipper/orders.html`):** Tinh chỉnh thẻ đơn hàng và 4 thẻ KPI sang kính mờ sáng màu, tăng độ tương phản của chữ và thông tin kiện hàng.
  4. **Các bảng biểu và card danh sách Admin:** Nâng cấp đồng bộ theo chuẩn Apple Liquid Glass (vát mép thấu kính, bóng đổ khuếch tán mềm).

---

## 3. Architecture & Proposed Approach

### 3.1. Hệ Thống Design Tokens Chuẩn Apple Liquid Glass (Antique Book Edition)
- **Ambient Canvas Illumination (Nền quang học toàn trang):**
  - Để hiệu ứng kính mờ trong suốt hoạt động có chiều sâu, nền `body` phải có ánh sáng tản sắc quang học nhẹ nhàng:
  ```css
  body {
      background: 
          radial-gradient(circle at 10% 12%, rgba(232, 222, 200, 0.6) 0%, transparent 45%), 
          radial-gradient(circle at 90% 85%, rgba(140, 74, 39, 0.08) 0%, transparent 45%), 
          radial-gradient(circle at 50% 50%, rgba(245, 158, 11, 0.03) 0%, transparent 50%),
          #F8F5EE !important;
      background-attachment: fixed !important;
  }
  ```
- **Optical Glass Surface Tokens (Bạch Ngọc Kính Mờ):**
  - `--glass-surface: rgba(255, 255, 255, 0.78)`: Độ trong suốt quang học cao, truyền thấu ánh sáng nền.
  - `--glass-surface-hover: rgba(255, 255, 255, 0.92)`: Tăng độ đặc khi rê chuột.
  - `--glass-border: 1.5px solid rgba(255, 255, 255, 0.85)`: Viền thấu kính trong suốt.
  - `--glass-blur: blur(28px) saturate(190%) brightness(102%)`: Độ tán xạ ánh sáng chuẩn Apple Sonoma/iOS 18.
  - `--glass-rim-chiseled: inset 0 1.5px 1.5px 0 rgba(255, 255, 255, 0.98), inset 0 -1px 1px 0 rgba(45, 24, 15, 0.03)`: Vết cắt vát mép thấu kính phản quang đón sáng mặt trên.
  - `--glass-shadow-soft: 0 14px 38px -4px rgba(45, 24, 15, 0.07), 0 4px 12px -2px rgba(0, 0, 0, 0.03)`: Bóng đổ nâng nổi bề mặt kính.
- **Vintage Ink Typography Tokens (Mực In Sách Cổ Điển):**
  - Chữ chính: `--ink-dark: #1C1917` (Mực than cổ điển, tương phản cao trên kính mờ).
  - Chữ phụ: `--ink-muted: #78716C` (Mực phai ghi xám nhẹ).
  - Nhãn thương hiệu: `--ink-leather: #8C4A27` (Nâu da thuộc gáy sách).
- **Curvature Scale (Apple Squircles & Pills - "Không Vuông Vức"):**
  - Khung card lớn & Sidebar: `border-radius: 20px - 24px`.
  - Nút bấm, input, dock lọc: `border-radius: 9999px` (Pill capsule).

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Nâng Cấp Sidebar & Layout Thành Bạch Ngọc Kính Mờ (`admin-layout.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/layout/admin-layout.html`
- **Mục tiêu:**
  - Thay đổi `#adminSidebar` từ màu đen gỗ gụ sang **Bạch Ngọc Kính Mờ trong suốt** (`background: rgba(255, 255, 255, 0.82); backdrop-filter: blur(28px);`).
  - Topbar `.admin-navbar` trong suốt kính mờ chuẩn Apple.
  - Menu navigation items: Hiệu ứng viên ngọc kính mờ khi hover (`rgba(255, 255, 255, 0.8)`) và active capsule nâu da bò (`#8C4A27`).
- **Mã nguồn CSS thay đổi:**
```css
        /* 1. SIDEBAR BẠCH NGỌC KÍNH MỜ */
        #adminSidebar {
            width: 265px;
            min-height: 100vh;
            background: rgba(255, 255, 255, 0.82);
            backdrop-filter: var(--glass-blur);
            -webkit-backdrop-filter: var(--glass-blur);
            border-right: 1.5px solid rgba(255, 255, 255, 0.85);
            box-shadow: 
                4px 0 24px rgba(45, 24, 15, 0.04), 
                inset -1.5px 0 1.5px rgba(255, 255, 255, 0.95);
            position: fixed;
            top: 0;
            left: 0;
            z-index: 1040;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
        }

        .sidebar-brand {
            padding: 22px 20px;
            display: flex;
            align-items: center;
            gap: 12px;
            border-bottom: 1px solid rgba(232, 222, 200, 0.6);
            text-decoration: none;
            color: var(--ink-dark);
            background: transparent;
        }

        .sidebar-brand-icon {
            width: 40px;
            height: 40px;
            border-radius: 12px;
            background: rgba(140, 74, 39, 0.1);
            border: 1.5px solid rgba(255, 255, 255, 0.9);
            box-shadow: 0 4px 12px rgba(140, 74, 39, 0.15), inset 0 1px 1px rgba(255, 255, 255, 0.8);
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--admin-primary);
            font-size: 19px;
            flex-shrink: 0;
        }

        .sidebar-brand-text h5 {
            margin: 0;
            font-size: 16.5px;
            font-weight: 800;
            letter-spacing: -0.3px;
            color: var(--ink-dark);
        }

        .sidebar-brand-text small {
            font-size: 11px;
            font-weight: 600;
            color: var(--ink-muted);
            display: block;
            margin-top: 1px;
        }

        .sidebar-heading {
            font-size: 10.5px;
            text-transform: uppercase;
            letter-spacing: 0.8px;
            color: #A8A29E;
            padding: 16px 20px 6px;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .sidebar-item {
            margin: 3px 10px;
        }

        .sidebar-item a {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 9px 14px;
            color: #44403C;
            text-decoration: none;
            font-size: 13.5px;
            font-weight: 600;
            border-radius: 9999px; /* Pill Capsule */
            transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
            position: relative;
        }

        .sidebar-item a i {
            width: 18px;
            text-align: center;
            font-size: 14.5px;
            color: #78716C;
            transition: all 0.2s;
        }

        .sidebar-item a:hover {
            background: rgba(255, 255, 255, 0.9);
            color: var(--ink-dark);
            transform: translateX(3px);
            box-shadow: 0 4px 14px rgba(45, 24, 15, 0.05), inset 0 1px 1px #fff;
        }

        .sidebar-item a:hover i {
            color: var(--admin-primary);
        }

        .sidebar-item.active a {
            background: linear-gradient(135deg, rgba(140, 74, 39, 0.12) 0%, rgba(238, 77, 45, 0.08) 100%);
            color: var(--admin-primary);
            border: 1.5px solid rgba(140, 74, 39, 0.25);
            font-weight: 700;
            box-shadow: 0 4px 14px rgba(140, 74, 39, 0.1), inset 0 1px 1px #fff;
        }

        .sidebar-item.active a i {
            color: var(--admin-primary);
        }

        /* 2. TOPBAR KÍNH MỜ APPLE */
        .admin-navbar {
            background: rgba(255, 255, 255, 0.75);
            backdrop-filter: var(--glass-blur);
            -webkit-backdrop-filter: var(--glass-blur);
            border-bottom: 1.5px solid rgba(255, 255, 255, 0.85);
            box-shadow: 0 4px 20px rgba(45, 24, 15, 0.03), inset 0 1px 1px rgba(255, 255, 255, 0.95);
            padding: 12px 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 1030;
        }

        .branch-badge {
            background: rgba(255, 255, 255, 0.65);
            backdrop-filter: blur(20px);
            border: 1.5px solid rgba(255, 255, 255, 0.85);
            border-radius: 9999px;
            padding: 6px 14px;
            font-size: 12.5px;
            box-shadow: 0 2px 8px rgba(45, 24, 15, 0.03), inset 0 1px 1px #fff;
        }
```

---

### Task 2: Chuyển Đổi Kênh Chat Chi Nhánh Sang Bạch Ngọc Kính Mờ (`admin/chat/index.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/chat/index.html`
- **Mục tiêu:**
  - Thay thế toàn bộ class `bg-dark text-white border-secondary` bằng các thẻ **Apple Liquid Glass Card** mượt mà.
  - Cột danh sách khách hàng và Cột khung hội thoại đều mang `background: rgba(255, 255, 255, 0.78); backdrop-filter: blur(28px); border: 1.5px solid rgba(255, 255, 255, 0.85); border-radius: 20px; box-shadow: var(--glass-shadow);`.
  - Bong bóng tin nhắn:
    - Khách hàng: Kính mờ trắng ngọc (`background: rgba(255, 255, 255, 0.9); border: 1.5px solid rgba(232, 222, 200, 0.8); color: #1C1917;`).
    - Quầy chi nhánh: Kính mờ hổ phách ấm (`background: rgba(255, 247, 237, 0.92); border: 1.5px solid rgba(253, 186, 116, 0.7); color: #1C1917;`).
  - Thanh nhập tin nhắn và nút đính kèm video/ảnh: Bo tròn pill capsule kính mờ.

---

### Task 3: Đồng Bộ Kênh Shipper Portal Chuẩn Kính Mờ Sáng (`shipper/orders.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/shipper/orders.html`
- **Mục tiêu:**
  - Giữ 100% hiệu ứng kính mờ (Liquid Glass) nhưng thay đổi nền từ sẫm màu sang màu trắng ngà sáng.
  - Thẻ tiêu đề Shipper: `background: rgba(255, 255, 255, 0.78); backdrop-filter: blur(28px); border: 1.5px solid rgba(255, 255, 255, 0.9); border-radius: 20px; box-shadow: var(--glass-shadow);`.
  - 4 thẻ KPI: Các phiến kính mờ độc lập, có ánh sáng màu tương ứng (Vàng hổ phách cho đang giao, Xanh ngọc cho đã giao, Đỏ son cho thất bại, Xanh dương cho COD), số to rõ nét màu `#1C1917`.
  - Thẻ kiện hàng: Nền kính mờ `rgba(255, 255, 255, 0.82)`, viền vát mép, danh sách sách bên trong nằm trên lớp kính con `rgba(0, 0, 0, 0.02)`.
  - Các nút hành động: Nút "Chỉ đường", "Gọi ngay", "Giao thành công" bo tròn pill sang trọng.

---

### Task 4: Nâng Cấp Bảng Biểu & Card Thống Kê Admin/Manager (`books/list.html`, `inventory/list.html`, `orders/list.html`, `users/list.html`)
- **Các tệp chỉnh sửa:**
  - `src/main/resources/templates/admin/inventory/list.html`
  - `src/main/resources/templates/admin/books/list.html`
  - `src/main/resources/templates/admin/orders/list.html`
  - `src/main/resources/templates/admin/users/list.html`
- **Mục tiêu:**
  - Thay đổi toàn bộ các card quản trị sang `glass-detail-card`: nền kính mờ trắng `rgba(255, 255, 255, 0.82)`, viền thấu kính vát mép `inset 0 1.5px 1.5px rgba(255, 255, 255, 0.95)`, bo góc `20px`.
  - Header bảng dữ liệu: Kính mờ trong suốt nhẹ `rgba(248, 250, 252, 0.5)`.
  - Hàng bảng dữ liệu: Hover nhẹ nhàng tạo vệt sáng ngọc `rgba(255, 255, 255, 0.6)`.

---

### Task 5: Kiểm Thử Trực Quan Đa Vai Trò (Visual QA với Playwright & Vision)
- **Kịch bản kiểm thử:**
  1. Đăng nhập `admin`: Kiểm tra Sidebar kính mờ Bạch Ngọc, Dashboard, Quản lý sách, Tồn kho.
  2. Đăng nhập `manager_td`: Kiểm tra Kênh Chat hai cột kính mờ sáng, Tồn kho chi nhánh Thủ Đức.
  3. Đăng nhập `shipper_hung`: Kiểm tra Kênh Shipper Portal với toàn bộ thẻ đơn hàng và KPI kính mờ.
  4. Chụp ảnh màn hình đối chiếu bằng Playwright (`/Users/phucdeptrai/admin_glass_perfect.png`, `/Users/phucdeptrai/manager_chat_glass_perfect.png`, `/Users/phucdeptrai/shipper_glass_perfect.png`) và thẩm định bằng `vision_analyze`.

---

## 5. Tests & Verification Commands
- **Biên dịch & Chạy Test Backend:**
```bash
mvn test
```
*Kỳ vọng:* `BUILD SUCCESS` (7/7 tests passed 100%).
- **Kiểm tra trạng thái server:**
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/login
```
*Kỳ vọng:* `200`.
- **Kiểm thử trực quan qua Playwright & Vision:**
  - Xác nhận toàn bộ 3 phân hệ (Admin, Manager, Shipper) đều đạt hiệu ứng Apple Liquid Glass trong suốt, sang trọng, nền sáng ngà hoài cổ, không còn mảng đen lỗi thời.

---

## 6. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi sử dụng `backdrop-filter: blur(...)` trên nhiều bảng dữ liệu dài, các trình duyệt cũ hoặc thiết bị yếu có thể bị giảm FPS khi cuộn trang.
  - *Giải pháp:* Tận dụng GPU compositing bằng `transform: translateZ(0)` và `-webkit-backdrop-filter`.
- **Đánh đổi:** Kính mờ đòi hỏi độ tương phản chữ phải được kiểm soát nghiêm ngặt để đảm bảo khả năng đọc (WCAG AA). Chúng tôi sử dụng màu mực in than `#1C1917` có độ tương phản $\ge 7:1$ trên nền kính trắng.
