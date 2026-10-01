package vn.iotstar.service.impl;

import org.springframework.stereotype.Service;
import vn.iotstar.service.IAiValuationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiValuationServiceImpl implements IAiValuationService {

    @Override
    public Map<String, Object> suggestBookValuation(
            String title,
            String author,
            BigDecimal originalPrice,
            Integer publishYear,
            Integer conditionPercent,
            String categoryName
    ) {
        Map<String, Object> result = new HashMap<>();

        // 1. Chuẩn hóa dữ liệu đầu vào
        if (conditionPercent == null || conditionPercent <= 0) {
            conditionPercent = 85;
        }
        int currentYear = Year.now().getValue();
        if (publishYear == null || publishYear <= 0) {
            publishYear = currentYear - 3;
        }

        BigDecimal basePrice = originalPrice;
        boolean estimatedOriginal = false;
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            basePrice = estimateBasePriceByCategory(categoryName);
            estimatedOriginal = true;
        }

        // 2. Tính toán hệ số độ mới (Condition Ratio)
        double conditionRatio;
        String conditionGrade;
        if (conditionPercent >= 95) {
            conditionRatio = 0.75;
            conditionGrade = "95% - 99% (Như mới / Like New)";
        } else if (conditionPercent >= 90) {
            conditionRatio = 0.65;
            conditionGrade = "90% - 94% (Rất tốt / Very Good)";
        } else if (conditionPercent >= 80) {
            conditionRatio = 0.55;
            conditionGrade = "80% - 89% (Tốt / Good)";
        } else if (conditionPercent >= 70) {
            conditionRatio = 0.45;
            conditionGrade = "70% - 79% (Khá / Acceptable)";
        } else {
            conditionRatio = 0.35;
            conditionGrade = "Dưới 70% (Đọc được / Fair)";
        }

        // 3. Hệ số độ tuổi & độ hiếm (Vintage / Rare factor)
        double ageFactor = 1.0;
        int age = currentYear - publishYear;
        String vintageNote = "";

        if (publishYear <= 1990) {
            // Sách trước 1990: Có giá trị sưu tầm cao
            ageFactor = 1.35;
            vintageNote = "Sách cổ/xuất bản trước năm 1990 mang giá trị văn hóa và sưu tầm đặc biệt (+35%).";
        } else if (publishYear <= 2005) {
            ageFactor = 1.15;
            vintageNote = "Ấn bản thâm niên trên 20 năm, bản in cũ ít tái bản (+15%).";
        } else if (age <= 2) {
            ageFactor = 1.10;
            vintageNote = "Ấn bản mới phát hành trong vòng 2 năm gần đây, nội dung còn rất mới (+10%).";
        } else {
            ageFactor = 0.95;
            vintageNote = "Ấn bản phổ thông phát hành cách đây " + age + " năm, giá đã khấu hao tự nhiên.";
        }

        // 4. Hệ số thể loại (Category Demand factor)
        double categoryFactor = 1.0;
        String catLower = (categoryName != null) ? categoryName.toLowerCase() : "";
        if (catLower.contains("kinh tế") || catLower.contains("tài chính") || catLower.contains("khởi nghiệp")) {
            categoryFactor = 1.05;
        } else if (catLower.contains("văn học") || catLower.contains("tiểu thuyết")) {
            categoryFactor = 1.0;
        } else if (catLower.contains("kỹ năng") || catLower.contains("tâm lý")) {
            categoryFactor = 1.05;
        } else if (catLower.contains("giáo trình") || catLower.contains("ngoại ngữ")) {
            categoryFactor = 0.90;
        }

        // 5. Tính giá bán đề xuất (Suggested Selling Price)
        double totalMultiplier = conditionRatio * ageFactor * categoryFactor;
        BigDecimal calculatedPrice = basePrice.multiply(BigDecimal.valueOf(totalMultiplier));
        
        // Làm tròn đến hàng nghìn gần nhất (ví dụ: 47,800 -> 48,000)
        BigDecimal suggestedPrice = roundToThousand(calculatedPrice);
        BigDecimal minPrice = roundToThousand(suggestedPrice.multiply(BigDecimal.valueOf(0.90)));
        BigDecimal maxPrice = roundToThousand(suggestedPrice.multiply(BigDecimal.valueOf(1.10)));

        // Giá thu mua / ký gửi (Consignment buy price): 50% giá bán đề xuất
        BigDecimal consignmentPrice = roundToThousand(suggestedPrice.multiply(BigDecimal.valueOf(0.50)));

        // Phần trăm tiết kiệm so với giá bìa
        int discountPercent = 0;
        if (basePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = basePrice.subtract(suggestedPrice);
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                discountPercent = diff.multiply(BigDecimal.valueOf(100)).divide(basePrice, 0, RoundingMode.HALF_UP).intValue();
            }
        }

        // 6. Xây dựng phân tích rationale
        StringBuilder rationale = new StringBuilder();
        rationale.append("• Phân hạng tình trạng: ").append(conditionGrade).append("\n");
        rationale.append("• ").append(vintageNote).append("\n");
        if (estimatedOriginal) {
            rationale.append("• Giá bìa gốc ước tính: ~").append(String.format("%,d", basePrice.longValue())).append(" đ theo thể loại ").append(categoryName != null ? categoryName : "chung").append(".\n");
        } else {
            rationale.append("• So với giá bìa gốc (").append(String.format("%,d", basePrice.longValue())).append(" đ): Tiết kiệm ").append(discountPercent).append("% cho bạn đọc.\n");
        }
        rationale.append("• Đề xuất giá thu mua / duyệt ký gửi cho khách: ").append(String.format("%,d", consignmentPrice.longValue())).append(" đ (Biên lợi nhuận an toàn 50%).\n");
        rationale.append("• Khuyến nghị niêm yết bán tại chuỗi: ").append(String.format("%,d", suggestedPrice.longValue())).append(" đ (Dải giá cạnh tranh: ")
                .append(String.format("%,d", minPrice.longValue())).append(" đ - ")
                .append(String.format("%,d", maxPrice.longValue())).append(" đ).");

        result.put("suggestedPrice", suggestedPrice);
        result.put("minPrice", minPrice);
        result.put("maxPrice", maxPrice);
        result.put("consignmentPrice", consignmentPrice);
        result.put("discountPercent", discountPercent);
        result.put("conditionGrade", conditionGrade);
        result.put("rationale", rationale.toString());

        return result;
    }

    @Override
    public Map<String, Object> generateBookDescription(
            String title,
            String author,
            String conditionNotes,
            Integer conditionPercent,
            String categoryName
    ) {
        Map<String, Object> result = new HashMap<>();

        if (title == null || title.trim().isEmpty()) {
            title = "Tác phẩm tuyển chọn";
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Nhiều tác giả";
        }
        if (conditionPercent == null || conditionPercent <= 0) {
            conditionPercent = 85;
        }

        String catDisplay = (categoryName != null && !categoryName.trim().isEmpty()) ? categoryName : "Sách chọn lọc";

        // Tóm tắt mở đầu
        String shortSummary = String.format("Ấn bản sách cũ chất lượng cao \"%s\" của tác giả %s thuộc thể loại %s, đã qua quy trình kiểm duyệt và thẩm định thực tế tại Chuỗi Sách Cũ TP.HCM.",
                title, author, catDisplay);

        // Mô tả chi tiết theo định dạng chuẩn
        StringBuilder desc = new StringBuilder();
        desc.append("📖 GIỚI THIỆU TÁC PHẨM:\n");
        desc.append("\"").append(title).append("\" là một trong những tựa sách tiêu biểu thuộc thể loại ")
            .append(catDisplay).append(" của tác giả ").append(author).append(". ")
            .append("Tác phẩm mang lại nhiều giá trị tri thức sâu sắc, góc nhìn thực tế và bài học bổ ích cho bạn đọc trên con đường khám phá tri thức và hoàn thiện bản thân.\n\n");

        desc.append("🔍 KẾT QUẢ THẨM ĐỊNH THỰC TẾ (ĐỘ MỚI ").append(conditionPercent).append("%):\n");
        if (conditionPercent >= 95) {
            desc.append("• Bìa sách: Rất đẹp, không trầy xước, mép sách phẳng phiu như sách mới xuất xưởng.\n");
            desc.append("• Gáy sách: Chắc chắn 100%, keo chỉ hoàn hảo, không có nếp gấp do mở sách mạnh tay.\n");
            desc.append("• Ruột sách: Giấy trắng sáng/ngà tự nhiên, không quăn góc, cam kết không ghi chú hay gạch chân.\n");
        } else if (conditionPercent >= 85) {
            desc.append("• Bìa sách: Sáng đẹp, chỉ có dấu vết thời gian nhẹ khó nhận thấy ở mép viền.\n");
            desc.append("• Gáy sách: Còn rất chắc chắn, giữ form sách chuẩn đẹp.\n");
            desc.append("• Ruột sách: Đầy đủ 100% các trang, giấy sạch sẽ, không rách rời hay ố vàng nặng.\n");
        } else {
            desc.append("• Bìa sách: Có dấu vết đã qua sử dụng, ngả màu vintage hoài niệm theo năm tháng.\n");
            desc.append("• Gáy sách: Đã cố định cẩn thận, mở đọc êm tay và bền bỉ.\n");
            desc.append("• Ruột sách: Giấy ố vàng nhẹ theo thời gian, đảm bảo đầy đủ toàn bộ nội dung, không rách mất chữ.\n");
        }

        if (conditionNotes != null && !conditionNotes.trim().isEmpty()) {
            desc.append("• Ghi chú chuyên viên: ").append(conditionNotes.trim()).append("\n");
        }
        desc.append("\n");

        desc.append("🛡️ CAM KẾT CHẤT LƯỢNG TỪ CHUỖI SÁCH CŨ TP.HCM:\n");
        desc.append("1. Ảnh chụp thật 100%: Hình ảnh hiển thị là ảnh chụp thực tế tại các chi nhánh cửa hàng.\n");
        desc.append("2. Đồng kiểm khi nhận: Quý khách được kiểm tra kỹ gáy sách, ruột sách trước khi thanh toán.\n");
        desc.append("3. Đổi trả miễn phí trong 7 ngày: Nếu phát hiện lỗi in ấn, thiếu trang hoặc khác xa mô tả.\n");
        desc.append("4. Đóng gói bảo vệ 3 lớp: Bọc màng co bảo vệ + xốp chống sốc + hộp carton cứng cáp.\n\n");

        desc.append("💡 ĐỀ XUẤT CHO BẠN ĐỌC:\n");
        desc.append("Thích hợp cho sinh viên, học sinh, người đi làm muốn tiết kiệm chi phí nhưng vẫn sở hữu cuốn sách giá trị; hoặc các bạn độc giả yêu mến cảm giác lật mở những trang sách đã mang dấu ấn thời gian.");

        // Từ khóa SEO
        String seoKeywords = String.format("%s, %s, mua sách cũ %s, sách cũ giá rẻ tphcm, sách cũ %d%%",
                title, author, title, conditionPercent);

        result.put("description", desc.toString());
        result.put("shortSummary", shortSummary);
        result.put("seoKeywords", seoKeywords);

        return result;
    }

    private BigDecimal estimateBasePriceByCategory(String categoryName) {
        if (categoryName == null) return BigDecimal.valueOf(110000);
        String lower = categoryName.toLowerCase();
        if (lower.contains("kinh tế") || lower.contains("tài chính")) return BigDecimal.valueOf(150000);
        if (lower.contains("kỹ năng") || lower.contains("tâm lý")) return BigDecimal.valueOf(120000);
        if (lower.contains("ngoại ngữ") || lower.contains("công nghệ")) return BigDecimal.valueOf(180000);
        if (lower.contains("thiếu nhi") || lower.contains("truyện tranh")) return BigDecimal.valueOf(60000);
        return BigDecimal.valueOf(100000);
    }

    private BigDecimal roundToThousand(BigDecimal amount) {
        long val = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        long rounded = Math.round((double) val / 1000.0) * 1000;
        return BigDecimal.valueOf(Math.max(rounded, 10000));
    }
}
