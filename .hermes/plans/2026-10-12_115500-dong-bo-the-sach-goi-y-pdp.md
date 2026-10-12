# Kế Hoạch Triển Khai: Tối Ưu Hóa & Đồng Bộ Thẻ Sách Gợi Ý Trên Trang Chi Tiết Sản Phẩm (Recommended Books Minimalist Redesign on PDP)

## 1. Goal
Đồng bộ hóa toàn diện 2 khối sách gợi ý chân trang "Các Tựa Sách Gợi Ý Cùng Thể Loại" và "Có Thể Bạn Cũng Thích" trên trang chi tiết sản phẩm (`/books/{id}` - `book-detail.html`) theo chuẩn thiết kế **Tiệm Sách Cổ Điển Tối Giản (Minimalist Antique Bookhouse)** đã áp dụng trên trang chủ và trang tìm kiếm: loại bỏ góc vuông 4px thô cứng, gỡ bỏ các badge neon dán đè bìa sách, hiển thị tên tác giả trang nhã và chuyển màu giá sang nâu da bò trầm `#8C4A27`.

---

## 2. Current Context & Assumptions
- **Trang mục tiêu:** Trang chi tiết sản phẩm `/books/{id}` (ví dụ: `http://localhost:8080/books/120`), quản lý bởi template `src/main/resources/templates/book-detail.html`.
- **Hiện trạng tồn tại tại 2 khối gợi ý chân trang (dòng 1812 - 1886):**
  1. **Khối 6 (`otherStoreBooks`):** "CÁC TỰA SÁCH GỢI Ý CÙNG THỂ LOẠI".
  2. **Khối 7 (`relatedBooks`):** "CÓ THỂ BẠN CŨNG THÍCH".
- **Các khuyết điểm thẩm mỹ đang tồn đọng:**
  - Khung bao kệ sách và thẻ sách dùng góc vuông thô cứng `border-radius: 4px`, viền xám đen xỉn màu `border: 1px solid rgba(0,0,0,0.08)`.
  - Thẻ sách bị dán 2 huy hiệu neon che góc ảnh bìa: tag xanh lục `rgba(22,163,74,0.9)` góc trái và tag đỏ `rgba(254,238,234,0.95)` góc phải.
  - Giá bán hiển thị màu đỏ tươi chói gắt `color: #ee4d2d; font-size: 14px; font-weight: 800;`.
  - Hiển thị đánh giá sao giả lập cố định `<i class="fa-solid fa-star fa-xs"></i> 4.9` gây rối mắt.
  - Dùng CSS inline và JS inline `onmouseover="this.style.transform='translateY(-3px)'"` rời rạc, không đồng bộ với hệ thống CSS chung.

---

## 3. Architecture & Proposed Approach
- **Đồng bộ hóa Component (Design Parity with Home & Search):**
  - Chuyển toàn bộ thẻ sách của 2 khối gợi ý sang cấu trúc thẻ sách chuẩn của ReBook: `border-radius: 14px`, viền chỉ be giấy `#ECE7DE`, nền trắng tinh khiết `#FFFFFF`, bóng đổ nhẹ `0 1px 4px rgba(45, 24, 15, 0.03)`, hover lift mượt mà `translateY(-4px)` kèm bóng khuếch tán `0 10px 28px rgba(45, 24, 15, 0.08)`.
  - Khung ảnh bìa cao `200px` trên nền giấy ngà sáng `#FAF8F5`, ảnh bìa bo góc nhẹ `4px`.
  - Badge độ mới dạng viên nhộng bán trong suốt kính mờ `card-condition-pill` (`backdrop-filter: blur(8px)`), nhãn giảm giá thanh nhã `card-discount-pill`.
  - Bổ sung tên tác giả kèm icon ngòi bút nhỏ `<i class="fa-solid fa-pen-nib text-muted"></i>`.
  - Giá bán hiển thị màu nâu da bò trầm `#8C4A27` hoài cổ, số lượng đã bán hiển thị khiêm tốn màu `#A8A29E`.
- **Nâng cấp Khối Bao Kệ Sách (`product-shelf-section`):**
  - Bo góc Squircle `16px`, viền be `#ECE7DE`, tiêu đề chữ hoa đậm nét `#1C1917`, nút liên kết "Xem Tất Cả" chuyển sang màu nâu da bò `#8C4A27`.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Bổ Sung Lớp CSS Cho Kệ Sách & Thẻ Sách Gợi Ý (`book-detail.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/book-detail.html`
- **Mục tiêu:** Thêm CSS chuyên biệt cho kệ sách và thẻ sách gợi ý vào thẻ `<style>` của `book-detail.html`.
- **Mã nguồn CSS bổ sung:**
```css
        /* ================= SHELF RECOMMENDED BOOKS (MINIMALIST ANTIQUE) ================= */
        .product-shelf-section {
            margin-top: 24px;
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #ECE7DE;
            padding: 22px 24px;
            box-shadow: 0 1px 4px rgba(45, 24, 15, 0.03);
        }

        .shelf-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 18px;
            padding-bottom: 12px;
            border-bottom: 1px solid #F5F2EB;
        }

        .shelf-title {
            font-size: 15px;
            font-weight: 800;
            color: #1C1917;
            text-transform: uppercase;
            letter-spacing: 0.4px;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .shelf-view-all {
            font-size: 13px;
            font-weight: 700;
            color: #8C4A27;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
            transition: color 0.15s ease;
        }
        .shelf-view-all:hover {
            color: #ee4d2d;
        }

        .shelf-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
            gap: 14px;
        }

        .shelf-book-card {
            background: #ffffff;
            border: 1px solid #ECE7DE;
            border-radius: 14px;
            overflow: hidden;
            text-decoration: none;
            color: inherit;
            display: flex;
            flex-direction: column;
            box-shadow: 0 1px 4px rgba(45, 24, 15, 0.03);
            transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
            position: relative;
            height: 100%;
        }
        .shelf-book-card:hover {
            transform: translateY(-4px);
            border-color: rgba(140, 74, 39, 0.35);
            box-shadow: 0 10px 28px rgba(45, 24, 15, 0.08);
        }

        .shelf-thumb-wrap {
            position: relative;
            width: 100%;
            height: 200px;
            background: #FAF8F5;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 12px;
            overflow: hidden;
        }
        .shelf-thumb-img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
            border-radius: 4px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
            transition: transform 0.25s ease;
        }
        .shelf-book-card:hover .shelf-thumb-img {
            transform: scale(1.03);
        }

        .shelf-condition-pill {
            position: absolute;
            top: 10px;
            left: 10px;
            background: rgba(255, 255, 255, 0.92);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            border: 1px solid rgba(232, 222, 200, 0.95);
            color: #57534E;
            font-size: 10px;
            font-weight: 700;
            padding: 2.5px 7px;
            border-radius: 9999px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
            letter-spacing: 0.2px;
        }

        .shelf-discount-pill {
            position: absolute;
            top: 10px;
            right: 10px;
            background: rgba(254, 242, 242, 0.95);
            color: #DC2626;
            border: 1px solid rgba(254, 202, 202, 0.85);
            font-size: 10px;
            font-weight: 800;
            padding: 2.5px 6px;
            border-radius: 9999px;
            box-shadow: 0 2px 6px rgba(220, 38, 38, 0.08);
        }

        .shelf-info-wrap {
            padding: 14px 12px 12px;
            display: flex;
            flex-direction: column;
            flex: 1;
            justify-content: space-between;
        }

        .shelf-book-title {
            font-size: 13px;
            font-weight: 700;
            color: #1C1917;
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            margin-bottom: 4px;
            height: 36px;
        }

        .shelf-author-meta {
            font-size: 11px;
            color: #78716C;
            margin-bottom: 8px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .shelf-price-row {
            display: flex;
            align-items: baseline;
            justify-content: space-between;
            margin-top: auto;
            padding-top: 8px;
            border-top: 1px solid #F5F2EB;
        }

        .shelf-price-val {
            font-size: 14.5px;
            font-weight: 800;
            color: #8C4A27 !important;
            letter-spacing: -0.2px;
        }

        .shelf-sold-val {
            font-size: 11px;
            color: #A8A29E;
            font-weight: 500;
        }
```

---

### Task 2: Cập Nhật HTML Khối 6: Gợi Ý Cùng Thể Loại (`book-detail.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/book-detail.html`
- **Mục tiêu:** Thay thế mã nguồn cũ tại dòng 1812-1848 bằng markup sạch, semantic, dùng các class vừa tạo.
- **Mã nguồn HTML mới:**
```html
        <!-- 6. KHỐI CÁC SẢN PHẨM GỢI Ý CÙNG THỂ LOẠI (TỐI GIẢN HOÀI CỔ) -->
        <section class="product-shelf-section" th:if="${otherStoreBooks != null && !otherStoreBooks.isEmpty()}">
            <div class="shelf-header">
                <h2 class="shelf-title">
                    <i class="fa-solid fa-bookmark text-warning opacity-75"></i>
                    <span>CÁC TỰA SÁCH GỢI Ý CÙNG THỂ LOẠI</span>
                </h2>
                <a th:href="@{/search(category=${book.category != null ? book.category.id : ''})}" class="shelf-view-all">
                    Xem Tất Cả <i class="fa-solid fa-chevron-right fa-xs"></i>
                </a>
            </div>
            <div class="shelf-grid">
                <a th:each="ob : ${otherStoreBooks}" th:href="@{/books/{id}(id=${ob.id})}" class="shelf-book-card">
                    <div class="shelf-thumb-wrap">
                        <img th:src="${ob.primaryImageUrl != null ? ob.primaryImageUrl : '/images/books/book_1.jpg'}" 
                             th:alt="${ob.title}" class="shelf-thumb-img" loading="lazy"
                             onerror="this.src='/images/books/book_1.jpg';">
                        <span class="shelf-condition-pill" th:text="${ob.conditionPercent} + '% mới'">90% mới</span>
                        <span class="shelf-discount-pill" th:if="${ob.discountPercent != null && ob.discountPercent > 0}" 
                              th:text="'-' + ${ob.discountPercent} + '%'">-20%</span>
                    </div>
                    <div class="shelf-info-wrap">
                        <div>
                            <div class="shelf-book-title" th:text="${ob.title}">Tên sách</div>
                            <div class="shelf-author-meta">
                                <i class="fa-solid fa-pen-nib text-muted" style="font-size: 10px;"></i>
                                <span th:text="${ob.author != null ? ob.author : 'Nhiều tác giả'}">Tác giả</span>
                            </div>
                        </div>
                        <div class="shelf-price-row">
                            <span class="shelf-price-val" th:text="'₫' + ${#numbers.formatDecimal(ob.minPrice, 0, 'POINT', 0, 'COMMA')}">₫50.000</span>
                            <span class="shelf-sold-val" th:text="'Đã bán ' + ${ob.totalSold != null ? ob.totalSold : 12}">Đã bán 12</span>
                        </div>
                    </div>
                </a>
            </div>
        </section>
```

---

### Task 3: Cập Nhật HTML Khối 7: Có Thể Bạn Cũng Thích (`book-detail.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/book-detail.html`
- **Mục tiêu:** Thay thế mã nguồn cũ tại dòng 1850-1886 bằng markup sạch, semantic tương ứng.
- **Mã nguồn HTML mới:**
```html
        <!-- 7. KHỐI CÓ THỂ BẠN CŨNG THÍCH (TỐI GIẢN HOÀI CỔ) -->
        <section class="product-shelf-section" th:if="${relatedBooks != null && !relatedBooks.isEmpty()}" style="margin-bottom: 50px;">
            <div class="shelf-header">
                <h2 class="shelf-title">
                    <i class="fa-solid fa-heart text-danger opacity-75"></i>
                    <span>CÓ THỂ BẠN CŨNG THÍCH</span>
                </h2>
                <a th:href="@{/search(categoryId=${book.category != null ? book.category.id : 1})}" class="shelf-view-all">
                    Xem Tất Cả <i class="fa-solid fa-chevron-right fa-xs"></i>
                </a>
            </div>
            <div class="shelf-grid">
                <a th:each="rb : ${relatedBooks}" th:href="@{/books/{id}(id=${rb.id})}" class="shelf-book-card">
                    <div class="shelf-thumb-wrap">
                        <img th:src="${rb.primaryImageUrl != null ? rb.primaryImageUrl : '/images/books/book_1.jpg'}" 
                             th:alt="${rb.title}" class="shelf-thumb-img" loading="lazy"
                             onerror="this.src='/images/books/book_1.jpg';">
                        <span class="shelf-condition-pill" th:text="${rb.conditionPercent} + '% mới'">90% mới</span>
                        <span class="shelf-discount-pill" th:if="${rb.discountPercent != null && rb.discountPercent > 0}" 
                              th:text="'-' + ${rb.discountPercent} + '%'">-20%</span>
                    </div>
                    <div class="shelf-info-wrap">
                        <div>
                            <div class="shelf-book-title" th:text="${rb.title}">Tên sách</div>
                            <div class="shelf-author-meta">
                                <i class="fa-solid fa-pen-nib text-muted" style="font-size: 10px;"></i>
                                <span th:text="${rb.author != null ? rb.author : 'Nhiều tác giả'}">Tác giả</span>
                            </div>
                        </div>
                        <div class="shelf-price-row">
                            <span class="shelf-price-val" th:text="'₫' + ${#numbers.formatDecimal(rb.minPrice, 0, 'POINT', 0, 'COMMA')}">₫50.000</span>
                            <span class="shelf-sold-val" th:text="'Đã bán ' + ${rb.totalSold != null ? rb.totalSold : 12}">Đã bán 12</span>
                        </div>
                    </div>
                </a>
            </div>
        </section>
```

---

### Task 4: Kiểm Thử Tự Động & Đồng Bộ Hot-Sync
- **Lệnh thực thi:**
  1. Đồng bộ tệp template sang thư mục `target/classes/templates/book-detail.html`.
  2. Chạy test suite Maven:
     ```bash
     mvn test
     ```
     *Kỳ vọng:* `BUILD SUCCESS` (7/7 tests passed 100%).

---

### Task 5: Kiểm Thử Trực Quan Với Playwright & Vision
- **Kịch bản kiểm thử:**
  1. Dùng Playwright mở URL `http://localhost:8080/books/120`.
  2. Cuộn xuống chân trang để hiển thị trọn vẹn 2 khối gợi ý.
  3. Chụp ảnh màn hình lưu tại `/Users/phucdeptrai/pdp_120_shelf_redesigned.png`.
  4. Phân tích ảnh bằng `vision_analyze` để kiểm chứng:
     - Góc thẻ sách đã bo tròn Apple Squircle 14px mềm mại (không còn 4px hộp nhọn).
     - Badge độ mới dạng viên nhộng trong suốt mờ kính nhẹ nhàng (không còn màu xanh lục đè bìa).
     - Giá bán chuyển sang màu nâu da bò trầm `#8C4A27`.
     - Tên tác giả hiển thị đồng bộ kèm icon ngòi bút.
     - Toàn bộ giao diện chân trang `/books/120` hài hòa 100% với trang chủ và trang tìm kiếm.

---

## 5. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi danh sách sách gợi ý có tựa đề quá dài, có bị tràn khung không?
  - *Giải pháp:* Đã cố định `height: 36px` và `-webkit-line-clamp: 2`, đảm bảo mọi thẻ sách luôn có chiều cao bằng nhau tuyệt đối trên toàn bộ hàng ngang.
- **Tính tương thích:** Không thay đổi bất kỳ logic Controller hay Entity nào, hoàn toàn là tối ưu tầng hiển thị HTML/CSS.
