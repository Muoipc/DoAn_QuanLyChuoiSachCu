# Kế Hoạch Triển Khai: Hoàn Thiện Kênh Điều Phối & Giao Hàng Shipper (Shipper Portal & API)

## 1. Goal
Hoàn thiện toàn diện kênh giao hàng Shipper (Web Portal & REST API) với giao diện Apple Liquid Glass chuyên biệt, cách ly tuyệt đối khỏi menu quản trị, bổ sung hiển thị chi tiết kiện sách, bản đồ dẫn đường 1 chạm, nhật ký lý do giao thất bại và bộ đếm tiền thu hộ (COD) thời gian thực.

---

## 2. Current Context & Assumptions
- **Hệ thống hiện tại:** Spring Boot 3.4.3, Spring Security 6, Thymeleaf, MySQL 8.x.
- **Tài khoản & Phân quyền:**
  - Role: `ROLE_SHIPPER` (id: 4).
  - Tài khoản Shipper mẫu: `shipper_hung` (id: 6, pass: `123456`, SĐT: `0901234567`).
  - Phân quyền URL: `/shipper/**` đã mở cho `ADMIN` và `SHIPPER`.
- **Thực trạng tồn tại cần khắc phục:**
  1. **Giao diện Sidebar bị lẫn lộn menu quản trị:** Khi đăng nhập bằng tài khoản `ROLE_SHIPPER`, thanh Sidebar (`admin-layout.html`) vẫn hiển thị toàn bộ 15 menu của Admin/Manager (Sách, Tồn kho, Chuyển kho, Chi nhánh, Ký gửi, Báo cáo,...). Khi Shipper click vào các menu này sẽ bị lỗi từ chối quyền (403/Redirect).
  2. **Thiếu thông tin kiện sách:** Trên thẻ đơn hàng (`shipper/orders.html`), Shipper chỉ thấy địa chỉ khách mà không thấy danh sách các cuốn sách bên trong kiện hàng. Link mã đơn trỏ về `/admin/orders/{id}` khiến Shipper bị chặn quyền 403 khi bấm xem chi tiết.
  3. **Báo thất bại chưa có lý do:** Khi bấm "Báo Giao Thất Bại / Hoàn Hàng", hệ thống chuyển thẳng sang `RETURNED` mà không cho Shipper chọn hoặc nhập lý do (Khách không nghe máy, Khách hẹn lại ngày mai, Sai địa chỉ,...).
  4. **Thiếu bản đồ dẫn đường:** Shipper chỉ có nút "Gọi ngay", thiếu nút "Chỉ đường" để mở trực tiếp Google Maps đến địa chỉ người nhận.
  5. **Thiếu thống kê tài chính ca giao:** Shipper không theo dõi được số tiền COD đã thu trong ngày, dẫn đến khó đối soát tiền mặt với thủ kho khi kết thúc ca giao.
  6. **Thiếu bộ đếm số lượng trên các tab:** Các tab "Đang đi giao", "Đã giao", "Giao thất bại" chưa có badge số lượng đơn.

---

## 3. Architecture & Proposed Approach
- **Giao diện Sidebar chuyên biệt theo vai trò (Role-Isolated Navigation):** Điều kiện hóa Sidebar trong `admin-layout.html`: nếu là `ROLE_SHIPPER` (không phải `ADMIN`), chỉ hiển thị duy nhất phân hệ "Nghiệp Vụ Giao Hàng" với các lối tắt trạng thái đơn hàng (`Đang Giao`, `Đã Giao`, `Thất Bại/Hoàn`, `Tất Cả`); ẩn toàn bộ menu quản trị kho và hệ thống.
- **Thẻ kiện hàng thông minh (Smart Delivery Card):** Tích hợp danh sách tóm tắt sách (thumbnail, tựa sách, số lượng, độ mới) trực tiếp vào thẻ đơn hàng; bổ sung nút "Chỉ đường" dẫn tới Google Maps; modal báo thất bại có lựa chọn lý do cụ thể và lưu vào ghi chú đơn hàng.
- **Thống kê COD & Bộ đếm thời gian thực (COD & Metric Engine):** Bổ sung phương thức tính tổng tiền COD và đếm đơn theo Shipper trong `OrderRepository` & `IOrderService`, hiển thị 4 thẻ KPI trên đầu trang Web và cung cấp endpoint REST `GET /api/shipper/stats` cho Mobile App.

---

## 4. Step-by-Step Tasks

### Task 1: Viết Unit Test cho Nghiệp vụ Shipper (TDD - Red Phase)
- **Tệp tạo mới:** `src/test/java/vn/iotstar/service/OrderServiceShipperTest.java`
- **Mã nguồn kiểm thử:**
```java
package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Order;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceShipperTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Thống kê số lượng đơn và tổng tiền COD thu hộ của Shipper")
    void testShipperStatsCalculation() {
        Long shipperId = 6L;
        when(orderRepository.countByShipperIdAndOrderStatus(shipperId, Order.OrderStatus.SHIPPING)).thenReturn(5L);
        when(orderRepository.countByShipperIdAndOrderStatus(shipperId, Order.OrderStatus.DELIVERED)).thenReturn(12L);
        when(orderRepository.countByShipperIdAndOrderStatus(shipperId, Order.OrderStatus.RETURNED)).thenReturn(1L);
        when(orderRepository.sumCodCollectedByShipper(shipperId)).thenReturn(new BigDecimal("1850000.00"));

        long shipping = orderService.countOrdersForShipper(shipperId, Order.OrderStatus.SHIPPING);
        long delivered = orderService.countOrdersForShipper(shipperId, Order.OrderStatus.DELIVERED);
        long returned = orderService.countOrdersForShipper(shipperId, Order.OrderStatus.RETURNED);
        BigDecimal codTotal = orderService.sumCodCollectedByShipper(shipperId);

        assertEquals(5L, shipping);
        assertEquals(12L, delivered);
        assertEquals(1L, returned);
        assertEquals(new BigDecimal("1850000.00"), codTotal);
    }

    @Test
    @DisplayName("Ghi nhận lý do khi Shipper báo giao thất bại / hoàn hàng")
    void testShipperReportDeliveryFailureWithNote() {
        Long orderId = 100L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderStatus(Order.OrderStatus.SHIPPING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(orderId, Order.OrderStatus.RETURNED, "Khách không nghe máy sau 3 cuộc gọi");

        assertEquals(Order.OrderStatus.RETURNED, order.getOrderStatus());
        assertTrue(order.getCustomerNotes() != null && order.getCustomerNotes().contains("Khách không nghe máy"));
    }
}
```
- **Xác minh Red Phase:**
```bash
mvn test -Dtest=OrderServiceShipperTest
```
*Kỳ vọng:* Biên dịch lỗi do `countOrdersForShipper` và `sumCodCollectedByShipper` chưa được định nghĩa.

---

### Task 2: Mở rộng `OrderRepository` & `IOrderService` (TDD - Green Phase)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/repository/OrderRepository.java`
- **Mã nguồn bổ sung:**
```java
    long countByShipperId(Long shipperId);

    long countByShipperIdAndOrderStatus(Long shipperId, Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.shipper.id = :shipperId AND o.orderStatus = 'DELIVERED' AND o.paymentMethod = 'COD'")
    BigDecimal sumCodCollectedByShipper(@Param("shipperId") Long shipperId);
```
- **Tệp chỉnh sửa 2:** `src/main/java/vn/iotstar/service/IOrderService.java`
- **Mã nguồn bổ sung:**
```java
    long countOrdersForShipper(Long shipperId, Order.OrderStatus status);
    BigDecimal sumCodCollectedByShipper(Long shipperId);
```
- **Tệp chỉnh sửa 3:** `src/main/java/vn/iotstar/service/impl/OrderServiceImpl.java`
- **Mã nguồn triển khai:**
```java
    @Override
    @Transactional(readOnly = true)
    public long countOrdersForShipper(Long shipperId, Order.OrderStatus status) {
        if (shipperId == null) return 0;
        if (status != null) {
            return orderRepository.countByShipperIdAndOrderStatus(shipperId, status);
        }
        return orderRepository.countByShipperId(shipperId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumCodCollectedByShipper(Long shipperId) {
        if (shipperId == null) return BigDecimal.ZERO;
        return orderRepository.sumCodCollectedByShipper(shipperId);
    }
```
- Cập nhật thêm trong `updateOrderStatus`: Nếu có `note` truyền vào khi chuyển sang `RETURNED`, ghi nhận lý do thất bại vào `customerNotes`:
```java
        if (note != null && !note.trim().isEmpty()) {
            String prefix = "[Lý do Shipper]: " + note.trim();
            if (order.getCustomerNotes() == null || order.getCustomerNotes().isBlank()) {
                order.setCustomerNotes(prefix);
            } else {
                order.setCustomerNotes(order.getCustomerNotes() + " | " + prefix);
            }
        }
```
- **Xác minh Green Phase:**
```bash
mvn test -Dtest=OrderServiceShipperTest
```
*Kỳ vọng:* `BUILD SUCCESS` (2 tests passed).

---

### Task 3: Cách ly giao diện Sidebar & Topbar cho Shipper (`admin-layout.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/layout/admin-layout.html`
- **Mục tiêu:** Ẩn toàn bộ menu quản trị hệ thống, chỉ hiển thị menu giao hàng khi đăng nhập bằng `ROLE_SHIPPER`.
- **Mã nguồn cập nhật Sidebar:**
```html
        <ul class="sidebar-menu">
            <!-- 1. GIAO DIỆN DÀNH RIÊNG CHO SHIPPER (ROLE_SHIPPER) -->
            <th:block th:if="${#authorization.expression('hasRole(''SHIPPER'') and !hasRole(''ADMIN'')')}">
                <li class="sidebar-heading">
                    <i class="fa-solid fa-motorcycle text-warning opacity-75" style="font-size: 10px;"></i>
                    <span>Nghiệp Vụ Giao Hàng</span>
                </li>
                <li class="sidebar-item" th:classappend="${selectedStatus != null and selectedStatus.name() == 'SHIPPING'} ? 'active' : ''">
                    <a th:href="@{/shipper/orders(status='SHIPPING')}">
                        <i class="fa-solid fa-truck-fast"></i>
                        <span>Đơn Cần Đi Giao</span>
                    </a>
                </li>
                <li class="sidebar-item" th:classappend="${selectedStatus != null and selectedStatus.name() == 'DELIVERED'} ? 'active' : ''">
                    <a th:href="@{/shipper/orders(status='DELIVERED')}">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>Đã Giao Thành Công</span>
                    </a>
                </li>
                <li class="sidebar-item" th:classappend="${selectedStatus != null and selectedStatus.name() == 'RETURNED'} ? 'active' : ''">
                    <a th:href="@{/shipper/orders(status='RETURNED')}">
                        <i class="fa-solid fa-rotate-left"></i>
                        <span>Giao Thất Bại / Hoàn</span>
                    </a>
                </li>
                <li class="sidebar-item" th:classappend="${selectedStatus == null and #httpServletRequest != null and #httpServletRequest.requestURI.startsWith('/shipper')} ? 'active' : ''">
                    <a th:href="@{/shipper/orders}">
                        <i class="fa-solid fa-clipboard-list"></i>
                        <span>Tất Cả Đơn Của Tôi</span>
                    </a>
                </li>
            </th:block>

            <!-- 2. GIAO DIỆN QUẢN TRỊ ADMIN & STORE MANAGER -->
            <th:block th:unless="${#authorization.expression('hasRole(''SHIPPER'') and !hasRole(''ADMIN'')')}">
                <!-- Giữ nguyên các khối Kho & Sách Cũ, Đơn Hàng & Ký Gửi, Hệ Thống & Báo Cáo hiện tại -->
            </th:block>
```
- **Topbar Brand & Badge:**
  - Nếu là Shipper: Topbar hiển thị badge: `Cổng Giao Hàng Shipper` và tên tài khoản kèm icon xe máy.

---

### Task 4: Nâng cấp Controller phục vụ Kênh Shipper Web (`ShipperController.java`)
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/shipper/ShipperController.java`
- **Thay đổi:**
  1. Tính toán 4 chỉ số KPI: `countShipping`, `countDelivered`, `countReturned`, `totalCodCollected`.
  2. Bổ sung `note` khi Shipper gửi form cập nhật trạng thái.
```java
        // Thống kê số lượng đơn và tiền COD
        model.addAttribute("countAll", orderService.countOrdersForShipper(currentShipperId, null));
        model.addAttribute("countShipping", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.SHIPPING));
        model.addAttribute("countDelivered", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.DELIVERED));
        model.addAttribute("countReturned", orderService.countOrdersForShipper(currentShipperId, Order.OrderStatus.RETURNED));
        model.addAttribute("totalCodCollected", orderService.sumCodCollectedByShipper(currentShipperId));
```

---

### Task 5: Hoàn thiện giao diện chi tiết đơn & tính năng hỗ trợ Shipper (`shipper/orders.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/shipper/orders.html`
- **Các thành phần bổ sung:**
  1. **Thanh 4 KPI Mini Liquid Glass:**
     - `Đang Đi Giao`: Hiển thị số lượng đơn `countShipping` kèm icon xe máy vàng.
     - `Đã Giao Thành Công`: Hiển thị `countDelivered` kèm icon tích xanh.
     - `Giao Thất Bại / Hoàn`: Hiển thị `countReturned` kèm icon đỏ.
     - `Tiền COD Đã Thu`: Hiển thị số tiền `totalCodCollected` (ví dụ: `450.000 ₫`) nổi bật cam Shopee.
  2. **Bộ đếm Badge trên các tab lọc:**
     - `Tất cả đơn (N)`
     - `Đang đi giao (X)`
     - `Đã giao thành công (Y)`
     - `Giao thất bại / Hoàn (Z)`
  3. **Danh sách tóm tắt sách trong kiện hàng:**
     - Ngay trong mỗi thẻ đơn hàng, hiển thị danh sách sách bên trong:
     ```html
     <div class="parcel-items-list mb-3 p-2 rounded-3" style="background: rgba(0,0,0,0.02); border: 1px solid #ECE7DE;">
         <div class="small fw-semibold text-muted mb-1"><i class="fa-solid fa-boxes-packing me-1"></i>Kiện sách gồm (<span th:text="${ord.items.size()}">1</span> món):</div>
         <div th:each="it : ${ord.items}" class="d-flex align-items-center gap-2 py-1 border-bottom border-light-subtle">
             <img th:src="${it.book != null and it.book.primaryImageUrl != null ? it.book.primaryImageUrl : '/images/books/book_1.jpg'}" style="width: 28px; height: 38px; object-fit: cover; border-radius: 4px;">
             <div class="flex-grow-1 min-w-0" style="font-size: 12px;">
                 <div class="text-truncate fw-semibold text-dark" th:text="${it.bookTitle}">Tựa sách</div>
                 <div class="text-muted" style="font-size: 11px;" th:text="'x' + ${it.quantity} + ' · ' + ${#numbers.formatDecimal(it.price, 0, 'COMMA', 0, 'POINT')} + ' đ'">x1 · 85,000 đ</div>
             </div>
         </div>
     </div>
     ```
  4. **Nút "Chỉ đường" (Google Maps):**
     - Đặt cạnh nút "Gọi ngay":
     ```html
     <a th:href="'https://www.google.com/maps/search/?api=1&query=' + ${#uris.escapeQueryParam(ord.receiverAddress)}" target="_blank" class="btn btn-sm btn-outline-primary rounded-pill px-2.5 py-1" style="font-size: 11px;">
         <i class="fa-solid fa-location-arrow me-1"></i>Chỉ đường
     </a>
     ```
  5. **Modal Báo giao thất bại có lựa chọn lý do:**
     - Modal hỏi lý do thất bại với các radio chip hoặc select:
       - `Khách không nghe máy (đã gọi 3 lần)`
       - `Khách hẹn lại ngày/giờ khác`
       - `Sai địa chỉ / Không liên hệ được`
       - `Khách từ chối nhận hàng khi đồng kiểm`
       - `Khách không chuẩn bị đủ tiền COD`
       - `Lý do khác (nhập text)`
     - Form đính kèm `note` gửi về `POST /shipper/orders/{id}/update`.

---

### Task 6: Mở rộng REST API Mobile Shipper (`ShipperRestController.java`)
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/api/ShipperRestController.java`
- **Mã nguồn bổ sung:**
  1. `GET /api/shipper/stats`:
  ```java
    @GetMapping("/stats")
    public ResponseEntity<?> getShipperStats() {
        Long shipperId = getCurrentShipperId();
        if (shipperId == null) shipperId = 6L;

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("shipperId", shipperId);
        resp.put("countShipping", orderService.countOrdersForShipper(shipperId, Order.OrderStatus.SHIPPING));
        resp.put("countDelivered", orderService.countOrdersForShipper(shipperId, Order.OrderStatus.DELIVERED));
        resp.put("countReturned", orderService.countOrdersForShipper(shipperId, Order.OrderStatus.RETURNED));
        resp.put("totalCodCollected", orderService.sumCodCollectedByShipper(shipperId));
        return ResponseEntity.ok(resp);
    }
  ```
  2. `POST /api/shipper/orders/{id}/fail`:
  ```java
    @PostMapping("/orders/{id}/fail")
    public ResponseEntity<?> failDelivery(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload
    ) {
        String reason = payload != null ? payload.getOrDefault("reason", "Khách không nhận hàng") : "Giao thất bại";
        try {
            orderService.updateOrderStatus(id, Order.OrderStatus.RETURNED, reason);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Đã ghi nhận giao thất bại đơn hàng #" + id);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
  ```

---

### Task 7: Kiểm thử toàn diện & Xác thực (Verification)
- **Kiểm thử 1 - Unit Tests:** Chạy toàn bộ test suite Maven:
```bash
mvn test
```
*Kỳ vọng:* Toàn bộ test `OrderServiceShipperTest`, `ReportServiceBranchTest`, `VoucherServiceTest` đều PASS.
- **Kiểm thử 2 - Đăng nhập tài khoản Shipper (`shipper_hung` / `123456`):**
  - Tự động chuyển hướng về `/shipper/orders`.
  - Thanh Sidebar chỉ hiển thị duy nhất các mục giao hàng, ẩn 100% menu quản trị kho/báo cáo/người dùng.
  - Thấy 4 thẻ KPI: Số đơn đang giao, đã giao, thất bại và tổng tiền COD đã thu.
  - Mỗi thẻ đơn hiển thị danh sách sách bên trong kiện hàng.
  - Bấm "Chỉ đường" mở Google Maps với đúng địa chỉ khách.
  - Bấm "Báo giao thất bại" bật modal chọn lý do -> cập nhật thành công kèm ghi chú.
  - Bấm "Xác nhận giao thành công" -> Đơn chuyển sang `DELIVERED`, tự động cập nhật COD sang `PAID`.
- **Kiểm thử 3 - REST API:**
  - `curl -s http://localhost:8080/api/shipper/stats` -> Trả về JSON thống kê chính xác.

---

## 5. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi đơn hàng có nhiều sách (`items`), nếu duyệt trong template Thymeleaf có thể phát sinh query N+1 nếu chưa fetch join.
  - *Giải pháp:* Đảm bảo `findOrdersForShipper` sử dụng query có `LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book` hoặc `open-in-view` nạp đầy đủ.
- **Đánh đổi:** Shipper dùng chung layout `admin-layout.html` thay vì tạo layout riêng biệt.
  - *Lợi ích:* Tiết kiệm mã nguồn, tái sử dụng 100% theme Apple Liquid Glass và WebSocket STOMP notification mà không phải viết lại CSS/JS từ đầu.
- **Câu hỏi mở:** Có cần cho phép Shipper chụp ảnh bằng chứng giao hàng (Proof of Delivery - PoD) tải lên Cloudinary không?
  - *Đề xuất hợp lý:* Ở giai đoạn hiện tại, xác nhận 1-chạm kèm ghi chú là đủ đáp ứng rubric đồ án; có thể mở rộng upload ảnh PoD ở pha sau nếu có yêu cầu.
