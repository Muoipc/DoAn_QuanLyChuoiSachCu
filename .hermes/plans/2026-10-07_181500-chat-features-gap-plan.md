# Kế Hoạch Hoàn Thiện Các Tính Năng Chat & Quản Lý Xóa Cuộc Trò Chuyện Chuẩn Shopee

## Goal
Hoàn thiện toàn bộ các tính năng tương tác và quản lý tin nhắn với cửa hàng, lấy trọng tâm là **hệ thống Xóa đoạn chat / Xóa cuộc trò chuyện đa chi nhánh chuẩn Shopee Web Chat** (menu tùy chọn 3 chấm `⋮`, modal xác nhận Shopee tinh gọn, xóa từng tin nhắn hoặc xóa sạch toàn bộ hội thoại), kết hợp tải ảnh/video sách thực tế, đính kèm thẻ sách đang xem trên PDP, chip câu hỏi nhanh và đồng bộ thời gian thực qua WebSocket.

---

## Current Context & Assumptions
- **Hiện trạng:**
  - `StoreChatController.java` đã có endpoint `/api/chat/store/{storeId}/delete-conversation` (xóa tất cả tin nhắn giữa user và store), nhưng giao diện phía người dùng chỉ có nút icon thùng rác thô sơ, dùng `window.confirm()` mặc định của trình duyệt và thiếu menu tùy chọn chuẩn.
  - Người dùng yêu cầu hoàn thiện đầy đủ các chức năng: **xóa đoạn chat với shop**, xóa từng tin nhắn, và các tùy chọn quản lý hội thoại.
  - Hệ thống chat hiện tại áp dụng mô hình 2 chế độ xem (Master List 5 chi nhánh <-> Detail Chat Room). Cần tích hợp tùy chọn xóa ở cả 2 cấp độ:
    1. *Ngoài danh sách shop (Master list):* Nút thao tác nhanh / menu hành động trên từng chi nhánh để xóa hội thoại.
    2. *Bên trong phòng chat (Detail room):* Menu tùy chọn 3 chấm `⋮` trên topbar chứa tác vụ "Xóa cuộc trò chuyện".
    3. *Trên từng tin nhắn (Message level):* Nút xóa tin nhắn đơn lẻ khi rê chuột.
- **Giả định:** Người dùng yêu cầu trải nghiệm mượt mà, modal xác nhận in-app chuẩn Shopee (không dùng popup alert/confirm của browser), xóa xong tự động làm sạch màn hình và cập nhật danh sách shop tức thì.

---

## Architecture & Proposed Approach
1. **Quản Lý & Xóa Đoạn Chat Với Shop (Shopee Web Chat Actions):**
   - **Menu 3 Chấm Trên Topbar Phòng Chat (`#storeChatOptionsMenu`):** Bấm mở dropdown menu tinh gọn gồm:
     - `🗑 Xóa cuộc trò chuyện` (Xóa toàn bộ lịch sử tin nhắn với chi nhánh này).
     - `🧹 Làm sạch màn hình chat`.
   - **Thao Tác Xóa Trên Danh Sách Shop:** Khi hover vào một chi nhánh trong danh sách, icon thùng rác `🗑` xuất hiện kèm tooltip "Xóa cuộc trò chuyện".
   - **Modal Xác Nhận Shopee In-App (`#miniChatDeleteModal`):** Modal nổi bo góc mềm mại, hiển thị: *"Xóa cuộc trò chuyện này? Toàn bộ lịch sử tin nhắn với [Tên Chi Nhánh] sẽ bị xóa vĩnh viễn"*, với 2 nút: `Hủy` (xám) và `Xác nhận xóa` (đỏ cam Shopee).
   - **Xóa Từng Tin Nhắn (Single Message Delete):** Khi hover vào bong bóng tin nhắn bất kỳ, xuất hiện nút `🗑` nhỏ để xóa riêng tin nhắn đó khỏi DB và DOM.
2. **Các Tính Năng Trợ Lực Bổ Trợ:**
   - **Tải ảnh/video thực tế tại quầy:** Nút camera/ảnh `📷`, khung xem trước ảnh thu nhỏ trước khi bấm gửi.
   - **Thanh ngữ cảnh sách PDP:** Đính kèm thẻ sách mỏng (ảnh, tên sách, giá) khi mở từ trang chi tiết sản phẩm.
   - **Chip câu hỏi nhanh 1-chạm:** Gợi ý hỏi tồn kho quầy, xin ảnh thật, hỏi phí ship.
   - **Thời gian gửi tin & tích trạng thái:** Nhãn giờ `HH:mm ✓` dưới mỗi bong bóng tin nhắn.

---

## Step-by-Step Tasks

### Task 1: Xây Dựng Menu Tùy Chọn & Modal Xác Nhận Xóa Cuộc Trò Chuyện Chuẩn Shopee (Ưu Tiên #1)
- **Tập tin:**
  - `src/main/resources/templates/fragments/header.html`
  - `src/main/java/vn/iotstar/controller/StoreChatController.java`
- **Mô tả:** Triển khai hoàn chỉnh tính năng xóa đoạn chat với shop:
  1. Thêm Modal xác nhận xóa `#miniChatDeleteModal` phong cách Shopee nằm trong widget chat (không dùng `window.confirm`).
  2. Bổ sung nút 3 chấm `⋮` trên topbar phòng chat (`#miniChatRoomView`), click mở dropdown menu chứa mục "Xóa cuộc trò chuyện".
  3. Duy trì nút thùng rác `🗑` xóa nhanh khi hover chi nhánh ở danh sách ngoài (`#miniChatShopListView`).
  4. Viết hàm `promptDeleteStoreChat(storeId, storeName)`, `executeDeleteChat()`, gọi API `POST /api/chat/store/{storeId}/delete-conversation`, làm sạch tin nhắn và cập nhật danh sách shop ngay lập tức.
- **Chi tiết mã nguồn giao diện Modal & Dropdown:**
  ```html
  <!-- MODAL XÁC NHẬN XÓA SHOPEE -->
  <div id="miniChatDeleteModal" style="display:none; position:absolute; inset:0; background:rgba(0,0,0,0.45); z-index:100; border-radius:16px; align-items:center; justify-content:center; padding:20px;">
      <div style="background:#FFFFFF; border-radius:14px; padding:20px 16px; width:100%; max-width:280px; box-shadow:0 12px 30px rgba(0,0,0,0.18); text-align:center;">
          <div style="width:42px; height:42px; border-radius:50%; background:#FEE2E2; color:#DC2626; display:flex; align-items:center; justify-content:center; margin:0 auto 12px; font-size:18px;">
              <i class="fa-regular fa-trash-can"></i>
          </div>
          <strong style="font-size:13.5px; color:#1C1917; display:block; margin-bottom:6px;">Xóa Cuộc Trò Chuyện?</strong>
          <p style="font-size:12px; color:#78716C; line-height:1.45; margin-bottom:16px;">
              Toàn bộ lịch sử tin nhắn với <strong id="deleteTargetStoreName" style="color:#1C1917;"></strong> sẽ bị xóa vĩnh viễn và không thể khôi phục.
          </p>
          <div style="display:flex; gap:8px;">
              <button type="button" onclick="closeDeleteChatModal()" style="flex:1; height:34px; border-radius:8px; border:1px solid #E5E0D8; background:#FAF8F5; color:#57534E; font-size:12px; font-weight:600; cursor:pointer;">
                  Hủy
              </button>
              <button type="button" onclick="executeDeleteChat()" style="flex:1; height:34px; border-radius:8px; border:none; background:#DC2626; color:#FFFFFF; font-size:12px; font-weight:600; cursor:pointer;">
                  Xác nhận xóa
              </button>
          </div>
      </div>
  </div>

  <!-- MENU 3 CHẤM TOPBAR -->
  <div style="position:relative;">
      <button type="button" onclick="toggleStoreChatMenu(event)" title="Tùy chọn" style="background:none; border:none; color:#78716C; font-size:14px; cursor:pointer; padding:3px 6px; border-radius:4px;">
          <i class="fa-solid fa-ellipsis-vertical"></i>
      </button>
      <div id="storeChatActionDropdown" style="display:none; position:absolute; right:0; top:100%; background:#FFFFFF; border:1px solid #ECE7DE; border-radius:8px; box-shadow:0 6px 18px rgba(0,0,0,0.12); width:180px; z-index:90; padding:4px 0; font-size:12px;">
          <button type="button" onclick="promptDeleteCurrentStoreChat()" style="width:100%; text-align:left; padding:8px 12px; border:none; background:none; color:#DC2626; display:flex; align-items:center; gap:8px; cursor:pointer;">
              <i class="fa-regular fa-trash-can"></i> Xóa cuộc trò chuyện
          </button>
          <button type="button" onclick="clearLocalChatView()" style="width:100%; text-align:left; padding:8px 12px; border:none; background:none; color:#57534E; display:flex; align-items:center; gap:8px; cursor:pointer;">
              <i class="fa-solid fa-broom"></i> Làm sạch màn hình
          </button>
      </div>
  </div>
  ```
- **Lệnh xác minh:**
  ```bash
  curl -s -X POST http://localhost:8080/api/chat/store/1/delete-conversation
  ```
  Kỳ vọng: Trả về `{"success": true}`.

---

### Task 2: Tùy Chọn Xóa Từng Tin Nhắn Đơn Lẻ (Single Message Delete)
- **Tập tin:**
  - `src/main/resources/templates/fragments/header.html`
  - `src/main/java/vn/iotstar/controller/StoreChatController.java`
- **Mô tả:** Cho phép người dùng thu hồi/xóa một tin nhắn cụ thể đã gửi hoặc nhận nhầm.
- **Chi tiết triển khai:**
  1. Thêm endpoint: `POST /api/chat/message/{messageId}/delete` xóa dòng tin nhắn trong DB (dùng `chatMessageRepository.deleteById(messageId)`).
  2. Khi rê chuột vào tin nhắn trong `miniChatBody`, hiển thị icon thùng rác nhỏ `🗑`. Bấm vào gọi API và xóa tin nhắn khỏi màn hình.
- **Xác minh:** Bấm icon thùng rác trên tin nhắn -> Tin nhắn biến mất khỏi khung chat.

---

### Task 3: Kẹp Ảnh / Video Thực Tế Sách Tại Quầy (Client Media Upload & Preview)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:** Nút kẹp ảnh/camera `📷` bên cạnh ô nhập tin nhắn, cho phép khách hàng chụp hoặc gửi ảnh sách cần kiểm tra.
- **Chi tiết triển khai:**
  - Input file ẩn `accept="image/*,video/*"`.
  - Khung xem trước ảnh thumbnail nhỏ (kèm nút `×` để hủy trước khi gửi).
  - Tải lên endpoint có sẵn `/api/chat/upload-media`, lấy URL và đính kèm vào tin nhắn.
- **Xác minh:** Chọn ảnh -> Thấy ảnh xem trước -> Bấm gửi -> Tin nhắn hiển thị ảnh rõ nét.

---

### Task 4: Thanh Ngữ Cảnh Thẻ Sách Đang Xem (PDP Contextual Card)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:** Khi khách hàng bấm "Chat Ngay" từ trang chi tiết cuốn sách (`/books/{id}`), đầu khung chat xuất hiện thanh nhỏ hiển thị: Ảnh bìa, Tên sách, Giá bán, Nút "Gửi kèm sản phẩm này".
- **Chi tiết triển khai:**
  - `openChatWithStore(storeId, storeName, bookId, bookTitle, bookPrice, bookImage)` truyền đầy đủ context của sách.
  - Hiển thị thanh đính kèm mỏng trên khung nhập; khi khách bấm gửi sẽ tự động gắn `bookId` vào tin nhắn.
- **Xác minh:** Từ trang PDP bấm Chat Ngay, khung chat hiện thẻ sách trực quan đúng chuẩn Shopee.

---

### Task 5: Bộ Phím Tắt Gợi Ý Câu Hỏi Nhanh 1-Chạm (Quick Action Chips)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:** Dải chip cuộn ngang các câu hỏi thường gặp:
  - *"🔍 Còn sách này tại quầy không?"*
  - *"📸 Shop chụp ảnh thực tế tình trạng sách giúp mình với!"*
  - *"🚚 Phí ship và thời gian giao hàng thế nào?"*
  - *"⏰ Địa chỉ quầy và giờ mở cửa chi nhánh?"*
- **Chi tiết triển khai:** Bấm vào chip là tự động gửi tin nhắn ngay mà không cần khách gõ phím.
- **Xác minh:** Bấm chip, tin nhắn gửi ngay và bot/quầy phản hồi ngay lập tức.

---

### Task 6: Hiển Thị Thời Gian Tin Nhắn (Timestamp) & Trạng Thái Gửi (`✓`)
- **Tập tin:** `src/main/resources/templates/fragments/header.html`
- **Mô tả:** Thêm giờ gửi tin (ví dụ: `15:45 ✓`) vào góc dưới bên phải mỗi bong bóng tin nhắn, giúp cuộc trò chuyện chuyên nghiệp và rõ ràng.

---

## Tests & Validation Strategy
1. **Kiểm thử Xóa Cuộc Trò Chuyện (End-to-End):**
   - Mở widget chat, gửi 2 tin nhắn cho Chi Nhánh 1.
   - Bấm nút 3 chấm `⋮` trên topbar phòng chat -> Chọn "Xóa cuộc trò chuyện".
   - Kiểm tra Modal xác nhận Shopee bật lên: Hiển thị đúng tên "Tổng Kho Sách Cũ TP.HCM".
   - Bấm nút đỏ "Xác nhận xóa" -> Khung chat được làm sạch, danh sách shop cập nhật không còn tin nhắn cũ.
   - Mở tab khác hoặc reload lại: Xác nhận toàn bộ tin nhắn đã bị xóa sạch trong DB.
2. **Kiểm thử Xóa Từng Tin Nhắn:**
   - Rê chuột vào 1 tin nhắn -> Bấm icon thùng rác -> Xác nhận tin biến mất ngay lập tức.
3. **Kiểm thử Gửi Ảnh & Thẻ Sách:**
   - Tải 1 ảnh lên khung chat -> Ảnh gửi thành công và render chuẩn.

---

## Risks & Tradeoffs
- **Bấm nhầm:** Được ngăn chặn hoàn toàn nhờ Modal xác nhận in-app chuẩn Shopee.
- **Hiệu ứng xóa:** Xóa mềm ở UI trước, gọi API bất đồng bộ phía sau để mang lại trải nghiệm phản hồi tức thì.
