# Kế Hoạch Triển Khai: Đồng Bộ Bộ Lọc Tìm Kiếm Header với Trang Đầu (Synchronize Header Filter with Home Page)

## 1. Goal
Đồng bộ hóa 100% phần nút kích hoạt và bảng popover bộ lọc trên thanh tìm kiếm Header (`fragments/header.html`) với thanh tìm kiếm của trang đầu (`home.html`): thay nút vị trí `[Pin] Toàn TP.HCM` bằng nút `[Sliders] Bộ lọc`, bổ sung đầy đủ các tiêu chí lọc chuyên sâu (Sắp xếp theo, Danh mục thể loại, Khoảng giá nhanh, Chi nhánh cửa hàng) và đồng bộ hành vi JavaScript gửi form tìm kiếm.

---

## 2. Current Context & Assumptions
- **Hiện trạng trên trang đầu (`home.html`):**
  - Nút bấm trên thanh Hero Search: Icon bộ lọc `<i class="fa-solid fa-sliders" style="color:#EE4D2D;"></i>`, nhãn chữ `Bộ lọc` (font-weight: 700), mũi tên xổ xuống.
  - Khi bấm mở popover:
    1. **Sắp xếp theo:** 5 chip radio (`Phổ biến`, `Mới nhất`, `Bán chạy`, `Giá: Thấp → Cao`, `Giá: Cao → Thấp`).
    2. **Danh mục thể loại:** Dropdown chọn danh mục (`cat`).
    3. **Khoảng giá (₫):** Ô nhập từ giá - đến giá và 4 chip chọn nhanh (`Dưới 50k`, `50k - 100k`, `100k - 200k`, `Trên 200k`).
    4. **Chi nhánh cửa hàng:** Dropdown chọn chi nhánh (`store`).
    5. **Hành động footer:** Nút `Đặt lại` và nút `Áp dụng bộ lọc`.
- **Hiện trạng tại thanh Header con (`fragments/header.html` - như ảnh đính kèm `image_042fd1.png`):**
  - Nút bấm hiển thị: Icon ghim đỏ `location-pin-svg`, nhãn chữ `Toàn TP.HCM` (khiến người dùng nhầm tưởng đây chỉ là nút chọn vị trí, không phải bộ lọc đa tiêu chí).
  - Popover bên trong: Thiếu hoàn toàn mục **Sắp xếp theo** (Sort by), thiếu các chip chọn khoảng giá nhanh, bố cục và nhãn tiêu đề không đồng nhất với trang đầu.
  - Khi người dùng điều hướng từ trang chủ sang trang chi tiết sách (`/books/{id}`), trang tìm kiếm (`/search`), giỏ hàng, v.v., giao diện thanh tìm kiếm bị phân mảnh và lệch tone trải nghiệm.

---

## 3. Architecture & Proposed Approach
- **Đồng bộ hóa Component (Visual & Functional Parity):** Cập nhật markup và CSS của `.chotot-filter-trigger-wrap` trong `fragments/header.html` sao cho nút bấm có icon `fa-sliders` màu đỏ cam Shopee `#EE4D2D` và nhãn `Bộ lọc` giống hệt trang đầu (`home.html`).
- **Thống nhất cấu trúc Popover Modal:** Tái cấu trúc form popover `#chototFilterPopover` trong `header.html` khớp 1:1 với `#heroFilterPopover` của `home.html` gồm đủ 4 tầng tiêu chí (`sort`, `cat`, `minPrice`/`maxPrice` kèm quick price chips, `store`) và 2 nút hành động `Xóa lọc` / `Áp dụng`.
- **Đồng bộ tham số truy vấn tìm kiếm:** Form `#headerSearchForm` gửi request `GET /search` với đầy đủ bộ tham số chuẩn hóa (`keyword`, `sort`, `cat`, `minPrice`, `maxPrice`, `store`), khớp hoàn toàn với controller `SearchController.java`.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Thay đổi Nút Kích Hoạt Bộ Lọc trên Header (`fragments/header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:** Thay thế icon ghim vị trí và chữ `Toàn TP.HCM` bằng icon `fa-sliders` và chữ `Bộ lọc`.
- **Mã nguồn thay đổi (khoảng dòng 1471 - 1482):**
```html
                <!-- Nút mở bộ lọc popover (Đồng bộ 100% với trang đầu home.html) -->
                <div class="chotot-filter-trigger-wrap">
                    <button type="button" class="chotot-location-button-wrap" id="headerFilterBtn" onclick="toggleChototFilterPopover(event)">
                        <i class="fa-solid fa-sliders" style="color:#EE4D2D; font-size:13px; margin-right:2px;"></i>
                        <span id="filterBtnLabel" style="font-size:13.5px; font-weight:700; color:#1C1917;">Bộ lọc</span>
                        <svg class="chevron-down-svg" id="filterBtnArrow" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="12" height="12" fill="none">
                            <path d="M12.4495 14.8316C12.2013 15.0561 11.7987 15.0561 11.5505 14.8316L6.18623 9.98133C6.0044 9.81692 5.95001 9.56967 6.04841 9.35486C6.14682 9.14006 6.37864 9 6.63578 9L17.3642 9C17.6214 9 17.8532 9.14006 17.9516 9.35487C18.05 9.56967 17.9956 9.81693 17.8138 9.98133L12.4495 14.8316Z" fill="currentColor"></path>
                        </svg>
                    </button>
```

---

### Task 2: Đồng Bộ Cấu Trúc Bảng Popover Bộ Lọc (`fragments/header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:** Cập nhật nội dung bên trong `#chototFilterPopover` khớp đầy đủ các tầng tiêu chí như `home.html`.
- **Mã nguồn thay đổi (khoảng dòng 1483 - 1560):**
```html
                    <!-- BẢNG BỘ LỌC TÌM KIẾM POPOVER (ĐỒNG BỘ 100% VỚI TRANG ĐẦU HOME.HTML) -->
                    <div class="chotot-filter-modal-popover" id="chototFilterPopover" style="display:none;">
                        <div class="filter-popover-header">
                            <div style="display:flex; align-items:center; gap:8px;">
                                <i class="fa-solid fa-sliders text-danger" style="color:#EE4D2D;"></i>
                                <span style="font-weight:800; font-size:14px; color:#1C1917;">Bộ Lọc Tìm Kiếm</span>
                            </div>
                            <button type="button" class="btn-close-filter-popover" onclick="closeChototFilterPopover(event)">&times;</button>
                        </div>

                        <div class="filter-popover-body">
                            <!-- 1. TIÊU CHÍ SẮP XẾP (Phổ biến, Mới nhất, Bán chạy, Giá: Thấp -> Cao, Giá: Cao -> Thấp) -->
                            <div class="filter-section-row">
                                <label class="filter-field-label">Sắp xếp theo</label>
                                <div class="sort-chips-grid">
                                    <label class="sort-chip">
                                        <input type="radio" name="sort" value="popular" th:checked="${selectedSort == null or selectedSort == 'popular'}">
                                        <span><i class="fa-solid fa-fire me-1" style="color:#EE4D2D;"></i>Phổ biến</span>
                                    </label>
                                    <label class="sort-chip">
                                        <input type="radio" name="sort" value="latest" th:checked="${selectedSort == 'latest'}">
                                        <span><i class="fa-solid fa-sparkles me-1" style="color:#F59E0B;"></i>Mới nhất</span>
                                    </label>
                                    <label class="sort-chip">
                                        <input type="radio" name="sort" value="sales" th:checked="${selectedSort == 'sales'}">
                                        <span><i class="fa-solid fa-chart-line me-1" style="color:#10B981;"></i>Bán chạy</span>
                                    </label>
                                    <label class="sort-chip">
                                        <input type="radio" name="sort" value="price-asc" th:checked="${selectedSort == 'price-asc'}">
                                        <span><i class="fa-solid fa-arrow-up-1-9 me-1" style="color:#6366F1;"></i>Giá: Thấp → Cao</span>
                                    </label>
                                    <label class="sort-chip">
                                        <input type="radio" name="sort" value="price-desc" th:checked="${selectedSort == 'price-desc'}">
                                        <span><i class="fa-solid fa-arrow-down-9-1 me-1" style="color:#EC4899;"></i>Giá: Cao → Thấp</span>
                                    </label>
                                </div>
                            </div>

                            <!-- 2. DANH MỤC THỂ LOẠI -->
                            <div class="filter-section-row">
                                <label class="filter-field-label">Danh mục thể loại</label>
                                <div class="custom-select-wrap">
                                    <select name="cat" id="headerFilterCatSelect" class="custom-filter-select">
                                        <option value="">Tất cả thể loại</option>
                                        <option th:if="${categories != null}" th:each="c : ${categories}" th:value="${c.id}" th:text="${c.categoryName}" th:selected="${selectedCat != null and selectedCat == c.id}">Văn Học</option>
                                    </select>
                                    <i class="fa-solid fa-chevron-down custom-select-arrow"></i>
                                </div>
                            </div>

                            <!-- 3. KHOẢNG GIÁ (₫) KÈM CHIP NHANH -->
                            <div class="filter-section-row">
                                <label class="filter-field-label">Khoảng giá (₫)</label>
                                <div class="price-range-inputs">
                                    <input type="number" name="minPrice" id="headerMinPrice" class="price-input-box" placeholder="Từ ₫" th:value="${selectedMinPrice}">
                                    <span style="color:#A8A29E;">-</span>
                                    <input type="number" name="maxPrice" id="headerMaxPrice" class="price-input-box" placeholder="Đến ₫" th:value="${selectedMaxPrice}">
                                </div>
                                <div class="quick-price-chips mt-2">
                                    <button type="button" class="quick-price-chip" onclick="setHeaderPriceRange(0, 50000)">Dưới 50k</button>
                                    <button type="button" class="quick-price-chip" onclick="setHeaderPriceRange(50000, 100000)">50k - 100k</button>
                                    <button type="button" class="quick-price-chip" onclick="setHeaderPriceRange(100000, 200000)">100k - 200k</button>
                                    <button type="button" class="quick-price-chip" onclick="setHeaderPriceRange(200000, 500000)">Trên 200k</button>
                                </div>
                            </div>

                            <!-- 4. CHI NHÁNH CỬA HÀNG -->
                            <div class="filter-section-row">
                                <label class="filter-field-label">Chi nhánh cửa hàng</label>
                                <div class="custom-select-wrap">
                                    <select name="store" id="headerFilterStoreSelect" class="custom-filter-select">
                                        <option value="">Toàn TP.HCM (5 Shop)</option>
                                        <option th:if="${stores != null}" th:each="st : ${stores}" th:value="${st.id}" th:text="${st.storeName}" th:selected="${selectedStore != null and selectedStore == st.id}">Thủ Đức</option>
                                    </select>
                                    <i class="fa-solid fa-chevron-down custom-select-arrow"></i>
                                </div>
                            </div>
                        </div>

                        <!-- Footer Actions: Đặt lại & Áp dụng bộ lọc -->
                        <div class="filter-popover-footer">
                            <button type="button" class="btn-popover-reset" onclick="resetHeaderPopoverFilters(event)">Đặt lại</button>
                            <button type="button" class="btn-popover-apply" onclick="applyHeaderPopoverFilters(event)">Áp dụng bộ lọc</button>
                        </div>
                    </div>
```

---

### Task 3: Bổ Sung CSS Cho Chip Sắp Xếp & Chip Giá Nhanh trong Header (`fragments/header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:** Bổ sung CSS cho `.sort-chips-grid`, `.sort-chip`, `.quick-price-chips`, `.quick-price-chip` để hiển thị đẹp mắt, bo tròn chuẩn Apple Squircle/Pill đồng bộ với `home.html`.
- **Mã nguồn CSS bổ sung:**
```css
        /* Sort Chips Grid đồng bộ trang đầu */
        .sort-chips-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 6px;
        }
        .sort-chip {
            margin: 0;
            cursor: pointer;
        }
        .sort-chip input[type="radio"] {
            display: none;
        }
        .sort-chip span {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 7px 10px;
            background: #F8F5EE;
            border: 1px solid #ECE7DE;
            border-radius: 9999px;
            font-size: 12px;
            font-weight: 600;
            color: #44403C;
            transition: all 0.2s ease;
            white-space: nowrap;
        }
        .sort-chip input[type="radio"]:checked + span {
            background: #FFF7ED;
            border-color: #EE4D2D;
            color: #EE4D2D;
            font-weight: 700;
            box-shadow: 0 2px 8px rgba(238, 77, 45, 0.15);
        }
        .quick-price-chips {
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
        }
        .quick-price-chip {
            background: #F8F5EE;
            border: 1px solid #ECE7DE;
            border-radius: 9999px;
            padding: 4px 10px;
            font-size: 11px;
            font-weight: 600;
            color: #57534E;
            cursor: pointer;
            transition: all 0.15s ease;
        }
        .quick-price-chip:hover {
            background: #FFF7ED;
            border-color: #FED7AA;
            color: #EE4D2D;
        }
```

---

### Task 4: Cập Nhật JavaScript Điều Khiển Popover Header (`fragments/header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:** Đồng bộ hàm `resetHeaderPopoverFilters`, `applyHeaderPopoverFilters`, `setHeaderPriceRange` với `home.html`.
- **Mã nguồn JS cập nhật:**
```javascript
        function resetHeaderPopoverFilters(event) {
            if (event) event.preventDefault();
            const sortInp = document.querySelector('#chototFilterPopover input[name="sort"][value="popular"]');
            if (sortInp) sortInp.checked = true;
            const catSel = document.getElementById('headerFilterCatSelect');
            if (catSel) catSel.value = '';
            const minP = document.getElementById('headerMinPrice');
            const maxP = document.getElementById('headerMaxPrice');
            if (minP) minP.value = '';
            if (maxP) maxP.value = '';
            const storeSel = document.getElementById('headerFilterStoreSelect');
            if (storeSel) storeSel.value = '';
        }

        function applyHeaderPopoverFilters(event) {
            if (event) event.preventDefault();
            const form = document.getElementById('headerSearchForm');
            if (form) form.submit();
        }

        function setHeaderPriceRange(min, max) {
            const minInput = document.getElementById('headerMinPrice');
            const maxInput = document.getElementById('headerMaxPrice');
            if (minInput) minInput.value = min;
            if (maxInput) maxInput.value = max;
        }
```

---

### Task 5: Kiểm Thử Trực Quan (Verification với Playwright)
- **Kịch bản kiểm thử:**
  1. Mở trang con `http://localhost:8080/books/116` (hoặc `/search`).
  2. Xác minh thanh tìm kiếm Header:
     - Nút bộ lọc hiển thị icon sliders đỏ cam `<i class="fa-solid fa-sliders"></i>`.
     - Nhãn chữ hiển thị là `Bộ lọc` (không còn chữ `Toàn TP.HCM` kèm ghim đỏ).
  3. Bấm vào nút `Bộ lọc`:
     - Bảng Popover mở ra với tiêu đề `Bộ Lọc Tìm Kiếm`.
     - Đầy đủ 4 phần: Sắp xếp theo (5 chips), Danh mục thể loại, Khoảng giá (+ 4 quick chips), Chi nhánh cửa hàng.
     - Hai nút bấm `Đặt lại` và `Áp dụng bộ lọc`.
  4. Chụp ảnh màn hình đối chiếu và dùng `vision_analyze` xác nhận đồng bộ 100% với trang đầu.

---

## 5. Tests & Verification Commands
- **Biên dịch & Chạy Test Suite:**
```bash
mvn test
```
*Kỳ vọng:* `BUILD SUCCESS` (7/7 tests passed 100%).
- **Kiểm thử trên trình duyệt bằng Playwright:**
```javascript
await page.goto('http://localhost:8080/books/116');
await page.screenshot({ path: '/Users/phucdeptrai/header_filter_synced.png' });
```
- **Kiểm tra tương tác Popover:**
```javascript
await page.click('#headerFilterBtn');
await page.waitForTimeout(300);
await page.screenshot({ path: '/Users/phucdeptrai/header_filter_popover_open.png' });
```

---

## 6. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi thêm 5 radio chip sắp xếp và chip giá nhanh, chiều cao popover trong header có thể tăng lên.
  - *Giải pháp:* Đặt `max-height: 480px; overflow-y: auto;` với thanh cuộn mỏng tinh tế để đảm bảo popover hiển thị trọn vẹn trên màn hình laptop nhỏ mà không bị tràn màn hình.
- **Tính tương thích:** Không làm ảnh hưởng đến các trang khác vì controller `/search` đã hỗ trợ sẵn các tham số `sort`, `cat`, `minPrice`, `maxPrice`, `store`.
