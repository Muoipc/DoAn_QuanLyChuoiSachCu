# Kế Hoạch Khắc Phục: Lỗi Duplicate (Nhân Đôi) Tin Nhắn & Lệch Format Thời Gian Trong Chat

## 1. Goal
Loại bỏ hoàn toàn hiện tượng nhân đôi tin nhắn của khách hàng và đồng nhất định dạng thời gian giữa giao diện gửi tức thì (Optimistic UI) và cơ chế tự động đồng bộ (Live Polling).

---

## 2. Nguyên Nhân Gốc Rễ (Root Cause)
1. **Xung đột giữa Optimistic UI và Live Polling:**
   - Trong `header.html`, hàm `sendMiniChatMessage()` ngay lập tức gọi `appendChatMessageDOM('CUSTOMER', text, ...)` để vẽ bong bóng tin nhắn lên màn hình (format client: `21:58 ✓`).
   - API `POST /api/chat/store/{storeId}/send` lưu tin nhắn vào MySQL.
   - Hàm `startMiniChatPolling()` chạy mỗi 3 giây gọi `GET /api/chat/store/{storeId}/messages`. Client chỉ so sánh `messages.length > lastClientRenderedCount` (chỉ số mảng) mà **không kiểm tra ID tin nhắn**.
   - Do tin nhắn tạm local không được đồng bộ chỉ số và không có `msgId`, vòng lặp polling duyệt qua tin nhắn vừa lưu trong DB và append thêm một lần nữa (format server: `21:58 13/10 ✓`).
2. **Thiếu Khử Trùng Lặp (Deduplication):**
   - DOM phần tử tin nhắn không gắn `data-chat-msg-id`.
   - Client không lưu danh sách `Set` các ID tin nhắn đã render để bỏ qua khi polling quét lại.
3. **Thiếu Debounce phím Enter / nút gửi:**
   - Nhấn Enter liên tục hoặc click đúp có thể kích hoạt 2 request gửi song song.

---

## 3. Các Bước Triển Khai (Actionable Tasks)

### Task 1: Khử Trùng Lặp Theo Message ID Phía Client (`fragments/header.html`)
- **Tệp:** `src/main/resources/templates/fragments/header.html`
- **Hành động:**
  1. Khởi tạo `var renderedMsgIds = new Set();`
  2. Bổ sung tham số `msgId` vào `appendChatMessageDOM(..., msgId)`.
  3. Trước khi tạo row DOM:
     - Nếu `msgId && renderedMsgIds.has(msgId)` -> Bỏ qua ngay (`return;`).
     - Gắn `row.setAttribute('data-msg-id', msgId);` và `renderedMsgIds.add(msgId);`.
  4. Trong vòng lặp Polling:
     - Chỉ append những tin nhắn chưa có trong `renderedMsgIds`.
  5. Cập nhật hàm `loadStoreChatMessages`: Reset `renderedMsgIds.clear()` khi chuyển shop.

### Task 2: Điều Chỉnh Luồng Gửi & Khóa Debounce (`fragments/header.html`)
- **Tệp:** `src/main/resources/templates/fragments/header.html`
- **Hành động:**
  1. Thêm cờ `var isSendingMiniChat = false;`
  2. Trong `sendMiniChatMessage()`:
     - Nếu `isSendingMiniChat` đang `true` -> chặn không cho gửi tiếp.
     - Đặt `isSendingMiniChat = true;`.
     - Tạo `tempId = 'temp-' + Date.now();` cho tin nhắn tạm của khách.
     - Gắn `row.setAttribute('data-msg-id', tempId);` và `renderedMsgIds.add(tempId);`.
  3. Trong `postChatMessageToServer()`:
     - Khi server trả về `data.userMsgId` (ID vừa sinh trong DB):
       - Tìm phần tử `[data-msg-id="' + tempId + '"]`, cập nhật lại thuộc tính thành `data-msg-id="data.userMsgId"`.
       - Xóa `tempId` khỏi `renderedMsgIds`, thêm `data.userMsgId` vào `renderedMsgIds`.
     - Giải phóng `isSendingMiniChat = false;`.

### Task 3: Trả Về `userMsgId` Từ Server (`StoreChatController.java`)
- **Tệp:** `src/main/java/vn/iotstar/controller/StoreChatController.java`
- **Hành động:**
  - Trong API `sendCustomerMessage`, bổ sung vào response map:
    ```java
    res.put("userMsgId", userMsg.getId());
    ```
  - Đảm bảo client có ID chính xác để đối chiếu và thay thế tin nhắn tạm.

### Task 4: Đồng Bộ Khử Trùng Lặp Cho Trang Quản Lý (`admin/chat/index.html`)
- **Tệp:** `src/main/resources/templates/admin/chat/index.html`
- **Hành động:**
  - Khởi tạo `const renderedAdminMsgIds = new Set();`
  - Gắn `data-msg-id` cho từng tin nhắn của khách và nhân viên.
  - Hàm `pollAdminMessages()` kiểm tra `if (!renderedAdminMsgIds.has(m.id))` mới append vào khung chat.

### Task 5: Kiểm Thử Xác Minh
1. `mvn test`: Chạy toàn bộ test suite đảm bảo không gãy logic backend.
2. Hot-sync vào `target/classes/`.
3. Kiểm thử gửi tin trên trình duyệt: Gõ `hi`, spam phím Enter -> Chỉ xuất hiện duy nhất 1 tin nhắn. Polling 3s sau đó không bị nhảy thêm tin thứ 2.
