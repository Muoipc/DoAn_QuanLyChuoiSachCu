# Kế Hoạch Triển Khai: Tạo Tài Khoản Quản Lý 5 Chi Nhánh & Hoàn Thiện Tính Năng Nhắn Tin Thực Tế Với Từng Shop

## 1. Goal
Hoàn thiện tính năng nhắn tin thực tế 2 chiều giữa khách hàng và từng chi nhánh (Shop): tạo đủ 5 tài khoản Quản lý cửa hàng (ROLE_MANAGER) cho 5 chi nhánh, tích hợp **công tắc bật/tắt chế độ AI trả lời thay thế (AI Auto-Reply Toggle)** linh hoạt theo nhu cầu của từng shop, và bổ sung cơ chế đồng bộ tin nhắn thời gian thực (Live Polling 3s) trên cả giao diện khách hàng và trang quản trị shop.

---

## 2. Current Context & Assumptions
- **Hiện trạng tài khoản chi nhánh:**
  - Bảng `stores` có 5 chi nhánh:
    - ID 1: *Tổng Kho Sách Cũ TP.HCM (Thủ Đức)* — Manager ID: 2 (`manager_td`).
    - ID 2: *Chi Nhánh 2: Đường Sách Quận 1* — Manager ID: 3 (`manager_q1`).
    - ID 3: *Chi Nhánh 3: Làng Đại Học Quốc Gia* — Manager ID: `NULL` (chưa có tài khoản).
    - ID 4: *Chi Nhánh 4: Phố Sách Cũ Phú Nhuận* — Manager ID: `NULL` (chưa có tài khoản).
    - ID 5: *Chi Nhánh 5: Khu Học Thuật Quận 5* — Manager ID: `NULL` (chưa có tài khoản).
  - Người dùng không thể đăng nhập vào Shop 3, Shop 4, Shop 5 để kiểm thử kênh chat quản trị.
- **Hiện trạng nghiệp vụ Chat (`StoreChatController.java` & `header.html`):**
  - Trước đây, hệ thống mặc định AI luôn tự động trả lời mọi tin nhắn khách gửi, không cho phép nhân viên quầy chủ động kiểm soát việc tự trả lời hay nhờ AI hỗ trợ.
  - Cần một cơ chế công tắc (Toggle Switch) linh hoạt cho Quản lý từng chi nhánh:
    - **Khi BẬT Chế độ AI:** Trợ lý AI sẽ tự động phân tích CSDL và trả lời ngay lập tức các thắc mắc của khách (tồn kho, độ mới, giá bán, địa chỉ, giờ mở cửa...).
    - **Khi TẮT Chế độ AI:** Kênh chat chuyển sang chế độ Nhân viên trực tiếp — AI không can thiệp, tin nhắn chờ nhân viên quầy trả lời trực tiếp từ `/admin/chat`.
  - Phía khách hàng (`header.html`): Cần cơ chế tự động lấy tin nhắn mới (polling) khi đang mở khung chat.
  - Phía quản lý shop (`admin/chat/index.html`): Cần cập nhật tin nhắn và bật/tắt AI qua AJAX tức thì không cần reload trang.

---

## 3. Architecture & Proposed Approach
- **Mô hình Trò Chuyện Đa Chi Nhánh Thực Tế & Công Tắc AI (Multi-Branch Real Chat & AI Toggle):**
  - **Khởi tạo & Phân quyền:** Cấp đủ 5 tài khoản `ROLE_MANAGER` với mật khẩu đồng bộ `123456`, gán đúng `manager_id` vào 5 chi nhánh tương ứng trong bảng `stores`. Mỗi quản lý khi đăng nhập vào `/admin/chat` chỉ xem và quản lý đúng chi nhánh của mình.
  - **Cơ chế Bật/Tắt AI Trả Lời Giùm (`ai_reply_enabled`):**
    - Thêm trường `ai_reply_enabled` vào bảng `stores` và entity `Store.java`.
    - API `POST /api/chat/store/{storeId}/toggle-ai`: Cho phép quản lý chi nhánh bật hoặc tắt chế độ AI trả lời tự động bằng 1 cú chạm.
    - Trong `sendCustomerMessage`:
      - Nếu `store.getAiReplyEnabled() == true`: AI tự động phân tích và sinh câu trả lời gửi ngay cho khách dưới tên `Trợ lý AI - [Tên Shop]`.
      - Nếu `store.getAiReplyEnabled() == false`: Tin nhắn được chuyển vào hàng đợi `needsAttention = true` để nhân viên shop tự tay soạn câu trả lời (kèm 1 tin thông báo tiếp nhận lịch sự ở lần đầu tiên).
  - **Đồng bộ Thời Gian Thực 2 Chiều (Bi-directional Live Polling):**
    - Khách hàng (Client mini-chat): Chạy `setInterval` 3 giây gọi `GET /api/chat/store/{storeId}/messages`, nhận ngay câu trả lời của nhân viên quầy hoặc AI.
    - Quản lý cửa hàng (Manager Portal): Thêm API `GET /api/chat/staff/conversation?storeId={storeId}&userId={userId}`, trang `/admin/chat` tự động nạp tin nhắn mới của khách và cập nhật danh sách hội thoại.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Tạo Tài Khoản Quản Lý Cho 5 Chi Nhánh & Bổ Sung Cột `ai_reply_enabled`
- **Mục tiêu:** Cấp 3 tài khoản quản lý mới cho Store 3, 4, 5, liên kết `manager_id` và bổ sung trường cờ `ai_reply_enabled` trong bảng `stores`.
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

### Task 2: Cập Nhật Entity `Store.java` & API Bật/Tắt Chế Độ AI Trả Lời Giùm
- **Tệp chỉnh sửa:**
  - `src/main/java/vn/iotstar/entity/Store.java`
  - `src/main/java/vn/iotstar/controller/StoreChatController.java`
- **Mục tiêu:**
  1. Trong `Store.java`: Thêm thuộc tính `aiReplyEnabled` (Boolean) kèm getter/setter.
  2. Bổ sung API `POST /api/chat/store/{storeId}/toggle-ai`:
     ```java
     @PostMapping("/api/chat/store/{storeId}/toggle-ai")
     @ResponseBody
     public ResponseEntity<Map<String, Object>> toggleAiReply(
             @PathVariable Long storeId,
             @AuthenticationPrincipal CustomUserDetails userDetails) {
         // Kiểm tra quyền ROLE_MANAGER của chi nhánh
         List<Store> managerStores = storeRepository.findByManagerId(userDetails.getId());
         Store store = managerStores.stream().filter(s -> s.getId().equals(storeId)).findFirst().orElse(null);
         if (store == null) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không có quyền quản lý chi nhánh này"));
         }
         boolean current = Boolean.TRUE.equals(store.getAiReplyEnabled());
         store.setAiReplyEnabled(!current);
         storeRepository.save(store);
         return ResponseEntity.ok(Map.of("success", true, "aiReplyEnabled", store.getAiReplyEnabled()));
     }
     ```
  3. Trong `sendCustomerMessage`:
     - Nếu `Boolean.TRUE.equals(store.getAiReplyEnabled())`: Kích hoạt `generateStoreAiReply()` tự động trả lời thay cho nhân viên quầy.
     - Nếu `aiReplyEnabled == false`: Không tự động sinh câu trả lời bot, chỉ gửi 1 lời chào tiếp nhận ở tin nhắn đầu tiên; các tin nhắn sau gắn `needsAttention = true` để nhân viên shop tự tay trả lời.
  4. Bổ sung endpoint `GET /api/chat/staff/conversation`: Lấy danh sách tin nhắn của khách hàng dưới dạng JSON để giao diện quản trị poll mượt mà không reload trang.
- **Lệnh xác thực:**
```bash
mvn -f pom.xml test-compile
```

---

### Task 3: Cài Đặt Live Polling Phía Khách Hàng (`header.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/fragments/header.html`
- **Mục tiêu:**
  1. Khi khách hàng bấm vào một shop (`openMiniChatRoom(storeId)`):
     - Khởi tạo `chatPollingTimer = setInterval(pollNewMiniChatMessages, 3000);`
  2. Hàm `pollNewMiniChatMessages()`:
     - Gọi `fetch('/api/chat/store/' + currentStoreId + '/messages')`.
     - So sánh số lượng tin nhắn hiện tại trong DOM (`#miniChatMessagesList .chat-msg-row`).
     - Nếu phát hiện có tin nhắn mới từ `STORE_STAFF`, tự động chèn tin nhắn mới vào cuối khung chat và cuộn mượt xuống đáy!
  3. Khi đóng khung chat (`toggleAiMiniChat()`, `backToShopList()`):
     - Gọi `clearInterval(chatPollingTimer);` để tránh lãng phí băng thông.
- **Mã nguồn JavaScript bổ sung trong `header.html`:**
```javascript
var chatPollingTimer = null;
var lastRenderedMsgCount = 0;

function startMiniChatPolling() {
    stopMiniChatPolling();
    chatPollingTimer = setInterval(function() {
        if (!currentStoreId || document.getElementById('miniChatRoomView').style.display === 'none') {
            stopMiniChatPolling();
            return;
        }
        fetch('/api/chat/store/' + currentStoreId + '/messages')
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data.success && data.messages) {
                    if (data.messages.length > lastRenderedMsgCount) {
                        // Nạp bổ sung các tin nhắn mới
                        for (var i = lastRenderedMsgCount; i < data.messages.length; i++) {
                            var m = data.messages[i];
                            // Chỉ render nếu tin nhắn đó không phải tin vừa gửi từ local
                            appendChatMessageDOM(m.senderType, m.content, m.bookTitle, m.bookPrice, m.bookImage, null, m.bookId, m.mediaUrl, m.mediaType, m.createdAt);
                        }
                        lastRenderedMsgCount = data.messages.length;
                        scrollMiniChatBottom();
                    }
                }
            })
            .catch(function(err) { console.debug('Polling check:', err); });
    }, 3000);
}

function stopMiniChatPolling() {
    if (chatPollingTimer) {
        clearInterval(chatPollingTimer);
        chatPollingTimer = null;
    }
}
```

---

### Task 4: Cài Đặt Công Tắc Bật/Tắt AI & Trả Lời AJAX Trực Tiếp (`admin/chat/index.html`)
- **Tệp chỉnh sửa:** `src/main/resources/templates/admin/chat/index.html`
- **Mục tiêu:**
  1. Thêm công tắc gạt **Bật/Tắt Trợ Lý AI Trả Lời Giùm** trên thanh tiêu đề của Kênh Tin Nhắn:
     - Hiển thị trạng thái hiện tại (`aiReplyEnabled`):
       - Bật: `[ 🟢 Trợ lý AI đang trực trả lời tự động ]`
       - Tắt: `[ ⚪ Nhân viên trực tiếp quản lý quầy ]`
     - Bấm công tắc gọi AJAX `POST /api/chat/store/' + currentStoreId + '/toggle-ai`, cập nhật trạng thái tức thì không cần F5.
  2. Sửa hàm `postAdminReply`:
     - Thay thế lệnh `location.reload();` bằng hàm chèn trực tiếp tin nhắn của quản lý vào danh sách hội thoại.
     - Xóa trắng ô input và cuộn xuống đáy.
  3. Bổ sung Live Polling trên trang quản trị:
     - Mỗi 3 giây gọi API kiểm tra tin nhắn mới của khách hàng đang chọn (`activeCustomerId`).
     - Nếu có tin nhắn mới từ khách hàng, tự động hiện lên màn hình quản trị và cập nhật danh sách khách hàng bên trái.
- **Mã nguồn HTML & JavaScript cập nhật:**
```html
<!-- CÔNG TẮC GẠT AI TRẢ LỜI THAY TRÊN HEADER ADMIN CHAT -->
<div style="display:flex; align-items:center; gap:10px; background:rgba(255,255,255,0.85); padding:6px 14px; border-radius:9999px; border:1px solid #ECE7DE; box-shadow:0 2px 6px rgba(0,0,0,0.04);">
    <span style="font-size:12.5px; font-weight:700; color:#1C1917;">🤖 Trợ lý AI trả lời giùm:</span>
    <label class="switch" style="position:relative; display:inline-block; width:38px; height:22px; margin:0;">
        <input type="checkbox" id="aiReplyToggleInput" th:checked="${currentStore != null and currentStore.aiReplyEnabled == true}" onchange="toggleAiReplyMode(this.checked)">
        <span class="slider round"></span>
    </label>
    <span id="aiReplyStatusLabel" style="font-size:11.5px; font-weight:600;" th:styleappend="${currentStore != null and currentStore.aiReplyEnabled == true} ? 'color:#16A34A;' : 'color:#78716C;'" th:text="${currentStore != null and currentStore.aiReplyEnabled == true ? 'ĐANG BẬT' : 'ĐANG TẮT'}">ĐANG TẮT</span>
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
                alert(data.message || 'Lỗi cập nhật chế độ AI');
                document.getElementById('aiReplyToggleInput').checked = !isChecked;
            }
        })
        .catch(err => {
            console.error('Lỗi toggle AI:', err);
            document.getElementById('aiReplyToggleInput').checked = !isChecked;
        });
}

function postAdminReply(content, mediaUrl, mediaType) {
    const params = new URLSearchParams();
    params.append('storeId', currentStoreId);
    params.append('userId', activeCustomerId);
    if (content) params.append('content', content);
    if (mediaUrl) params.append('mediaUrl', mediaUrl);
    if (mediaType) params.append('mediaType', mediaType);

    fetch('/api/chat/staff/reply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            appendStaffMessageToAdminDOM(content, mediaUrl, mediaType);
            const badge = document.getElementById('needsAttentionAlert');
            if (badge) badge.style.display = 'none';
        } else {
            alert(data.message || 'Lỗi gửi tin nhắn');
        }
    });
}
```

---

### Task 5: Viết Unit Test TDD Kiểm Thử Phân Quyền & Chat Đa Chi Nhánh
- **Tệp tạo mới:** `src/test/java/vn/iotstar/service/StoreChatBranchTest.java`
- **Mục tiêu:** Kiểm thử cách ly tin nhắn giữa 5 chi nhánh:
  1. Quản lý chi nhánh A không thể trả lời tin nhắn của chi nhánh B (`403 Forbidden`).
  2. Tin nhắn khách hàng gửi tới chi nhánh A chỉ xuất hiện trong lịch sử của chi nhánh A.
- **Lệnh thực thi kiểm thử:**
```bash
mvn -f pom.xml test -Dtest=StoreChatBranchTest
```
- **Kết quả kỳ vọng:** Tests run: 2, Failures: 0, Errors: 0, BUILD SUCCESS.

---

### Task 6: Bảng Danh Sách Tài Khoản Đăng Nhập & Kịch Bản Kiểm Thử Thực Tế

| STT | Tên Chi Nhánh | Địa Chỉ Thực Tế | Username | Mật Khẩu | Quyền Hạn | URL Truy Cập Quản Lý |
| :---: | :--- | :--- | :---: | :---: | :---: | :--- |
| **1** | **Tổng Kho Thủ Đức (CN 1)** | 48 Võ Văn Ngân, TP. Thủ Đức | `manager_td` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **2** | **Đường Sách Quận 1 (CN 2)** | Nguyễn Văn Bình, Bến Nghé, Q.1 | `manager_q1` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **3** | **Làng ĐH Quốc Gia (CN 3)** | Khu đô thị ĐHQG, Linh Trung | `manager_dhqg` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **4** | **Phố Sách Phú Nhuận (CN 4)**| 120 Trần Huy Liệu, Phú Nhuận | `manager_pn` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |
| **5** | **Khu Học Thuật Q.5 (CN 5)** | 280 An Dương Vương, Quận 5 | `manager_q5` | `123456` | `ROLE_MANAGER` | `http://localhost:8080/admin/chat` |

#### Kịch Bản Kiểm Thử Song Song 2 Trình Duyệt:
1. **Trình duyệt A (Khách hàng):**
   - Đăng nhập `phuc_customer` / `123456` (hoặc để Anonymous).
   - Bấm icon chat nổi 3D góc dưới màn hình -> Chọn **Chi Nhánh 2: Đường Sách Quận 1**.
   - Gõ: *"Shop ơi cuốn Đắc Nhân Tâm bản 95% mới còn ở quầy Q.1 không?"* -> Bấm Gửi.
   - Thấy tin nhắn của mình hiện lên kèm lời nhắn tiếp nhận của shop.
2. **Trình duyệt B (Quản lý Shop Quận 1):**
   - Mở ẩn danh hoặc trình duyệt khác, truy cập `http://localhost:8080/login`.
   - Đăng nhập `manager_q1` / `123456`.
   - Vào menu **Kênh Tin Nhắn** (`/admin/chat`).
   - Thấy ngay khách hàng `Nguyễn Song Hoàng Phúc` kèm badge đỏ `[CẦN TRẢ LỜI]`.
   - Nhập: *"Dạ cuốn đó còn 2 bản ở kệ A2 quầy Q.1 nhé bạn, bạn ghé xem trực tiếp được nha!"* -> Bấm Gửi Tin.
3. **Kiểm tra đồng bộ:**
   - Quay lại Trình duyệt A: Tin nhắn của `manager_q1` tự động xuất hiện trên màn hình của khách trong vòng 3 giây mà không cần F5!

---

## 5. Risks & Tradeoffs
- **Tải hệ thống với Polling (Server Load):**
  - *Giải pháp:* Chỉ chạy polling khi khung chat đang hiển thị (`display !== 'none'`). Tần số 3 giây là tối ưu cho môi trường local và demo bảo vệ đồ án, không gây nghẽn Tomcat.
- **Cách ly dữ liệu:**
  - Hệ thống kiểm tra nghiêm ngặt `storeRepository.findByManagerId(userDetails.getId())`, ngăn chặn tuyệt đối lỗi IDOR xem trộm tin nhắn chéo giữa các quản lý cửa hàng.
