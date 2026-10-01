package vn.iotstar.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service định giá và sáng tạo nội dung tự động bằng AI (AI Auto-Writer & Valuation)
 * Phục vụ cho Quản trị viên và Quản lý cửa hàng sách cũ khi nhập sách và thẩm định ký gửi
 */
public interface IAiValuationService {

    /**
     * AI Gợi ý định giá sách cũ:
     * Dựa trên giá bìa gốc, năm xuất bản, độ mới (%), thể loại, và nhu cầu thị trường
     * Trả về khoảng giá bán đề xuất, giá thu mua/ký gửi và bản phân tích lý do định giá.
     */
    Map<String, Object> suggestBookValuation(
            String title,
            String author,
            BigDecimal originalPrice,
            Integer publishYear,
            Integer conditionPercent,
            String categoryName
    );

    /**
     * AI Soạn thảo mô tả sách cũ tự động (Auto-Writer):
     * Sinh đoạn văn giới thiệu hấp dẫn, tình trạng thực tế chi tiết,
     * cam kết chuỗi và gợi ý đối tượng độc giả phù hợp.
     */
    Map<String, Object> generateBookDescription(
            String title,
            String author,
            String conditionNotes,
            Integer conditionPercent,
            String categoryName
    );
}
