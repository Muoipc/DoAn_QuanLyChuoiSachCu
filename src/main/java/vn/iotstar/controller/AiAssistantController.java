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

    /**
     * GET /ai-assistant: Giao diện Trợ lý AI toàn màn hình
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("stores", storeRepository.findByIsActiveTrue());
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

        String lower = userMsg.toLowerCase();
        List<Map<String, Object>> suggestedBooks = new ArrayList<>();
        StringBuilder reply = new StringBuilder();

        // 1. TRƯỜNG HỢP: TRA CỨU TỒN KHO CHI NHÁNH CỤ THỂ
        if (lower.contains("chi nhánh") || lower.contains("ở đâu") || lower.contains("còn hàng") || lower.contains("kho")) {
            List<Book> allBooks = bookRepository.findAllActiveWithImages();
            Book matchedBook = null;
            for (Book b : allBooks) {
                String cleanTitle = b.getTitle().toLowerCase().replaceAll("\\(.*?\\)", "").trim();
                String authorClean = b.getAuthor().toLowerCase();
                if (lower.contains(cleanTitle) || cleanTitle.contains("đắc nhân tâm") && lower.contains("đắc nhân tâm")
                    || lower.contains(authorClean)) {
                    matchedBook = b;
                    break;
                }
            }

            if (matchedBook != null) {
                List<Inventory> invList = inventoryRepository.findByBookIdWithStore(matchedBook.getId());
                reply.append("Dạ em đã kiểm tra tồn kho thời gian thực cho cuốn **\"").append(matchedBook.getTitle()).append("\"**:\n\n");
                int totalStock = 0;
                for (Inventory inv : invList) {
                    totalStock += inv.getQuantity();
                    reply.append("• **").append(inv.getStore().getStoreName()).append("**: Còn **")
                         .append(inv.getQuantity()).append(" cuốn** (").append(inv.getStore().getAddress()).append(")\n");
                }
                reply.append("\n👉 Tổng cộng hệ thống 5 chi nhánh TP.HCM hiện còn **").append(totalStock).append(" cuốn** với độ mới **")
                     .append(matchedBook.getConditionPercent()).append("%**. Bạn có thể đặt giao tận nơi hoặc chọn lấy trực tiếp tại cửa hàng gần nhất ạ!");

                suggestedBooks.add(buildBookCardData(matchedBook, invList));
            } else {
                reply.append("Dạ hiện tại hệ thống chuỗi có 5 chi nhánh tại TP.HCM:\n")
                     .append("1. **CN Thủ Đức**: 48 Võ Văn Ngân, P. Linh Chiểu (Đối diện HCMUTE)\n")
                     .append("2. **CN Quận 1**: Đường Sách Nguyễn Văn Bình, P. Bến Nghé\n")
                     .append("3. **CN Làng ĐHQG**: KĐT ĐHQG, P. Linh Trung, TP. Thủ Đức\n")
                     .append("4. **CN Phú Nhuận**: 120 Trần Huy Liệu, P. 15\n")
                     .append("5. **CN Quận 5**: 280 An Dương Vương, P. 4\n\n")
                     .append("Bạn muốn kiểm tra cuốn sách nào cụ thể tại các chi nhánh trên, cứ nhắn tên sách cho em nhé!");
            }
        }
        // 2. TRƯỜNG HỢP: TƯ VẤN SÁCH CHỮA LÀNH, GIẢM STRESS, TÂM LÝ
        else if (lower.contains("chữa lành") || lower.contains("stress") || lower.contains("buồn") || lower.contains("áp lực") || lower.contains("tâm trạng") || lower.contains("bình yên")) {
            reply.append("Dạ em rất hiểu cảm xúc của bạn. Những lúc thấy chênh vênh hay áp lực học tập, công việc, một cuốn sách tĩnh lặng sẽ là liều thuốc dịu dàng nhất. Em xin gợi ý cho bạn 2 cuốn sách cũ được rất nhiều bạn đọc Sài Gòn tìm đọc:\n\n")
                 .append("1. **Yêu Những Điều Không Hoàn Hảo (Đại đức Hae Min)**: Cuốn sách giúp bạn bao dung hơn với chính mình, buông bỏ kỳ vọng quá mức và tìm lại sự bình yên trong tâm trí.\n")
                 .append("2. **Học Cách Mặc Kệ Điểm Yếu**: Cuốn sách giúp bạn giải tỏa nỗi sợ bị đánh giá, tập trung vào điểm mạnh và sống nhẹ nhõm hơn mỗi ngày.\n\n")
                 .append("Cả 2 cuốn đều là bản tuyển chọn độ mới 90-95%, trang sách sạch sẽ, gáy nguyên vẹn ạ!");

            findAndAddBookCard("Yêu Những Điều Không Hoàn Hảo", suggestedBooks);
            findAndAddBookCard("Học Cách Mặc Kệ", suggestedBooks);
        }
        // 3. TRƯỜNG HỢP: TƯ VẤN SÁCH KINH TẾ, KHỞI NGHIỆP, TÀI CHÍNH
        else if (lower.contains("kinh tế") || lower.contains("khởi nghiệp") || lower.contains("làm giàu") || lower.contains("tiền") || lower.contains("tài chính") || lower.contains("kinh doanh")) {
            reply.append("Dạ đối với lĩnh vực kinh doanh, khởi nghiệp và tư duy tài chính, sách cũ là lựa chọn cực kỳ thông minh vì kiến thức kinh điển không bao giờ lỗi thời mà giá lại tiết kiệm đến 50%:\n\n")
                 .append("• **Nghĩ Giàu Và Làm Giàu (Napoleon Hill)**: Kim chỉ nam tư duy làm giàu kinh điển thế giới.\n")
                 .append("• **Tư Duy Nhanh Và Chậm (Daniel Kahneman)**: Hiểu rõ tâm lý ra quyết định và bẫy sai lầm trong đầu tư.\n")
                 .append("• **Khởi Nghiệp Tinh Gọn (Eric Ries)**: Phương pháp thực chiến giảm thiểu rủi ro cho startup.\n\n")
                 .append("Dưới đây là các bản sách cũ đang có sẵn tại các chi nhánh TP.HCM để bạn tham khảo:");

            findAndAddBookCard("Nghĩ Giàu", suggestedBooks);
            findAndAddBookCard("Tư Duy Nhanh", suggestedBooks);
        }
        // 4. TRƯỜNG HỢP: SÁCH CÔNG NGHỆ THÔNG TIN, LẬP TRÌNH, SINH VIÊN HCMUTE
        else if (lower.contains("công nghệ") || lower.contains("cntt") || lower.contains("lập trình") || lower.contains("java") || lower.contains("spring") || lower.contains("it") || lower.contains("giáo trình")) {
            reply.append("Dạ chào bạn sinh viên! Sách chuyên ngành CNTT và công nghệ tại chuỗi luôn được trợ giá đặc biệt cho sinh viên HCMUTE và Làng ĐHQG:\n\n")
                 .append("• **Clean Code (Mã Sạch - Robert C. Martin)**: Cuốn sách gối đầu giường của mọi lập trình viên chuyên nghiệp.\n")
                 .append("• **Lập Trình Web Java & Spring Boot**: Tài liệu thực chiến bám sát chương trình đồ án môn học.\n\n")
                 .append("Tất cả sách kỹ thuật đều được giữ gìn cẩn thận, không mất trang, hỗ trợ đổi trả 7 ngày nếu không phù hợp ạ!");

            findAndAddBookCard("Clean Code", suggestedBooks);
            findAndAddBookCard("Lập Trình", suggestedBooks);
        }
        // 5. TRƯỜNG HỢP: TƯ VẤN THEO NGÂN SÁCH SINH VIÊN (DƯỚI 50K / DƯỚI 70K)
        else if (lower.contains("dưới 50") || lower.contains("dưới 70") || lower.contains("giá rẻ") || lower.contains("tiết kiệm") || lower.contains("ngân sách")) {
            reply.append("Dạ thấu hiểu ngân sách sinh viên, chuỗi có khu vực **Sách Đồng Giá Tuyển Chọn** từ 29.000₫ đến 68.000₫ nhưng độ mới vẫn đảm bảo trên 90%:\n\n")
                 .append("Em đã lọc ra những cuốn sách bán chạy nhất có giá hạt dẻ dưới đây để bạn tham khảo nha. Đơn từ 150K còn được Freeship nội thành nữa đó ạ!");

            List<Book> cheapBooks = bookRepository.findAllActiveWithImages().stream()
                    .filter(b -> b.getPrice().compareTo(BigDecimal.valueOf(70000)) <= 0)
                    .limit(3)
                    .toList();
            for (Book b : cheapBooks) {
                suggestedBooks.add(buildBookCardData(b, inventoryRepository.findByBookIdWithStore(b.getId())));
            }
        }
        // 6. TRƯỜNG HỢP: HỎI VỀ KIỂM ĐỊNH ĐỘ MỚI % VÀ ĐỔI TRẢ
        else if (lower.contains("kiểm định") || lower.contains("độ mới") || lower.contains("chất lượng") || lower.contains("đổi trả") || lower.contains("bảo hành")) {
            reply.append("Dạ chuỗi áp dụng quy trình kiểm định 4 cấp độ nghiêm ngặt cho từng cuốn sách cũ trước khi lên kệ:\n\n")
                 .append("• **95% - 99% Mới (Như mới)**: Sách gần như chưa đọc, bìa bóng láng, gáy phẳng phiu, không tì vết.\n")
                 .append("• **90% - 94% Mới (Rất tốt)**: Bìa mép hơi sờn nhẹ tự nhiên, ruột sách trắng sạch, không viết vẽ, không ố vàng.\n")
                 .append("• **85% - 89% Mới (Tốt)**: Giấy ngả màu thời gian nhẹ, gáy chắc chắn, đầy đủ 100% trang chữ không long tróc.\n\n")
                 .append("🛡️ **Cam kết độc quyền chuỗi sách cũ TP.HCM:**\n")
                 .append("1. **Đồng kiểm COD 100%**: Khách mở gói hàng xem trực tiếp bìa và ruột sách trước khi trả tiền.\n")
                 .append("2. **Đổi trả 7 ngày miễn phí** tại bất kỳ chi nhánh nào trong 5 shop nếu sách không đúng mô tả.");
        }
        // 7. CÂU HỎI CHUNG / TỰ DO
        else {
            reply.append("Dạ em đã nhận được câu hỏi của bạn. Tại Chuỗi Cửa Hàng Sách Cũ TP.HCM, tụi em hiện có hơn 10.000+ đầu sách thuộc các thể loại Văn học, Kỹ năng, Kinh tế, Công nghệ thông tin và Ngoại ngữ.\n\n")
                 .append("Dưới đây là một số tựa sách kinh điển đang được bạn đọc săn đón nhiều nhất tại hệ thống 5 chi nhánh. Bạn có thể bấm vào để xem chi tiết tình trạng sách và tồn kho nhé!");

            List<Book> featured = bookRepository.findTopSoldWithImages(10);
            if (featured.isEmpty()) {
                featured = bookRepository.findAllActiveWithImages();
            }
            for (int i = 0; i < Math.min(featured.size(), 2); i++) {
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
                storesStr.append(invList.get(i).getStore().getStoreName()).append(" (").append(invList.get(i).getQuantity()).append(")");
            }
            if (invList.size() > 2) {
                storesStr.append("...");
            }
        } else {
            storesStr.append("Kho TP.HCM");
        }
        card.put("availableStores", storesStr.toString());

        return card;
    }

    private void findAndAddBookCard(String keyword, List<Map<String, Object>> suggestedBooks) {
        List<Book> matches = bookRepository.findByTitleContainingIgnoreCase(keyword);
        if (!matches.isEmpty()) {
            Book b = matches.get(0);
            suggestedBooks.add(buildBookCardData(b, inventoryRepository.findByBookIdWithStore(b.getId())));
        }
    }
}
