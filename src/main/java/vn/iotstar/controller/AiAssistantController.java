package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * ============================================================================
 * CONTROLLER: TRỢ LÝ AI TƯ VẤN SÁCH CŨ THÔNG MINH (AI ASSISTANT)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đối tác kiểm tra: Cường (24162013) - Nhánh: feature/admin-cuong
 *
 * Ý nghĩa nghiệp vụ:
 * 1. Đóng vai chuyên viên tư vấn sách cũ am hiểu văn hóa đọc Sài Gòn.
 * 2. Phân tích ngữ cảnh câu hỏi của khách hàng:
 *    - Tư vấn sách theo cảm xúc (buồn, stress, cần chữa lành, khởi nghiệp, học tập).
 *    - Lọc sách theo ngân sách sinh viên (dưới 50k, dưới 70k).
 *    - Tra cứu tồn kho thời gian thực tại 5 chi nhánh TP.HCM khi khách hỏi vị trí sách.
 *    - Giải thích các tiêu chuẩn thẩm định độ mới % và chính sách đồng kiểm chuỗi.
 * 3. Trả về câu trả lời kèm thẻ Card sách tương tác (bìa, giá, độ mới, nút mua ngay).
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
     * POST /ai-assistant/chat: Xử lý tin nhắn hỏi đáp tư vấn sách cũ từ khách hàng
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> payload) {
        String userMsg = payload.getOrDefault("message", "").trim();
        Map<String, Object> resp = new HashMap<>();

        if (userMsg.isEmpty()) {
            resp.put("reply", "Dạ chào bạn! Em là Trợ lý AI của Chuỗi Sách Cũ TP.HCM. Em có thể giúp bạn tìm sách, tư vấn theo cảm xúc, kiểm tra tồn kho tại 5 chi nhánh hoặc giải thích chính sách kiểm định sách cũ ạ!");
            resp.put("suggestedBooks", Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        List<Map<String, Object>> suggestedBooks = new ArrayList<>();

        // ƯU TIÊN 1: SỬ DỤNG MÔ HÌNH NGÔN NGỮ LỚN TỪ OPENROUTER NẾU ĐÃ CẤU HÌNH API KEY
        if (openRouterService != null && openRouterService.isConfigured()) {
            List<Book> activeBooks = bookRepository.findAllActiveWithImages();
            StringBuilder catalogContext = new StringBuilder();
            for (Book b : activeBooks) {
                catalogContext.append("- ID ").append(b.getId())
                        .append(": \"").append(b.getTitle()).append("\"")
                        .append(" | Tác giả: ").append(b.getAuthor() != null ? b.getAuthor() : "Khác")
                        .append(" | Giá: ₫").append(b.getPrice() != null ? b.getPrice().longValue() : 0)
                        .append(" | Độ mới: ").append(b.getConditionPercent() != null ? b.getConditionPercent() : 90).append("%\n");
            }

            String aiReply = openRouterService.generateReply(userMsg, catalogContext.toString());
            if (aiReply != null && !aiReply.isBlank()) {
                // Tự động đính kèm thẻ sách tương tác nếu câu hỏi hoặc câu trả lời đề cập đến tựa sách
                String combined = (userMsg + " " + aiReply).toLowerCase();
                for (Book b : activeBooks) {
                    String cleanTitle = (b.getTitle() != null) ? b.getTitle().toLowerCase().replaceAll("\\(.*?\\)", "").trim() : "";
                    if (!cleanTitle.isEmpty() && cleanTitle.length() >= 3 && combined.contains(cleanTitle)) {
                        boolean exists = suggestedBooks.stream().anyMatch(m -> b.getId().equals(m.get("id")));
                        if (!exists) {
                            suggestedBooks.add(buildBookCardData(b, inventoryRepository.findByBookIdWithStore(b.getId())));
                            if (suggestedBooks.size() >= 3) break;
                        }
                    }
                }

                resp.put("reply", aiReply);
                resp.put("suggestedBooks", suggestedBooks);
                resp.put("provider", "openrouter");
                return ResponseEntity.ok(resp);
            }
        }

        // ƯU TIÊN 2: BỘ PHÂN TÍCH NỘI BỘ & TRA CỨU CSDL THỜI GIAN THỰC (FALLBACK)
        String lower = userMsg.toLowerCase();
        StringBuilder reply = new StringBuilder();

        // 1. TRƯỜNG HỢP: TÌM KIẾM ĐÍCH DANH THEO TỰA SÁCH HOẶC TÁC GIẢ TỪ CSDL
        List<Book> allActiveBooks = bookRepository.findAllActiveWithImages();
        Book directMatch = null;
        for (Book b : allActiveBooks) {
            String rawTitle = (b.getTitle() != null) ? b.getTitle().toLowerCase() : "";
            String cleanTitle = rawTitle.replaceAll("\\(.*?\\)", "").trim();
            String authorLower = (b.getAuthor() != null) ? b.getAuthor().toLowerCase().trim() : "";
            
            // Khớp tựa sách sạch (từ 3 ký tự) hoặc tác giả (từ 4 ký tự)
            if (!cleanTitle.isEmpty() && cleanTitle.length() >= 3 && lower.contains(cleanTitle)) {
                directMatch = b;
                break;
            }
            if (!authorLower.isEmpty() && authorLower.length() >= 4 && lower.contains(authorLower)) {
                directMatch = b;
                break;
            }
        }

        // 2. TRƯỜNG HỢP: TRA CỨU TỒN KHO CHI NHÁNH CỤ THỂ
        if (lower.contains("chi nhánh") || lower.contains("ở đâu") || lower.contains("còn hàng") || lower.contains("kho")) {
            Book matchedBook = (directMatch != null) ? directMatch : null;
            if (matchedBook == null) {
                for (Book b : allActiveBooks) {
                    String cleanTitle = (b.getTitle() != null) ? b.getTitle().toLowerCase().replaceAll("\\(.*?\\)", "").trim() : "";
                    if (!cleanTitle.isEmpty() && lower.contains(cleanTitle)) {
                        matchedBook = b;
                        break;
                    }
                }
            }

            if (matchedBook != null) {
                List<Inventory> invList = inventoryRepository.findByBookIdWithStore(matchedBook.getId());
                reply.append("Dạ em đã kiểm tra tồn kho thời gian thực cho cuốn **\"").append(matchedBook.getTitle() != null ? matchedBook.getTitle() : "Sách").append("\"**:\n\n");
                int totalStock = 0;
                for (Inventory inv : invList) {
                    int qty = (inv.getQuantity() != null) ? inv.getQuantity() : 0;
                    totalStock += qty;
                    String storeName = (inv.getStore() != null && inv.getStore().getStoreName() != null) ? inv.getStore().getStoreName() : "Chi nhánh";
                    String storeAddr = (inv.getStore() != null && inv.getStore().getAddress() != null) ? inv.getStore().getAddress() : "TP.HCM";
                    reply.append("• **").append(storeName).append("**: Còn **")
                         .append(qty).append(" cuốn** (").append(storeAddr).append(")\n");
                }
                reply.append("\n👉 Tổng cộng hệ thống 5 chi nhánh TP.HCM hiện còn **").append(totalStock).append(" cuốn** với độ mới **")
                     .append(matchedBook.getConditionPercent() != null ? matchedBook.getConditionPercent() : 90).append("%**. Bạn có thể bấm **Mua Ngay** bên dưới hoặc ghé chi nhánh gần nhất ạ!");

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
        // 3. TRƯỜNG HỢP: KHÁCH HỎI TRỰC TIẾP TỰA SÁCH CÓ TRONG KHO
        else if (directMatch != null) {
            List<Inventory> invList = inventoryRepository.findByBookIdWithStore(directMatch.getId());
            reply.append("Dạ cuốn **\"").append(directMatch.getTitle()).append("\"** của tác giả **")
                 .append(directMatch.getAuthor() != null ? directMatch.getAuthor() : "Nhiều tác giả")
                 .append("** đang có sẵn tại hệ thống Chuỗi Sách Cũ TP.HCM với thông tin kiểm định:\n\n")
                 .append("• **Tình trạng thẩm định**: Độ mới đạt **").append(directMatch.getConditionPercent() != null ? directMatch.getConditionPercent() : 90).append("%** (gáy tốt, giấy sạch đẹp, đủ 100% trang).\n")
                 .append("• **Giá ưu đãi sinh viên**: **₫").append(directMatch.getPrice() != null ? String.format(java.util.Locale.US, "%,d", directMatch.getPrice().longValue()).replace(',', '.') : "0")
                 .append("** (Tiết kiệm so với giá bìa gốc).\n")
                 .append("• **Chính sách**: Được đồng kiểm tra hàng tận tay trước khi trả tiền.\n\n")
                 .append("Em gửi bạn thông tin sách bên dưới để bạn xem chi tiết hoặc đặt mua nhanh nha!");

            suggestedBooks.add(buildBookCardData(directMatch, invList));
        }
        // 4. TRƯỜNG HỢP: TƯ VẤN SÁCH CHỮA LÀNH, GIẢM STRESS, TÂM LÝ
        else if (lower.contains("chữa lành") || lower.contains("stress") || lower.contains("buồn") || lower.contains("áp lực") || lower.contains("tâm trạng") || lower.contains("bình yên")) {
            reply.append("Dạ em rất hiểu cảm xúc của bạn. Những lúc thấy chênh vênh hay áp lực học tập, công việc, một cuốn sách tĩnh lặng sẽ là liều thuốc dịu dàng nhất. Em xin gợi ý cho bạn 2 cuốn sách cũ được rất nhiều bạn đọc Sài Gòn tìm đọc:\n\n")
                 .append("1. **Yêu Những Điều Không Hoàn Hảo (Đại đức Hae Min)**: Cuốn sách giúp bạn bao dung hơn với chính mình, buông bỏ kỳ vọng quá mức và tìm lại sự bình yên trong tâm trí.\n")
                 .append("2. **Học Cách Mặc Kệ Điểm Yếu**: Cuốn sách giúp bạn giải tỏa nỗi sợ bị đánh giá, tập trung vào điểm mạnh và sống nhẹ nhõm hơn mỗi ngày.\n\n")
                 .append("Cả 2 cuốn đều là bản tuyển chọn độ mới 90-95%, trang sách sạch sẽ, gáy nguyên vẹn ạ!");

            findAndAddBookCard("Yêu Những Điều Không Hoàn Hảo", suggestedBooks);
            findAndAddBookCard("Học Cách Mặc Kệ", suggestedBooks);
        }
        // 5. TRƯỜNG HỢP: TƯ VẤN SÁCH KINH TẾ, KHỞI NGHIỆP, TÀI CHÍNH
        else if (lower.contains("kinh tế") || lower.contains("khởi nghiệp") || lower.contains("làm giàu") || lower.contains("tiền") || lower.contains("tài chính") || lower.contains("kinh doanh")) {
            reply.append("Dạ đối với lĩnh vực kinh doanh, khởi nghiệp và tư duy tài chính, sách cũ là lựa chọn cực kỳ thông minh vì kiến thức kinh điển không bao giờ lỗi thời mà giá lại tiết kiệm đến 50%:\n\n")
                 .append("• **Nghĩ Giàu Và Làm Giàu (Napoleon Hill)**: Kim chỉ nam tư duy làm giàu kinh điển thế giới.\n")
                 .append("• **Tư Duy Nhanh Và Chậm (Daniel Kahneman)**: Hiểu rõ tâm lý ra quyết định và bẫy sai lầm trong đầu tư.\n")
                 .append("• **Khởi Nghiệp Tinh Gọn (Eric Ries)**: Phương pháp thực chiến giảm thiểu rủi ro cho startup.\n\n")
                 .append("Dưới đây là các bản sách cũ đang có sẵn tại các chi nhánh TP.HCM để bạn tham khảo:");

            findAndAddBookCard("Nghĩ Giàu", suggestedBooks);
            findAndAddBookCard("Tư Duy Nhanh", suggestedBooks);
        }
        // 6. TRƯỜNG HỢP: SÁCH CÔNG NGHỆ THÔNG TIN, LẬP TRÌNH, SINH VIÊN HCMUTE
        else if (lower.contains("công nghệ") || lower.contains("cntt") || lower.contains("lập trình") || lower.contains("java") || lower.contains("spring") || lower.contains("it") || lower.contains("giáo trình")) {
            reply.append("Dạ chào bạn sinh viên! Sách chuyên ngành CNTT và công nghệ tại chuỗi luôn được trợ giá đặc biệt cho sinh viên HCMUTE và Làng ĐHQG:\n\n")
                 .append("• **Clean Code (Mã Sạch - Robert C. Martin)**: Cuốn sách gối đầu giường của mọi lập trình viên chuyên nghiệp.\n")
                 .append("• **Lập Trình Web Java & Spring Boot**: Tài liệu thực chiến bám sát chương trình đồ án môn học.\n\n")
                 .append("Tất cả sách kỹ thuật đều được giữ gìn cẩn thận, không mất trang, hỗ trợ đổi trả 7 ngày nếu không phù hợp ạ!");

            findAndAddBookCard("Clean Code", suggestedBooks);
            findAndAddBookCard("Lập Trình", suggestedBooks);
        }
        // 7. TRƯỜNG HỢP: TÁC GIẢ NGUYỄN NHẬT ÁNH / VĂN HỌC TUỔI THƠ
        else if (lower.contains("nguyễn nhật ánh") || lower.contains("tuổi thơ") || lower.contains("kính vạn hoa") || lower.contains("mắt biếc")) {
            reply.append("Dạ tác giả **Nguyễn Nhật Ánh** luôn là một trong những mảng sách cũ được yêu thích nhất tại chuỗi! Các tác phẩm mang lại cảm giác hoài niệm, ấm áp và trong trẻo của tuổi học trò:\n\n")
                 .append("Em đã tìm thấy các tựa sách Nguyễn Nhật Ánh có sẵn độ mới cao dưới đây:");

            findAndAddBookCard("Kính Vạn Hoa", suggestedBooks);
            findAndAddBookCard("Nguyễn Nhật Ánh", suggestedBooks);
            if (suggestedBooks.isEmpty()) {
                findAndAddBookCard("Cho Tôi Xin Một Vé", suggestedBooks);
            }
        }
        // 8. TRƯỜNG HỢP: TƯ VẤN THEO NGÂN SÁCH SINH VIÊN (DƯỚI 50K / DƯỚI 70K / RẺ)
        else if (lower.contains("dưới 50") || lower.contains("dưới 70") || lower.contains("giá rẻ") || lower.contains("tiết kiệm") || lower.contains("ngân sách")) {
            reply.append("Dạ thấu hiểu ngân sách sinh viên, chuỗi có khu vực **Sách Đồng Giá Tuyển Chọn** từ 29.000₫ đến 68.000₫ nhưng độ mới vẫn đảm bảo trên 90%:\n\n")
                 .append("Em đã lọc ra những cuốn sách bán chạy nhất có giá hạt dẻ dưới đây để bạn tham khảo nha. Đơn từ 150K còn được Freeship nội thành nữa đó ạ!");

            List<Book> cheapBooks = bookRepository.findAllActiveWithImages().stream()
                    .filter(b -> b.getPrice() != null && b.getPrice().compareTo(BigDecimal.valueOf(70000)) <= 0)
                    .limit(3)
                    .toList();
            for (Book b : cheapBooks) {
                suggestedBooks.add(buildBookCardData(b, inventoryRepository.findByBookIdWithStore(b.getId())));
            }
        }
        // 9. TRƯỜNG HỢP: HỎI VỀ PHÍ SHIP, VẬN CHUYỂN, GIAO HÀNG
        else if (lower.contains("ship") || lower.contains("vận chuyển") || lower.contains("giao hàng") || lower.contains("bao lâu")) {
            reply.append("Dạ chính sách vận chuyển của Chuỗi Sách Cũ TP.HCM rất linh hoạt:\n\n")
                 .append("• **Nhận tại cửa hàng (Click & Collect)**: **Miễn phí 100% tiền ship** tại bất kỳ chi nhánh nào trong 5 shop.\n")
                 .append("• **Giao hàng tận nơi (Express)**: **Miễn phí vận chuyển (Freeship)** cho đơn hàng từ **150.000₫** trở lên. Đơn dưới 150K phí ship chỉ từ ₫15.000 - ₫25.000 tuỳ khu vực.\n")
                 .append("• **Thời gian giao hàng**:\n")
                 .append("   - Nội thành TP.HCM: Giao nhanh từ **1 - 2 ngày**.\n")
                 .append("   - Các tỉnh lân cận: Từ **2 - 4 ngày** làm việc.\n\n")
                 .append("Đặc biệt: Tất cả đơn hàng đều được **đồng kiểm 100%** (xem sách rồi mới trả tiền cho shipper)!");
        }
        // 10. TRƯỜNG HỢP: HỎI VỀ PHƯƠNG THỨC THANH TOÁN (COD, VNPAY)
        else if (lower.contains("thanh toán") || lower.contains("vnpay") || lower.contains("cod") || lower.contains("chuyển khoản") || lower.contains("tiền mặt")) {
            reply.append("Dạ hệ thống hiện hỗ trợ 2 hình thức thanh toán cực kỳ tiện lợi và bảo mật:\n\n")
                 .append("1. **Thanh toán khi nhận hàng (COD)**: Nhận sách tại nhà, kiểm tra chất lượng giấy, gáy sách rồi mới thanh toán tiền mặt cho shipper.\n")
                 .append("2. **Thanh toán trực tuyến VNPay**: Quét mã VNPAY-QR, chuyển khoản Internet Banking hoặc dùng thẻ ATM nội địa / Visa / Mastercard an toàn.\n\n")
                 .append("Ngoài ra bạn còn có thể áp dụng thêm **Shopee Voucher** và **Điểm tích lũy sách cũ** để giảm thêm tiền khi thanh toán nữa nhé!");
        }
        // 11. TRƯỜNG HỢP: HỎI VỀ KÝ GỬI & BÁN LẠI SÁCH CŨ
        else if (lower.contains("ký gửi") || lower.contains("bán sách") || lower.contains("thanh lý") || lower.contains("thu mua")) {
            reply.append("Dạ bạn có sách cũ ở nhà không còn đọc muốn gửi chuỗi thanh lý hộ? Tụi em hỗ trợ quy trình ký gửi 3 bước rất nhanh gọn:\n\n")
                 .append("1. **Đăng ký online**: Vào mục **[Ký Gửi Sách Cũ](/consignments)**, nhập tên sách, độ mới % và tải ảnh bìa/ruột sách.\n")
                 .append("2. **Thẩm định giá**: Hệ thống gợi ý mức giá bán tối ưu. Bạn có thể chọn nhận **tiền mặt hoa hồng** hoặc đổi lấy **Voucher mua sách mới**.\n")
                 .append("3. **Gửi sách**: Mang sách ghé chi nhánh gần nhất trong 5 cửa hàng TP.HCM để nhân viên tiếp nhận lên kệ.\n\n")
                 .append("👉 Bạn có thể nhấn vào menu **Ký Gửi Sách Cũ** trên thanh điều hướng để tạo phiếu ngay nhé!");
        }
        // 12. TRƯỜNG HỢP: HỎI VỀ KIỂM ĐỊNH ĐỘ MỚI % VÀ ĐỔI TRẢ
        else if (lower.contains("kiểm định") || lower.contains("độ mới") || lower.contains("chất lượng") || lower.contains("đổi trả") || lower.contains("bảo hành")) {
            reply.append("Dạ chuỗi áp dụng quy trình kiểm định 4 cấp độ nghiêm ngặt cho từng cuốn sách cũ trước khi lên kệ:\n\n")
                 .append("• **95% - 99% Mới (Như mới)**: Sách gần như chưa đọc, bìa bóng láng, gáy phẳng phiu, không tì vết.\n")
                 .append("• **90% - 94% Mới (Rất tốt)**: Bìa mép hơi sờn nhẹ tự nhiên, ruột sách trắng sạch, không viết vẽ, không ố vàng.\n")
                 .append("• **85% - 89% Mới (Tốt)**: Giấy ngả màu thời gian nhẹ, gáy chắc chắn, đầy đủ 100% trang chữ không long tróc.\n\n")
                 .append("🛡️ **Cam kết độc quyền chuỗi sách cũ TP.HCM:**\n")
                 .append("1. **Đồng kiểm COD 100%**: Khách mở gói hàng xem trực tiếp bìa và ruột sách trước khi trả tiền.\n")
                 .append("2. **Đổi trả 7 ngày miễn phí** tại bất kỳ chi nhánh nào trong 5 shop nếu sách không đúng mô tả.");
        }
        // 13. CÂU HỎI CHUNG / TỰ DO
        else {
            reply.append("Dạ em đã nhận được câu hỏi của bạn. Tại Chuỗi Cửa Hàng Sách Cũ TP.HCM, tụi em hiện có hơn 10.000+ đầu sách thuộc các thể loại Văn học, Kỹ năng, Kinh tế, Công nghệ thông tin và Ngoại ngữ.\n\n")
                 .append("Dưới đây là một số tựa sách kinh điển đang được bạn đọc săn đón nhiều nhất tại hệ thống 5 chi nhánh. Bạn có thể bấm vào để xem chi tiết tình trạng sách và đặt mua nhé!");

            List<Book> featured = bookRepository.findTopSoldWithImages(10);
            if (featured.isEmpty()) {
                featured = bookRepository.findAllActiveWithImages();
            }
            for (int i = 0; i < Math.min(featured.size(), 3); i++) {
                Book b = featured.get(i);
                suggestedBooks.add(buildBookCardData(b, inventoryRepository.findByBookIdWithStore(b.getId())));
            }
        }

        resp.put("reply", reply.toString());
        resp.put("suggestedBooks", suggestedBooks);
        return ResponseEntity.ok(resp);
    }

    /**
     * Helper đóng gói thông tin Card sách trả về JSON cho frontend
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
            if (invList.size() > 2) {
                storesStr.append("...");
            }
        } else {
            storesStr.append("Kho TP.HCM");
        }
        card.put("availableStores", storesStr.toString());
        card.put("buyNowUrl", "/checkout?buyNow=true&bookId=" + b.getId() + "&storeId=1&quantity=1");
        card.put("detailUrl", "/books/" + b.getId());

        return card;
    }

    private void findAndAddBookCard(String keyword, List<Map<String, Object>> suggestedBooks) {
        String kw = keyword.toLowerCase().trim();
        List<Book> all = bookRepository.findAllActiveWithImages();
        for (Book b : all) {
            String t = (b.getTitle() != null) ? b.getTitle().toLowerCase() : "";
            String a = (b.getAuthor() != null) ? b.getAuthor().toLowerCase() : "";
            if (t.contains(kw) || a.contains(kw)) {
                boolean exists = suggestedBooks.stream().anyMatch(m -> b.getId().equals(m.get("id")));
                if (!exists) {
                    suggestedBooks.add(buildBookCardData(b, inventoryRepository.findByBookIdWithStore(b.getId())));
                    if (suggestedBooks.size() >= 3) break;
                }
            }
        }
    }
}
