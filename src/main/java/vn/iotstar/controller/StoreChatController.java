package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    // Presence Tracking: Quản lý liveness của Quản lý từng chi nhánh (Thread-safe)
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
            return true; // Chế độ AI trực 24/7
        }
        Long lastSeen = STORE_LAST_SEEN.get(store.getId());
        return lastSeen != null && (System.currentTimeMillis() - lastSeen) < PRESENCE_TIMEOUT_MS;
    }

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

    @Autowired
    private AiAssistantController aiAssistantController;

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
            if (isVideo && !ext.matches("\\.(mp4|webm|mov|m4v|mkv|avi)")) {
                ext = ".mp4";
            }
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
            item.put("isOnline", isStoreOnline(store));

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
        res.put("aiReplyEnabled", store != null ? Boolean.TRUE.equals(store.getAiReplyEnabled()) : false);
        res.put("isOnline", isStoreOnline(store));
        res.put("managerName", (store != null && store.getManager() != null) ? store.getManager().getFullName() : "Quản lý cửa hàng");
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

        // Lấy lịch sử tin nhắn trước khi lưu tin mới
        List<ChatMessage> prevMessages = chatMessageRepository.findByUserIdAndStoreIdOrderByCreatedAtAsc(currentUser.getId(), storeId);

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
        userMsg.setNeedsAttention(true);
        chatMessageRepository.save(userMsg);
        res.put("userMsgId", userMsg.getId());

        boolean isAiEnabled = (store.getAiReplyEnabled() == null || Boolean.TRUE.equals(store.getAiReplyEnabled()));
        String lower = userMsg.getContent().toLowerCase();
        boolean customerExplicitlyWantsHuman = lower.contains("gặp người thật") || lower.contains("nhân viên đâu")
                || lower.contains("alo shop") || lower.contains("thủ kho") || lower.contains("khiếu nại")
                || lower.contains("bớt giá") || lower.contains("trả giá") || lower.contains("[khách hàng yêu cầu gặp nhân viên");

        // 1. Nếu khách yêu cầu gặp người thật hoặc hỏi các câu mặc cả/khiếu nại
        if (customerExplicitlyWantsHuman) {
            ChatMessage handoffMsg = new ChatMessage();
            handoffMsg.setUser(currentUser);
            handoffMsg.setStore(store);
            handoffMsg.setSenderType(ChatMessage.SenderType.STORE_STAFF);
            handoffMsg.setSenderName("Hệ thống chuyển giao");
            handoffMsg.setContent("Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy " + store.getStoreName() + " sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨");
            handoffMsg.setNeedsAttention(true);
            chatMessageRepository.save(handoffMsg);

            res.put("success", true);
            res.put("needsStaff", true);
            res.put("reply", handoffMsg.getContent());
            res.put("replyMsgId", handoffMsg.getId());
            res.put("senderName", handoffMsg.getSenderName());
            res.put("storeId", storeId);
            res.put("createdAt", handoffMsg.getCreatedAt().format(TIME_FMT));
            return ResponseEntity.ok(res);
        }

        // 2. Nếu Shop đang BẬT chế độ Trợ lý AI trả lời thay
        if (isAiEnabled) {
            Map<String, Object> aiResult = generateStoreAiReply(store, book, userMsg.getContent());
            boolean needsStaff = Boolean.TRUE.equals(aiResult.get("needsStaff"));

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
            res.put("replyMsgId", aiMsg.getId());
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

        // 3. Nếu Shop đang TẮT chế độ AI (Chế độ Nhân viên trực quầy đối thoại)
        if (prevMessages.isEmpty()) {
            // Tin nhắn đầu tiên: gửi 1 câu chào tiếp nhận tự động
            ChatMessage greeting = new ChatMessage();
            greeting.setUser(currentUser);
            greeting.setStore(store);
            greeting.setSenderType(ChatMessage.SenderType.STORE_STAFF);
            greeting.setSenderName("Quản lý quầy - " + store.getStoreName());
            greeting.setContent("Dạ chào bạn! " + store.getStoreName() + " đã nhận được tin nhắn. Quản lý cửa hàng đang kiểm tra quầy và sẽ phản hồi cho bạn ngay trong ít phút nhé ạ! ✨");
            greeting.setNeedsAttention(true);
            chatMessageRepository.save(greeting);

            res.put("success", true);
            res.put("needsStaff", true);
            res.put("reply", greeting.getContent());
            res.put("replyMsgId", greeting.getId());
            res.put("senderName", greeting.getSenderName());
            res.put("storeId", storeId);
            res.put("createdAt", greeting.getCreatedAt().format(TIME_FMT));
            return ResponseEntity.ok(res);
        }

        // Các tin nhắn tiếp theo: Chờ nhân viên quầy trả lời trực tiếp
        res.put("success", true);
        res.put("needsStaff", true);
        res.put("reply", null);
        res.put("storeId", storeId);
        return ResponseEntity.ok(res);
    }

    /**
     * TÍCH HỢP TRÍ TUỆ NHÂN TẠO (AI STORE ASSISTANT):
     * - Tự động tra cứu tồn kho, giá bán, độ mới, địa chỉ, giờ mở cửa và chính sách của chi nhánh.
     * - Tự động phát hiện "câu hỏi đặc biệt" (trả giá, giữ cọc, chụp ảnh góc rách, khiếu nại, yêu cầu gặp người thật)
     *   để thông báo chuyển tiếp cho Thủ kho/Quản lý chi nhánh trực tiếp trả lời.
     */
    private static String removeDiacritics(String str) {
        if (str == null) return "";
        String nfd = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase()
                .trim();
    }

    private Map<String, Object> generateStoreAiReply(Store store, Book book, String userText) {
        Map<String, Object> result = new HashMap<>();
        if (userText == null || userText.isBlank()) {
            result.put("reply", "Dạ ReBook xin chào bạn! Em là Trợ lý AI của " + store.getStoreName() + " 📚. Em có thể tra cứu tồn kho sách, độ mới, giá bán và chính sách tại quầy cho mình ạ!");
            result.put("needsStaff", false);
            return result;
        }

        String rawLower = userText.toLowerCase().trim();
        String unaccentQuery = removeDiacritics(userText);

        // 1. NHÓM CÂU HỎI ĐẶC BIỆT CẦN THỦ KHO / QUẢN LÝ TRỰC TIẾP TRẢ LỜI
        if (rawLower.contains("[video thực tế]") || rawLower.contains("video") || rawLower.contains("clip")) {
            String vidMsg = "Dạ chi nhánh đã nhận được video kiểm tra sách từ bạn ạ! Quản lý quầy " + store.getStoreName() + " sẽ xem video và phản hồi cho bạn ngay ít phút nữa nhé! 📹✨";
            result.put("reply", vidMsg);
            result.put("needsStaff", true);
            return result;
        }

        boolean isSpecial = rawLower.contains("giảm giá thêm") || rawLower.contains("bớt giá") || rawLower.contains("fix giá") ||
                rawLower.contains("bớt không") || rawLower.contains("chiết khấu riêng") ||
                rawLower.contains("giữ sách") || rawLower.contains("cọc trước") || rawLower.contains("đặt cọc") ||
                rawLower.contains("để riêng") || rawLower.contains("để dành") ||
                rawLower.contains("chụp góc") || rawLower.contains("trang 50") || rawLower.contains("mục lục") ||
                rawLower.contains("chữ ký") || rawLower.contains("rách mép") || rawLower.contains("bung keo") ||
                rawLower.contains("gặp quản lý") || rawLower.contains("gặp chủ") || rawLower.contains("gặp người thật") ||
                rawLower.contains("nhân viên đâu") || rawLower.contains("thủ kho đâu") || rawLower.contains("người thật") ||
                rawLower.contains("khiếu nại") || rawLower.contains("bị ướt") || rawLower.contains("giao sai") ||
                rawLower.contains("mua sỉ") || rawLower.contains("số lượng lớn");

        if (isSpecial) {
            String escortMsg = "Dạ đây là yêu cầu đặc biệt cần nhân viên kiểm tra thực tế tại kệ/kho sách. Em đã chuyển tiếp tin nhắn đến Quản lý chi nhánh " + store.getStoreName() + ". Quản lý quầy sẽ trực tiếp phản hồi bạn ngay ít phút nữa nhé! ⏳👨‍💼";
            result.put("reply", escortMsg);
            result.put("needsStaff", true);
            return result;
        }

        // 2. ĐỊA CHỈ, VỊ TRÍ, ĐƯỜNG ĐI CHI NHÁNH
        if (unaccentQuery.contains("o dau") || unaccentQuery.contains("dia chi") || unaccentQuery.contains("vi tri") || unaccentQuery.contains("cho nao") || unaccentQuery.contains("gan dau")) {
            String addrMsg = "Dạ chi nhánh **" + store.getStoreName() + "** có địa chỉ tại: " + store.getAddress() + " (" + store.getDistrict() + "). Cửa hàng có chỗ để xe máy miễn phí và bàn đọc sách trải nghiệm, rất mong được đón tiếp bạn ghé quầy ạ! 📍";
            result.put("reply", addrMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 3. GIỜ MỞ CỬA, THỜI GIAN HOẠT ĐỘNG CHI NHÁNH
        if (unaccentQuery.contains("mo cua") || unaccentQuery.contains("dong cua") || unaccentQuery.contains("may gio") || unaccentQuery.contains("gio lam viec") || unaccentQuery.contains("thoi gian")) {
            String hoursMsg = "Dạ chi nhánh **" + store.getStoreName() + "** mở cửa phục vụ từ 08:00 sáng đến 21:30 tối hàng ngày (kể cả Thứ 7, Chủ Nhật và ngày lễ) ạ! Bạn có thể ghé bất cứ lúc nào trong khung giờ này nhé! ⏰";
            result.put("reply", hoursMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 4. TƯ VẤN SÁCH & TỒN KHO THỜI GIAN THỰC (ĐỒNG BỘ TRÍ TUỆ VỚI THANH SEARCH AI)
        if (aiAssistantController != null) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", userText);
                ResponseEntity<Map<String, Object>> aiResp = aiAssistantController.chat(payload);
                if (aiResp != null && aiResp.getBody() != null) {
                    Map<String, Object> body = aiResp.getBody();
                    String reply = (String) body.get("reply");
                    if (reply != null && !reply.isBlank()) {
                        Book suggestedBook = book;
                        Object suggObj = body.get("suggestedBooks");
                        if (suggObj instanceof List) {
                            List<?> list = (List<?>) suggObj;
                            if (!list.isEmpty() && list.get(0) instanceof Map) {
                                Map<?, ?> firstBookMap = (Map<?, ?>) list.get(0);
                                Object idObj = firstBookMap.get("id");
                                if (idObj instanceof Number) {
                                    Long bId = ((Number) idObj).longValue();
                                    suggestedBook = bookRepository.findById(bId).orElse(suggestedBook);
                                }
                            }
                        }

                        // Kiểm tra tồn kho tại chi nhánh hiện tại để chỉ dẫn kệ quầy thực tế
                        if (suggestedBook != null) {
                            result.put("suggestedBook", suggestedBook);
                            Optional<Inventory> storeInv = inventoryRepository.findByStoreIdAndBookId(store.getId(), suggestedBook.getId());
                            int curStock = storeInv.map(Inventory::getQuantity).orElse(0);
                            if (curStock > 0) {
                                reply += "\n\n📍 Cuốn **\"" + suggestedBook.getTitle() + "\"** hiện **CÒN " + curStock + " CUỐN TRÊN KỆ** tại "
                                        + store.getStoreName() + " (" + (store.getAddress() != null ? store.getAddress() : "TP.HCM") + "). Bạn có thể bấm nút **Mua** bên dưới để giữ sách ngay nhé! 📚✨";
                            }
                        }

                        result.put("reply", reply);
                        result.put("needsStaff", false);
                        return result;
                    }
                }
            } catch (Exception e) {
                System.err.println("[StoreChat AI] Ngoại lệ khi gọi AiAssistant: " + e.getMessage());
            }
        }

        // 5. CHÀO HỎI & BẮT ĐẦU TRÒ CHUYỆN (GREETING INTENT)
        boolean isGreeting = rawLower.matches(".*\\b(chào|chao|hi|hello|alo|shop ơi|shop oi|ad ơi|ad oi|bạn ơi|ban oi|hey|helo)\\b.*")
                || rawLower.equals("hi") || rawLower.equals("hello") || rawLower.equals("alo") || rawLower.equals("chao shop") || rawLower.equals("chào shop");

        if (isGreeting) {
            String greetMsg = "Dạ ReBook xin chào bạn! Em là Trợ lý AI của **" + store.getStoreName() + "** 📚✨\n" +
                    "Hiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?";
            result.put("reply", greetMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 3. ĐỊA CHỈ, VỊ TRÍ, ĐƯỜNG ĐI
        if (unaccentQuery.contains("o dau") || unaccentQuery.contains("dia chi") || unaccentQuery.contains("vi tri") || unaccentQuery.contains("cho nao") || unaccentQuery.contains("gan dau")) {
            String addrMsg = "Dạ chi nhánh **" + store.getStoreName() + "** có địa chỉ tại: " + store.getAddress() + " (" + store.getDistrict() + "). Cửa hàng có chỗ để xe máy miễn phí và bàn đọc sách trải nghiệm, rất mong được đón tiếp bạn ghé quầy ạ! 📍";
            result.put("reply", addrMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 4. GIỜ MỞ CỬA, THỜI GIAN HOẠT ĐỘNG
        if (unaccentQuery.contains("mo cua") || unaccentQuery.contains("dong cua") || unaccentQuery.contains("may gio") || unaccentQuery.contains("gio lam viec") || unaccentQuery.contains("thoi gian")) {
            String hoursMsg = "Dạ chi nhánh **" + store.getStoreName() + "** mở cửa phục vụ từ 08:00 sáng đến 21:30 tối hàng ngày (kể cả Thứ 7, Chủ Nhật và ngày lễ) ạ! Bạn có thể ghé bất cứ lúc nào trong khung giờ này nhé! ⏰";
            result.put("reply", hoursMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 5. PHÍ SHIP, GIAO HÀNG HỎA TỐC
        if (unaccentQuery.contains("ship") || unaccentQuery.contains("giao hang") || unaccentQuery.contains("hoa toc") || unaccentQuery.contains("van chuyen") || unaccentQuery.contains("bao lau")) {
            String shipMsg = "Dạ chi nhánh hỗ trợ giao hỏa tốc 2-4h nội thành TP.HCM (phí từ 18.000₫ - 25.000₫) và Freeship cho đơn từ 150.000₫ ạ. Bạn luôn được ĐỒNG KIỂM - kiểm tra tình trạng sách thực tế trước khi thanh toán nhận hàng nhé! 🚀";
            result.put("reply", shipMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 6. ĐỔI TRẢ, BẢO HÀNH, CHẤT LƯỢNG SÁCH
        if (unaccentQuery.contains("doi tra") || unaccentQuery.contains("bao hanh") || unaccentQuery.contains("dong kiem") || unaccentQuery.contains("chat luong")) {
            String policyMsg = "Dạ toàn bộ sách cũ tại **" + store.getStoreName() + "** đều qua quy trình kiểm định 4 bước: kiểm tra đủ trang, vệ sinh gáy bìa và phân loại độ mới rõ ràng. Chuỗi hỗ trợ đổi trả miễn phí trong 3 ngày nếu sách bị lỗi hoặc không đúng mô tả ạ! 🛡️";
            result.put("reply", policyMsg);
            result.put("needsStaff", false);
            return result;
        }

        // 7. TRA CỨU SÁCH CỤ THỂ THEO TỰA HOẶC TÁC GIẢ (HỖ TRỢ CẢ CÓ DẤU & KHÔNG DẤU)
        Book targetBook = book;
        if (targetBook == null) {
            List<Book> allBooks = bookRepository.findAllActiveWithImages();
            int bestScore = 0;

            for (Book b : allBooks) {
                String bTitle = b.getTitle() != null ? b.getTitle() : "";
                String bAuthor = b.getAuthor() != null ? b.getAuthor() : "";
                String unaccentTitle = removeDiacritics(bTitle);
                String unaccentAuthor = removeDiacritics(bAuthor);

                int score = 0;
                // 1. Kiểm tra khớp toàn bộ tựa sách
                if (unaccentQuery.contains(unaccentTitle) || unaccentTitle.contains(unaccentQuery)) {
                    score += 15;
                }
                // 2. Tách cụm từ tựa sách chính (bỏ ngoặc)
                String mainTitle = unaccentTitle.replaceAll("\\(.*?\\)", "").trim();
                if (mainTitle.length() >= 3 && (unaccentQuery.contains(mainTitle) || mainTitle.contains(unaccentQuery))) {
                    score += 12;
                }
                // 3. Kiểm tra cụm tiếng Anh / phụ đề trong ngoặc đơn (ví dụ: The Alchemist, Atomic Habits...)
                java.util.regex.Matcher mPar = java.util.regex.Pattern.compile("\\((.*?)\\)").matcher(unaccentTitle);
                while (mPar.find()) {
                    String inside = mPar.group(1).trim();
                    if (inside.length() >= 3 && (unaccentQuery.contains(inside) || inside.contains(unaccentQuery))) {
                        score += 14;
                    }
                }
                // 4. Khớp tác giả
                if (!unaccentAuthor.isEmpty() && (unaccentQuery.contains(unaccentAuthor) || unaccentAuthor.contains(unaccentQuery))) {
                    score += 10;
                }
                // 5. Khớp từng từ khóa chính (Keywords >= 3 ký tự)
                String[] qWords = unaccentQuery.replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
                int wordHits = 0;
                for (String w : qWords) {
                    if (w.length() >= 3 && !w.equals("cuon") && !w.equals("sach") && !w.equals("khong") && !w.equals("chua") && !w.equals("con") && !w.equals("shop")) {
                        if (unaccentTitle.contains(w)) {
                            wordHits++;
                        }
                    }
                }
                if (wordHits >= 2 || (wordHits >= 1 && mainTitle.length() <= 8)) {
                    score += wordHits * 3;
                }

                if (score > bestScore) {
                    bestScore = score;
                    targetBook = b;
                }
            }
        }

        if (targetBook != null) {
            Optional<Inventory> invOpt = inventoryRepository.findByStoreIdAndBookId(store.getId(), targetBook.getId());
            int stock = invOpt.map(Inventory::getQuantity).orElse(0);
            int cond = targetBook.getConditionPercent() != null ? targetBook.getConditionPercent() : 90;
            String priceStr = String.format("%,d", targetBook.getPrice().longValue()).replace(',', '.') + "₫";
            String authorName = targetBook.getAuthor() != null ? targetBook.getAuthor() : "Tác giả nổi tiếng";

            if (stock > 0) {
                String stockMsg = "Dạ cuốn sách **\"" + targetBook.getTitle() + "\"** (" + authorName + ") hiện **ĐANG CÓ SẴN** tại " + store.getStoreName() + " ạ!\n" +
                        "• Tồn kho quầy: **" + stock + " cuốn** trên kệ.\n" +
                        "• Tình trạng: Độ mới **" + cond + "%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n" +
                        "• Giá bán cũ: **" + priceStr + "**.\n" +
                        "Bạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨";
                result.put("reply", stockMsg);
                result.put("needsStaff", false);
                result.put("suggestedBook", targetBook);
                return result;
            } else {
                // Kiểm tra xem các chi nhánh khác có còn hàng không
                List<Inventory> otherInvs = inventoryRepository.findByBookIdWithStore(targetBook.getId());
                List<String> availableBranches = new ArrayList<>();
                for (Inventory oi : otherInvs) {
                    if (oi.getQuantity() != null && oi.getQuantity() > 0 && oi.getStore() != null && !oi.getStore().getId().equals(store.getId())) {
                        availableBranches.add("• " + oi.getStore().getStoreName() + " (còn " + oi.getQuantity() + " cuốn)");
                    }
                }

                StringBuilder outMsg = new StringBuilder("Dạ cuốn sách **\"" + targetBook.getTitle() + "\"** tại quầy " + store.getStoreName() + " vừa tạm hết trên kệ ạ.\n");
                if (!availableBranches.isEmpty()) {
                    outMsg.append("Tuy nhiên, hệ thống ReBook kiểm tra thấy vẫn còn tại:\n");
                    for (String bInfo : availableBranches) {
                        outMsg.append(bInfo).append("\n");
                    }
                    outMsg.append("Bạn có thể đặt trực tiếp trên web để chuỗi điều chuyển giao nhanh 2-4h nhé!");
                } else {
                    outMsg.append("Hiện tại toàn bộ chuỗi đang tạm hết tựa sách này. Bạn có thể để lại số điện thoại hoặc bấm nút [Gặp nhân viên quầy] để khi sách về chi nhánh sẽ báo bạn ngay nhé!");
                }
                result.put("reply", outMsg.toString());
                result.put("needsStaff", false);
                result.put("suggestedBook", targetBook);
                return result;
            }
        }

        // 8. HỎI GỢI Ý THEO CHỦ ĐỀ / THỂ LOẠI
        boolean isRecommendation = unaccentQuery.contains("goi y") || unaccentQuery.contains("sach hay") || unaccentQuery.contains("nen doc")
                || unaccentQuery.contains("tam ly") || unaccentQuery.contains("kinh te") || unaccentQuery.contains("van hoc")
                || unaccentQuery.contains("cntt") || unaccentQuery.contains("lap trinh") || unaccentQuery.contains("ky nang")
                || unaccentQuery.contains("ngoai ngu") || unaccentQuery.contains("lich su") || unaccentQuery.contains("tieu thuyet");

        if (isRecommendation) {
            List<Inventory> storeInvs = inventoryRepository.findByStoreIdWithBooks(store.getId());
            StringBuilder suggMsg = new StringBuilder("Dạ tại quầy **" + store.getStoreName() + "** đang có các đầu sách nổi bật được bạn đọc yêu thích:\n");
            int count = 0;
            Book topBook = null;
            for (Inventory inv : storeInvs) {
                if (inv.getQuantity() > 0 && inv.getBook() != null) {
                    if (topBook == null) topBook = inv.getBook();
                    suggMsg.append("📖 **").append(inv.getBook().getTitle()).append("** - ")
                            .append(String.format("%,d", inv.getBook().getPrice().longValue()).replace(',', '.')).append("₫")
                            .append(" (mới ").append(inv.getBook().getConditionPercent() != null ? inv.getBook().getConditionPercent() : 90).append("%)\n");
                    count++;
                    if (count >= 3) break;
                }
            }
            suggMsg.append("\nBạn có thể nhắn tên tựa sách cụ thể để em kiểm tra chi tiết độ mới và tồn kho kệ sách nhé! ✨");
            result.put("reply", suggMsg.toString());
            result.put("needsStaff", false);
            if (topBook != null) {
                result.put("suggestedBook", topBook);
            }
            return result;
        }

        // 9. CÂU HỎI CHUNG / CHƯA RÕ NGHĨA -> PHẢN HỒI THÔNG MINH, TỰ NHIÊN
        String defaultMsg = "Dạ hiện tại em chưa tìm thấy sách khớp chính xác với từ khóa **\"" + userText + "\"** tại quầy " + store.getStoreName() + ".\n\n" +
                "Bạn có thể thử:\n" +
                "1. Nhắn tên tựa sách cụ thể (ví dụ: *Đắc Nhân Tâm*, *Nhà Giả Kim*, *Hoàng Tử Bé*...)\n" +
                "2. Nhắn thể loại bạn quan tâm (ví dụ: *sách kinh tế*, *sách kỹ năng*, *văn học*...)\n" +
                "3. Hoặc bấm nút **[👨‍💼 Gặp nhân viên quầy]** bên dưới để Quản lý chi nhánh hỗ trợ tra cứu kho sách giúp mình nhé! 📖✨";
        result.put("reply", defaultMsg);
        result.put("needsStaff", false);
        return result;
    }

    /**
     * Trang Kênh Quản Trị: Quản lý Chat của Chi Nhánh (Dành riêng cho Quản lý chi nhánh - ROLE_MANAGER)
     * Admin hệ thống được chuyển hướng vì chat trực tiếp thuộc thẩm quyền từng cửa hàng.
     */
    @GetMapping("/admin/chat")
    public String adminChatView(
            @RequestParam(value = "userId", required = false) Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect,
            Model model) {

        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (isAdmin) {
            redirect.addFlashAttribute("errorMessage", "Kênh tin nhắn trực tiếp do Quản lý từng chi nhánh cửa hàng phụ trách. Admin hệ thống chỉ quản lý cấu hình chuỗi và báo cáo tổng quan.");
            return "redirect:/admin";
        }

        // Quản lý chi nhánh: Chỉ được xem hội thoại thuộc chi nhánh mình phụ trách
        Store managedStore = (userDetails != null && userDetails.getId() != null)
                ? storeRepository.findByManagerId(userDetails.getId()).stream().findFirst().orElse(null)
                : null;

        if (managedStore == null) {
            model.addAttribute("stores", Collections.emptyList());
            model.addAttribute("currentStore", null);
            model.addAttribute("currentStoreId", null);
            model.addAttribute("customers", Collections.emptyList());
            model.addAttribute("activeCustomer", null);
            model.addAttribute("conversation", Collections.emptyList());
            model.addAttribute("needAttentionUserIds", Collections.emptyList());
            model.addAttribute("activeCustomerNeedsAttention", false);
            model.addAttribute("pageTitle", "Kênh Tin Nhắn Chi Nhánh");
            return "admin/chat/index";
        }

        Long activeStoreId = managedStore.getId();
        Store currentStore = managedStore;
        List<User> customers = chatMessageRepository.findDistinctUsersByStoreId(activeStoreId);

        User activeCustomer = null;
        List<ChatMessage> conversation = Collections.emptyList();

        if (targetUserId != null) {
            activeCustomer = userRepository.findById(targetUserId).orElse(null);
        } else if (!customers.isEmpty()) {
            activeCustomer = customers.get(0);
        }

        if (activeCustomer != null) {
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

        model.addAttribute("stores", List.of(managedStore));
        model.addAttribute("currentStore", currentStore);
        model.addAttribute("currentManagedStore", currentStore);
        model.addAttribute("currentStoreId", activeStoreId);
        model.addAttribute("customers", customers);
        model.addAttribute("activeCustomer", activeCustomer);
        model.addAttribute("conversation", conversation);
        model.addAttribute("needAttentionUserIds", needAttentionUserIds);
        model.addAttribute("activeCustomerNeedsAttention", activeCustomerNeedsAttention);
        model.addAttribute("pageTitle", "Kênh Tin Nhắn — " + currentStore.getStoreName());

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

        // Kiểm tra quyền: Manager chỉ được trả lời tin nhắn của chi nhánh mình
        if (userDetails != null && "ROLE_MANAGER".equals(userDetails.getRoleName())) {
            List<Store> managerStores = storeRepository.findByManagerId(userDetails.getId());
            boolean ownsStore = managerStores.stream().anyMatch(s -> s.getId().equals(storeId));
            if (!ownsStore) {
                res.put("success", false);
                res.put("message", "Bạn không có quyền trả lời tin nhắn của chi nhánh khác!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
        }

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

    /**
     * API: Quản lý bật/tắt chế độ AI trả lời tự động thay nhân viên
     */
    @PostMapping("/api/chat/store/{storeId}/toggle-ai")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleAiReply(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> res = new HashMap<>();
        if (userDetails != null && "ROLE_MANAGER".equals(userDetails.getRoleName())) {
            List<Store> managerStores = storeRepository.findByManagerId(userDetails.getId());
            boolean ownsStore = managerStores.stream().anyMatch(s -> s.getId().equals(storeId));
            if (!ownsStore) {
                res.put("success", false);
                res.put("message", "Bạn không có quyền quản lý chi nhánh này!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
        }
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            res.put("success", false);
            res.put("message", "Chi nhánh không tồn tại.");
            return ResponseEntity.badRequest().body(res);
        }
        boolean newState = !Boolean.TRUE.equals(store.getAiReplyEnabled());
        store.setAiReplyEnabled(newState);
        storeRepository.save(store);

        res.put("success", true);
        res.put("aiReplyEnabled", newState);
        res.put("message", newState ? "Đã bật chế độ Trợ lý AI trả lời tự động." : "Đã chuyển sang chế độ Nhân viên trực quầy.");
        return ResponseEntity.ok(res);
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
        Map<String, Object> res = new HashMap<>();

        if (currentUser == null || store == null) {
            res.put("success", false);
            return ResponseEntity.badRequest().body(res);
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
        noticeMsg.setContent("Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy " + store.getStoreName() + " sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨");
        noticeMsg.setNeedsAttention(true);
        chatMessageRepository.save(noticeMsg);

        res.put("success", true);
        res.put("reply", noticeMsg.getContent());
        res.put("senderName", noticeMsg.getSenderName());
        res.put("createdAt", noticeMsg.getCreatedAt().format(TIME_FMT));
        return ResponseEntity.ok(res);
    }

    /**
     * API: Nạp hội thoại thời gian thực cho trang Quản trị chi nhánh (Live Polling 3s)
     */
    @GetMapping("/api/chat/staff/conversation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStaffConversation(
            @RequestParam("storeId") Long storeId,
            @RequestParam("userId") Long customerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> res = new HashMap<>();
        if (userDetails != null && "ROLE_MANAGER".equals(userDetails.getRoleName())) {
            List<Store> managerStores = storeRepository.findByManagerId(userDetails.getId());
            boolean ownsStore = managerStores.stream().anyMatch(s -> s.getId().equals(storeId));
            if (!ownsStore) {
                res.put("success", false);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
        }
        touchStorePresence(storeId);
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
        res.put("success", true);
        res.put("messages", dtos);
        return ResponseEntity.ok(res);
    }

    /**
     * API: Heartbeat ghi nhận Quản lý chi nhánh đang mở tab chat
     */
    @PostMapping("/api/chat/staff/heartbeat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> staffHeartbeat(
            @RequestParam("storeId") Long storeId) {
        touchStorePresence(storeId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
