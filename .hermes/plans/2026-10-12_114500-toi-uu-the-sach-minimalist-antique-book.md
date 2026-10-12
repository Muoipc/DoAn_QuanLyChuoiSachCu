# Kế Hoạch Triển Khai: Tối Ưu Hóa Thẻ Sách Tối Giản Hoài Cổ (Minimalist Antique Book Card Redesign)

## 1. Goal
Tái thiết kế toàn diện thẻ sách (Book Card) và thanh phân loại Feed trên trang chủ (`home.html`) và trang tìm kiếm (`search.html`) theo phong cách **Tiệm Sách Cổ Điển Tối Giản (Minimalist Antique Bookhouse)**: loại bỏ các huy hiệu dán đè che bìa sách lòe loẹt, xóa bỏ dòng địa chỉ lặp lại thừa thãi, bổ sung tên tác giả thanh nhã và tinh chỉnh bố cục thẻ sách với tỷ lệ thoáng đãng, sắc nét.

---

## 2. Current Context & Assumptions
- **Hiện trạng hiển thị (dựa trên ảnh chụp thực tế `image_3ab002.png`):**
  - **Badge chồng chéo che ảnh bìa:** Mỗi thẻ sách đang bị dán 2 huy hiệu góc: tag giảm giá vàng chói (`-40%`) ở góc trên phải và tag độ mới xanh lục (`TỪ 95% MỚI`) ở góc dưới trái. Hai mảng màu neon này đè lên ảnh bìa, tạo cảm giác như trang web bán hàng xả kho đại trà, phá vỡ tính thẩm mỹ hoài cổ của tiệm sách cũ ReBook.
  - **Dòng thông tin rác lặp lại:** Dòng `📍 Có tại: CN Thủ Đức, Q.1` lặp lại trên 100% các thẻ sách, gây rối mắt và lãng phí không gian dọc mà không mang lại giá trị phân biệt.
  - **Thiếu thông tin tác giả:** Đối với người mua sách (đặc biệt là sách cũ, sách văn học, triết học), tên tác giả là tiêu chí nhận diện quan trọng hàng đầu nhưng lại bị ẩn mất trên thẻ trang chủ.
  - **Cụm giá cả và thanh Tab:** Giá bán đang dùng màu đỏ tươi chói gắt `#EE4D2D`, thanh tab feed ("Dành cho bạn", "Mới nhất", "Bán chạy nhất") có dòng chữ cam kết xanh lá cây cồng kềnh.

---

## 3. Architecture & Proposed Approach
- **Triết lý Tinh Giản (Restrained Editorial Book Aesthetic):** Trả lại sự tập trung vào cuốn sách — gỡ bỏ toàn bộ badge che bìa sách; thay bằng một nhãn capsule độ mới bán trong suốt cực kỳ thanh nhã (ví dụ: `95% mới` hoặc `Như mới`) đặt khéo léo ở góc ảnh.
- **Cấu trúc Thẻ Sách Chuẩn Thư Tịch (Book Collector Card Hierarchy):**
  1. *Khung ảnh bìa:* Ảnh bìa sách đặt gọn gàng trên nền giấy ngà sáng `#FAF8F5`, bóng đổ nhẹ nổi tự nhiên khi hover.
  2. *Tựa sách:* Tiêu chuẩn 2 dòng (`line-clamp: 2`), font chữ đậm màu than `#1C1917`.
  3. *Tên tác giả:* Dòng chữ nghiêng/ghi xám thanh lịch `#78716C` kèm icon ngòi bút nhỏ.
  4. *Cụm giá & Đã bán:* Giá bán màu nâu da bò trầm `#8C4A27` hoặc cam đất cổ điển, giá gốc gạch ngang mờ (nếu có giảm giá), số lượng đã bán tinh tế bên phải.
- **Thanh Tab Phân Loại Segmented Pill:** Thiết kế lại cụm tab "Dành cho bạn / Mới nhất / Bán chạy" dạng capsule phẳng tối giản, loại bỏ icon rườm rà.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Thiết Kế Lại CSS Cho Thẻ Sách Tối Giản (`home.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/home.html`
- **Mục tiêu:** Thay thế CSS `.shopee-product-card`, `.product-thumb-wrap`, `.card-discount-tag`, `.card-condition-tag`, `.product-store-smart-tag`.
- **Mã nguồn CSS cập nhật:**
```css
        /* ================= 6. MINIMALIST ANTIQUE BOOK CARD ================= */
        .shopee-product-card {
            background: #ffffff;
            border: 1px solid #ECE7DE;
            border-radius: 14px;
            display: flex;
            flex-direction: column;
            text-decoration: none;
            overflow: hidden;
            box-shadow: 0 1px 3px rgba(45, 24, 15, 0.03);
            transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
            position: relative;
            height: 100%;
        }
        .shopee-product-card:hover {
            border-color: rgba(140, 74, 39, 0.35);
            box-shadow: 0 10px 28px rgba(45, 24, 15, 0.08);
            transform: translateY(-4px);
        }

        .product-thumb-wrap {
            position: relative;
            height: 210px;
            width: 100%;
            background: #FAF8F5;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 12px;
            overflow: hidden;
        }

        .product-thumb-img {
            max-height: 100%;
            max-width: 100%;
            object-fit: contain;
            border-radius: 4px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            transition: transform 0.25s ease;
        }
        .shopee-product-card:hover .product-thumb-img {
            transform: scale(1.03);
        }

        /* Badge Độ Mới Tinh Tế (Thanh nhã, không che khuất bìa sách) */
        .card-condition-pill {
            position: absolute;
            top: 10px;
            left: 10px;
            background: rgba(255, 255, 255, 0.88);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            border: 1px solid rgba(232, 222, 200, 0.9);
            color: #57534E;
            font-size: 10.5px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 9999px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
            letter-spacing: 0.2px;
        }

        /* Badge Giảm Giá Nhẹ Nhàng (Chỉ hiện khi có sale, nền be ấm) */
        .card-discount-pill {
            position: absolute;
            top: 10px;
            right: 10px;
            background: rgba(254, 242, 242, 0.92);
            color: #DC2626;
            border: 1px solid rgba(254, 202, 202, 0.8);
            font-size: 10.5px;
            font-weight: 800;
            padding: 3px 7px;
            border-radius: 9999px;
        }

        .product-info-wrap {
            padding: 14px 14px 12px;
            display: flex;
            flex-direction: column;
            flex: 1;
            justify-content: space-between;
        }

        .product-title {
            font-size: 13.5px;
            font-weight: 700;
            color: #1C1917;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            margin-bottom: 4px;
            height: 38px;
        }

        .product-author-meta {
            font-size: 11.5px;
            color: #78716C;
            margin-bottom: 10px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .product-price-row {
            display: flex;
            align-items: baseline;
            justify-content: space-between;
            margin-top: auto;
            padding-top: 6px;
            border-top: 1px solid #F5F2EB;
        }

        .price-current {
            font-size: 15px;
            font-weight: 800;
            color: #8C4A27 !important; /* Nâu da thuộc cổ điển */
            letter-spacing: -0.2px;
        }

        .price-sold-count {
            font-size: 11px;
            color: #A8A29E;
            font-weight: 500;
        }

        /* Thanh Tab Phân Loại Tối Giản */
        .chotot-feed-tabs-bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
            padding-bottom: 12px;
            border-bottom: 1.5px solid #ECE7DE;
        }

        .chotot-feed-tabs {
            display: inline-flex;
            gap: 8px;
        }

        .chotot-feed-tab {
            padding: 8px 18px;
            border-radius: 9999px;
            font-size: 13.5px;
            font-weight: 600;
            color: #57534E;
            text-decoration: none;
            background: transparent;
            transition: all 0.2s ease;
        }
        .chotot-feed-tab:hover {
            color: #1C1917;
            background: #F5F2EB;
        }
        .chotot-feed-tab.active {
            background: #1C1917;
            color: #FFFFFF;
            font-weight: 700;
        }
```

---

### Task 2: Tinh Giản Cấu Trúc HTML Thẻ Sách Trên Trang Chủ (`home.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/home.html`
- **Mục tiêu:**
  - Bỏ dòng `📍 Có tại: CN Thủ Đức, Q.1`.
  - Thay badge góc bằng `.card-condition-pill` và `.card-discount-pill`.
  - Bổ sung tên tác giả `${book.author}`.
- **Mã nguồn HTML cập nhật (khoảng dòng 2030 - 2060):**
```html
            <!-- Lưới 6 cột chứa toàn bộ 20 đầu sách tuyển chọn -->
            <div class="daily-grid-6cols">
                <a th:each="book : ${dailyBooks}" th:href="@{'/books/' + ${book.id}}" class="shopee-product-card">
                    <div class="product-thumb-wrap">
                        <img th:src="${book.primaryImageUrl != null ? book.primaryImageUrl : '/images/books/book_1.jpg'}" 
                             th:alt="${book.title}" class="product-thumb-img" loading="lazy"
                             onerror="this.src='/images/books/book_1.jpg';">
                        
                        <!-- Badge Độ mới thanh lịch -->
                        <span class="card-condition-pill" th:text="${book.conditionPercent} + '% mới'">95% mới</span>

                        <!-- Badge Giảm giá nhẹ nhàng (chỉ hiện khi có sale) -->
                        <span class="card-discount-pill" th:if="${book.maxDiscountPercent > 0}" th:text="'-' + ${book.maxDiscountPercent} + '%'">-40%</span>
                    </div>

                    <div class="product-info-wrap">
                        <h4 class="product-title" th:text="${book.title}">Tên sách cũ tuyển chọn</h4>

                        <!-- Tác giả sách hoài cổ -->
                        <div class="product-author-meta">
                            <i class="fa-solid fa-pen-nib text-muted" style="font-size: 10px;"></i>
                            <span th:text="${book.author != null && !#strings.isEmpty(book.author) ? book.author : 'Nhiều tác giả'}">Tác giả</span>
                        </div>

                        <div class="product-price-row">
                            <span class="price-current" th:text="'₫' + ${#numbers.formatDecimal(book.minPrice != null ? book.minPrice : book.price, 0, 'POINT', 0, 'COMMA')}">₫68.000</span>
                            <span class="price-sold-count" th:text="'Đã bán ' + ${book.totalSold != null ? book.totalSold : 12}">Đã bán 12</span>
                        </div>
                    </div>
                </a>
            </div>
```

---

### Task 3: Đồng Bộ Thẻ Sách Trên Trang Tìm Kiếm (`search.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/search.html`
- **Mục tiêu:** Áp dụng cùng ngôn ngữ thiết kế: bỏ dòng `📍 Có tại: 5 Chi Nhánh`, tinh chỉnh badge độ mới thành pill capsule, đồng bộ màu giá `#8C4A27`.
- **Mã nguồn HTML cập nhật (khoảng dòng 1373 - 1395):**
```html
                <a th:each="book : ${books}" th:href="@{'/books/' + ${book.id}}" class="shopee-product-card">
                    <div class="product-thumb-wrap">
                        <img th:src="${book.primaryImageUrl != null ? book.primaryImageUrl : '/images/books/book_1.jpg'}" 
                             th:alt="${book.title}" class="product-thumb-img" loading="lazy"
                             onerror="this.src='/images/books/book_1.jpg';">
                        <span class="card-condition-pill" th:text="${book.conditionPercent} + '% mới'">95% mới</span>
                        <span class="card-discount-pill" th:if="${book.maxDiscountPercent > 0}" th:text="'-' + ${book.maxDiscountPercent} + '%'">-40%</span>
                    </div>
                    <div class="product-info-wrap">
                        <h4 class="product-title" th:text="${book.title}">Tên sách</h4>
                        
                        <div class="product-author-meta">
                            <i class="fa-solid fa-pen-nib text-muted" style="font-size: 10px;"></i>
                            <span th:text="${book.author != null && !#strings.isEmpty(book.author) ? book.author : 'Nhiều tác giả'}">Tác giả</span>
                        </div>

                        <div class="product-price-row">
                            <span class="price-current" th:text="'₫' + ${#numbers.formatDecimal(book.minPrice != null ? book.minPrice : book.price, 0, 'POINT', 0, 'COMMA')}">₫68.000</span>
                            <span class="price-sold-count" th:text="'Đã bán ' + ${book.totalSold != null ? book.totalSold : 12}">Đã bán 12</span>
                        </div>
                    </div>
                </a>
```

---

### Task 4: Kiểm Thử Trực Quan & So Sánh Đối Chiếu
- **Kịch bản kiểm thử:**
  1. Biên dịch và kiểm tra không có lỗi cú pháp template Thymeleaf:
     ```bash
     mvn test
     ```
  2. Dùng Playwright chụp ảnh màn hình trang chủ `http://localhost:8080/` tại phần lưới sách:
     ```javascript
     await page.goto('http://localhost:8080/', { waitUntil: 'domcontentloaded' });
     await page.locator('.daily-discover-section').screenshot({ path: '/Users/phucdeptrai/books_minimalist_redesign.png' });
     ```
  3. Sử dụng `vision_analyze` kiểm tra đối chiếu với ảnh gốc `image_3ab002.png`:
     - Xác nhận đã hết badge vàng/xanh neon che bìa sách.
     - Xác nhận đã biến mất dòng "Có tại: CN Thủ Đức..." lặp lại.
     - Xác nhận hiển thị tên tác giả trang nhã và giá màu nâu da bò `#8C4A27` tinh tế.

---

## 5. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi xóa dòng chi nhánh, khách hàng có thắc mắc sách đó có sẵn tại chi nhánh nào không?
  - *Giải pháp:* Khách hàng có thể lọc theo chi nhánh ngay trên thanh tìm kiếm "Bộ lọc" (đã hoàn thiện ở bước trước), hoặc xem chi tiết số lượng tồn kho từng chi nhánh tại trang chi tiết sách (`/books/{id}`). Việc giữ sạch sẽ trang chủ mang lại trải nghiệm duyệt sách thoải mái hơn rất nhiều.
- **Đánh đổi:** Loại bỏ màu sắc rực rỡ khiến thẻ sách trầm tĩnh hơn, nhưng tăng tính chuyên nghiệp, định vị thương hiệu ReBook là chuỗi sách tuyển chọn chất lượng.
