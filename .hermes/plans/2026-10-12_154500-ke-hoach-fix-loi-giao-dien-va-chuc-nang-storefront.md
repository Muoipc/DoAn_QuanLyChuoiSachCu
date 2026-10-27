# Kế Hoạch Triển Khai: Khắc Phục Toàn Diện Các Lỗi Giao Diện & Hoàn Thiện Chức Năng Bán Hàng (Storefront Integrity & Functional Completion Plan)

## 1. Goal
Hoàn thiện toàn bộ các tính năng còn thiếu, sửa chữa các nút bấm hỏng, xóa bỏ dữ liệu giả lập (mock/hardcoded) và đồng bộ hóa trải nghiệm người dùng trên 25 trang giao diện công khai của ReBook theo chuẩn kiến trúc Spring Boot 3.4 & Thymeleaf.

---

## 2. Current Context & Assumptions
- **Codebase hiện tại:** Spring Boot 3.4.3, Spring Security 6, Thymeleaf, Spring Data JPA, MySQL 8.
- **Nhánh Git đang làm việc:** `feature/client-phuc` (head: `52cc474`).
- **Hiện trạng tồn tại qua rà soát:**
  - 5 lỗi logic/nút bấm không chạy: Tab lọc trang chủ sai alias query, nút Mua Lại chỉ redirect mà không thêm sách vào giỏ, nút Trả Hàng nhảy sang trang trợ giúp, chia sẻ mạng xã hội thiếu code 3/4 kênh, form gửi ticket không lưu database.
  - 5 khối dữ liệu tĩnh giả lập: 5 thẻ sách đề xuất giỏ hàng hardcode HTML, checkbox điểm tích lũy Shopee Coins ảo, ngày giao hàng dự kiến hardcode tháng 9, khóa cứng đơn vị vận chuyển không cho chọn, chuông thông báo header gắn cứng mã đơn giả `#ORD259683735`.
  - 3 tương tác phía client: Nút theo dõi shop F5 là mất, nút xem thêm trang chủ là thẻ redirect, thẻ flash sale chưa đồng bộ tối giản hoài cổ.

---

## 3. Architecture & Proposed Approach
- **Nguyên tắc Kiến Trúc:**
  1. *Backend-Driven State:* Mọi trạng thái tương tác hiển thị cho người dùng (Đơn vị vận chuyển, Sách gợi ý, Trạng thái theo dõi shop, Thông báo đơn hàng) phải được điều khiển từ Spring Data Repository, không gán tĩnh trên HTML.
  2. *Param Robustness (Tolerant Input):* Bộ điều khiển Controller chấp nhận linh hoạt các alias lọc thường gặp (`newest`/`latest`, `bestseller`/`sales`) để tránh lỗi rơi vào nhánh fallback ngầm.
  3. *Seamless Cart Rehydration:* Nghiệp vụ "Mua Lại" lặp qua `OrderItem` của đơn cũ và nạp thẳng vào `CartItem` của user, sau đó chuyển hướng vào `/cart` có hiển thị sách ngay lập tức.
  4. *Visual Parity:* Đồng bộ toàn bộ các thẻ sách còn sót lại (như Flash Sale) sang ngôn ngữ Tiệm Sách Cổ Điển Tối Giản (viền be `#ECE7DE`, pill mờ kính, giá nâu da bò `#8C4A27`).

---

## 4. Step-by-Step Implementation Tasks

---

### GIAI ĐOẠN 1: SỬA LỖI LOGIC NGHIỆP VỤ & NÚT BẤM HỎNG (CRITICAL BUGS)

#### Task 1.1: Sửa Lỗi Sắp Xếp Tab Feed Trang Chủ (`SearchController.java` & `home.html`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/SearchController.java`
- **Mục tiêu:** Mở rộng tiếp nhận alias `newest` (bằng với `latest`) và `bestseller` (bằng với `sales`).
- **Code thay đổi (dòng 134-152):**
```java
        // 6. Sắp xếp kết quả (Hỗ trợ alias bestseller, newest đồng bộ trang chủ)
        if ("sales".equalsIgnoreCase(sort) || "bestseller".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted((b1, b2) -> Integer.compare(
                    b2.getTotalSold() != null ? b2.getTotalSold() : 0,
                    b1.getTotalSold() != null ? b1.getTotalSold() : 0
            )).collect(Collectors.toList());
        } else if ("price-asc".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted(Comparator.comparing(b -> b.getPrice() != null ? b.getPrice() : BigDecimal.ZERO)).collect(Collectors.toList());
        } else if ("price-desc".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted((b1, b2) -> {
                BigDecimal p1 = b1.getPrice() != null ? b1.getPrice() : BigDecimal.ZERO;
                BigDecimal p2 = b2.getPrice() != null ? b2.getPrice() : BigDecimal.ZERO;
                return p2.compareTo(p1);
            }).collect(Collectors.toList());
        } else if ("latest".equalsIgnoreCase(sort) || "newest".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted((b1, b2) -> {
                if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
                return b2.getCreatedAt().compareTo(b1.getCreatedAt());
            }).collect(Collectors.toList());
        } else {
            // "popular": Sắp xếp theo lượt xem
            allBooks = allBooks.stream().sorted((b1, b2) -> Integer.compare(
                    b2.getViewsCount() != null ? b2.getViewsCount() : 0,
                    b1.getViewsCount() != null ? b1.getViewsCount() : 0
            )).collect(Collectors.toList());
        }
```
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/home.html` (khoảng dòng 720 & 2010):
  - Chuẩn hóa link tab: `th:href="@{/search(sort='latest')}"` và `th:href="@{/search(sort='sales')}"`.

---

#### Task 1.2: Triển Khai Tính Năng "Mua Lại" (Re-order) Thực Tế (`OrderHistoryController.java`, `order-history.html`, `order-detail.html`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/OrderHistoryController.java`
- **Mục tiêu:** Thêm endpoint `POST /orders/reorder/{orderCode}` nạp lại toàn bộ sách của đơn hàng cũ vào giỏ hàng của user và redirect sang `/cart`.
- **Code bổ sung vào `OrderHistoryController.java`:**
```java
    @Autowired
    private vn.iotstar.service.ICartService cartService;

    /**
     * POST /orders/reorder/{orderCode}: Mua lại đơn hàng cũ
     * Tự động thêm toàn bộ sách trong đơn cũ vào giỏ hàng và chuyển hướng đến trang Giỏ Hàng.
     */
    @PostMapping("/reorder/{orderCode}")
    public String reorderOldOrder(
            @PathVariable("orderCode") String orderCode,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderService.findByOrderCode(orderCode);
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng cần mua lại!");
            return "redirect:/orders";
        }

        int addedCount = 0;
        Long storeId = order.getStore() != null ? order.getStore().getId() : 1L;
        for (OrderItem item : order.getItems()) {
            if (item.getBook() != null) {
                int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                cartService.addToCart(currentUser.getId(), item.getBook().getId(), storeId, qty);
                addedCount++;
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", 
                "Đã thêm " + addedCount + " tựa sách từ đơn hàng #" + orderCode + " vào giỏ hàng thành công!");
        return "redirect:/cart";
    }
```
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/order-history.html`
  - Đổi thẻ `<a>` Mua Lại thành form submit:
```html
                    <form th:action="@{'/orders/reorder/' + ${order.orderCode}}" method="post" style="display:inline;">
                        <button type="submit" class="btn-order-action btn-action-outline btn-reorder">
                            <i class="fa-solid fa-rotate-right"></i> Mua Lại
                        </button>
                    </form>
```
- **Tệp chỉnh sửa 3:** `src/main/resources/templates/order-detail.html`
  - Đổi link Mua Lại thành form submit tương ứng.

---

#### Task 1.3: Xây Dựng Quy Trình "Yêu Cầu Trả Hàng / Hoàn Tiền" (`order-detail.html` & `OrderHistoryController.java`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/OrderHistoryController.java`
- **Mục tiêu:** Thêm endpoint `POST /orders/request-return/{orderCode}` nhận lý do trả hàng từ khách, cập nhật `customerNotes` và chuyển trạng thái hoặc ghi nhận yêu cầu.
- **Code bổ sung:**
```java
    /**
     * POST /orders/request-return/{orderCode}: Khách hàng gửi yêu cầu đổi trả / hoàn tiền
     */
    @PostMapping("/request-return/{orderCode}")
    public String requestOrderReturn(
            @PathVariable("orderCode") String orderCode,
            @RequestParam("returnReason") String returnReason,
            @RequestParam(value = "returnNote", required = false) String returnNote,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        Order order = orderService.findByOrderCode(orderCode);
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/orders";
        }

        if (order.getOrderStatus() != Order.OrderStatus.DELIVERED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể yêu cầu đổi trả với đơn hàng đã giao thành công!");
            return "redirect:/orders/" + orderCode;
        }

        String note = (order.getCustomerNotes() != null ? order.getCustomerNotes() + " | " : "") +
                "[Yêu cầu đổi trả]: " + returnReason + (returnNote != null && !returnNote.isBlank() ? " (" + returnNote + ")" : "");
        order.setCustomerNotes(note);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("successMessage", 
                "Yêu cầu đổi trả sách của bạn đã được gửi tới Quản lý chi nhánh. Chúng tôi sẽ liên hệ trong 24h để hỗ trợ thu hồi hoặc đổi cuốn mới!");
        return "redirect:/orders/" + orderCode;
    }
```
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/order-detail.html`
  - Bổ sung modal thân thiện chọn lý do (Sách rách trang/gáy ố nặng, Không đúng tựa sách đã đặt, Sai chất lượng mô tả) và gửi form `POST /orders/request-return/{orderCode}`.

---

#### Task 1.4: Hoàn Thiện Đầy Đủ Các Kênh Chia Sẻ Mạng Xã Hội (`book-detail.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/book-detail.html`
- **Mục tiêu:** Cập nhật hàm JavaScript `shareSocial(network)` hỗ trợ Facebook, Twitter/X, Pinterest, Messenger và Copy Link.
- **Code cập nhật:**
```javascript
        function shareSocial(network) {
            const url = window.location.href;
            const title = document.title;
            const imgEl = document.getElementById('mainCoverImg') || document.querySelector('.gallery-main-img img');
            const img = imgEl ? imgEl.src : '';

            if (network === 'facebook') {
                window.open('https://www.facebook.com/sharer/sharer.php?u=' + encodeURIComponent(url), 'fbShare', 'width=640,height=480');
            } else if (network === 'twitter') {
                window.open('https://twitter.com/intent/tweet?url=' + encodeURIComponent(url) + '&text=' + encodeURIComponent(title), 'twShare', 'width=640,height=480');
            } else if (network === 'pinterest') {
                window.open('https://pinterest.com/pin/create/button/?url=' + encodeURIComponent(url) + '&media=' + encodeURIComponent(img) + '&description=' + encodeURIComponent(title), 'pinShare', 'width=750,height=550');
            } else if (network === 'messenger') {
                // Hỗ trợ chia sẻ Messenger hoặc sao chép liên kết vào clipboard
                if (navigator.clipboard) {
                    navigator.clipboard.writeText(url).then(function() {
                        if (typeof showShopeeToast === 'function') {
                            showShopeeToast('Đã sao chép liên kết sách vào bộ nhớ tạm để gửi qua Messenger!');
                        } else {
                            alert('Đã sao chép liên kết sách để gửi qua tin nhắn!');
                        }
                    });
                } else {
                    window.open('https://www.messenger.com/', '_blank');
                }
            }
        }
```

---

#### Task 1.5: Lưu Phiếu Hỗ Trợ Khách Hàng Vào Database (`SupportTicket.java`, `SupportTicketRepository.java`, `HelpController.java`)
- **Tệp tạo mới 1:** `src/main/java/vn/iotstar/entity/SupportTicket.java`
  - Các trường: `id`, `fullName`, `email`, `phone`, `orderCode`, `category`, `message`, `status` (`OPEN`, `RESOLVED`), `createdAt`.
- **Tệp tạo mới 2:** `src/main/java/vn/iotstar/repository/SupportTicketRepository.java`
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/HelpController.java`
  - Inject `SupportTicketRepository` và lưu ticket vào database trước khi redirect:
```java
        SupportTicket ticket = new SupportTicket();
        ticket.setFullName(fullName);
        ticket.setEmail(email);
        ticket.setPhone(phone);
        ticket.setOrderCode(orderCode);
        ticket.setCategory(category);
        ticket.setMessage(message);
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);
        supportTicketRepository.save(ticket);
```

---

### GIAI ĐOẠN 2: XÓA BỎ DỮ LIỆU TĨNH & ĐỘNG HÓA GIỎ HÀNG, THANH TOÁN

#### Task 2.1: Động Hóa 5 Thẻ Sách Đề Xuất Giỏ Hàng (`CartController.java` & `cart.html`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/CartController.java`
- **Mục tiêu:** Nạp danh sách `recommendedBooks` (Top bán chạy từ `bookRepository.findTopSoldWithImages(6)`) vào model.
- **Code bổ sung vào `CartController.java`:**
```java
        List<Book> recommendedBooks = bookRepository.findTopSoldWithImages(6);
        if (recommendedBooks == null || recommendedBooks.isEmpty()) {
            recommendedBooks = bookRepository.findAllActiveWithImages().stream().limit(6).collect(Collectors.toList());
        }
        model.addAttribute("recommendedBooks", recommendedBooks);
```
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/cart.html`
  - Thay thế 5 thẻ HTML tĩnh trong `.recommend-grid` bằng vòng lặp Thymeleaf:
```html
                <div class="recommend-grid">
                    <a th:each="rb : ${recommendedBooks}" th:href="@{'/books/' + ${rb.id}}" class="recommend-card">
                        <img th:src="${rb.primaryImageUrl != null ? rb.primaryImageUrl : '/images/books/book_1.jpg'}" 
                             th:alt="${rb.title}" class="recommend-thumb" onerror="this.src='/images/books/book_1.jpg';">
                        <div class="recommend-name" th:text="${rb.title}">Tên sách</div>
                        <div class="recommend-price" th:text="'₫' + ${#numbers.formatDecimal(rb.minPrice != null ? rb.minPrice : rb.price, 0, 'POINT', 0, 'COMMA')}">₫45.000</div>
                    </a>
                </div>
```

---

#### Task 2.2: Loại Bỏ Dòng "Điểm Tích Lũy Sách Cũ" Ảo Khỏi Giỏ Hàng (`cart.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/cart.html`
- **Mục tiêu:** Xóa khối HTML `Điểm Tích Lũy Sách Cũ (Dùng 2.000 Điểm tiết kiệm ₫2.000)` và hàm JS `toggleCartCoins()` tính nhẩm ảo để đồng nhất 100% số tiền giữa Giỏ hàng và Thanh toán.

---

#### Task 2.3: Động Hóa Ngày Giao Hàng & Cho Phép Chọn Đơn Vị Vận Chuyển (`checkout.html` & `CheckoutController.java`)
- **Tệp chỉnh sửa 1:** `src/main/resources/templates/checkout.html`
- **Mục tiêu:**
  - Render danh sách đơn vị vận chuyển từ `${shippingUnits}` (đã có sẵn trong model từ `shippingUnitRepository.findByIsActiveTrue()`).
  - Cho phép người dùng chọn đơn vị (ví dụ: Giao Hàng Nhanh 2-3 ngày 25k, Viettel Post 3-4 ngày 20k, Hỏa Tốc Trong Ngày 40k).
  - Tính ngày nhận hàng động bằng JavaScript: Lấy `ngày hiện tại + 2` đến `ngày hiện tại + 4` định dạng ngày/tháng tiếng Việt (ví dụ: `14 Th10 - 16 Th10`), xóa sạch chuỗi text cứng `26 Th09 - 28 Th09`.

---

#### Task 2.4: Chuẩn Hóa Trung Tâm Thông Báo Header (`header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:**
  - Khi chưa đăng nhập: Ẩn badge `5` đỏ chót trên chuông thông báo.
  - Khi đã đăng nhập: Nếu user có đơn hàng gần nhất, hiển thị thông báo với mã đơn thực tế của user (ví dụ: `Đơn hàng #` + `${latestOrder.orderCode}` + ` đang được chuẩn bị`); nếu chưa có đơn, hiển thị thông báo chào mừng và voucher ưu đãi sinh viên.

---

### GIAI ĐOẠN 3: HOÀN THIỆN TƯƠNG TÁC LƯU TRỮ TRẠNG THÁI

#### Task 3.1: Hoàn Thiện Tương Tác "Theo Dõi Cửa Hàng" (Follow Shop) (`StoreController.java` & `store-detail.html`)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/controller/StoreController.java`
  - Bổ sung session map hoặc cookie theo dõi `followedStores` của user để khi F5 trang, trạng thái "Đã Theo Dõi" không bị mất.
- **Tệp chỉnh sửa 2:** `src/main/resources/templates/store-detail.html`
  - Gọi API AJAX lưu trạng thái và kiểm tra `isFollowed` khi render.

#### Task 3.2: Đồng Bộ Thống Kê Chi Nhánh Từ Database (`StoreController.java`, `book-detail.html`, `store-detail.html`)
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/StoreController.java` & `BookController.java`
  - Đếm số lượng đầu sách thực tế của chi nhánh từ `inventoryRepository.countByStoreId(storeId)` thay vì gán cứng text `120`.

---

### GIAI ĐOẠN 4: CHUẨN HÓA THẨM MỸ ĐỒNG BỘ NHẬN DIỆN REBOOK

#### Task 4.1: Đồng Bộ 5 Thẻ Sách Flash Sale Trang Chủ Sang Phong Cách Tối Giản Hoài Cổ (`home.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/home.html`
- **Mục tiêu:** Cập nhật CSS và HTML của `.flash-book-card`:
  - Nền trắng phẳng bo góc 14px, viền be giấy `#ECE7DE`.
  - Thay badge neon bằng pill mờ kính `.card-condition-pill` (`95% mới`) và `.card-discount-pill` (`-43%`).
  - Giá màu nâu da bò `#8C4A27`.
  - Thanh tiến độ bán `ĐÃ BÁN X` phẳng, thanh lịch.

---

## 5. Tests / Validation Strategy
- **Kiểm thử tự động Maven:**
  ```bash
  mvn test
  ```
  Đảm bảo 7/7 unit tests sẵn có tiếp tục PASS 100%, không phát sinh lỗi compile template.
- **Kiểm thử nghiệp vụ bằng Playwright:**
  1. Kiểm tra Mua Lại: Đăng nhập user `phucdeptrai`, mở `/orders`, bấm nút "Mua Lại" trên đơn đã giao -> xác nhận trình duyệt điều hướng sang `/cart` với giỏ hàng chứa đúng các cuốn sách từ đơn cũ.
  2. Kiểm tra Trả Hàng: Mở chi tiết đơn hàng `/orders/{code}`, bấm "Yêu Cầu Trả Hàng", nhập lý do và submit -> xác nhận đơn ghi nhận ghi chú `[Yêu cầu đổi trả]: ...`.
  3. Kiểm tra Checkout: Mở `/checkout` -> xác minh ngày giao hàng hiển thị đúng tháng 10 hiện tại, chọn đơn vị vận chuyển khác nhau cập nhật phí ship đúng.
  4. Kiểm tra Tab Feed Trang Chủ: Bấm tab "Mới nhất" và "Bán chạy nhất" -> xác minh controller trả về kết quả lọc chính xác.

---

## 6. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi xóa bỏ dòng tính nhẩm Điểm Tích Lũy trong giỏ hàng, tổng tiền giỏ hàng sẽ khớp hoàn toàn 1:1 với trang Checkout, triệt tiêu mọi sai lệch số tiền thanh toán.
- **Tương thích:** Các thay đổi không làm thay đổi cấu trúc bảng chính (Orders, Books, Users, Stores), bảo đảm toàn vẹn dữ liệu MySQL hiện hữu.
