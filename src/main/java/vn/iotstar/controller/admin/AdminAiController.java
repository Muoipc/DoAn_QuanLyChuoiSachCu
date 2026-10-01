package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.service.IAiValuationService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller phục vụ các tính năng AI hỗ trợ Quản trị viên & Thủ kho:
 * 1. AI gợi ý định giá sách cũ theo thuật toán độ mới & độ hiếm
 * 2. AI Auto-Writer viết mô tả sách và cam kết chất lượng chuẩn SEO
 */
@RestController
@RequestMapping("/admin/ai")
public class AdminAiController {

    @Autowired
    private IAiValuationService aiValuationService;

    /**
     * POST /admin/ai/suggest-valuation
     * Nhận thông số sách, trả về mức giá đề xuất và phân tích định giá
     */
    @PostMapping("/suggest-valuation")
    public ResponseEntity<?> suggestValuation(@RequestBody Map<String, Object> payload) {
        try {
            String title = (String) payload.getOrDefault("title", "");
            String author = (String) payload.getOrDefault("author", "");
            String categoryName = (String) payload.getOrDefault("categoryName", "");

            BigDecimal originalPrice = null;
            if (payload.get("originalPrice") != null) {
                try {
                    originalPrice = new BigDecimal(payload.get("originalPrice").toString());
                } catch (Exception ignored) {}
            }

            Integer publishYear = null;
            if (payload.get("publishYear") != null) {
                try {
                    publishYear = Integer.parseInt(payload.get("publishYear").toString());
                } catch (Exception ignored) {}
            }

            Integer conditionPercent = null;
            if (payload.get("conditionPercent") != null) {
                try {
                    conditionPercent = Integer.parseInt(payload.get("conditionPercent").toString());
                } catch (Exception ignored) {}
            }

            Map<String, Object> valuation = aiValuationService.suggestBookValuation(
                    title, author, originalPrice, publishYear, conditionPercent, categoryName
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", valuation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi xử lý định giá AI: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * POST /admin/ai/generate-description
     * Tự động sinh mô tả chi tiết, tình trạng sách và cam kết chuỗi
     */
    @PostMapping("/generate-description")
    public ResponseEntity<?> generateDescription(@RequestBody Map<String, Object> payload) {
        try {
            String title = (String) payload.getOrDefault("title", "");
            String author = (String) payload.getOrDefault("author", "");
            String conditionNotes = (String) payload.getOrDefault("conditionNotes", "");
            String categoryName = (String) payload.getOrDefault("categoryName", "");

            Integer conditionPercent = null;
            if (payload.get("conditionPercent") != null) {
                try {
                    conditionPercent = Integer.parseInt(payload.get("conditionPercent").toString());
                } catch (Exception ignored) {}
            }

            Map<String, Object> content = aiValuationService.generateBookDescription(
                    title, author, conditionNotes, conditionPercent, categoryName
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi sinh mô tả AI: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
