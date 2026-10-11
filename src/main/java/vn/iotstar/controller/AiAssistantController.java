package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Inventory;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.StoreRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * CONTROLLER: TRỢ LÝ AI TƯ VẤN SÁCH CŨ THÔNG MINH (AI ASSISTANT)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đối tác kiểm tra: Cường (24162013) - Nhánh: feature/admin-cuong
 *
 * [TỐI ƯU] Các cải tiến so với phiên bản gốc:
 * 1. FIX N+1 QUERY: Dùng findByBookIdsWithStore() batch load thay vì gọi DB trong vòng lặp.
 * 2. RAG CONTEXT FILTER: Chỉ gửi top 50 sách bán chạy vào AI, tránh tốn token không cần thiết.
 * 3. CONVERSATION HISTORY: Frontend gửi kèm lịch sử, backend forward vào OpenRouter messages array.
 * 4. CACHE WITHIN REQUEST: Toàn bộ sách và inventory được load 1 lần duy nhất mỗi request chat.
 * 5. FALLBACK PATTERN: Vẫn giữ đầy đủ bộ rule-based khi OpenRouter không khả dụng.
 * ============================================================================
 */
@Controller
@RequestMapping("/ai-assistant")
public class AiAssistantController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private vn.iotstar.service.IOpenRouterService openRouterService;

    /** Số sách tối đa đưa vào RAG Context của AI để tiết kiệm token */
    private static final int RAG_CONTEXT_LIMIT = 50;

    /**
     * GET /ai-assistant: Giao diện Trợ lý AI toàn màn hình
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("stores", storeRepository.findByIsActiveTrue());
        model.addAttribute("isOpenRouterActive", openRouterService != null && openRouterService.isConfigured());
        return "ai-assistant";
    }

    /**
     * POST /ai-assistant/chat: Xử lý tin nhắn hỏi đáp tư vấn sách cũ từ khách hàng.
     *
     * Request body:
     * {
     *   "message": "câu hỏi của user",
     *   "history": [{"role":"user","content":"..."}, {"role":"assistant","content":"..."}]
     * }
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        String userMsg = ((String) payload.getOrDefault("message", "")).trim();
        Map<String, Object> resp = new HashMap<>();

        if (userMsg.isEmpty()) {
            resp.put("reply", "Dạ chào bạn! Em là Trợ lý AI của Chuỗi Sách Cũ TP.HCM. Em có thể giúp bạn tìm sách, tư vấn theo cảm xúc, kiểm tra tồn kho tại 5 chi nhánh hoặc giải thích chính sách kiểm định sách cũ ạ!");
            resp.put("suggestedBooks", Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        // [TỐI ƯU] Lấy lịch sử hội thoại từ request (Conversation History)
        @SuppressWarnings("unchecked")
        List<Map<String, String>> conversationHistory = (List<Map<String, String>>) payload.getOrDefault("history", Collections.emptyList());

        List<Map<String, Object>> suggestedBooks = new ArrayList<>();

        // =====================================================================
        // ƯU TIÊN 1: SỬ DỤNG OPENROUTER AI NẾU ĐÃ CẤU HÌNH API KEY
        // =====================================================================
        if (openRouterService != null && openRouterService.isConfigured()) {
            // [TỐI ƯU - RAG FILTER] Chỉ lấy top 50 sách bán chạy thay vì toàn bộ catalog
            List<Book> ragBooks = bookRepository.findTop50ForAiContext(
                    PageRequest.of(0, RAG_CONTEXT_LIMIT));

            // Bổ sung sách liên quan trực tiếp đến từ khóa user (ưu tiên độ chính xác)
            List<Book> keywordBooks = bookRepository.findActiveByKeywordWithImages(userMsg);
            for (Book kb : keywordBooks) {
                if (ragBooks.stream().noneMatch(b -> b.getId().equals(kb.getId()))) {
                    ragBooks.add(kb);
                }
            }

            // Batch load inventory for RAG books so AI has real-time stock at all 5 branches
            List<Long> allRagBookIds = ragBooks.stream().map(Book::getId).collect(Collectors.toList());
            Map<Long, List<Inventory>> ragInventoryMap = batchLoadInventory(allRagBookIds);

            // Build RAG context string with real-time branch stock
            StringBuilder catalogContext = new StringBuilder();
            for (Book b : ragBooks) {
                List<Inventory> invs = ragInventoryMap.getOrDefault(b.getId(), Collections.emptyList());
                String branchStock = invs.stream()
                        .filter(i -> i.getQuantity() != null && i.getQuantity() > 0 && i.getStore() != null)
                        .map(i -> i.getStore().getStoreName() + " (" + i.getQuantity() + " cuốn)")
                        .collect(Collectors.joining(", "));
                if (branchStock.isEmpty()) {
                    branchStock = "Kho trung tâm TP.HCM";
                }

                catalogContext.append("- ID ").append(b.getId())
                        .append(": \"").append(b.getTitle()).append("\"")
                        .append(" | Tác giả: ").append(b.getAuthor() != null ? b.getAuthor() : "Khác")
                        .append(" | Giá: ₫").append(b.getPrice() != null ? b.getPrice().longValue() : 0)
                        .append(" | Độ mới: ").append(b.getConditionPercent() != null ? b.getConditionPercent() : 90).append("%")
                        .append(" | Tồn kho chi nhánh: ").append(branchStock).append("\n");
            }

            // [TỐI ƯU - CONVERSATION HISTORY] Gửi kèm lịch sử hội thoại
            String aiReply = openRouterService.generateReplyWithHistory(
                    userMsg, catalogContext.toString(), conversationHistory);

            if (aiReply != null && !aiReply.isBlank()) {
                // [TỐI ƯU - BATCH LOAD] Tìm sách được đề cập rồi batch load inventory 1 lần
                String combined = (userMsg + " " + aiReply).toLowerCase();
                List<Book> matchedBooks = ragBooks.stream()
                        .filter(b -> {
                            String cleanTitle = (b.getTitle() != null)
                                    ? b.getTitle().toLowerCase().replaceAll("\\(.*?\\)", "").trim() : "";
                            return cleanTitle.length() >= 3 && combined.contains(cleanTitle);
                        })
                        .limit(3)
                        .collect(Collectors.toList());

                // Nếu chưa match được cuốn nào, thử tìm trong keyword books
                if (matchedBooks.isEmpty()) {
                    for (Book kb : keywordBooks) {
                        String cleanTitle = (kb.getTitle() != null) ? kb.getTitle().toLowerCase().trim() : "";
                        if (cleanTitle.length() >= 3 && combined.contains(cleanTitle)) {
                            matchedBooks.add(kb);
                            if (matchedBooks.size() >= 3) break;
                        }
                    }
                }

                if (!matchedBooks.isEmpty()) {
                    for (Book b : matchedBooks) {
                        suggestedBooks.add(buildBookCardData(b, ragInventoryMap.getOrDefault(b.getId(), Collections.emptyList())));
                    }
                }

                resp.put("reply", aiReply);
                resp.put("suggestedBooks", suggestedBooks);
                resp.put("provider", "openrouter");
                return ResponseEntity.ok(resp);
            }
        }

        // =====================================================================
        // ƯU TIÊN 2: RULE-BASED FALLBACK — TRA CỨU CSDL THỜI GIAN THỰC
        // [TỐI ƯU] Load sách 1 lần, dùng lại cho mọi case phía dưới
        // =====================================================================
        String lower = userMsg.toLowerCase();
        StringBuilder reply = new StringBuilder();

        List<Book> allActiveBooks = bookRepository.findAllActiveWithImages();

        // 1. TÌM KIẾM ĐÍCH DANH THEO TỰA SÁCH HOẶC TÁC GIẢ
        Book directMatch = findDirectMatch(allActiveBooks, lower);

        // 2. TRA CỨU TỒN KHO CHI NHÁNH CỤ THỂ
        if (lower.contains("chi nhánh") || lower.contains("ở đâu") || lower.contains("còn hàng") || lower.contains("kho")) {
            Book matchedBook = (directMatch != null) ? directMatch : findDirectMatch(allActiveBooks, lower);

            if (matchedBook != null) {
                List<Inventory> invList = inventoryRepository.findByBookIdWithStore(matchedBook.getId());
                reply.append("Dạ em đã kiểm tra tồn kho thời gian thực cho cuốn **\"")
                     .append(matchedBook.getTitle() != null ? matchedBook.getTitle() : "Sách").append("\"**:\n\n");
                int totalStock = 0;
                for (Inventory inv : invList) {
                    int qty = (inv.getQuantity() != null) ? inv.getQuantity() : 0;
                    totalStock += qty;
                    String storeName = (inv.getStore() != null && inv.getStore().getStoreName() != null) ? inv.getStore().getStoreName() : "Chi nhánh";
                    String storeAddr = (inv.getStore() != null && inv.getStore().getAddress() != null) ? inv.getStore().getAddress() : "TP.HCM";
                    reply.append("• **").append(storeName).append("**: Còn **")
                         .append(qty).append(" cuốn** (").append(storeAddr).append(")\n");
                }
                reply.append("\n👉 Tổng cộng hệ thống 5 chi nhánh TP.HCM hiện còn **").append(totalStock)
                     .append(" cuốn** với độ mới **")
                     .append(matchedBook.getConditionPercent() != null ? matchedBook.getConditionPercent() : 90)
                     .append("%**. Bạn có thể bấm **Mua Ngay** bên dưới hoặc ghé chi nhánh gần nhất ạ!");

                suggestedBooks.add(buildBookCardData(matchedBook, invList));
            } else {
                reply.append("Dạ hiện tại hệ thống chuỗi có 5 chi nhánh đang phục vụ tại TP.HCM:\n\n")
                     .append("1. **CN Thủ Đức (Trụ sở)**: 48 Võ Văn Ngân, P. Linh Chiểu (Gần ĐH SPKT HCMUTE)\n")
                     .append("2. **CN Quận 1**: Đường Sách Nguyễn Văn Bình, P. Bến Nghé\n")
                     .append("3. **CN Làng ĐHQG**: KĐT ĐHQG, P. Linh Trung, TP. Thủ Đức\n")
                     .append("4. **CN Phú Nhuận**: 120 Trần Huy Liệu, P. 15\n")
                     .append("5. **CN Quận 5**: 280 An Dương Vương, P. 4\n\n")
                     .append("Tất cả chi nhánh đều mở cửa từ **08:00 đến 21:30** mỗi ngày. Bạn cần tìm cuốn sách cụ thể nào, cứ nhắn tên sách cho em kiểm tra ngay nhé!");
            }
        }
        // 3. KHÁCH HỎI TRỰC TIẾP TỰA SÁCH CÓ TRONG KHO
        else if (directMatch != null) {
            List<Inventory> invList = inventoryRepository.findByBookIdWithStore(directMatch.getId());
            reply.append("Dạ cuốn **\"").append(directMatch.getTitle()).append("\"** của tác giả **")
                 .append(directMatch.getAuthor() != null ? directMatch.getAuthor() : "Nhiều tác giả")
                 .append("** đang có sẵn tại hệ thống Chuỗi Sách Cũ TP.HCM với thông tin kiểm định:\n\n")
                 .append("• **Tình trạng thẩm định**: Độ mới đạt **")
                 .append(directMatch.getConditionPercent() != null ? directMatch.getConditionPercent() : 90)
                 .append("%** (gáy tốt, giấy sạch đẹp, đủ 100% trang).\n")
                 .append("• **Giá ưu đãi sinh viên**: **₫")
                 .append(directMatch.getPrice() != null ? String.format(Locale.US, "%,d", directMatch.getPrice().longValue()).replace(',', '.') : "0")
                 .append("** (Tiết kiệm so với giá bìa gốc).\n")
                 .append("• **Chính sách**: Được đồng kiểm tra hàng tận tay trước khi trả tiền.\n\n")
                 .append("Em gửi bạn thông tin sách bên dưới để bạn xem chi tiết hoặc đặt mua nhanh nha!");

            suggestedBooks.add(buildBookCardData(directMatch, invList));
        }
        // 4. TƯ VẤN SÁCH CHỮA LÀNH, GIẢM STRESS, TÂM LÝ
        else if (lower.contains("chữa lành") || lower.contains("stress") || lower.contains("buồn") || lower.contains("áp lực") || lower.contains("tâm trạng") || lower.contains("bình yên")) {
            reply.append("Dạ em rất hiểu cảm xúc của bạn. Những lúc thấy chênh vênh hay áp lực học tập, công việc, một cuốn sách tĩnh lặng sẽ là liều thuốc dịu dàng nhất. Em xin gợi ý cho bạn 2 cuốn sách cũ được rất nhiều bạn đọc Sài Gòn tìm đọc:\n\n")
                 .append("1. **Yêu Những Điều Không Hoàn Hảo (Đại đức Hae Min)**: Cuốn sách giúp bạn bao dung hơn với chính mình, buông bỏ kỳ vọng quá mức và tìm lại sự bình yên trong tâm trí.\n")
                 .append("2. **Học Cách Mặc Kệ Điểm Yếu**: Cuốn sách giúp bạn giải tỏa nỗi sợ bị đánh giá, tập trung vào điểm mạnh và sống nhẹ nhõm hơn mỗi ngày.\n\n")
                 .append("Cả 2 cuốn đều là bản tuyển chọn độ mới 90-95%, trang sách sạch sẽ, gáy nguyên vẹn ạ!");

            findAndAddBookCards(List.of("Yêu Những Điều Không Hoàn Hảo", "Học Cách Mặc Kệ"), allActiveBooks, suggestedBooks);
        }
        // 5. TƯ VẤN SÁCH KINH TẾ, KHỞI NGHIỆP, TÀI CHÍNH
        else if (lower.contains("kinh tế") || lower.contains("khởi nghiệp") || lower.contains("làm giàu") || lower.contains("tiền") || lower.contains("tài chính") || lower.contains("kinh doanh")) {
            reply.append("Dạ đối với lĩnh vực kinh doanh, khởi nghiệp và tư duy tài chính, sách cũ là lựa chọn cực kỳ thông minh vì kiến thức kinh điển không bao giờ lỗi thời mà giá lại tiết kiệm đến 50%:\n\n")
                 .append("• **Nghĩ Giàu Và Làm Giàu (Napoleon Hill)**: Kim chỉ nam tư duy làm giàu kinh điển thế giới.\n")
                 .append("• **Tư Duy Nhanh Và Chậm (Daniel Kahneman)**: Hiểu rõ tâm lý ra quyết định và bẫy sai lầm trong đầu tư.\n")
                 .append("• **Khởi Nghiệp Tinh Gọn (Eric Ries)**: Phương pháp thực chiến giảm thiểu rủi ro cho startup.\n\n")
                 .append("Dưới đây là các bản sách cũ đang có sẵn tại các chi nhánh TP.HCM để bạn tham khảo:");

            findAndAddBookCards(List.of("Nghĩ Giàu", "Tư Duy Nhanh"), allActiveBooks, suggestedBooks);
        }
        // 6. SÁCH CÔNG NGHỆ THÔNG TIN, LẬP TRÌNH, SINH VIÊN HCMUTE
        else if (lower.contains("công nghệ") || lower.contains("cntt") || lower.contains("lập trình") || lower.contains("java") || lower.contains("spring") || lower.contains("giáo trình")) {
            reply.append("Dạ chào bạn sinh viên! Sách chuyên ngành CNTT và công nghệ tại chuỗi luôn được trợ giá đặc biệt cho sinh viên HCMUTE và Làng ĐHQG:\n\n")
                 .append("• **Clean Code (Mã Sạch - Robert C. Martin)**: Cuốn sách gối đầu giường của mọi lập trình viên chuyên nghiệp.\n")
                 .append("• **Lập Trình Web Java & Spring Boot**: Tài liệu thực chiến bám sát chương trình đồ án môn học.\n\n")
                 .append("Tất cả sách kỹ thuật đều được giữ gìn cẩn thận, không mất trang, hỗ trợ đổi trả 7 ngày nếu không phù hợp ạ!");

            findAndAddBookCards(List.of("Clean Code", "Lập Trình"), allActiveBooks, suggestedBooks);
        }
        // 7. TÁC GIẢ NGUYỄN NHẬT ÁNH / VĂN HỌC TUỔI THƠ
        else if (lower.contains("nguyễn nhật ánh") || lower.contains("tuổi thơ") || lower.contains("kính vạn hoa") || lower.contains("mắt biếc")) {
            reply.append("Dạ tác giả **Nguyễn Nhật Ánh** luôn là một trong những mảng sách cũ được yêu thích nhất tại chuỗi! Các tác phẩm mang lại cảm giác hoài niệm, ấm áp và trong trẻo của tuổi học trò:\n\n")
                 .append("Em đã tìm thấy các tựa sách Nguyễn Nhật Ánh có sẵn độ mới cao dưới đây:");

            findAndAddBookCards(List.of("Kính Vạn Hoa", "Nguyễn Nhật Ánh", "Cho Tôi Xin Một Vé"), allActiveBooks, suggestedBooks);
        }
        // 8. TƯ VẤN THEO NGÂN SÁCH SINH VIÊN
        else if (lower.contains("dưới 50") || lower.contains("dưới 70") || lower.contains("giá rẻ") || lower.contains("tiết kiệm") || lower.contains("ngân sách")) {
            reply.append("Dạ thấu hiểu ngân sách sinh viên, chuỗi có khu vực **Sách Đồng Giá Tuyển Chọn** từ 29.000₫ đến 68.000₫ nhưng độ mới vẫn đảm bảo trên 90%:\n\n")
                 .append("Em đã lọc ra những cuốn sách bán chạy nhất có giá hạt dẻ dưới đây để bạn tham khảo nha. Đơn từ 150K còn được Freeship nội thành nữa đó ạ!");

            BigDecimal maxPrice = lower.contains("dưới 50") ? BigDecimal.valueOf(50000) : BigDecimal.valueOf(70000);
            // [TỐI ƯU] Filter từ danh sách đã load — không gọi DB thêm lần nào
            List<Book> cheapBooks = allActiveBooks.stream()
                    .filter(b -> b.getPrice() != null && b.getPrice().compareTo(maxPrice) <= 0)
                    .limit(3)
                    .collect(Collectors.toList());

            if (!cheapBooks.isEmpty()) {
                List<Long> bookIds = cheapBooks.stream().map(Book::getId).collect(Collectors.toList());
                Map<Long, List<Inventory>> inventoryMap = batchLoadInventory(bookIds);
                for (Book b : cheapBooks) {
                    suggestedBooks.add(buildBookCardData(b, inventoryMap.getOrDefault(b.getId(), Collections.emptyList())));
                }
            }
        }
        // 9. PHÍ SHIP, VẬN CHUYỂN, GIAO HÀNG
        else if (lower.contains("ship") || lower.contains("vận chuyển") || lower.contains("giao hàng") || lower.contains("bao lâu")) {
            reply.append("Dạ chính sách vận chuyển của Chuỗi Sách Cũ TP.HCM rất linh hoạt:\n\n")
                 .append("• **Nhận tại cửa hàng (Click & Collect)**: **Miễn phí 100% tiền ship** tại bất kỳ chi nhánh nào trong 5 shop.\n")
                 .append("• **Giao hàng tận nơi (Express)**: **Freeship** cho đơn từ **150.000₫** trở lên. Đơn dưới 150K phí ship chỉ từ ₫15.000 - ₫25.000 tuỳ khu vực.\n")
                 .append("• **Thời gian giao hàng**:\n")
                 .append("   - Nội thành TP.HCM: Giao nhanh từ **1 - 2 ngày**.\n")
                 .append("   - Các tỉnh lân cận: Từ **2 - 4 ngày** làm việc.\n\n")
                 .append("Đặc biệt: Tất cả đơn hàng đều được **đồng kiểm 100%** (xem sách rồi mới trả tiền cho shipper)!");
        }
        // 10. PHƯƠNG THỨC THANH TOÁN
        else if (lower.contains("thanh toán") || lower.contains("vnpay") || lower.contains("cod") || lower.contains("chuyển khoản") || lower.contains("tiền mặt")) {
            reply.append("Dạ hệ thống hiện hỗ trợ 2 hình thức thanh toán cực kỳ tiện lợi và bảo mật:\n\n")
                 .append("1. **Thanh toán khi nhận hàng (COD)**: Nhận sách tại nhà, kiểm tra chất lượng giấy, gáy sách rồi mới thanh toán tiền mặt cho shipper.\n")
                 .append("2. **Thanh toán trực tuyến VNPay**: Quét mã VNPAY-QR, chuyển khoản Internet Banking hoặc dùng thẻ ATM nội địa / Visa / Mastercard an toàn.\n\n")
                 .append("Ngoài ra bạn còn có thể áp dụng thêm **Voucher** và **Điểm tích lũy sách cũ** để giảm thêm tiền khi thanh toán nữa nhé!");
        }
        // 11. KÝ GỬI & BÁN LẠI SÁCH CŨ
        else if (lower.contains("ký gửi") || lower.contains("bán sách") || lower.contains("thanh lý") || lower.contains("thu mua")) {
            reply.append("Dạ bạn có sách cũ ở nhà không còn đọc muốn gửi chuỗi thanh lý hộ? Tụi em hỗ trợ quy trình ký gửi 3 bước rất nhanh gọn:\n\n")
                 .append("1. **Đăng ký online**: Vào mục **[Ký Gửi Sách Cũ](/consignments)**, nhập tên sách, độ mới % và tải ảnh bìa/ruột sách.\n")
                 .append("2. **Thẩm định giá**: Hệ thống gợi ý mức giá bán tối ưu. Bạn có thể chọn nhận **tiền mặt hoa hồng** hoặc đổi lấy **Voucher mua sách mới**.\n")
                 .append("3. **Gửi sách**: Mang sách ghé chi nhánh gần nhất trong 5 cửa hàng TP.HCM để nhân viên tiếp nhận lên kệ.\n\n")
                 .append("👉 Bạn có thể nhấn vào menu **Ký Gửi Sách Cũ** trên thanh điều hướng để tạo phiếu ngay nhé!");
        }
        // 12. KIỂM ĐỊNH ĐỘ MỚI % VÀ ĐỔI TRẢ
        else if (lower.contains("kiểm định") || lower.contains("độ mới") || lower.contains("chất lượng") || lower.contains("đổi trả") || lower.contains("bảo hành")) {
            reply.append("Dạ chuỗi áp dụng quy trình kiểm định 4 cấp độ nghiêm ngặt cho từng cuốn sách cũ trước khi lên kệ:\n\n")
                 .append("• **95% - 99% Mới (Như mới)**: Sách gần như chưa đọc, bìa bóng láng, gáy phẳng phiu, không tì vết.\n")
                 .append("• **90% - 94% Mới (Rất tốt)**: Bìa mép hơi sờn nhẹ tự nhiên, ruột sách trắng sạch, không viết vẽ, không ố vàng.\n")
                 .append("• **85% - 89% Mới (Tốt)**: Giấy ngả màu thời gian nhẹ, gáy chắc chắn, đầy đủ 100% trang chữ không long tróc.\n\n")
                 .append("🛡️ **Cam kết độc quyền chuỗi sách cũ TP.HCM:**\n")
                 .append("1. **Đồng kiểm COD 100%**: Khách mở gói hàng xem trực tiếp bìa và ruột sách trước khi trả tiền.\n")
                 .append("2. **Đổi trả 7 ngày miễn phí** tại bất kỳ chi nhánh nào trong 5 shop nếu sách không đúng mô tả.");
        }
        // 13. TÌM KIẾM THEO TỪ KHÓA TỰ DO TRONG DANH MỤC SÁCH
        else {
            // Lọc các từ khóa có ý nghĩa từ câu hỏi (bỏ từ dừng tiếng Việt phổ biến)
            String cleanQuery = lower.replaceAll("\\b(tìm|sách|cuốn|bán|có|không|ạ|dạ|cho|mình|em|tôi|với|về|nào|những|các|thể|loại|gì|được|ở|tại|giá)\\b", " ")
                                     .replaceAll("\\s+", " ").trim();

            List<Book> keywordMatches = new ArrayList<>();
            if (cleanQuery.length() >= 2) {
                String[] words = cleanQuery.split(" ");
                for (Book b : allActiveBooks) {
                    String titleLower = (b.getTitle() != null) ? b.getTitle().toLowerCase() : "";
                    String authorLower = (b.getAuthor() != null) ? b.getAuthor().toLowerCase() : "";
                    String catLower = (b.getCategory() != null && b.getCategory().getCategoryName() != null) 
                            ? b.getCategory().getCategoryName().toLowerCase() : "";

                    // Khớp nguyên cụm hoặc khớp nhiều từ
                    boolean match = titleLower.contains(cleanQuery) || authorLower.contains(cleanQuery) || catLower.contains(cleanQuery);
                    if (!match && words.length > 1) {
                        int matchedWordCount = 0;
                        for (String w : words) {
                            if (w.length() >= 2 && (titleLower.contains(w) || authorLower.contains(w) || catLower.contains(w))) {
                                matchedWordCount++;
                            }
                        }
                        if (matchedWordCount >= Math.min(2, words.length)) {
                            match = true;
                        }
                    }

                    if (match && keywordMatches.stream().noneMatch(k -> k.getId().equals(b.getId()))) {
                        keywordMatches.add(b);
                        if (keywordMatches.size() >= 3) break;
                    }
                }
            }

            if (!keywordMatches.isEmpty()) {
                reply.append("Dạ em đã tra cứu CSDL hệ thống và tìm thấy các cuốn sách cũ phù hợp với yêu cầu **\"").append(userMsg).append("\"** của bạn:\n\n");
                for (Book b : keywordMatches) {
                    reply.append("• **").append(b.getTitle()).append("**");
                    if (b.getAuthor() != null && !b.getAuthor().isBlank()) {
                        reply.append(" (Tác giả: ").append(b.getAuthor()).append(")");
                    }
                    reply.append(" — Độ mới: **").append(b.getConditionPercent() != null ? b.getConditionPercent() : 90).append("%**");
                    if (b.getPrice() != null) {
                        reply.append(" — Giá: **₫").append(String.format(Locale.US, "%,d", b.getPrice().longValue()).replace(',', '.')).append("**\n");
                    } else {
                        reply.append("\n");
                    }
                }
                reply.append("\nTất cả đều được kiểm định kỹ càng và hỗ trợ xem sách trước khi thanh toán. Em gửi bạn thẻ sách chi tiết bên dưới nhé!");

                List<Long> bookIds = keywordMatches.stream().map(Book::getId).collect(Collectors.toList());
                Map<Long, List<Inventory>> inventoryMap = batchLoadInventory(bookIds);
                for (Book b : keywordMatches) {
                    suggestedBooks.add(buildBookCardData(b, inventoryMap.getOrDefault(b.getId(), Collections.emptyList())));
                }
            } else {
                reply.append("Dạ em đã nhận được câu hỏi của bạn. Tại Chuỗi Cửa Hàng Sách Cũ TP.HCM, tụi em hiện có hàng nghìn đầu sách được kiểm định chất lượng tại 5 chi nhánh.\n\n")
                     .append("Dưới đây là một số tựa sách kinh điển đang được bạn đọc săn đón nhiều nhất tại hệ thống 5 chi nhánh. Bạn có thể bấm vào để xem chi tiết tình trạng sách và đặt mua nhé!");

                List<Book> featured = bookRepository.findTopSoldWithImages(10);
                if (featured.isEmpty()) {
                    featured = allActiveBooks.subList(0, Math.min(3, allActiveBooks.size()));
                }
                List<Book> topBooks = featured.stream().limit(3).collect(Collectors.toList());
                if (!topBooks.isEmpty()) {
                    List<Long> bookIds = topBooks.stream().map(Book::getId).collect(Collectors.toList());
                    Map<Long, List<Inventory>> inventoryMap = batchLoadInventory(bookIds);
                    for (Book b : topBooks) {
                        suggestedBooks.add(buildBookCardData(b, inventoryMap.getOrDefault(b.getId(), Collections.emptyList())));
                    }
                }
            }
        }

        resp.put("reply", reply.toString());
        resp.put("suggestedBooks", suggestedBooks);
        return ResponseEntity.ok(resp);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * [TỐI ƯU - FIX N+1] Batch load inventory cho nhiều sách cùng lúc.
     * Gọi DB 1 lần duy nhất rồi group theo bookId ở tầng ứng dụng.
     */
    private Map<Long, List<Inventory>> batchLoadInventory(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) return Collections.emptyMap();
        List<Inventory> allInventories = inventoryRepository.findByBookIdsWithStore(bookIds);
        return allInventories.stream()
                .collect(Collectors.groupingBy(inv -> inv.getBook().getId()));
    }

    /**
     * Tìm sách khớp trực tiếp theo tên sách hoặc tác giả trong tin nhắn người dùng.
     */
    private Book findDirectMatch(List<Book> books, String lower) {
        for (Book b : books) {
            String rawTitle = (b.getTitle() != null) ? b.getTitle().toLowerCase() : "";
            String cleanTitle = rawTitle.replaceAll("\\(.*?\\)", "").trim();
            String authorLower = (b.getAuthor() != null) ? b.getAuthor().toLowerCase().trim() : "";

            if (!cleanTitle.isEmpty() && cleanTitle.length() >= 3 && lower.contains(cleanTitle)) {
                return b;
            }
            if (!authorLower.isEmpty() && authorLower.length() >= 4 && lower.contains(authorLower)) {
                return b;
            }
        }
        return null;
    }

    /**
     * [TỐI ƯU] Tìm và thêm book cards theo danh sách keywords.
     * Batch load inventory 1 lần cho tất cả sách tìm được.
     */
    private void findAndAddBookCards(List<String> keywords, List<Book> allBooks,
                                      List<Map<String, Object>> suggestedBooks) {
        List<Book> found = new ArrayList<>();
        for (String keyword : keywords) {
            String kw = keyword.toLowerCase().trim();
            for (Book b : allBooks) {
                String t = (b.getTitle() != null) ? b.getTitle().toLowerCase() : "";
                String a = (b.getAuthor() != null) ? b.getAuthor().toLowerCase() : "";
                if ((t.contains(kw) || a.contains(kw))
                        && found.stream().noneMatch(f -> f.getId().equals(b.getId()))
                        && suggestedBooks.stream().noneMatch(m -> b.getId().equals(m.get("id")))) {
                    found.add(b);
                    if (found.size() + suggestedBooks.size() >= 3) break;
                }
            }
            if (found.size() + suggestedBooks.size() >= 3) break;
        }

        if (!found.isEmpty()) {
            List<Long> bookIds = found.stream().map(Book::getId).collect(Collectors.toList());
            Map<Long, List<Inventory>> inventoryMap = batchLoadInventory(bookIds);
            for (Book b : found) {
                suggestedBooks.add(buildBookCardData(b, inventoryMap.getOrDefault(b.getId(), Collections.emptyList())));
            }
        }
    }

    /**
     * Đóng gói thông tin Card sách trả về JSON cho frontend.
     */
    private Map<String, Object> buildBookCardData(Book b, List<Inventory> invList) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", b.getId());
        card.put("title", b.getTitle());
        card.put("author", b.getAuthor());
        card.put("price", b.getPrice());
        card.put("originalPrice", b.getOriginalPrice());
        card.put("conditionPercent", b.getConditionPercent());
        card.put("imageUrl", b.getPrimaryImageUrl() != null ? b.getPrimaryImageUrl() : "/images/books/book_1.jpg");

        StringBuilder storesStr = new StringBuilder();
        if (invList != null && !invList.isEmpty()) {
            for (int i = 0; i < Math.min(invList.size(), 2); i++) {
                if (i > 0) storesStr.append(", ");
                Inventory itemInv = invList.get(i);
                String sName = (itemInv.getStore() != null && itemInv.getStore().getStoreName() != null) ? itemInv.getStore().getStoreName() : "Chi nhánh";
                int sQty = itemInv.getQuantity() != null ? itemInv.getQuantity() : 0;
                storesStr.append(sName).append(" (").append(sQty).append(")");
            }
            if (invList.size() > 2) storesStr.append("...");
        } else {
            storesStr.append("Kho TP.HCM");
        }
        card.put("availableStores", storesStr.toString());
        card.put("buyNowUrl", "/checkout?buyNow=true&bookId=" + b.getId() + "&storeId=1&quantity=1");
        card.put("detailUrl", "/books/" + b.getId());
        return card;
    }
}
