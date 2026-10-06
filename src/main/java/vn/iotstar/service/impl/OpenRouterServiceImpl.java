package vn.iotstar.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.iotstar.service.IOpenRouterService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Triển khai dịch vụ kết nối OpenRouter API (OpenAI Compatible) cho Trợ Lý AI.
 *
 * [TỐI ƯU] Các cải tiến so với phiên bản gốc:
 * 1. Conversation History: Hỗ trợ multi-turn chat — AI nhớ lịch sử hội thoại trước đó.
 * 2. Retry Logic: Tự động thử lại tối đa 2 lần nếu API trả về lỗi 5xx hoặc timeout mạng.
 * 3. System Prompt: Được build có cấu trúc rõ ràng, dễ maintain và A/B test.
 * 4. HTTP Timeout: Tổng 20s với retry so với 15s không retry ở phiên bản cũ.
 * 5. Tự động trả về null để Controller kích hoạt Fallback (rule-based) khi lỗi — không crash.
 */
@Service
public class OpenRouterServiceImpl implements IOpenRouterService {

    @Value("${openrouter.api-key:}")
    private String apiKey;

    @Value("${openrouter.model:google/gemini-2.5-flash-lite}")
    private String modelName;

    @Value("${openrouter.base-url:https://openrouter.ai/api/v1/chat/completions}")
    private String baseUrl;

    /** Số lần retry tối đa khi API lỗi 5xx hoặc timeout mạng */
    private static final int MAX_RETRIES = 2;

    /** Giới hạn số tin nhắn lịch sử gửi lên mỗi request để tránh vượt context window */
    private static final int MAX_HISTORY_MESSAGES = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenRouterServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("your_key_here");
    }

    @Override
    public String generateReply(String userMessage, String catalogContext) {
        return generateReplyWithHistory(userMessage, catalogContext, Collections.emptyList());
    }

    /**
     * [TỐI ƯU - Conversation History + Retry Logic]
     * Gửi tin nhắn kèm lịch sử hội thoại đến OpenRouter với cơ chế retry tự động.
     */
    @Override
    public String generateReplyWithHistory(String userMessage, String catalogContext,
                                            List<Map<String, String>> conversationHistory) {
        if (!isConfigured()) {
            return null;
        }

        try {
            String systemPrompt = buildSystemPrompt(catalogContext);
            String jsonPayload = buildRequestPayload(systemPrompt, userMessage, conversationHistory);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "Chuoi Sach Cu TP.HCM")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // [TỐI ƯU] Retry tối đa MAX_RETRIES lần nếu lỗi 5xx hoặc IOException
            for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
                try {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String content = extractContent(response.body());
                        if (content != null && !content.isBlank()) {
                            return content.trim();
                        }
                        return null;
                    }

                    if (response.statusCode() >= 500 && attempt < MAX_RETRIES) {
                        System.err.println("[OpenRouter] Lỗi " + response.statusCode()
                                + ", thử lại lần " + (attempt + 1) + "/" + MAX_RETRIES);
                        Thread.sleep(1000L * (attempt + 1)); // Exponential backoff: 1s, 2s
                        continue;
                    }

                    // Lỗi 4xx — không retry
                    System.err.println("[OpenRouter API] Mã lỗi HTTP: " + response.statusCode()
                            + " — " + response.body());
                    return null;

                } catch (java.io.IOException e) {
                    if (attempt < MAX_RETRIES) {
                        System.err.println("[OpenRouter] Lỗi mạng (attempt " + attempt + "): "
                                + e.getMessage() + " — đang thử lại...");
                        Thread.sleep(1000L * (attempt + 1));
                    } else {
                        System.err.println("[OpenRouter API] Lỗi mạng sau " + MAX_RETRIES
                                + " lần retry: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[OpenRouter API] Ngoại lệ nghiêm trọng: " + e.getMessage());
        }

        return null; // Fallback sang bộ rule-based nội bộ
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * [TỐI ƯU] Build system prompt có cấu trúc rõ ràng — dễ maintain và A/B test.
     * Tách thành 4 phần: nhân vật AI, đặc điểm chuỗi, RAG catalog, yêu cầu trả lời.
     */
    private String buildSystemPrompt(String catalogContext) {
        return "Bạn là Trợ lý AI thông minh, am hiểu và nhiệt tình của 'Chuỗi Cửa Hàng Sách Cũ TP.HCM'.\n\n"

             + "### ĐẶC ĐIỂM CHUỖI:\n"
             + "- Có 5 chi nhánh tại TP.HCM: Thủ Đức (gần HCMUTE), Quận 1 (đường sách), Làng ĐHQG, Phú Nhuận, Quận 5.\n"
             + "- Đảm bảo độ mới từ 80% đến 99%, đồng kiểm COD 100% khi nhận hàng, đổi trả 7 ngày miễn phí.\n"
             + "- Freeship cho đơn từ 150.000₫, nhận tại quầy miễn phí 100% ship.\n"
             + "- Có dịch vụ ký gửi thanh lý sách cũ online — nhận hoa hồng hoặc đổi voucher.\n\n"

             + "### DANH MỤC SÁCH THỰC TẾ (RAG Context — Top bán chạy nhất):\n"
             + catalogContext + "\n\n"

             + "### YÊU CẦU TRẢ LỜI:\n"
             + "1. Chuẩn phong cách Google AI Overview: Cực kỳ súc tích, trực diện (tối đa 2-3 câu hoặc 2 gạch đầu dòng).\n"
             + "2. Không chào hỏi hay rào đón dài dòng. Nêu ngay tựa sách phù hợp và lý do ngắn gọn.\n"
             + "3. Ưu tiên gợi ý sách THỰC TẾ có trong danh mục trên kèm giá bán.\n"
             + "4. Dùng Markdown rõ ràng (**tên sách in đậm**).\n"
             + "5. Nhớ ngữ cảnh câu hỏi của khách.";
    }

    /**
     * [TỐI ƯU] Build request payload kèm conversation history.
     * Chỉ gửi MAX_HISTORY_MESSAGES tin nhắn gần nhất để tránh vượt context window.
     */
    private String buildRequestPayload(String systemPrompt, String userMessage,
                                        List<Map<String, String>> conversationHistory) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        // [TỐI ƯU - QUOTA FALLBACK] Danh sách model fallback tự động nếu model chính hết quota
        requestBody.put("models", List.of(
                modelName,
                "google/gemini-2.0-flash-exp:free",
                "meta-llama/llama-3.3-70b-instruct:free",
                "deepseek/deepseek-r1:free",
                "qwen/qwen-2.5-coder-32b-instruct:free"
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 800);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            int startIdx = Math.max(0, conversationHistory.size() - MAX_HISTORY_MESSAGES);
            messages.addAll(conversationHistory.subList(startIdx, conversationHistory.size()));
        }

        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * Parse chuỗi content từ JSON response của OpenRouter API.
     */
    private String extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText(null);
            }
        } catch (Exception e) {
            System.err.println("[OpenRouter] Lỗi parse response: " + e.getMessage());
        }
        return null;
    }
}
