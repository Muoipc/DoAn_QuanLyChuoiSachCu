# Kế Hoạch Triển Khai: Chuẩn Hóa Trạng Thái Online/Offline Thực Tế Của Chi Nhánh

## 1. Goal
Hoàn thiện tính năng hiển thị trạng thái hoạt động thực tế (Presence Tracking) của 5 chi nhánh: thay thế dòng chữ hiển thị rườm rà mang tính chất demo bằng nhãn tối giản `🟢 Online` / `⚪ Offline` (hoặc chấm xanh tinh gọn), kết hợp cơ chế kiểm tra sự hiện diện thực tế (Heartbeat / Activity Threshold) của Quản lý quầy và chế độ Trợ lý AI.

---

## 2. Current Context & Assumptions
- **Hiện trạng giao diện:**
  - Tiêu đề khung chat (`header.html`) đang hiển thị dòng text dài dòng: `🟢 Quản Lý CN Thủ Đức đang trực` hoặc `🤖 Trợ lý AI đang trực`. Dòng chữ này gây rối mắt, chiếm diện tích và tạo cảm giác "ảo/demo" vì không phản ánh đúng trạng thái thực tế người dùng đang trực hay vắng mặt.
- **Hiện trạng backend:**
  - Hệ thống chỉ kiểm tra nếu Store có `manager != null` thì coi là "đang trực", hoàn toàn không kiểm tra Quản lý có đang đăng nhập hay mở tab chat hay không.
  - Khi Quản lý đóng trình duyệt hoặc offline, khách hàng vẫn thấy báo "đang trực".

---

## 3. Architecture & Proposed Approach
- **Cơ chế Xác Thực Sự Hiện Diện Thực Tế (Real-Time Presence Tracking):**
  - Sử dụng bộ nhớ đệm `ConcurrentHashMap<Long, Long> STORE_LAST_SEEN` (lưu timestamp mili-giây hoạt động gần nhất của từng chi nhánh) trong `StoreChatController`:
    - Mỗi khi Quản lý chi nhánh truy cập trang `/admin/chat`, gửi tin nhắn (`/reply`), hoặc khi trang admin thực hiện polling định kỳ 3 giây (`/conversation`), hệ thống cập nhật `STORE_LAST_SEEN.put(storeId, now)`.
  - **Quy tắc xác định Online:**
    - `isOnline = true` khi:
      1. Quản lý chi nhánh có hoạt động trong vòng 5 phút gần nhất (`now - lastSeen < 300,000ms`), HOẶC
      2. Chi nhánh đang BẬT chế độ Trợ lý AI (`aiReplyEnabled = true` — sẵn sàng hỗ trợ tự động 24/7).
    - `isOnline = false` (Offline) khi: Quản lý không hoạt động quá 5 phút VÀ chế độ AI đang tắt.
- **Tối Giản Hóa Trực Quan (Minimalist Status Indicator):**
  - **Trên Header Room View (`header.html`):**
    - Loại bỏ hoàn toàn câu chữ dài dòng.
    - Khi Online: `TP. Thủ Đức · <span style="color:#059669;"><span class="status-dot-green"></span> Online</span>`.
    - Khi Offline: `TP. Thủ Đức · <span style="color:#78716C;"><span class="status-dot-gray"></span> Offline</span>`.
    - Chấm tròn góc ảnh đại diện chi nhánh tự động chuyển xanh (`#10B981`) khi Online, hoặc xám (`#A8A29E`) khi Offline.
  - **Trên Danh Sách 5 Chi Nhánh (`miniChatShopListView`):**
    - Hiển thị chấm trạng thái tương ứng ngay góc avatar mỗi chi nhánh, giúp khách hàng nhận biết ngay shop nào đang online trước khi bấm vào.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Xây Dựng Presence Tracking Trong `StoreChatController.java`
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/StoreChatController.java`
- **Mục tiêu:** Quản lý liveness trong bộ nhớ và trả về thuộc tính `isOnline` qua các API.
- **Chi tiết mã nguồn cần thêm:**
```java
    // Quản lý thời điểm hoạt động gần nhất của Quản lý từng chi nhánh (Thread-safe)
    private static final java.util.concurrent.ConcurrentMap<Long, Long> STORE_LAST_SEEN = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long PRESENCE_TIMEOUT_MS = 5 * 60 * 1000L; // 5 phút

    public static void touchStorePresence(Long storeId) {
        if (storeId != null) {
            STORE_LAST_SEEN.put(storeId, System.currentTimeMillis());
        }
    }

    public static boolean isStoreOnline(Store store) {
        if (store == null) return false;
        if (Boolean.TRUE.equals(store.getAiReplyEnabled())) {
            return true; // AI trực 24/7
        }
        Long lastSeen = STORE_LAST_SEEN.get(store.getId());
        return lastSeen != null && (System.currentTimeMillis() - lastSeen) < PRESENCE_TIMEOUT_MS;
    }
```
- **Tích hợp vào các endpoint:**
  1. Trong `getStoreMessages` (`GET /api/chat/store/{storeId}/messages`):
     ```java
     boolean isOnline = isStoreOnline(store);
     res.put("isOnline", isOnline);
     ```
  2. Trong `getConversations` (`GET /api/chat/conversations`):
     ```java
     boolean isOnline = isStoreOnline(store);
     item.put("isOnline", isOnline);
     ```
  3. Trong `getStaffConversation` (`GET /api/chat/staff/conversation`) & `replyCustomerMessage`:
     ```java
     touchStorePresence(storeId);
     ```
  4. Bổ sung endpoint nhận tín hiệu duy trì hiện diện:
     ```java
     @PostMapping("/api/chat/staff/heartbeat")
     @ResponseBody
     public ResponseEntity<Map<String, Object>> staffHeartbeat(
             @RequestParam("storeId") Long storeId) {
         touchStorePresence(storeId);
         return ResponseEntity.ok(Map.of("success", true));
     }
     ```

---

### Task 2: Tối Giản Hóa Trực Quan Header Khung Chat Trong `fragments/header.html`
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:** Thay thế dòng text dài dòng bằng chấm xanh / xám và nhãn `Online` / `Offline` thanh thoát.
- **Chi tiết mã nguồn cần cập nhật:**
```html
<!-- DƯỚI TÊN CHI NHÁNH TRÊN HEADER -->
<div style="font-size:10.5px; display:flex; align-items:center; gap:4px; font-weight:500;">
    <span id="activeStoreDistrict">TP. Thủ Đức</span> · 
    <span id="activeStoreStaffStatus">
        <span style="display:inline-flex; align-items:center; gap:3.5px; color:#059669; font-weight:600;">
            <span style="width:6px; height:6px; border-radius:50%; background:#10B981; display:inline-block;"></span> Online
        </span>
    </span>
</div>
```
```javascript
// CẬP NHẬT TRONG HÀM loadStoreChatMessages & startMiniChatPolling
function updateStoreStatusUI(isOnline) {
    var statusEl = document.getElementById('activeStoreStaffStatus');
    var dotEl = document.getElementById('activeStoreAvatarDot');
    if (statusEl) {
        if (isOnline) {
            statusEl.innerHTML = '<span style="display:inline-flex; align-items:center; gap:3.5px; color:#059669; font-weight:600;"><span style="width:6px; height:6px; border-radius:50%; background:#10B981; display:inline-block; box-shadow:0 0 0 2px rgba(16,185,129,0.2);"></span> Online</span>';
            if (dotEl) dotEl.style.background = '#10B981';
        } else {
            statusEl.innerHTML = '<span style="display:inline-flex; align-items:center; gap:3.5px; color:#78716C; font-weight:500;"><span style="width:6px; height:6px; border-radius:50%; background:#A8A29E; display:inline-block;"></span> Offline</span>';
            if (dotEl) dotEl.style.background = '#A8A29E';
        }
    }
}
```

---

### Task 3: Hiển Thị Chấm Online / Offline Trên Danh Sách 5 Chi Nhánh (`fragments/header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html` (hàm `loadShopConversations`)
- **Mục tiêu:** Hiển thị chấm xanh góc ảnh đại diện nếu chi nhánh đang online, chấm xám nếu offline.
- **Chi tiết mã nguồn:**
```javascript
// TRÊN AVATAR CỦA TỪNG CHI NHÁNH
var onlineDot = s.isOnline 
    ? '<span style="position:absolute; bottom:0; right:0; width:9px; height:9px; border-radius:50%; background:#10B981; border:2px solid #FFFFFF;" title="Đang trực tuyến"></span>'
    : '<span style="position:absolute; bottom:0; right:0; width:9px; height:9px; border-radius:50%; background:#A8A29E; border:2px solid #FFFFFF;" title="Ngoại tuyến"></span>';
```

---

### Task 4: Gửi Heartbeat Từ Trang Quản Trị Chi Nhánh (`admin/chat/index.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/chat/index.html`
- **Mục tiêu:** Khi Quản lý mở trang chat, tự động ghi nhận sự hiện diện thực tế.
- **Chi tiết mã nguồn:**
```javascript
// Gửi Heartbeat định kỳ 30 giây khi trang admin chat đang mở
function sendStaffHeartbeat() {
    if (!currentStoreId) return;
    fetch('/api/chat/staff/heartbeat?storeId=' + currentStoreId, { method: 'POST' })
        .catch(function(e) { console.debug(e); });
}

window.addEventListener('load', function() {
    sendStaffHeartbeat();
    setInterval(sendStaffHeartbeat, 30000);
});
```

---

### Task 5: Viết Unit Test & Kiểm Thử Nghiệp Vụ Presence
- **Tệp bổ sung:** `src/test/java/vn/iotstar/service/StoreChatBranchTest.java`
- **Mục tiêu test:**
  1. Kiểm tra Store không có hoạt động và AI tắt -> `isStoreOnline` trả về `false`.
  2. Cập nhật `touchStorePresence` -> `isStoreOnline` chuyển sang `true`.
  3. Store bật AI -> Luôn trả về `true`.
- **Lệnh thực thi:**
```bash
mvn -f pom.xml test -Dtest=StoreChatBranchTest
```

---

## 5. Risks & Tradeoffs
- **Bộ nhớ vs Cơ sở dữ liệu:** Lưu trạng thái online trong RAM (`ConcurrentHashMap`) thay vì ghi DB liên tục giúp tránh nghẽn truy vấn I/O MySQL khi nhiều cửa hàng polling cùng lúc.
- **Độ trễ:** Thời gian chuyển từ Online sang Offline là 5 phút sau khi đóng tab, phù hợp với tiêu chuẩn chat thương mại điện tử (tránh nhấp nháy trạng thái khi người dùng chuyển trang).
