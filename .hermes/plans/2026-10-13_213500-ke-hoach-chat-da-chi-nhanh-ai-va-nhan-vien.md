# Kế Hoạch Triển Khai: Hệ Thống Nhắn Tin Đa Chi Nhánh — Kết Hợp Nhân Viên Quầy Thực Tế & Chế Độ Trợ Lý AI

## 1. Goal
Xây dựng giải pháp nhắn tin 2 chiều toàn diện giữa khách hàng và 5 chi nhánh chuỗi ReBook: cấp đủ 5 tài khoản Quản lý cửa hàng (ROLE_MANAGER), tích hợp công tắc bật/tắt chế độ AI trả lời tự động cho từng shop, hỗ trợ khách hàng chuyển đổi sang người thật 1-chạm `[👨‍💼 Gặp nhân viên quầy]` kèm nhận diện từ khóa NLP, và đồng bộ tin nhắn thời gian thực (Live Polling 3s) ở cả hai đầu giao diện.

---

## 2. Current Context & Assumptions
- **Hiện trạng 5 chi nhánh cửa hàng:**
  - `Store 1` (Tổng Kho Thủ Đức): Đã có `manager_td` (ID: 2).
  - `Store 2` (Đường Sách Quận 1): Đã có `manager_q1` (ID: 3).
  - `Store 3` (Làng ĐH Quốc Gia): `manager_id = NULL` (chưa có tài khoản đăng nhập).
  - `Store 4` (Phố Sách Phú Nhuận): `manager_id = NULL` (chưa có tài khoản đăng nhập).
  - `Store 5` (Khu Học Thuật Quận 5): `manager_id = NULL` (chưa có tài khoản đăng nhập).
- **Điểm nghẽn trong nghiệp vụ chat hiện tại:**
  - AI bot tự động trả lời đè lên 100% tin nhắn khách gửi (`StoreChatController.java`), khiến kênh chat mang tính chất "demo bot", triệt tiêu vai trò của nhân viên cửa hàng.
  - Quản lý cửa hàng không có công tắc để chủ động quyết định: lúc nào muốn AI trực thay (ngoài giờ, bận rộn) và lúc nào muốn trực tiếp tư vấn cho khách.
  - Khi Shop bật chế độ AI, khách hàng bị kẹt với bot, chưa có nút bấm hoặc cơ chế chuyển tiếp sang người thật khi cần thương lượng giá, xem ảnh chi tiết hay khiếu nại.
  - Cả khách hàng (`header.html`) và Quản lý shop (`admin/chat/index.html`) đều không có cơ chế tự động nạp tin nhắn mới (polling); gửi tin nhắn bị reload trang hoặc phải F5 mới thấy phản hồi.

---

## 3. Architecture & Proposed Approach
- **1. Phân quyền & Cách ly Chi Nhánh (Strict Multi-Tenant Isolation):**
  - Cấp 3 tài khoản Quản lý mới (`manager_dhqg`, `manager_pn`, `manager_q5`), mật khẩu đồng bộ `123456`, gán đúng `manager_id` vào bảng `stores`.
  - Quản lý của Shop nào chỉ được quyền đọc và trả lời khách hàng gửi đến đúng Shop đó (`403 Forbidden` nếu truy cập chéo).
- **2. Cơ chế Linh Hoạt: Quản Lý Trực Quầy vs Trợ Lý AI Trả Lời Giùm:**
  - Bổ sung trường `ai_reply_enabled` (TINYINT) vào bảng `stores` và entity `Store.java`.
  - **Trên giao diện Quản lý (`/admin/chat`):** Công tắc Toggle Switch Apple Liquid Glass cho phép Quản lý bật hoặc tắt chế độ AI trả lời thay thế bất kỳ lúc nào qua AJAX.
    - *Khi TẮT:* Nhân viên trực tiếp quản lý quầy — AI không can thiệp, tin nhắn của khách chờ nhân viên trả lời.
    - *Khi BẬT:* AI tự động trả lời thay nhân viên đối với các câu hỏi thông thường.
- **3. Cơ chế Khách Hàng Chuyển Sang Người Thật Khi Shop Đang Bật AI:**
  - **Cách 1: Nút 1-chạm `[👨‍💼 Gặp nhân viên quầy]`:** Đặt ngay trên thanh nhập tin nhắn của khách. Bấm vào lập tức ngắt AI cho phiên chat này, kích hoạt cờ `needsAttention = true` và badge đỏ thông báo cho Shop.
  - **Cách 2: Nhận diện từ khóa tự động (NLP Intent Fallback):** Khi khách gõ các từ khóa: *"gặp người thật"*, *"alo shop"*, *"thủ kho đâu"*, *"khiếu nại"*, *"nhân viên đâu"*, *"bớt giá"*, *"chụp ảnh thật"*, AI tự động dừng trả lời và thông báo chuyển giao cho nhân viên quầy.
- **4. Đồng bộ Thời Gian Thực 2 Chiều (Zero Page Reload):**
  - Khách hàng (`header.html`): Timer 3 giây gọi `GET /api/chat/store/{storeId}/messages`, tự động nhận và hiển thị ngay câu trả lời của nhân viên quầy.
  - Quản lý shop (`admin/chat/index.html`): Timer 3 giây gọi `GET /api/chat/staff/conversation`, tự động cập nhật tin nhắn mới của khách và danh sách khách hàng mà không cần F5. Gửi tin bằng AJAX không giật màn hình.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: CSDL MySQL — Cấp 5 Tài Khoản Quản Lý & Thêm Cột `ai_reply_enabled`
- **Mục tiêu:** Cấp đủ 5 tài khoản Quản lý cho 5 chi nhánh và bổ sung trường cờ `ai_reply_enabled`.
- **Lệnh SQL thực thi:**
```sql
-- 1. Bổ sung cột bật/tắt chế độ AI trả lời tự động cho từng chi nhánh
ALTER TABLE stores ADD COLUMN IF NOT EXISTS ai_reply_enabled TINYINT(1) DEFAULT 0;

-- 2. Thêm tài khoản quản lý cho Store 3 (Làng ĐH), Store 4 (Phú Nhuận), Store 5 (Quận 5)
-- Mật khẩu BCrypt cho '123456': $2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i
INSERT INTO users (username, password, full_name, email, phone, role_id, enabled)
VALUES 
('manager_dhqg', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Quản Lý CN Làng ĐHQG', 'dhqg@rebook.vn', '0901234503', 2, 1),
('manager_pn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Quản Lý CN Phú Nhuận', 'phunhuan@rebook.vn', '0901234504', 2, 1),
('manager_q5', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Quản Lý CN Quận 5', 'quan5@rebook.vn', '0901234505', 2, 1)
ON DUPLICATE KEY UPDATE enabled = 1;

-- 3. Cập nhật manager_id trong bảng stores
UPDATE stores SET manager_id = (SELECT id FROM users WHERE username = 'manager_dhqg') WHERE id = 3;
UPDATE stores SET manager_id = (SELECT id FROM users WHERE username = 'manager_pn') WHERE id = 4;
UPDATE stores SET manager_id = (SELECT id FROM users WHERE username = 'manager_q5') WHERE id = 5;
```
- **Lệnh xác thực trong Terminal:**
```bash
mysql -u root -p123456 old_book_store_db -e "SELECT s.id, s.store_name, u.username, u.full_name, s.ai_reply_enabled FROM stores s LEFT JOIN users u ON s.manager_id = u.id;"
```
- **Kết quả kỳ vọng:** Đầy đủ 5 chi nhánh đều có username tương ứng (`manager_td`, `manager_q1`, `manager_dhqg`, `manager_pn`, `manager_q5`) và cột `ai_reply_enabled` = 0.

---

### Task 2: Cập Nhật Entity `Store.java`
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/entity/Store.java`
- **Mục tiêu:** Bổ sung thuộc tính `aiReplyEnabled` (Boolean) ánh xạ với cột `ai_reply_enabled`.
- **Mã nguồn thêm vào:**
```java
    @Column(name = "ai_reply_enabled")
    private Boolean aiReplyEnabled = false;

    public Boolean getAiReplyEnabled() {
        return aiReplyEnabled != null ? aiReplyEnabled : false;
    }

    public void setAiReplyEnabled(Boolean aiReplyEnabled) {
        this.aiReplyEnabled = aiReplyEnabled;
    }
```
- **Lệnh xác thực:**
```bash
mvn -f pom.xml compile
```

---

### Task 3: Nâng Cấp `StoreChatController.java` — Logic Phân Luồng AI, Bật/Tắt & Yêu Cầu Người Thật
- **Tệp chỉnh sửa:** `src/main/java/vn/iotstar/controller/StoreChatController.java`
- **Mục tiêu:**
  1. **API Bật/Tắt AI cho Quản lý:**
     `POST /api/chat/store/{storeId}/toggle-ai`
  2. **API Yêu cầu gặp nhân viên quầy cho Khách hàng:**
     `POST /api/chat/store/{storeId}/request-staff`
  3. **API nạp hội thoại thời gian thực cho Quản lý:**
     `GET /api/chat/staff/conversation?storeId={storeId}&userId={userId}`
  4. **Phân luồng tin nhắn khách hàng trong `sendCustomerMessage`:**
     - Kiểm tra nếu khách có yêu cầu gặp người thật hoặc nội dung khớp từ khóa đặc biệt -> Gắn cờ `needsAttention = true`, ngắt bot.
     - Nếu Shop đang BẬT chế độ AI (`store.getAiReplyEnabled() == true`) VÀ chưa yêu cầu người thật -> AI sinh câu trả lời tự động.
     - Nếu Shop đang TẮT chế độ AI -> Chỉ gửi tin nhắn tiếp nhận ở lần đầu tiên; các tin nhắn sau chờ nhân viên shop trả lời trực tiếp.
- **Mã nguồn chi tiết:**
```java
    /**
     * API: Quản lý bật/tắt chế độ AI trả lời giùm
     */
    @PostMapping("/api/chat/store/{storeId}/toggle-ai")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleAiReply(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Store> managerStores = storeRepository.findByManagerId(userDetails.getId());
        Store store = managerStores.stream().filter(s -> s.getId().equals(storeId)).findFirst().orElse(null);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Bạn không có quyền quản lý chi nhánh này!"));
        }
        boolean newState = !Boolean.TRUE.equals(store.getAiReplyEnabled());
        store.setAiReplyEnabled(newState);
        storeRepository.save(store);
        return ResponseEntity.ok(Map.of("success", true, "aiReplyEnabled", newState));
    }

    /**
     * API: Khách hàng bấm nút yêu cầu gặp nhân viên quầy trực tiếp
     */
    @PostMapping("/api/chat/store/{storeId}/request-staff")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> requestHumanStaff(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = resolveCurrentUser(userDetails);
        Store store = storeRepository.findById(storeId).orElse(null);
        if (currentUser == null || store == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }

        ChatMessage reqMsg = new ChatMessage();
        reqMsg.setUser(currentUser);
        reqMsg.setStore(store);
        reqMsg.setSenderType(ChatMessage.SenderType.CUSTOMER);
        reqMsg.setSenderName(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
        reqMsg.setContent("[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]");
        reqMsg.setNeedsAttention(true);
        chatMessageRepository.save(reqMsg);

        ChatMessage noticeMsg = new ChatMessage();
        noticeMsg.setUser(currentUser);
        noticeMsg.setStore(store);
        noticeMsg.setSenderType(ChatMessage.SenderType.STORE_STAFF);
        noticeMsg.setSenderName("Hệ thống chuyển giao");
        noticeMsg.setContent("Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy " + store.getStoreName() + " sẽ tiếp quản cuộc trò chuyện và phản hồi cho bạn ngay trong ít phút nhé ạ! 👨‍💼✨");
        noticeMsg.setNeedsAttention(true);
        chatMessageRepository.save(noticeMsg);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "reply", noticeMsg.getContent(),
            "createdAt", noticeMsg.getCreatedAt().format(TIME_FMT)
        ));
    }

    /**
     * API: Nạp hội thoại thời gian thực cho trang Quản trị chi nhánh (Live Polling)
     */
    @GetMapping("/api/chat/staff/conversation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStaffConversation(
            @RequestParam("storeId") Long storeId,
            @RequestParam("userId") Long customerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatMessage> conversation = chatMessageRepository.findConversation(storeId, customerId);
        List<Map<String, Object>> dtos = new ArrayList<>();
        for (ChatMessage m : conversation) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("content", m.getContent());
            map.put("mediaUrl", m.getMediaUrl());
            map.put("mediaType", m.getMediaType());
            map.put("senderType", m.getSenderType().name());
            map.put("senderName", m.getSenderName());
            map.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().format(TIME_FMT) : "");
            dtos.add(map);
        }
        return ResponseEntity.ok(Map.of("success", true, "messages", dtos));
    }
```
- **Lệnh xác thực:**
```bash
mvn -f pom.xml test-compile
```

---

### Task 4: Giao Diện Khách Hàng — Nút "Gặp Nhân Viên Quầy" & Polling 3s (`header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:**
  1. Thêm nút bấm 1-chạm `[👨‍💼 Gặp nhân viên quầy]` ngay trên thanh nhập tin nhắn:
     - Nút nhỏ gọn, phong cách pill capsule thanh nhã.
     - Bấm vào gọi `fetch('/api/chat/store/' + currentStoreId + '/request-staff', { method: 'POST' })`.
     - Chèn ngay thông báo chuyển giao vào khung chat.
  2. Bổ sung hàm Polling tự động 3 giây:
     - Khi mở phòng chat (`openMiniChatRoom`): Gọi `startMiniChatPolling()`.
     - Tự động lấy tin nhắn mới từ API `/api/chat/store/{storeId}/messages`.
     - Khi nhân viên shop trả lời từ trang quản trị, tin nhắn tự động xuất hiện ngay trên màn hình của khách!
     - Khi đóng chat: Tự động `stopMiniChatPolling()`.
- **Mã nguồn HTML & JS cập nhật:**
```html
<!-- DẢI HÀNH ĐỘNG NHANH TRÊN Ô CHAT CỦA KHÁCH -->
<div id="miniChatQuickActions" style="padding:4px 14px 6px; display:flex; gap:6px; align-items:center; background:#FFFFFF;">
    <button type="button" onclick="requestHumanStaff()" style="border:1px solid #ECE7DE; background:#FAF8F5; color:#8C4A27; font-size:11.5px; font-weight:700; padding:3px 10px; border-radius:9999px; cursor:pointer; display:inline-flex; align-items:center; gap:4px; transition:all 0.15s;" onmouseover="this.style.background='#F5EFEB'" onmouseout="this.style.background='#FAF8F5'">
        <i class="fa-solid fa-user-tie"></i> <span>Gặp nhân viên quầy</span>
    </button>
</div>
```
```javascript
function requestHumanStaff() {
    if (!currentStoreId) return;
    fetch('/api/chat/store/' + currentStoreId + '/request-staff', { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                appendChatMessageDOM('CUSTOMER', '[Yêu cầu gặp nhân viên quầy trực tiếp]', null, null, null, null, null, null, null, data.createdAt);
                appendChatMessageDOM('STORE_STAFF', data.reply, null, null, null, null, null, null, null, data.createdAt);
                scrollMiniChatBottom();
            }
        });
}

function startMiniChatPolling() {
    stopMiniChatPolling();
    chatPollingTimer = setInterval(function() {
        if (!currentStoreId || document.getElementById('miniChatRoomView').style.display === 'none') {
            stopMiniChatPolling();
            return;
        }
        fetch('/api/chat/store/' + currentStoreId + '/messages')
            .then(res => res.json())
            .then(data => {
                if (data.success && data.messages) {
                    if (data.messages.length > lastRenderedMsgCount) {
                        for (var i = lastRenderedMsgCount; i < data.messages.length; i++) {
                            var m = data.messages[i];
                            appendChatMessageDOM(m.senderType, m.content, m.bookTitle, m.bookPrice, m.bookImage, null, m.bookId, m.mediaUrl, m.mediaType, m.createdAt);
                        }
                        lastRenderedMsgCount = data.messages.length;
                        scrollMiniChatBottom();
                    }
                }
            })
            .catch(function(e) { console.debug(e); });
    }, 3000);
}
```

---

### Task 5: Giao Diện Quản Lý Shop — Công Tắc Gạt AI & Trả Lời Không Reload (`admin/chat/index.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/chat/index.html`
- **Mục tiêu:**
  1. Thêm công tắc gạt **Bật/Tắt Trợ Lý AI Trả Lời Giùm** trên header Kênh Tin Nhắn:
     - Giao diện Apple Liquid Glass bo tròn, hiển thị trạng thái động `[🟢 ĐANG BẬT]` / `[⚪ ĐANG TẮT]`.
     - Bấm công tắc gọi AJAX `POST /api/chat/store/{storeId}/toggle-ai`, cập nhật trạng thái tức thì không reload.
  2. Thay thế `location.reload()` khi nhân viên bấm "Gửi Tin":
     - Gửi AJAX `POST /api/chat/staff/reply`.
     - Nối tin nhắn của nhân viên thẳng vào khung chat và cuộn xuống mượt mà.
  3. Polling tự động 3 giây trên trang quản trị:
     - Gọi `GET /api/chat/staff/conversation?storeId={storeId}&userId={activeCustomerId}`.
     - Tự động nạp tin nhắn mới khi khách hàng gõ từ web storefront mà không cần bấm F5!
- **Mã nguồn chi tiết:**
```html
<!-- CÔNG TẮC BẬT/TẮT AI TRÊN HEADER KÊNH TIN NHẮN -->
<div style="display:flex; align-items:center; gap:10px; background:rgba(255,255,255,0.85); padding:6px 14px; border-radius:9999px; border:1px solid #ECE7DE; box-shadow:0 2px 6px rgba(0,0,0,0.04);">
    <span style="font-size:12.5px; font-weight:700; color:#1C1917;">🤖 Trợ lý AI trả lời giùm:</span>
    <label class="switch" style="position:relative; display:inline-block; width:38px; height:22px; margin:0;">
        <input type="checkbox" id="aiReplyToggleInput" th:checked="${currentStore != null and currentStore.aiReplyEnabled == true}" onchange="toggleAiReplyMode(this.checked)">
        <span class="slider round"></span>
    </label>
    <span id="aiReplyStatusLabel" style="font-size:11.5px; font-weight:700;" th:styleappend="${currentStore != null and currentStore.aiReplyEnabled == true} ? 'color:#16A34A;' : 'color:#78716C;'" th:text="${currentStore != null and currentStore.aiReplyEnabled == true ? 'ĐANG BẬT' : 'ĐANG TẮT'}">ĐANG TẮT</span>
</div>
```
```javascript
function toggleAiReplyMode(isChecked) {
    fetch('/api/chat/store/' + currentStoreId + '/toggle-ai', { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const label = document.getElementById('aiReplyStatusLabel');
                if (label) {
                    label.textContent = data.aiReplyEnabled ? 'ĐANG BẬT' : 'ĐANG TẮT';
                    label.style.color = data.aiReplyEnabled ? '#16A34A' : '#78716C';
                }
            } else {
                alert(data.message || 'Lỗi cập nhật');
                document.getElementById('aiReplyToggleInput').checked = !isChecked;
            }
        });
}

function startAdminChatPolling() {
    if (!currentStoreId || !activeCustomerId) return;
    setInterval(function() {
        fetch('/api/chat/staff/conversation?storeId=' + currentStoreId + '&userId=' + activeCustomerId)
            .then(res => res.json())
            .then(data => {
                if (data.success && data.messages) {
                    renderAdminMessagesDelta(data.messages);
                }
            });
    }, 3000);
}
```

---

### Task 6: Viết Unit Test Kiểm Thử Phân Quyền & Chat Đa Chi Nhánh
- **Tệp tạo mới:** `src/test/java/vn/iotstar/service/StoreChatBranchTest.java`
- **Mục tiêu:**
  1. Kiểm thử cách ly chi nhánh: Quản lý Store 1 không thể can thiệp hoặc toggle AI của Store 2 (`403 Forbidden`).
  2. Kiểm thử logic AI Toggle: Bật AI thì sinh câu trả lời bot, Tắt AI thì không tự động sinh câu trả lời bot.
- **Lệnh thực thi:**
```bash
mvn -f pom.xml test -Dtest=StoreChatBranchTest
```
- **Kết quả kỳ vọng:** Tests run: 2, Failures: 0, Errors: 0, BUILD SUCCESS.

---

### Task 7: Bảng Danh Sách Tài Khoản Đăng Nhập & Kịch Bản Kiểm Thử Thực Tế

| STT | Tên Chi Nhánh | Địa Chỉ Thực Tế | Username | Mật Khẩu | Quyền Hạn | URL Truy Cập Quản Lý |
| :---: | :--- | :--- | :---: | :---: | :---: | :--- |
| **1** | **Tổng Kho Thủ Đức (CN 1)** | 48 Võ Văn Ngân, TP. Thủ Đức | `manager_td` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **2** | **Đường Sách Quận 1 (CN 2)** | Nguyễn Văn Bình, Bến Nghé, Q.1 | `manager_q1` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **3** | **Làng ĐH Quốc Gia (CN 3)** | Khu đô thị ĐHQG, Linh Trung | `manager_dhqg` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **4** | **Phố Sách Phú Nhuận (CN 4)**| 120 Trần Huy Liệu, Phú Nhuận | `manager_pn` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **5** | **Khu Học Thuật Q.5 (CN 5)** | 280 An Dương Vương, Quận 5 | `manager_q5` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |

#### Kịch Bản Kiểm Thử Song Song 2 Trình Duyệt:
1. **Trường hợp 1: Shop TẮT AI (Nhân viên trực tiếp đối thoại):**
   - Shop 2 (`manager_q1`) vào `/admin/chat`, gạt công tắc AI sang `ĐANG TẮT`.
   - Khách hàng nhắn: *"Shop ơi cuốn này còn không?"* -> Không có bot trả lời xen vào.
   - Quản lý gõ: *"Dạ còn 1 cuốn trên kệ nhé bạn!"* -> Trên màn hình khách hàng tự động nhận được tin nhắn sau 3 giây.
2. **Trường hợp 2: Shop BẬT AI nhưng khách bấm "Gặp nhân viên quầy":**
   - Shop 1 (`manager_td`) vào `/admin/chat`, gạt công tắc AI sang `ĐANG BẬT`.
   - Khách hàng nhắn thông thường -> AI tự giải đáp nhanh thông tin sách.
   - Khách hàng muốn trả giá hoặc khiếu nại -> Bấm nút `[👨‍💼 Gặp nhân viên quầy]` hoặc gõ *"gặp người thật"*.
   - Hệ thống ngắt AI, hiện badge đỏ `[CẦN TRẢ LỜI]` tại quầy Thủ Đức để quản lý `manager_td` trực tiếp trả lời.

---

## 5. Risks & Tradeoffs
- **Tải hệ thống với Polling:** Timer polling chỉ chạy khi cửa sổ chat đang mở (`display !== 'none'`). Tần số 3 giây bảo đảm độ mượt mà cao mà không gây quá tải cho máy chủ Spring Boot.
- **Bảo mật:** Bắt buộc kiểm tra quyền sở hữu chi nhánh qua session `CustomUserDetails` ở mọi endpoint quản trị.
