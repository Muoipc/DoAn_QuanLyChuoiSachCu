# Kế Hoạch Triển Khai: Menu 3 Chấm Quản Lý Chat, Hiển Thị Tin Chưa Đọc, Gửi Ảnh/Video & Thời Gian Tin Nhắn (Chuẩn Shopee Web Chat)

## Goal
Hoàn thiện toàn bộ các tính năng tương tác hội thoại với cửa hàng theo đúng yêu cầu: Menu 3 chấm `⋮` đầy đủ 4 tùy chọn (Xóa, Ghim, Tắt thông báo, Đánh dấu chưa đọc), hiển thị số lượng tin nhắn chưa đọc (unread badge) trên nút nổi FAB và danh sách shop, gửi hình ảnh/video thực tế kiểm tra sách trong khung chat, và hiển thị thời gian gửi tin nhắn (timestamp) trên từng bong bóng chat.

---

## Current Context & Assumptions
- **Mã nguồn hiện tại:**
  - `StoreChatController.java`: Đã có sẵn API `/api/chat/conversations` tính toán trường `unreadCount` cho từng shop; API `/api/chat/store/{storeId}/mark-read` đánh dấu đã đọc; API `/api/chat/upload-media` nhận ảnh & video; API `/api/chat/store/{storeId}/send` đã nhận `mediaUrl` & `mediaType`; API `/api/chat/store/{storeId}/delete-conversation` đã có sẵn.
  - `header.html` (`#aiMiniChatWidget` & `#aiFloatingBtn`): Chưa hiển thị badge số tin chưa đọc trên nút nổi tròn góc phải; chưa có menu 3 chấm thả xuống; chưa có nút upload ảnh/video và preview thumbnail; chưa hiển thị nhãn giờ trên từng tin nhắn.
- **Giả định:** Người dùng yêu cầu trải nghiệm chuẩn Shopee Web Chat: Badge đỏ hiển thị số tin chưa đọc trực quan, menu 3 chấm tinh gọn, modal xác nhận in-app không dùng `window.confirm` thô sơ, upload ảnh/video mượt mà và thời gian hiển thị gọn gàng trong bong bóng chat.

---

## Architecture & Proposed Approach
1. **Menu 3 Chấm `⋮` (Shopee Web Chat Action Dropdown):**
   - Vị trí: Đặt trên thanh header của phòng chat chi tiết (`#miniChatRoomView`).
   - Giao diện: Nút icon `⋮` (`<i class="fa-solid fa-ellipsis-vertical"></i>`). Khi click mở popup dropdown menu nổi:
     - 📌 **Ghim cuộc trò chuyện:** Ghim chi nhánh này lên đỉnh danh sách chat (lưu `pinnedStores` trong `localStorage`, gắn badge ghim nhỏ).
     - 🔕 **Tắt thông báo / Bật thông báo:** Chuyển đổi trạng thái nhận thông báo cho shop này (lưu `mutedStores`, hiển thị icon chuông gạch chéo).
     - ✉️ **Đánh dấu chưa đọc:** Đánh dấu chấm đỏ unread badge trên dòng shop ở danh sách ngoài.
     - 🗑️ **Xóa cuộc trò chuyện:** Mở Modal xác nhận in-app Shopee -> Xóa toàn bộ lịch sử tin nhắn của shop này trong DB và làm sạch màn hình.
2. **Hệ Thống Hiển Thị Tin Nhắn Chưa Đọc (Unread Message Badges):**
   - **Nút nổi toàn cục FAB (`#aiFloatingBtn`):** Hiển thị huy hiệu đỏ tròn ở góc trên (`#aiFloatingUnreadBadge`), tự động đếm tổng số tin nhắn chưa đọc từ tất cả các shop.
   - **Danh sách shop Master List (`.shop-conv-row`):** Nếu `unreadCount > 0`, dòng preview tin nhắn in đậm (`font-weight: 700; color: #1C1917`), góc phải hiển thị badge đỏ bo tròn chứa số lượng tin chưa đọc.
   - **Tự động xóa unread khi mở chat:** Khi click vào shop để mở phòng chat, tự động gọi `/api/chat/store/{storeId}/mark-read` để xóa unread count và cập nhật lại badge trên FAB.
3. **Gửi Ảnh & Video Trong Đoạn Chat (Media Upload & Preview):**
   - Nút kẹp tệp / camera `📷` bên cạnh ô nhập tin nhắn (`<input type="file" id="chatMediaFileInput" accept="image/*,video/*">`).
   - Khung xem trước Thumbnail thu nhỏ ngay trên thanh input:
     - Nếu là ảnh: Hiển thị ảnh thu nhỏ 44x44px.
     - Nếu là video: Hiển thị icon video kèm tên file.
     - Nút `×` nhỏ để hủy đính kèm nếu chọn nhầm.
   - Khi bấm gửi (hoặc nhấn Enter): Tải tệp lên `/api/chat/upload-media`, nhận về `mediaUrl` & `mediaType`, rồi gửi kèm vào `/api/chat/store/{storeId}/send`.
   - Hiển thị trong bong bóng chat:
     - Ảnh: Thẻ `<img>` bo góc 10px, click để xem ảnh lớn.
     - Video: Thẻ `<video controls>` phát trực tiếp trong chat.
4. **Thời Gian Gửi Tin Nhắn (Timestamp & Status Tick):**
   - Góc dưới bên phải mỗi tin nhắn (của cả Khách hàng và Shop) hiển thị thời gian gửi dạng `HH:mm` (lấy từ trường `createdAt` của backend hoặc giờ hiện tại).
   - Tin nhắn của khách hàng có thêm dấu tích `✓` (Đã gửi thành công).

---

## Step-by-Step Tasks

### Task 1: Xây Dựng Menu 3 Chấm `⋮` Với Đầy Đủ 4 Tùy Chọn (Xóa, Ghim, Tắt thông báo, Đánh dấu chưa đọc)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:**
  1. Thêm nút menu 3 chấm `⋮` vào topbar của `#miniChatRoomView`.
  2. Tạo dropdown menu `#storeChatDropdownMenu` chứa đúng 4 mục:
     - `📌 Ghim lên đầu` (hoặc `Bỏ ghim`)
     - `🔕 Tắt thông báo` (hoặc `🔔 Bật thông báo`)
     - `✉️ Đánh dấu chưa đọc`
     - `🗑️ Xóa cuộc trò chuyện` (màu đỏ nổi bật)
  3. Tạo Modal xác nhận Shopee `#miniChatDeleteModal` với nội dung cảnh báo xóa vĩnh viễn và 2 nút: `Hủy` / `Xác nhận xóa`.
  4. Viết các hàm JavaScript điều khiển:
     - `toggleStoreChatMenu(e)`: Đóng/mở dropdown menu khi click.
     - `togglePinStore(storeId)`: Cập nhật danh sách shop đã ghim vào `PINNED_STORES_MAP`, tự động đưa shop lên đầu bảng ở Master list.
     - `toggleMuteStore(storeId)`: Cập nhật `MUTED_STORES_MAP`, hiển thị icon chuông tắt `🔕`.
     - `markStoreUnread(storeId)`: Đánh dấu `UNREAD_MANUAL_MAP`, tăng unread count, hiển thị badge trên dòng shop và quay lại danh sách.
     - `promptDeleteStore(storeId, storeName)`: Mở Modal xác nhận xóa Shopee.
     - `executeDeleteStoreChat()`: Gọi POST `/api/chat/store/{storeId}/delete-conversation`, reload danh sách và làm sạch tin nhắn.
- **Xác minh:** Click nút `⋮` -> Menu xổ ra đầy đủ 4 tùy chọn; click từng tùy chọn thực hiện đúng logic và đổi trạng thái giao diện tức thì.

---

### Task 2: Hiển Thị Tin Nhắn Chưa Đọc (Unread Message Badges)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:**
  1. Trên nút FAB `#aiFloatingBtn`:
     Thêm thẻ badge đỏ:
     ```html
     <span id="aiFloatingUnreadBadge" style="display:none; position:absolute; top:-3px; right:-3px; background:#EE4D2D; color:#FFFFFF; font-size:11px; font-weight:700; min-width:18px; height:18px; border-radius:9999px; display:none; align-items:center; justify-content:center; padding:0 4px; border:2px solid #FFFFFF;"></span>
     ```
  2. Trong hàm `loadShopConversations()`:
     - Tính tổng số tin nhắn chưa đọc `totalUnread = sum(s.unreadCount)`.
     - Nếu `totalUnread > 0`: Cập nhật text badge và hiển thị trên nút FAB; nếu bằng 0: ẩn badge.
     - Trên từng dòng shop (`.shop-conv-row`): Nếu `s.unreadCount > 0`, hiển thị badge đỏ bo tròn chứa số unread, và in đậm chữ preview tin nhắn cuối.
  3. Khi mở phòng chat (`openChatWithStore`):
     - Gọi POST `/api/chat/store/{storeId}/mark-read`.
     - Reset unread của shop đó về 0, cập nhật lại badge trên FAB ngay lập tức.
- **Xác minh:** Khi có tin nhắn từ shop chưa đọc, nút FAB hiện badge đỏ; mở chat thì badge biến mất.

---

### Task 3: Tích Hợp Gửi Ảnh & Video Trong Đoạn Chat (Media Upload & Preview)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:**
  1. Thêm nút icon ảnh/camera `📷` bên cạnh ô nhập tin nhắn:
     ```html
     <input type="file" id="chatMediaFileInput" accept="image/*,video/*" style="display:none;" onchange="handleMediaSelected(event)">
     <button type="button" onclick="document.getElementById('chatMediaFileInput').click()" title="Đính kèm ảnh hoặc video thực tế" style="width:36px; height:36px; border-radius:8px; border:1px solid #E5E0D8; background:#FFFFFF; color:#78716C; cursor:pointer; display:flex; align-items:center; justify-content:center; font-size:14px;">
         <i class="fa-regular fa-image"></i>
     </button>
     ```
  2. Thêm thanh xem trước `#chatMediaPreviewBar` nằm ngay trên input:
     - Thumbnail ảnh hoặc icon video.
     - Tên file cắt gọn.
     - Nút `×` để hủy chọn.
  3. Khi gửi tin nhắn (`sendMiniChatMessage`):
     - Nếu có file đang chờ: Gửi POST `/api/chat/upload-media` trước qua `FormData`.
     - Lấy `mediaUrl` và `mediaType` trả về -> truyền vào API `/api/chat/store/{storeId}/send`.
  4. Cập nhật hàm `appendChatMessageDOM(senderType, content, bookTitle, ..., mediaUrl, mediaType)`:
     - Nếu `mediaUrl` tồn tại và `mediaType === 'IMAGE'`: Render `<img src="${mediaUrl}" style="max-width:100%; max-height:200px; border-radius:8px; margin-top:6px; cursor:pointer;" onclick="window.open(this.src)">`.
     - Nếu `mediaUrl` tồn tại và `mediaType === 'VIDEO'`: Render `<video src="${mediaUrl}" controls style="max-width:100%; max-height:220px; border-radius:8px; margin-top:6px;"></video>`.
- **Lệnh xác minh:**
  ```bash
  curl -s -F "file=@src/main/resources/static/images/brand-book-icon.png" http://localhost:8080/api/chat/upload-media
  ```
  Kỳ vọng: Trả về JSON `{"success": true, "mediaUrl": "/uploads/chat/...", "mediaType": "IMAGE"}`.

---

### Task 4: Hiển Thị Thời Gian Gửi Tin Nhắn (Timestamp & Delivery Status)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:**
  1. Trích xuất thời gian `createdAt` từ backend (dạng `HH:mm dd/MM` hoặc lấy giờ hiện tại dạng `HH:mm` khi gửi tin mới).
  2. Bổ sung nhãn thời gian nhỏ ở góc dưới bên phải mỗi bong bóng chat:
     - Phía người dùng (Customer - nền nâu `#8C4A27`): Nhãn giờ màu `#E5D5C5` kèm tích `✓`.
     - Phía Shop / Nhân viên quầy (Shop - nền sáng `#FAF8F5`): Nhãn giờ màu `#A8A29E`.
  3. Cập nhật CSS đảm bảo bong bóng tự động co giãn vừa vặn, không bị đè chữ hay tràn viền.
- **Xác minh:** Mọi tin nhắn trong lịch sử và tin nhắn mới gửi đều hiển thị rõ thời gian gửi ở góc dưới (ví dụ: `18:30 ✓`).

---

## Tests & Validation Strategy
1. **Kiểm thử Menu 3 Chấm:**
   - Mở phòng chat với Chi Nhánh 1 -> Bấm `⋮`.
   - Chọn "Ghim": Quay lại danh sách thấy Chi Nhánh 1 có badge ghim và nằm ở trên cùng.
   - Chọn "Tắt thông báo": Menu đổi thành "Bật thông báo", cạnh tên shop hiện icon chuông tắt `🔕`.
   - Chọn "Đánh dấu chưa đọc": Quay lại danh sách thấy badge chưa đọc đỏ.
   - Chọn "Xóa cuộc trò chuyện": Modal xác nhận Shopee bật lên -> Bấm xác nhận -> Hội thoại bị xóa sạch trong DB và khung chat được làm mới.
2. **Kiểm thử Tin Chưa Đọc:**
   - Kiểm tra badge đỏ trên nút FAB khi có tin chưa đọc.
   - Mở chat -> Xác nhận badge biến mất.
3. **Kiểm thử Gửi Ảnh & Video:**
   - Chọn file ảnh -> Khung xem trước hiển thị thumbnail -> Bấm Gửi -> Tin nhắn xuất hiện với ảnh rõ nét.
   - Chọn file video (mp4) -> Khung xem trước hiển thị icon video -> Bấm Gửi -> Tin nhắn xuất hiện với trình phát video có nút Play.
4. **Kiểm thử Thời Gian Tin Nhắn:**
   - Gửi tin nhắn -> Xác nhận có giờ phút và dấu tích `✓`.

---

## Risks & Tradeoffs
- **Dung lượng video:** Trình duyệt có thể mất 1-2 giây để tải video lên server; hiển thị loading nhẹ trên thanh preview trong lúc upload.
- **Lưu trữ trạng thái Ghim/Mute/Unread phía Client:** Sử dụng `localStorage` kết hợp đồng bộ API để phản hồi tức thì với tốc độ 0ms.
