# Kế Hoạch Đồng Bộ Toàn Diện Kênh Quản Trị Với Trang Bán Hàng Kết Hợp Hiệu Ứng Bạch Ngọc Kính Lỏng (White Apple Liquid Glass on Warm Book Canvas)

## 1. Mục Tiêu (Goal)
Đồng bộ hóa 100% ngôn ngữ thiết kế giữa Kênh Quản Trị (Admin / Store Manager / Shipper) và Trang Bán Hàng (Storefront ReBook), kết hợp toàn diện hiệu ứng **Bạch Ngọc Kính Lỏng (White Apple Liquid Glass)** khúc xạ ánh sáng cao cấp trên nền canvas ngà tối giản; đồng thời xử lý triệt để thẻ sách storefront hoà mình vào 1 màu trắng liền mạch, kích hoạt hiệu ứng kính lỏng nổi bật khi hover theo đúng yêu cầu người dùng.

---

## 2. Hiện Trạng & Phân Tích Kỹ Thuật (Current Context & Analysis)

1. **Thẻ sách Storefront chưa "hoà mình 1 màu trắng" (`image_66198f.png`):**
   - File `home.html`, `search.html`, `book-detail.html`: `.product-thumb-wrap` đang có `background: #FAF8F5;` (mảng màu be/xám quanh ảnh bìa) và `.shopee-product-card` có `border: 1px solid #ECE7DE;`.
   - Khi hiển thị trên trang nền trắng, các thẻ bị đóng hộp vuông thô cứng, tương phản màu lộ liễu.
   - **Giải pháp:** Đưa nền `.product-thumb-wrap` về `#ffffff` 100%, thẻ bình thường phẳng tiệp màu vào nền web (`border: 1px solid transparent; box-shadow: none;`). Khi rê chuột (hover), kích hoạt hiệu ứng **Kính Lỏng Nổi (Liquid Glass Hover Lift)** với viền phản quang `inset 0 1.5px 1.5px rgba(255,255,255,1)` và bóng mờ khuếch tán mềm.

2. **Kênh Quản Trị (Admin / Manager / Shipper) bị lệch pha và lạm dụng gradient sepia bẩn:**
   - **Nền Body:** Đang dùng 3 đốm `radial-gradient` vàng/nâu sepia lem nhem (`rgba(232, 222, 200, 0.6)... #F8F5EE`), tạo cảm giác nền tường cũ kỹ ẩm mốc, lệch chuẩn hoàn toàn với storefront.
   - **Phân mảnh CSS giữa các trang:**
     - `books/list.html`: Tự định nghĩa `.card-liquid-glass` với gradient 3 màu sặc sỡ (`#8C4A27` -> `#A0522D` -> `#EE4D2D`).
     - `orders/list.html`: Dùng `.ios-status-dock` với bóng đổ nặng.
     - `categories/list.html`, `stores/list.html`, `shipping-units/list.html`: Dùng Bootstrap mặc định (nút xanh `btn-primary`, bảng xám `table-light`).
     - `reports/dashboard.html`: Dùng thẻ KPI kính mờ đục với bóng xám đục.
   - **Giải pháp:** Nâng cấp toàn diện lên hệ thống **Bạch Ngọc Kính Lỏng (White Apple Liquid Glass)** đồng bộ:
     - Nền canvas mịn màng sạch sẽ `#FAFAF9` (không còn đốm gradient lốm đốm).
     - Sidebar, Topbar, Card KPI, Bảng dữ liệu chế tác từ thấu kính mờ bạch ngọc trong suốt `rgba(255, 255, 255, 0.82) - rgba(255, 255, 255, 0.92)`, khúc xạ ánh sáng `backdrop-filter: blur(28px) saturate(190%)`.
     - Vát mép thấu kính phản quang đón sáng (`inset 0 1.5px 1.5px rgba(255, 255, 255, 0.98)`).
     - Màu điểm nhấn thống nhất: Nâu da bò vintage `#8C4A27`, chữ mực than `#1C1917`.

---

## 3. Kiến Trúc Bạch Ngọc Kính Lỏng (Liquid Glass Design System Tokens)

Định nghĩa toàn bộ Design Tokens tập trung tại `admin-layout.html`:

```css
:root {
    /* Nền Canvas Sạch Sẽ (Loại bỏ triệt để đốm radial-gradient) */
    --rebook-canvas: #FAFAF9;
    --rebook-white: #FFFFFF;
    
    /* Mực & Typography */
    --rebook-ink-main: #1C1917;
    --rebook-ink-secondary: #44403C;
    --rebook-ink-muted: #78716C;
    --rebook-border: #ECE7DE;
    --rebook-border-subtle: rgba(236, 231, 222, 0.65);

    /* Màu Nhấn Thương Hiệu ReBook */
    --rebook-leather: #8C4A27;
    --rebook-leather-hover: #733615;
    --rebook-leather-soft: #F5ECE4;
    --rebook-leather-glow: rgba(140, 74, 39, 0.15);

    /* Trạng thái */
    --rebook-emerald: #059669;
    --rebook-emerald-soft: #ECFDF5;
    --rebook-amber: #B45309;
    --rebook-amber-soft: #FFFBEB;
    --rebook-rose: #DC2626;
    --rebook-rose-soft: #FEF2F2;

    /* Apple Liquid Glass Bạch Ngọc Tokens */
    --liquid-glass-bg: rgba(255, 255, 255, 0.82);
    --liquid-glass-card: rgba(255, 255, 255, 0.90);
    --liquid-glass-blur: blur(28px) saturate(190%) brightness(102%);
    --liquid-glass-border: 1.5px solid rgba(255, 255, 255, 0.92);
    --liquid-glass-rim: inset 0 1.5px 1.5px 0 rgba(255, 255, 255, 0.98), inset 0 -1px 1px 0 rgba(45, 24, 15, 0.02);
    --liquid-glass-shadow: 0 12px 32px -4px rgba(45, 24, 15, 0.05), 0 4px 12px -2px rgba(0, 0, 0, 0.02);
    --liquid-glass-hover-shadow: 0 18px 42px -4px rgba(45, 24, 15, 0.09), inset 0 2px 2px rgba(255, 255, 255, 1);
    
    /* Bo góc Squircle Apple */
    --squircle-sm: 8px;
    --squircle-md: 12px;
    --squircle-lg: 16px;
    --squircle-pill: 9999px;
}
```

---

## 4. Kế Hoạch Triển Khai Chi Tiết Từng Nhiệm Vụ (Step-by-Step Tasks)

### Nhiệm Vụ 1: Thẻ Sách Storefront Hoà Nền Trắng Thuần & Kính Lỏng Khi Hover
- **Tập tin:**
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/search.html`
  - `src/main/resources/templates/book-detail.html`
- **Mã nguồn áp dụng:**
  ```css
  /* Thẻ sản phẩm hoà mình hoàn toàn vào nền trắng */
  .shopee-product-card, .shelf-book-card {
      background: #FFFFFF !important;
      border: 1px solid transparent !important; /* Xóa bỏ viền hộp lộ liễu */
      border-radius: 14px;
      display: flex;
      flex-direction: column;
      text-decoration: none;
      overflow: hidden;
      box-shadow: none !important;
      transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
      position: relative;
      height: 100%;
  }
  
  /* Khung chứa ảnh bìa hoà mình 100% màu trắng */
  .product-thumb-wrap {
      position: relative;
      height: 210px;
      width: 100%;
      background: #FFFFFF !important; /* XÓA BỎ #FAF8F5 GÂY LỘ HỘP */
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 10px;
      overflow: hidden;
  }
  
  .product-thumb-img {
      max-height: 100%;
      max-width: 100%;
      object-fit: contain;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* Bóng êm của chính bìa sách */
      transition: transform 0.25s ease;
  }

  /* Hiệu ứng Kính Lỏng Bạch Ngọc khi hover */
  .shopee-product-card:hover, .shelf-book-card:hover {
      background: rgba(255, 255, 255, 0.95) !important;
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border-color: rgba(236, 231, 222, 0.9) !important;
      box-shadow: 
          0 14px 34px -4px rgba(45, 24, 15, 0.08), 
          inset 0 1.5px 1.5px rgba(255, 255, 255, 1) !important;
      transform: translateY(-4px);
      z-index: 2;
  }
  .shopee-product-card:hover .product-thumb-img, .shelf-book-card:hover .product-thumb-img {
      transform: scale(1.04);
  }
  ```
- **Xác thực:** Playwright chụp ảnh lưới sản phẩm trên `home.html` và `search.html`. Kiểm tra màu nền giữa card và body không có sự lệch màu hay khung viền hộp.

---

### Nhiệm Vụ 2: Tái Thiết Kế Master Layout Kính Lỏng Bạch Ngọc (`admin-layout.html`)
- **Tập tin:** `src/main/resources/templates/admin/layout/admin-layout.html`
- **Mã nguồn áp dụng:**
  1. **Nền Body & Wrapper:**
     ```css
     body {
         font-family: "Roboto", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
         background-color: var(--rebook-canvas) !important;
         background-image: none !important; /* LOẠI BỎ TRIỆT ĐỂ RADIAL GRADIENT ĐỐM */
         color: var(--rebook-ink-main);
         margin: 0;
         padding: 0;
         overflow-x: hidden;
         -webkit-font-smoothing: antialiased;
     }
     ```
  2. **Sidebar Bạch Ngọc Kính Lỏng:**
     ```css
     #adminSidebar {
         width: 260px;
         min-height: 100vh;
         background: var(--liquid-glass-bg) !important;
         backdrop-filter: var(--liquid-glass-blur) !important;
         -webkit-backdrop-filter: var(--liquid-glass-blur) !important;
         border-right: var(--liquid-glass-border) !important;
         box-shadow: 4px 0 24px rgba(45, 24, 15, 0.03), inset -1.5px 0 1.5px rgba(255, 255, 255, 0.95) !important;
         position: fixed;
         top: 0;
         left: 0;
         z-index: 1040;
     }
     .sidebar-brand {
         padding: 18px 20px;
         border-bottom: 1px solid var(--rebook-border);
         display: flex;
         align-items: center;
         gap: 10px;
         text-decoration: none;
     }
     .sidebar-brand h5 {
         font-size: 18px;
         font-weight: 800;
         color: var(--rebook-ink-main);
         margin: 0;
     }
     .sidebar-item a {
         display: flex;
         align-items: center;
         gap: 12px;
         padding: 9px 16px;
         color: var(--rebook-ink-secondary);
         text-decoration: none;
         font-size: 13.5px;
         font-weight: 500;
         border-radius: var(--squircle-pill);
         transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
     }
     .sidebar-item a:hover {
         background: rgba(255, 255, 255, 0.85);
         color: var(--rebook-leather);
         transform: translateX(3px);
     }
     .sidebar-item.active a {
         background: var(--rebook-leather) !important;
         color: #FFFFFF !important;
         font-weight: 600;
         box-shadow: 0 4px 14px var(--rebook-leather-glow), inset 0 1px 1px rgba(255, 255, 255, 0.4);
     }
     .sidebar-item.active a i {
         color: #FFFFFF !important;
     }
     ```
  3. **Topbar Kính Lỏng Cố Định:**
     ```css
     .admin-navbar {
         background: var(--liquid-glass-bg) !important;
         backdrop-filter: var(--liquid-glass-blur) !important;
         -webkit-backdrop-filter: var(--liquid-glass-blur) !important;
         border-bottom: var(--liquid-glass-border) !important;
         box-shadow: 0 4px 18px rgba(45, 24, 15, 0.025), inset 0 1.5px 1.5px rgba(255, 255, 255, 0.95) !important;
         height: 62px;
         padding: 0 24px;
         display: flex;
         align-items: center;
         justify-content: space-between;
         position: sticky;
         top: 0;
         z-index: 1030;
     }
     ```
  4. **Các Thành Phần Dùng Chung Toàn Hệ Thống:**
     ```css
     /* Thẻ Kính Lỏng (Liquid Glass Card) */
     .card-custom, .card-liquid-glass, .glass-card, .admin-card {
         background: var(--liquid-glass-card) !important;
         backdrop-filter: var(--liquid-glass-blur) !important;
         -webkit-backdrop-filter: var(--liquid-glass-blur) !important;
         border: var(--liquid-glass-border) !important;
         border-radius: var(--squircle-lg) !important;
         box-shadow: var(--liquid-glass-shadow), var(--liquid-glass-rim) !important;
         margin-bottom: 22px;
         transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
     }
     .card-custom:hover, .card-liquid-glass:hover, .glass-card:hover {
         box-shadow: var(--liquid-glass-hover-shadow) !important;
     }
     .card-header, .card-liquid-header, .glass-card-header {
         background: rgba(255, 255, 255, 0.5) !important;
         border-bottom: 1px solid var(--rebook-border) !important;
         padding: 14px 22px !important;
         font-size: 14.5px !important;
         font-weight: 700 !important;
         color: var(--rebook-ink-main) !important;
         border-radius: var(--squircle-lg) var(--squircle-lg) 0 0 !important;
     }

     /* Nút Bấm Kính Lỏng & ReBook Leather */
     .btn-theme, .btn-liquid-primary, .btn-rebook-primary {
         background: var(--rebook-leather) !important;
         color: #FFFFFF !important;
         border: 1px solid var(--rebook-leather) !important;
         border-radius: var(--squircle-md) !important;
         padding: 8px 18px !important;
         font-size: 13.5px !important;
         font-weight: 600 !important;
         box-shadow: 0 4px 12px var(--rebook-leather-glow), inset 0 1px 1px rgba(255, 255, 255, 0.3) !important;
         transition: all 0.2s ease !important;
     }
     .btn-theme:hover, .btn-liquid-primary:hover, .btn-rebook-primary:hover {
         background: var(--rebook-leather-hover) !important;
         border-color: var(--rebook-leather-hover) !important;
         transform: translateY(-1px);
         box-shadow: 0 6px 16px var(--rebook-leather-glow) !important;
     }
     .btn-outline-theme, .btn-liquid-secondary, .btn-rebook-secondary {
         background: rgba(255, 255, 255, 0.8) !important;
         backdrop-filter: blur(12px) !important;
         color: var(--rebook-ink-main) !important;
         border: 1px solid var(--rebook-border) !important;
         border-radius: var(--squircle-md) !important;
         padding: 8px 18px !important;
         font-size: 13.5px !important;
         font-weight: 500 !important;
         box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.9) !important;
     }
     .btn-outline-theme:hover, .btn-liquid-secondary:hover {
         background: #FFFFFF !important;
         border-color: var(--rebook-leather) !important;
         color: var(--rebook-leather) !important;
     }

     /* Bảng Dữ Liệu Kính Mờ */
     .table {
         color: var(--rebook-ink-main) !important;
         margin-bottom: 0 !important;
     }
     .table thead th {
         background: rgba(250, 248, 245, 0.85) !important;
         color: var(--rebook-ink-muted) !important;
         font-size: 11.5px !important;
         font-weight: 700 !important;
         text-transform: uppercase !important;
         letter-spacing: 0.5px !important;
         border-bottom: 1.5px solid var(--rebook-border) !important;
         padding: 12px 18px !important;
     }
     .table tbody td {
         padding: 13px 18px !important;
         border-bottom: 1px solid var(--rebook-border-subtle) !important;
         font-size: 13.5px !important;
         background: transparent !important;
     }
     .table-hover tbody tr:hover td {
         background: rgba(140, 74, 39, 0.025) !important;
     }

     /* Form Controls */
     .form-control, .form-select, .glass-input, .glass-select {
         background: rgba(255, 255, 255, 0.9) !important;
         backdrop-filter: blur(12px) !important;
         border: 1px solid var(--rebook-border) !important;
         border-radius: var(--squircle-md) !important;
         padding: 9px 14px !important;
         font-size: 13.5px !important;
         color: var(--rebook-ink-main) !important;
         box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.02) !important;
     }
     .form-control:focus, .form-select:focus {
         background: #FFFFFF !important;
         border-color: var(--rebook-leather) !important;
         box-shadow: 0 0 0 3px rgba(140, 74, 39, 0.12), inset 0 1px 1px rgba(255, 255, 255, 1) !important;
     }
     ```

---

### Nhiệm Vụ 3: Dọn Dẹp CSS Cục Bộ & Đồng Bộ 12 Màn Hình Quản Trị
- **Các màn hình thực hiện:**
  1. `admin/reports/dashboard.html`: Nâng cấp 4 KPI sang thẻ Kính Lỏng Bạch Ngọc, icon tròn màu ấm, số doanh thu sắc nét.
  2. `admin/books/list.html` & `books/form.html`: Xóa bỏ 150 dòng CSS cục bộ cũ, chuyển sang thẻ kính lỏng chuẩn, thumbnail bìa sách hiển thị nổi khối.
  3. `admin/categories/list.html` & `categories/form.html`: Bỏ sạch nút xanh dương và bảng Bootstrap xám, dùng bảng kính lỏng và nút nâu da bò.
  4. `admin/orders/list.html` & `orders/detail.html`: Chuẩn hóa thanh tab lọc trạng thái phẳng kính mờ, thẻ kiện hàng và mẫu in phiếu giao hàng A4/A5 sạch sẽ.
  5. `admin/inventory/list.html`, `transfer.html`, `transfers.html`: 4 thẻ lọc tồn kho kính lỏng 1 chạm, bảng điều chuyển đồng bộ.
  6. `admin/consignments/list.html` & `consignments/detail.html`: Bảng phiếu ký gửi và modal thẩm định kính lỏng.
  7. `admin/stores/list.html`, `stores/form.html`, `users/list.html`: Danh sách chi nhánh, modal cấp tài khoản nhân viên và reset mật khẩu.
  8. `admin/vouchers/list.html`, `vouchers/form.html`, `shipping-units/list.html`, `shipping-units/form.html`: Bảng mã giảm giá và đơn vị vận chuyển.
  9. `admin/chat/index.html` & `shipper/orders.html`: Khung chat và cổng shipper kính lỏng sáng màu.

---

## 5. Kiểm Thử & Nghiệm Thu (Validation & Verification)

1. **Unit Test Backend:**
   ```bash
   mvn test
   ```
   *Yêu cầu:* 7/7 tests passed 100%.

2. **Visual Inspection (Playwright & Vision):**
   - Storefront (`/` và `/search`): Thẻ sách tiệp màu nền trắng 100% không viền hộp; hover kích hoạt hiệu ứng kính lỏng nổi bật.
   - Admin Layout (`/admin/reports`): Nền không còn đốm radial-gradient, sidebar và topbar kính lỏng bạch ngọc trong suốt tinh tế.
   - Toàn bộ 12 màn hình admin: Triệt tiêu các nút xanh dương Bootstrap và các gradient 3 màu lòe loẹt.

---
*Tài liệu kế hoạch cập nhật theo yêu cầu Kính Lỏng Bạch Ngọc vào ngày 12/10/2026.*
