package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.security.CustomUserDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class StoreChatController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM");
    private static final Long DEFAULT_GUEST_USER_ID = 4L; // Nguyễn Song Hoàng Phúc

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private User resolveCurrentUser(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userRepository.findById(userDetails.getId()).orElse(null);
        }
        return userRepository.findById(DEFAULT_GUEST_USER_ID).orElse(null);
    }

    /**
     * API: Tải tệp hình ảnh / video gửi trong chat
     */
    @PostMapping("/api/chat/upload-media")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadChatMedia(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        if (file == null || file.isEmpty()) {
            res.put("success", false);
            res.put("message", "Vui lòng chọn tệp hình ảnh hoặc video hợp lệ!");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            boolean isVideo = ext.equals(".mp4") || ext.equals(".webm") || ext.equals(".mov") || ext.equals(".m4v") ||
                    (file.getContentType() != null && file.getContentType().startsWith("video/"));
            String mediaType = isVideo ? "VIDEO" : "IMAGE";
            String filename = "chat_" + (isVideo ? "vid_" : "img_") + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            Path targetPath = Paths.get("target/classes/static/uploads/chat", filename);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());

            try {
                Path srcPath = Paths.get("src/main/resources/static/uploads/chat", filename);
                Files.createDirectories(srcPath.getParent());
                Files.write(srcPath, file.getBytes());
            } catch (Exception ignored) {}

            String mediaUrl = "/uploads/chat/" + filename;
            res.put("success", true);
            res.put("mediaUrl", mediaUrl);
            res.put("mediaType", mediaType);
            return ResponseEntity.ok(res);
        } catch (IOException e) {
            res.put("success", false);
            res.put("message", "Lỗi lưu tệp media: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    /**
     * API: Lấy danh sách hội thoại của khách hàng với các chi nhánh (Chuẩn Shopee Web Chat)
     */
    @GetMapping("/api/chat/conversations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = resolveCurrentUser(userDetails);
        Map<String, Object> res = new HashMap<>();

        if (currentUser == null) {
            res.put("success", false);
            res.put("conversations", Collections.emptyList());
            return ResponseEntity.ok(res);
        }

        List<Store> allStores = storeRepository.findByIsActiveTrue();
        List<Map<String, Object>> convList = new ArrayList<>();

        for (Store store : allStores) {
            List<ChatMessage> messages = chatMessageRepository.findByUserIdAndStoreIdOrderByCreatedAtAsc(currentUser.getId(), store.getId());
            Map<String, Object> item = new HashMap<>();
            item.put("storeId", store.getId());
            item.put("storeName", store.getStoreName());
            item.put("storeAvatar", store.getImage() != null ? store.getImage() : "/images/brand-book-icon.png");
            item.put("storeDistrict", store.getDistrict());

            if (!messages.isEmpty()) {
                ChatMessage last = messages.get(messages.size() - 1);
                item.put("lastMessage", last.getContent());
                item.put("lastTime", last.getCreatedAt() != null ? last.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "");
                item.put("lastTimestamp", last.getCreatedAt());
                long unread = messages.stream().filter(m -> m.getSenderType() == ChatMessage.SenderType.STORE_STAFF && !Boolean.TRUE.equals(m.getIsRead())).count();
                item.put("unreadCount", unread);
                item.put("hasHistory", true);
            } else {
                item.put("lastMessage", "Nhắn tin hỏi tồn kho, tình trạng sách tại quầy...");
                item.put("lastTime", "");
                item.put("lastTimestamp", java.time.LocalDateTime.of(2020, 1, 1, 0, 0));
                item.put("unreadCount", 0L);
                item.put("hasHistory", false);
            }
            convList.add(item);
        }

        // Sắp xếp cuộc trò chuyện có tin nhắn mới nhất lên đầu danh sách
        convList.sort((a, b) -> {
            java.time.LocalDateTime tA = (java.time.LocalDateTime) a.get("lastTimestamp");
            java.time.LocalDateTime tB = (java.time.LocalDateTime) b.get("lastTimestamp");
            if (tA != null && tB != null) return tB.compareTo(tA);
            return 0;
        });

        res.put("success", true);
        res.put("conversations", convList);
        return ResponseEntity.ok(res);
    }

    /**
     * API: Đánh dấu tất cả tin nhắn từ chi nhánh là đã đọc
     */
    @PostMapping("/api/chat/store/{storeId}/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markStoreMessagesAsRead(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = resolveCurrentUser(userDetails);
        Map<String, Object> res = new HashMap<>();

        if (currentUser != null) {
            chatMessageRepository.markAllAsRead(currentUser.getId(), storeId);
            res.put("success", true);
        } else {
            res.put("success", false);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * API: Đánh dấu cuộc trò chuyện là chưa đọc
     */
    @PostMapping("/api/chat/store/{storeId}/mark-unread")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markStoreMessagesAsUnread(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = resolveCurrentUser(userDetails);
        Map<String, Object> res = new HashMap<>();

        if (currentUser != null) {
            List<ChatMessage> list = chatMessageRepository.findByUserIdAndStoreIdOrderByCreatedAtAsc(currentUser.getId(), storeId);
            if (!list.isEmpty()) {
                ChatMessage last = list.get(list.size() - 1);
                last.setIsRead(false);
                chatMessageRepository.save(last);
            }
            res.put("success", true);
        } else {
            res.put("success", false);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * API: Xóa toàn bộ lịch sử trò chuyện với chi nhánh này
     */
    @PostMapping("/api/chat/store/{storeId}/delete-conversation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteStoreConversation(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = resolveCurrentUser(userDetails);
        Map<String, Object> res = new HashMap<>();

        if (currentUser != null) {
            chatMessageRepository.deleteByUserIdAndStoreId(currentUser.getId(), storeId);
            res.put("success", true);
        } else {
            res.put("success", false);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * API: Lấy danh sách tin nhắn giữa khách hàng và chi nhánh (Lưu lịch sử đầy đủ)
     */
    @GetMapping("/api/chat/store/{storeId}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStoreMessages(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = resolveCurrentUser(userDetails);
        Map<String, Object> res = new HashMap<>();

        if (currentUser == null) {
            res.put("success", false);
            res.put("messages", Collections.emptyList());
            return ResponseEntity.ok(res);
        }

        List<ChatMessage> list = chatMessageRepository.findByUserIdAndStoreIdOrderByCreatedAtAsc(currentUser.getId(), storeId);
        List<Map<String, Object>> dtos = new ArrayList<>();

        for (ChatMessage m : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("content", m.getContent());
            map.put("mediaUrl", m.getMediaUrl());
            map.put("mediaType", m.getMediaType());
            map.put("senderType", m.getSenderType().name());
            map.put("senderName", m.getSenderName());
            map.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().format(TIME_FMT) : "");
            if (m.getBook() != null) {
                map.put("bookId", m.getBook().getId());
                map.put("bookTitle", m.getBook().getTitle());
                map.put("bookPrice", m.getBook().getPrice());
                map.put("bookImage", m.getBook().getPrimaryImageUrl());
            }
            dtos.add(map);
        }

        Store store = storeRepository.findById(storeId).orElse(null);
        res.put("success", true);
        res.put("storeName", store != null ? store.getStoreName() : "Chi Nhánh");
        res.put("storeAvatar", (store != null && store.getImage() != null) ? store.getImage() : "/images/logo-books-badge.png");
        res.put("storeAddress", store != null ? store.getAddress() : "");
        res.put("messages", dtos);

        return ResponseEntity.ok(res);
    }

    /**
     * API: Khách hàng gửi tin nhắn đến chi nhánh (hỗ trợ kèm hình ảnh, video hoặc mã sách)
     */
    @PostMapping("/api/chat/store/{storeId}/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendCustomerMessage(
            @PathVariable Long storeId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "mediaUrl", required = false) String mediaUrl,
            @RequestParam(value = "mediaType", required = false) String mediaType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = resolveCurrentUser(userDetails);
        Map<String, Object> res = new HashMap<>();

        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasMedia = mediaUrl != null && !mediaUrl.trim().isEmpty();

        if (currentUser == null || (!hasContent && !hasMedia)) {
            res.put("success", false);
            res.put("message", "Nội dung tin nhắn hoặc tệp đính kèm không được để trống.");
            return ResponseEntity.badRequest().body(res);
        }

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            res.put("success", false);
            res.put("message", "Chi nhánh không tồn tại.");
            return ResponseEntity.badRequest().body(res);
        }

        Book book = (bookId != null) ? bookRepository.findById(bookId).orElse(null) : null;

        // Lưu tin nhắn của khách hàng
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUser(currentUser);
        userMsg.setStore(store);
        userMsg.setBook(book);
        userMsg.setContent(hasContent ? content.trim() : (hasMedia ? (mediaType != null && mediaType.equals("VIDEO") ? "[Video thực tế]" : "[Hình ảnh thực tế]") : ""));
        userMsg.setMediaUrl(mediaUrl);
        userMsg.setMediaType(mediaType);
        userMsg.setSenderType(ChatMessage.SenderType.CUSTOMER);
        userMsg.setSenderName(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
        chatMessageRepository.save(userMsg);

        // TÍCH HỢP TRÍ TUỆ NHÂN TẠO (AI STORE ASSISTANT):
        // - Với các câu thông thường (tồn kho, tình trạng độ mới, giá bán, địa chỉ, giờ mở cửa, phí ship, đổi trả): AI tự động tra cứu CSDL & phản hồi ngay lập tức.
        // - Với các câu đặc biệt (mặc cả giá, đặt cọc giữ sách, xem chi tiết trang rách/bung keo, khiếu nại, yêu cầu gặp người thật):
        //   AI thông báo chuyển tiếp cho Thủ kho/Quản lý chi nhánh và đánh dấu [CẦN QUẦY TRẢ LỜI] trên trang quản trị.
        Map<String, Object> aiResult = generateStoreAiReply(store, book, userMsg.getContent());
        boolean needsStaff = Boolean.TRUE.equals(aiResult.get("needsStaff"));

        if (needsStaff) {
            userMsg.setNeedsAttention(true);
            chatMessageRepository.save(userMsg);
        }

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setUser(currentUser);
        aiMsg.setStore(store);
        aiMsg.setSenderType(ChatMessage.SenderType.STORE_STAFF);
        aiMsg.setSenderName("Trợ lý AI - " + store.getStoreName());
        aiMsg.setContent((String) aiResult.get("reply"));
        aiMsg.setNeedsAttention(needsStaff);
        if (aiResult.containsKey("suggestedBook")) {
            aiMsg.setBook((Book) aiResult.get("suggestedBook"));
        }
        chatMessageRepository.save(aiMsg);

        res.put("success", true);
        res.put("needsStaff", needsStaff);
        res.put("reply", aiMsg.getContent());
        res.put("senderName", aiMsg.getSenderName());
        res.put("storeId", storeId);
        res.put("createdAt", aiMsg.getCreatedAt() != null ? aiMsg.getCreatedAt().format(TIME_FMT) : "");
        if (aiMsg.getBook() != null) {
            Map<String, Object> bMap = new HashMap<>();
            bMap.put("id", aiMsg.getBook().getId());
            bMap.put("title", aiMsg.getBook().getTitle());
            bMap.put("price", aiMsg.getBook().getPrice());
            bMap.put("imageUrl", aiMsg.getBook().getPrimaryImageUrl());
            bMap.put("conditionPercent", aiMsg.getBook().getConditionPercent());
            res.put("suggestedBook", bMap);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * TÍCH HỢP TRÍ TUỆ NHÂN TẠO (AI STORE ASSISTANT):
     * - Tự động tra cứu tồn kho, giá bán, độ mới, địa chỉ, giờ mở cửa và chính sách của chi nhánh.
     * - Tự động phát hiện "câu hỏi đặc biệt" (trả giá, giữ cọc, chụp ảnh góc rách, khiếu nại, yêu cầu gặp người thật)
     *   để thông báo chuyển tiếp cho Thủ kho/Quản lý chi nhánh trực tiếp trả lời.
     */
    private Map<String, Object> generateStoreAiReply(Store store, Book book, String userText) {
        Map<String, Object> result = new HashMap<>();
        if (userText == null || userText.isBlank()) {
            result.put("reply", "Dạ chào bạn! Em là Trợ lý AI của " + store.getStoreName() + ". Em có thể giải đáp về tồn kho sách, độ mới, giá bán, địa chỉ và giờ mở cửa của chi nhánh ạ!");
            result.put("needsStaff", false);
            return result;
        }

        String lower = userText.toLowerCase().trim();

        // 1. NHÓM CÂU HỎI ĐẶC BIỆT CẦN THỦ KHO / QUẢN LÝ TRỰC TIẾP TRẢ LỜI
        // (Mặc cả giá, đặt cọc giữ sách, xem góc rách/chữ ký cụ thể, khiếu nại, mua sỉ, hoặc yêu cầu gặp người thật)
        boolean isSpecial = lower.contains("giảm giá thêm") || lower.contains("bớt giá") || lower.contains("fix giá") ||
                lower.contains("bớt không") || lower.contains("chiết khấu riêng") ||
                lower.contains("giữ sách") || lower.contains("cọc trước") || lower.contains("đặt cọc") ||
                lower.contains("để riêng") || lower.contains("để dành") ||
                lower.contains("chụp góc") || lower.contains("trang 50") || lower.contains("mục lục") ||
                lower.contains("chữ ký") || lower.contains("rách mép") || lower.contains("bung keo") ||
                lower.contains("gặp quản lý") || lower.contains("gặp chủ") || lower.contains("gặp người thật") ||
                lower.contains("nhân viên đâu") || lower.contains("thủ kho đâu") || lower.contains("người thật") ||
                lower.contains("khiếu nại") || lower.contains("bị ướt") || lower.contains("giao sai") ||
                lower.contains("mua sỉ") || lower.contains("số lượng lớn");

        if (isSpecial) {
            String escortMsg = "Dạ đây là yêu cầu đặc biệt cần nhân viên kiểm tra thực tế tại kệ/kho sách. Em đã chuyển tiếp tin nhắn đến Thủ kho / Quản lý chi nhánh " + store.getStoreName() + ". Nhân viên quầy sẽ trực tiếp phản hồi anh/chị ngay ít phút nữa ạ! ⏳";
            result.put("reply", escortMsg);
            result.put("needsStaff", true);
            return result;
        }

        // 2. TRƯỜNG HỢP: KHÁCH HỎI VỀ ĐỊA CHỈ, VỊ TRÍ, ĐƯỜNG ĐI
        if (lower.contains("ở đâu") || lower.contains("địa chỉ") || lower.contains("vị trí") || lower.contains("chỗ nào") || lower.contains("gần đâu")) {
            String addrMsg = "Dạ chi nhánh " + store.getStoreName() + " có địa chỉ tại: " + store.getAddress() + " (" + store.getDistrict() + "). Cửa hàng có chỗ để xe máy miễn phí và bàn đọc sách trải nghiệm, rất mong được đón tiếp anh/chị ghé quầy ạ! 📍";
            result.put("reply", addrMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 3. TRƯỜNG HỢP: KHÁCH HỎI VỀ GIỜ MỞ CỬA, THỜI GIAN HOẠT ĐỘNG
        if (lower.contains("mở cửa") || lower.contains("đóng cửa") || lower.contains("mấy giờ") || lower.contains("giờ làm việc") || lower.contains("thời gian")) {
            String hoursMsg = "Dạ chi nhánh " + store.getStoreName() + " mở cửa phục vụ từ 08:00 sáng đến 21:30 tối hàng ngày (kể cả Thứ 7, Chủ Nhật và ngày lễ) ạ! Anh/chị có thể ghé bất cứ lúc nào trong khung giờ này nhé! ⏰";
            result.put("reply", hoursMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 4. TRƯỜNG HỢP: KHÁCH HỎI VỀ PHÍ SHIP, GIAO HỎA TỐC, THỜI GIAN GIAO
        if (lower.contains("ship") || lower.contains("giao hàng") || lower.contains("hỏa tốc") || lower.contains("vận chuyển") || lower.contains("bao lâu")) {
            String shipMsg = "Dạ chi nhánh có hỗ trợ giao hỏa tốc 2-4h nội thành TP.HCM (phí từ 18.000₫ - 25.000₫) và Freeship cho đơn từ 150.000₫ ạ. Anh/chị luôn được ĐỒNG KIỂM - kiểm tra tình trạng sách thực tế trước khi thanh toán nhận hàng nhé! 🚀";
            result.put("reply", shipMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 5. TRƯỜNG HỢP: KHÁCH HỎI VỀ ĐỔI TRẢ, BẢO HÀNH, CHẤT LƯỢNG SÁCH
        if (lower.contains("đổi trả") || lower.contains("bảo hành") || lower.contains("đồng kiểm") || lower.contains("chất lượng")) {
            String policyMsg = "Dạ toàn bộ sách cũ tại " + store.getStoreName() + " đều qua quy trình kiểm định 4 bước: kiểm tra đủ trang, vệ sinh gáy bìa và phân loại độ mới rõ ràng. Chuỗi hỗ trợ đổi trả miễn phí trong 3 ngày nếu sách bị lỗi hoặc không đúng mô tả ạ! 🛡️";
            result.put("reply", policyMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 6. TRƯỜNG HỢP: HỎI VỀ TỒN KHO, ĐỘ MỚI, GIÁ CỦA CUỐN SÁCH CỤ THỂ (HOẶC SÁCH ĐANG GHIM)
        Book targetBook = book;
        if (targetBook == null) {
            List<Book> allBooks = bookRepository.findAllActiveWithImages();
            for (Book b : allBooks) {
                String cleanT = b.getTitle().toLowerCase().replaceAll("\\(.*?\\)", "").trim();
                if (cleanT.length() >= 3 && lower.contains(cleanT)) {
                    targetBook = b;
                    break;
                }
            }
        }

        if (targetBook != null) {
            Optional<Inventory> invOpt = inventoryRepository.findByStoreIdAndBookId(store.getId(), targetBook.getId());
            int stock = invOpt.map(Inventory::getQuantity).orElse(0);
            int cond = targetBook.getConditionPercent() != null ? targetBook.getConditionPercent() : 90;
            String priceStr = String.format("%,d", targetBook.getPrice().longValue()).replace(',', '.') + "₫";

            if (stock > 0) {
                String stockMsg = "Dạ cuốn sách \"" + targetBook.getTitle() + "\" hiện ĐANG CÓ SẴN tại " + store.getStoreName() +
                        " (tồn kho: " + stock + " cuốn) ạ!\n" +
                        "• Tình trạng: Độ mới " + cond + "%, bìa gáy nguyên vẹn, trang giấy sạch đẹp.\n" +
                        "• Giá bán sách cũ: " + priceStr + ".\n" +
                        "Anh/chị có thể bấm đặt mua trực tiếp hoặc ghé quầy xem nhé!";
                result.put("reply", stockMsg);
                result.put("needsStaff", false);
                result.put("suggestedBook", targetBook);
                return result;
            } else {
                String outMsg = "Dạ cuốn \"" + targetBook.getTitle() + "\" tại " + store.getStoreName() + " tạm thời vừa hết hàng ạ. Tuy nhiên chuỗi vẫn còn tại các chi nhánh khác trong hệ thống. Anh/chị có thể đặt ship trực tiếp trên web để chuỗi điều chuyển giao tận nơi nhé!";
                result.put("reply", outMsg);
                result.put("needsStaff", false);
                return result;
            }
        }

        // 7. HỎI GỢI Ý SÁCH HAY THEO CHỦ ĐỀ
        if (lower.contains("gợi ý") || lower.contains("sách hay") || lower.contains("sách nên đọc") || lower.contains("tâm lý") || lower.contains("kinh tế") || lower.contains("văn học")) {
            List<Inventory> storeInvs = inventoryRepository.findByStoreIdWithBooks(store.getId());
            StringBuilder suggMsg = new StringBuilder("Dạ tại quầy " + store.getStoreName() + " đang có rất nhiều đầu sách cũ được bạn đọc yêu thích:\n");
            int count = 0;
            for (Inventory inv : storeInvs) {
                if (inv.getQuantity() > 0 && inv.getBook() != null) {
                    suggMsg.append("📖 \"").append(inv.getBook().getTitle()).append("\" - ₫").append(String.format("%,d", inv.getBook().getPrice().longValue()).replace(',', '.')).append("\n");
                    count++;
                    if (count >= 3) break;
                }
            }
            suggMsg.append("Anh/chị muốn tìm sách theo thể loại hay tác giả cụ thể nào để em tra cứu thêm ạ?");
            result.put("reply", suggMsg.toString());
            result.put("needsStaff", false);
            return result;
        }

        // 8. CÂU HỎI CHUNG / CHƯA RÕ NGHĨA -> AI CHÀO HỎI VÀ HỎI THÊM
        String defaultMsg = "Dạ em là Trợ lý AI của " + store.getStoreName() + ". Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨";
        result.put("reply", defaultMsg);
        result.put("needsStaff", false);
        return result;
    }

    /**
     * Trang Kênh Quản Trị: Quản lý Chat của Chi Nhánh (Dành cho Quản lý / Thủ kho / Admin)
     */
    @GetMapping("/admin/chat")
    public String adminChatView(
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "userId", required = false) Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        List<Store> stores;
        Long activeStoreId;

        if (isAdmin) {
            stores = storeRepository.findByIsActiveTrue();
            activeStoreId = (storeId != null) ? storeId : (!stores.isEmpty() ? stores.get(0).getId() : 1L);
        } else {
            List<Store> managedStores = (userDetails != null)
                    ? storeRepository.findByManagerId(userDetails.getId())
                    : Collections.emptyList();
            stores = managedStores;
            activeStoreId = !managedStores.isEmpty() ? managedStores.get(0).getId() : 1L;
        }

        Store currentStore = storeRepository.findById(activeStoreId).orElse(!stores.isEmpty() ? stores.get(0) : null);
        List<User> customers = chatMessageRepository.findDistinctUsersByStoreId(activeStoreId);

        User activeCustomer = null;
        List<ChatMessage> conversation = Collections.emptyList();

        if (targetUserId != null) {
            activeCustomer = userRepository.findById(targetUserId).orElse(null);
        } else if (!customers.isEmpty()) {
            activeCustomer = customers.get(0);
        }

        if (activeCustomer != null && currentStore != null) {
            conversation = chatMessageRepository.findConversation(currentStore.getId(), activeCustomer.getId());
            // Đánh dấu đã đọc
            for (ChatMessage m : conversation) {
                if (m.getSenderType() == ChatMessage.SenderType.CUSTOMER) {
                    m.setIsRead(true);
                }
            }
            chatMessageRepository.saveAll(conversation);
        }

        List<Long> needAttentionUserIds = chatMessageRepository.findUserIdsNeedingAttention(activeStoreId);
        boolean activeCustomerNeedsAttention = activeCustomer != null && needAttentionUserIds.contains(activeCustomer.getId());

        model.addAttribute("stores", stores);
        model.addAttribute("currentStore", currentStore);
        model.addAttribute("currentStoreId", activeStoreId);
        model.addAttribute("customers", customers);
        model.addAttribute("activeCustomer", activeCustomer);
        model.addAttribute("conversation", conversation);
        model.addAttribute("needAttentionUserIds", needAttentionUserIds);
        model.addAttribute("activeCustomerNeedsAttention", activeCustomerNeedsAttention);
        model.addAttribute("pageTitle", "Kênh Tin Nhắn Chi Nhánh — " + (currentStore != null ? currentStore.getStoreName() : ""));

        return "admin/chat/index";
    }

    /**
     * API: Nhân viên quầy chi nhánh trả lời tin nhắn của khách (hỗ trợ đính kèm ảnh chụp sách hoặc video thực tế)
     */
    @PostMapping("/api/chat/staff/reply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> staffReply(
            @RequestParam("storeId") Long storeId,
            @RequestParam("userId") Long customerId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "mediaUrl", required = false) String mediaUrl,
            @RequestParam(value = "mediaType", required = false) String mediaType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> res = new HashMap<>();
        Store store = storeRepository.findById(storeId).orElse(null);
        User customer = userRepository.findById(customerId).orElse(null);

        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasMedia = mediaUrl != null && !mediaUrl.trim().isEmpty();

        if (store == null || customer == null || (!hasContent && !hasMedia)) {
            res.put("success", false);
            res.put("message", "Dữ liệu phản hồi không hợp lệ.");
            return ResponseEntity.badRequest().body(res);
        }

        String staffName = "Thủ kho " + store.getStoreName();
        if (userDetails != null && userDetails.getFullName() != null && !userDetails.getFullName().isBlank()) {
            staffName = userDetails.getFullName();
        }

        ChatMessage reply = new ChatMessage();
        reply.setStore(store);
        reply.setUser(customer);
        reply.setSenderType(ChatMessage.SenderType.STORE_STAFF);
        reply.setSenderName(staffName);
        reply.setContent(hasContent ? content.trim() : (hasMedia ? (mediaType != null && mediaType.equals("VIDEO") ? "[Đã gửi video sách tại quầy]" : "[Đã gửi ảnh chụp sách tại quầy]") : ""));
        reply.setMediaUrl(mediaUrl);
        reply.setMediaType(mediaType);
        reply.setNeedsAttention(false);
        chatMessageRepository.save(reply);

        // Khi nhân viên trực quầy đã trả lời, xóa cờ cần chú ý
        List<ChatMessage> unhandled = chatMessageRepository.findByUserIdAndStoreIdOrderByCreatedAtAsc(customer.getId(), store.getId());
        for (ChatMessage m : unhandled) {
            if (Boolean.TRUE.equals(m.getNeedsAttention())) {
                m.setNeedsAttention(false);
            }
        }
        chatMessageRepository.saveAll(unhandled);

        res.put("success", true);
        return ResponseEntity.ok(res);
    }
}
