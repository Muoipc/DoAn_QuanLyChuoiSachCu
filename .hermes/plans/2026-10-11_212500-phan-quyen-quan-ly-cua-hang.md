# Kế Hoạch Triển Khai: Phân Quyền Chi Tiết Cho Quản Lý Cửa Hàng (Store Manager)

## 1. Goal
Thiết lập hệ thống phân quyền phân tầng và cách ly dữ liệu đa chi nhánh (Multi-Store Data Isolation) cho Quản lý cửa hàng (`ROLE_MANAGER`), đảm bảo mỗi quản lý chỉ được toàn quyền điều hành, xử lý đơn hàng, kho bãi, thẩm định ký gửi, chat và xem báo cáo tại duy nhất chi nhánh mình phụ trách; đồng thời chặn hoàn toàn quyền truy cập vào các chức năng quản trị cấp chuỗi của Quản trị viên (`ROLE_ADMIN`).

---

## 2. Current Context & Assumptions
- **Hệ thống hiện tại:** Spring Boot 3.4.3, Spring Security 6, Thymeleaf Layout Dialect, MySQL 8.x (`old_book_store_db`).
- **CSDL & Thực thể:**
  - Bảng `roles`: `ROLE_ADMIN` (id: 1), `ROLE_MANAGER` (id: 2), `ROLE_USER` (id: 3), `ROLE_SHIPPER` (id: 4).
  - Bảng `stores`: Có cột `manager_id` (FK trỏ tới `users.id`). Chi nhánh 1 (Tổng Kho) đang gán cho `manager_td` (user id: 2), các chi nhánh 2-5 đang `NULL`.
  - Bảng `users`: Đã có tài khoản `manager_td` (CN Thủ Đức) và `manager_q1` (CN Quận 1) mang vai trò `ROLE_MANAGER`.
- **Thực trạng phân quyền:**
  - `SecurityConfig.java` đang cấu hình `.requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")`. Điều này khiến `ROLE_MANAGER` có thể truy cập trái phép vào các tính năng cấp hệ thống như: Quản lý mã giảm giá toàn chuỗi (`/admin/vouchers/**`), Đơn vị vận chuyển (`/admin/shipping-units/**`), Tạo/Xóa chi nhánh (`/admin/stores/create`, `/admin/stores/delete/**`), và Quản lý danh mục thể loại (`/admin/categories/**`).
  - `AdminReportController.java` hiện chỉ tính tổng doanh thu toàn chuỗi, Manager vào xem dashboard sẽ thấy dữ liệu tài chính của cả 5 chi nhánh và có thể xuất Excel toàn bộ đơn hàng của chuỗi.
  - `admin-layout.html` đang hiển thị menu Vouchers, Shipping Units, Toàn bộ chi nhánh cho cả Manager mà chưa có điều kiện ẩn/hiện theo vai trò.
  - Bộ đếm trạng thái đơn hàng (`AdminOrderController.java`) đang đếm tổng toàn hệ thống chứ chưa lọc theo chi nhánh của Manager.

---

## 3. Architecture & Proposed Approach
Áp dụng chiến lược **Bảo Mật 3 Lớp (Defense-in-Depth)**:
1. **Lớp 1 - Route & URL Authorization (Web Firewall):** Tinh chỉnh `SecurityConfig.java` để phân định tuyệt đối quyền hạn cấp hệ thống: chỉ `ROLE_ADMIN` được truy cập Vouchers, Shipping Units, Tạo/Xóa Chi nhánh và Danh mục; cả `ADMIN` và `MANAGER` được truy cập Kho, Đơn hàng, Ký gửi, Báo cáo và Chat.
2. **Lớp 2 - Data-Level Tenancy Isolation (Bộ lọc dữ liệu chi nhánh):** Bổ sung phương thức báo cáo theo chi nhánh (`storeId`) vào `IReportService` và `ReportServiceImpl`. Cập nhật `AdminReportController` và `AdminOrderController` để tự động ràng buộc dữ liệu theo `storeService.findByManagerId(userId)`. Nếu Manager cố tình truy cập IDOR sang chi nhánh khác, hệ thống lập tức chặn và redirect về trang an toàn kèm thông báo lỗi.
3. **Lớp 3 - Adaptive UI (Giao diện thích ứng vai trò):** Cập nhật `admin-layout.html` sử dụng thẻ `sec:authorize="hasRole('ADMIN')"` để ẩn các menu không thuộc thẩm quyền, đổi nhãn điều hướng (ví dụ: "Báo Cáo Chi Nhánh" thay vì "Thống Kê Báo Cáo"), hiển thị huy hiệu chi nhánh trực thuộc trên Topbar và hiển thị cảnh báo thân thiện nếu tài khoản chưa được gán chi nhánh.

---

## 4. Step-by-Step Tasks

### Task 1: Chuẩn hóa dữ liệu phân công quản lý chi nhánh trong CSDL
- **Mục tiêu:** Gán đầy đủ `manager_id` cho các chi nhánh trong CSDL để phục vụ kiểm thử phân quyền 2 chi nhánh độc lập (`manager_td` cho CN 1, `manager_q1` cho CN 2).
- **Thao tác:** Chạy câu lệnh SQL cập nhật bảng `stores`:
```sql
-- Gán manager_td (id=2) quản lý Chi Nhánh 1 (Tổng Kho TP.HCM)
UPDATE stores SET manager_id = 2 WHERE id = 1;
-- Gán manager_q1 (id=3) quản lý Chi Nhánh 2 (Đường Sách Quận 1)
UPDATE stores SET manager_id = 3 WHERE id = 2;
```
- **Xác minh:**
```bash
mysql -u root -p123456 -e "SELECT id, store_name, manager_id FROM old_book_store_db.stores WHERE id IN (1, 2);"
```
*Kỳ vọng:* CN 1 có `manager_id = 2`, CN 2 có `manager_id = 3`.

---

### Task 2: Viết Unit Test kiểm thử Báo cáo theo từng chi nhánh (TDD - Red Phase)
- **Tệp tạo mới:** `src/test/java/vn/iotstar/service/ReportServiceBranchTest.java`
- **Nội dung kiểm thử:** Kiểm tra tính năng báo cáo theo chi nhánh riêng biệt không bị lẫn lộn dữ liệu giữa các cửa hàng:
```java
package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.impl.ReportServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceBranchTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StoreRepository storeRepository;

    private IReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(orderRepository, orderItemRepository, bookRepository, storeRepository);
    }

    @Test
    @DisplayName("Báo cáo tổng quan chỉ tính doanh thu và đơn hàng của chi nhánh được chỉ định")
    void testGetDashboardSummaryByStore() {
        Long storeId = 1L;
        when(orderRepository.countByStoreId(storeId)).thenReturn(15L);
        when(orderRepository.sumRevenueByStoreId(storeId)).thenReturn(new BigDecimal("2500000.00"));
        when(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.DELIVERED)).thenReturn(12L);
        when(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.CANCELLED)).thenReturn(2L);

        Map<String, Object> summary = reportService.getDashboardSummaryByStore(storeId);

        assertNotNull(summary);
        assertEquals(15L, summary.get("totalOrders"));
        assertEquals(new BigDecimal("2500000.00"), summary.get("totalRevenue"));
        assertEquals(12L, summary.get("completedOrders"));
        assertEquals(2L, summary.get("cancelledOrders"));
    }
}
```
- **Xác minh Red Phase:**
```bash
mvn test -Dtest=ReportServiceBranchTest
```
*Kỳ vọng:* Biên dịch lỗi do chưa khai báo phương thức `getDashboardSummaryByStore(Long storeId)`.

---

### Task 3: Bổ sung phương thức Báo cáo theo Chi nhánh vào `IReportService` và `ReportServiceImpl` (TDD - Green Phase)
- **Tệp chỉnh sửa 1:** `src/main/java/vn/iotstar/service/IReportService.java`
- **Mã nguồn bổ sung:**
```java
    // === CÁC PHƯƠNG THỨC BÁO CÁO DÀNH CHO QUẢN LÝ CHI NHÁNH ===
    Map<String, Object> getDashboardSummaryByStore(Long storeId);
    Map<Integer, BigDecimal> getMonthlyRevenueByStore(int year, Long storeId);
    Map<String, Long> getOrderStatusDistributionByStore(Long storeId);
    List<Book> getTopSellingBooksByStore(Long storeId, int limit);
    void exportStoreOrdersToExcel(HttpServletResponse response, Long storeId) throws IOException;
```
- **Tệp chỉnh sửa 2:** `src/main/java/vn/iotstar/repository/OrderRepository.java`
- **Mã nguồn bổ sung:**
```java
    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.store.id = :storeId AND o.status = 'DELIVERED'")
    BigDecimal sumRevenueByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId AND o.status = :status")
    long countByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId ORDER BY o.createdAt DESC")
    List<Order> findByStoreIdOrderByCreatedAtDesc(@Param("storeId") Long storeId);
```
- **Tệp chỉnh sửa 3:** `src/main/java/vn/iotstar/service/impl/ReportServiceImpl.java`
- **Mã nguồn triển khai:**
```java
    @Override
    public Map<String, Object> getDashboardSummaryByStore(Long storeId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", orderRepository.countByStoreId(storeId));
        summary.put("totalRevenue", orderRepository.sumRevenueByStoreId(storeId));
        summary.put("completedOrders", orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.DELIVERED));
        summary.put("cancelledOrders", orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.CANCELLED));
        return summary;
    }
```
- **Xác minh Green Phase:**
```bash
mvn test -Dtest=ReportServiceBranchTest
```
*Kỳ vọng:* `BUILD SUCCESS` (1 test passed).

---

### Task 4: Siết chặt URL Authorization trong `SecurityConfig.java`
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/config/SecurityConfig.java`
- **Thay đổi:** Tách bạch các quyền cấp Chuỗi (Admin only) và các quyền cấp Cửa hàng (Admin & Manager):
```java
                // 1. Phân hệ Quản trị viên cấp cao (ADMIN ONLY)
                .requestMatchers(
                    "/admin/users", "/admin/users/**",
                    "/admin/vouchers", "/admin/vouchers/**",
                    "/admin/shipping-units", "/admin/shipping-units/**",
                    "/admin/stores/create", "/admin/stores/delete/**",
                    "/admin/categories/create", "/admin/categories/edit/**",
                    "/admin/categories/save", "/admin/categories/delete/**"
                ).hasRole("ADMIN")

                // 2. Phân hệ Nghiệp vụ cửa hàng & chuỗi (ADMIN & STORE MANAGER)
                .requestMatchers(
                    "/admin/books", "/admin/books/**",
                    "/admin/categories",
                    "/admin/inventory", "/admin/inventory/**",
                    "/admin/stores", "/admin/stores/edit/**", "/admin/stores/save",
                    "/admin/chat", "/admin/chat/**",
                    "/admin/orders", "/admin/orders/**",
                    "/admin/consignments", "/admin/consignments/**",
                    "/admin/reports", "/admin/reports/**", "/admin/dashboard", "/admin",
                    "/store-manager/**"
                ).hasAnyRole("ADMIN", "MANAGER")

                // 3. Phân hệ Shipper
                .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")
```
- **Xác minh biên dịch:**
```bash
mvn compile -DskipTests
```
*Kỳ vọng:* `BUILD SUCCESS`.

---

### Task 5: Ràng buộc phạm vi chi nhánh trong `AdminReportController.java`
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/admin/AdminReportController.java`
- **Thay đổi:** Thêm `@AuthenticationPrincipal CustomUserDetails userDetails` vào `dashboard` và `exportExcel`.
```java
    @GetMapping({"", "/", "/dashboard", "/reports", "/analytics"})
    public String dashboard(
            @RequestParam(name = "year", required = false) Integer yearParam,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) throws JsonProcessingException {
        int year = (yearParam != null) ? yearParam : LocalDate.now().getYear();
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (isAdmin) {
            // ADMIN: Dữ liệu toàn chuỗi
            model.addAttribute("summary", reportService.getDashboardSummary());
            model.addAttribute("monthlyRevenueJson", objectMapper.writeValueAsString(new ArrayList<>(reportService.getMonthlyRevenue(year).values())));
            Map<String, BigDecimal> storeRev = reportService.getStoreRevenue();
            model.addAttribute("storeLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(storeRev.keySet())));
            model.addAttribute("storeDataJson", objectMapper.writeValueAsString(new ArrayList<>(storeRev.values())));
            model.addAttribute("statusLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(reportService.getOrderStatusDistribution().keySet())));
            model.addAttribute("statusDataJson", objectMapper.writeValueAsString(new ArrayList<>(reportService.getOrderStatusDistribution().values())));
            model.addAttribute("topBooks", reportService.getTopSellingBooks(5));
        } else {
            // STORE MANAGER: Dữ liệu duy nhất của chi nhánh quản lý
            Long storeId = (managedStore != null) ? managedStore.getId() : -1L;
            model.addAttribute("summary", reportService.getDashboardSummaryByStore(storeId));
            model.addAttribute("monthlyRevenueJson", objectMapper.writeValueAsString(new ArrayList<>(reportService.getMonthlyRevenueByStore(year, storeId).values())));
            model.addAttribute("storeLabelsJson", objectMapper.writeValueAsString(managedStore != null ? List.of(managedStore.getStoreName()) : List.of()));
            model.addAttribute("storeDataJson", objectMapper.writeValueAsString(managedStore != null ? List.of(reportService.getDashboardSummaryByStore(storeId).get("totalRevenue")) : List.of()));
            model.addAttribute("statusLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(reportService.getOrderStatusDistributionByStore(storeId).keySet())));
            model.addAttribute("statusDataJson", objectMapper.writeValueAsString(new ArrayList<>(reportService.getOrderStatusDistributionByStore(storeId).values())));
            model.addAttribute("topBooks", reportService.getTopSellingBooksByStore(storeId, 5));
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);
        model.addAttribute("selectedYear", year);
        return "admin/reports/dashboard";
    }

    @GetMapping("/reports/export-excel")
    public void exportExcel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) throws IOException {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (isAdmin) {
            reportService.exportOrdersToExcel(response);
        } else if (managedStore != null) {
            reportService.exportStoreOrdersToExcel(response, managedStore.getId());
        }
    }
```
- **Xác minh:** Biên dịch không có cảnh báo (`mvn compile -DskipTests`).

---

### Task 6: Lọc bộ đếm trạng thái đơn hàng theo chi nhánh trong `AdminOrderController.java`
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/admin/AdminOrderController.java`
- **Thay đổi:** Dùng `effectiveStoreId` để đếm số lượng đơn `NEW`, `CONFIRMED`,... cho Quản lý chi nhánh:
```java
        // Đếm theo từng trạng thái đơn hàng (Đồng bộ theo phạm vi chi nhánh)
        if (isAdmin) {
            model.addAttribute("countNew", orderService.countByStatus(Order.OrderStatus.NEW));
            model.addAttribute("countConfirmed", orderService.countByStatus(Order.OrderStatus.CONFIRMED));
            model.addAttribute("countShipping", orderService.countByStatus(Order.OrderStatus.SHIPPING));
            model.addAttribute("countDelivered", orderService.countByStatus(Order.OrderStatus.DELIVERED));
            model.addAttribute("countCancelled", orderService.countByStatus(Order.OrderStatus.CANCELLED));
            model.addAttribute("countReturned", orderService.countByStatus(Order.OrderStatus.RETURNED));
        } else {
            Long sId = (managedStore != null) ? managedStore.getId() : -1L;
            model.addAttribute("countNew", orderService.countByStoreIdAndStatus(sId, Order.OrderStatus.NEW));
            model.addAttribute("countConfirmed", orderService.countByStoreIdAndStatus(sId, Order.OrderStatus.CONFIRMED));
            model.addAttribute("countShipping", orderService.countByStoreIdAndStatus(sId, Order.OrderStatus.SHIPPING));
            model.addAttribute("countDelivered", orderService.countByStoreIdAndStatus(sId, Order.OrderStatus.DELIVERED));
            model.addAttribute("countCancelled", orderService.countByStoreIdAndStatus(sId, Order.OrderStatus.CANCELLED));
            model.addAttribute("countReturned", orderService.countByStoreIdAndStatus(sId, Order.OrderStatus.RETURNED));
        }
```

---

### Task 7: Cập nhật Sidebar & Topbar trong `admin-layout.html`
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/layout/admin-layout.html`
- **Thay đổi 1 (Ẩn các mục Admin-only):** Bọc các thẻ `<li>` không thuộc thẩm quyền của Manager:
```html
            <!-- Người dùng: Chỉ Admin -->
            <li class="sidebar-item" th:if="${#authorization.expression('hasRole(''ADMIN'')')}" ...>
                <a th:href="@{/admin/users}">...</a>
            </li>

            <!-- Mã Giảm Giá: Chỉ Admin -->
            <li class="sidebar-item" th:if="${#authorization.expression('hasRole(''ADMIN'')')}" th:classappend="${#httpServletRequest != null and #httpServletRequest.requestURI.startsWith('/admin/vouchers')} ? 'active' : ''">
                <a th:href="@{/admin/vouchers}">
                    <i class="fa-solid fa-ticket"></i>
                    <span>Mã Giảm Giá</span>
                </a>
            </li>

            <!-- Đơn Vị Vận Chuyển: Chỉ Admin -->
            <li class="sidebar-item" th:if="${#authorization.expression('hasRole(''ADMIN'')')}" th:classappend="${#httpServletRequest != null and #httpServletRequest.requestURI.startsWith('/admin/shipping-units')} ? 'active' : ''">
                <a th:href="@{/admin/shipping-units}">
                    <i class="fa-solid fa-truck-fast"></i>
                    <span>Đơn Vị Vận Chuyển</span>
                </a>
            </li>
```
- **Thay đổi 2 (Đổi tiêu đề báo cáo phù hợp):**
```html
            <li class="sidebar-item" th:classappend="${#httpServletRequest != null and #httpServletRequest.requestURI.startsWith('/admin/reports')} ? 'active' : ''">
                <a th:href="@{/admin/reports}">
                    <i class="fa-solid fa-chart-pie"></i>
                    <span th:text="${#authorization.expression('hasRole(''ADMIN'')') ? 'Thống Kê Báo Cáo' : 'Báo Cáo Chi Nhánh'}">Thống Kê Báo Cáo</span>
                </a>
            </li>
```
- **Thay đổi 3 (Hiển thị chi nhánh quản lý trên Topbar Header):**
```html
            <!-- Hiển thị badge chi nhánh đang quản lý -->
            <div class="d-none d-md-flex align-items-center me-3" th:if="${managedStore != null}">
                <span class="badge rounded-pill" style="background: rgba(140, 74, 39, 0.12); color: #8C4A27; border: 1px solid rgba(140, 74, 39, 0.25); font-size: 11.5px; padding: 6px 12px;">
                    <i class="fa-solid fa-store me-1"></i>
                    <strong th:text="${managedStore.storeName}">Chi Nhánh</strong>
                </span>
            </div>
```
- **Thay đổi 4 (Banner cảnh báo nếu Manager chưa được gán chi nhánh):**
```html
            <div class="alert alert-warning mb-3 d-flex align-items-center" role="alert" th:if="${#authorization.expression('hasRole(''ROLE_MANAGER'')') and managedStore == null}">
                <i class="fa-solid fa-triangle-exclamation me-2 fs-5"></i>
                <div>Tài khoản Quản lý của bạn chưa được liên kết với chi nhánh nào. Vui lòng liên hệ Quản trị viên (Admin) để được phân công cửa hàng phụ trách!</div>
            </div>
```

---

### Task 8: Kiểm thử tích hợp & Xác thực kết quả phân quyền (Verification)
- **Kiểm thử 1 - Đăng nhập tài khoản `admin`:**
  - Truy cập `/admin/users` -> HTTP 200 (Thành công).
  - Truy cập `/admin/vouchers` -> HTTP 200 (Thành công).
  - Truy cập `/admin/reports` -> Hiển thị biểu đồ doanh thu toàn bộ 5 chi nhánh.
  - Xem danh sách đơn hàng -> Thấy đầy đủ đơn của tất cả các chi nhánh.
- **Kiểm thử 2 - Đăng nhập tài khoản `manager_td` (Thủ Đức):**
  - Truy cập `/admin/users` -> Bị chặn (HTTP 403 hoặc chuyển hướng từ chối quyền).
  - Truy cập `/admin/vouchers` -> Bị chặn (HTTP 403).
  - Truy cập `/admin/shipping-units` -> Bị chặn (HTTP 403).
  - Truy cập `/admin/orders` -> Chỉ thấy đơn hàng của CN Thủ Đức (id=1). Thử truy cập `/admin/orders/{id_don_hang_quan_1}` -> Bị chặn với thông báo "Bạn không có quyền xem chi tiết đơn hàng của chi nhánh khác!".
  - Truy cập `/admin/reports` -> Chỉ thấy biểu đồ doanh thu và cơ cấu đơn hàng của riêng CN Thủ Đức. Nút "Xuất Excel" chỉ xuất các đơn của CN Thủ Đức.

---

## 5. Risks, Tradeoffs & Open Questions
- **Rủi ro:** Khi Manager chưa được gán chi nhánh (`managedStore == null`), nếu các Controller gọi `managedStore.getId()` trực tiếp sẽ gây `NullPointerException`.
  - *Giải pháp:* Đã bao bọc an toàn: `Long effectiveStoreId = (managedStore != null) ? managedStore.getId() : -1L;` và hiển thị banner thông báo thân thiện.
- **Đánh đổi:** Quản lý chi nhánh không thể tạo mới mã giảm giá riêng (Shop Voucher). Hiện tại toàn bộ voucher đều là voucher toàn chuỗi. Nếu sau này nhóm muốn mở rộng tính năng "Mã giảm giá riêng của từng shop" (giống Shopee), có thể thêm cột `store_id` vào bảng `vouchers`.
- **Câu hỏi mở:** Có cho phép Quản lý chi nhánh thêm sách mới vào danh mục hệ thống không (`/admin/books/create`)?
  - *Đề xuất hợp lý:* Cho phép Manager thêm sách cũ vào kho (`/admin/books/create`), nhưng chỉ thêm số lượng vào chi nhánh của mình (`inventoryService`).
