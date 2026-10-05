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
 * Ghi chú cho Cường:
 * 1. Tự động kiểm tra API Key từ biến môi trường OPENROUTER_API_KEY hoặc application.properties.
 * 2. Đưa ngữ cảnh danh mục sách (RAG Context) vào System Prompt để AI tư vấn bám sát dữ liệu thật.
 * 3. Nếu không có key hoặc xảy ra lỗi mạng / timeout, tự động trả về null để Controller
 *    kích hoạt cơ chế Fallback (Rule-based & DB Search nội bộ), tuyệt đối không để ứng dụng bị crash.
 */
@Service
public class OpenRouterServiceImpl implements IOpenRouterService {

    @Value("${openrouter.api-key:}")
    private String apiKey;

    @Value("${openrouter.model:google/gemini-2.5-flash-lite}")
    private String modelName;

    @Value("${openrouter.base-url:https://openrouter.ai/api/v1/chat/completions}")
    private String baseUrl;

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
        if (!isConfigured()) {
            return null;
        }

        try {
            String systemPrompt = "Bạn là Trợ lý AI thông minh, am hiểu và nhiệt tình của 'Chuỗi Cửa Hàng Sách Cũ TP.HCM'.\n"
                    + "Đặc điểm của chuỗi:\n"
                    + "- Có 5 chi nhánh tại TP.HCM (Thủ Đức gần HCMUTE, Quận 1 đường sách, Làng ĐHQG, Phú Nhuận, Quận 5).\n"
                    + "- Đảm bảo độ mới từ 80% đến 99%, đồng kiểm COD 100% khi nhận hàng, đổi trả 7 ngày miễn phí.\n"
                    + "- Miễn phí vận chuyển (Freeship) cho đơn hàng từ 150.000₫ trở lên, nhận tại quầy miễn phí 100% ship.\n"
                    + "- Có dịch vụ ký gửi thanh lý sách cũ online nhận hoa hồng hoặc đổi voucher.\n\n"
                    + "Dưới đây là danh mục sách thực tế đang có hàng trong CSDL của chuỗi:\n"
                    + catalogContext + "\n\n"
                    + "Yêu cầu trả lời:\n"
                    + "1. Xưng 'Em' và gọi khách là 'Bạn', văn phong ấm áp, gần gũi, tinh tế chuẩn văn hóa đọc Sài Gòn.\n"
                    + "2. Ưu tiên gợi ý các cuốn sách thực tế có trong danh mục trên kèm giá bán và chi nhánh có hàng.\n"
                    + "3. Sử dụng định dạng Markdown rõ ràng, ngắn gọn, có bullet points nếu liệt kê sách.\n"
                    + "4. Nếu khách hỏi sách không có trong kho, hãy trả lời lịch sự là shop hiện chưa có cuốn đó và gợi ý 1-2 cuốn tương tự đang có sẵn.";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 800);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));
            requestBody.put("messages", messages);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "Chuoi Sach Cu TP.HCM")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    JsonNode messageNode = choices.get(0).path("message");
                    String content = messageNode.path("content").asText();
                    if (content != null && !content.isBlank()) {
                        return content.trim();
                    }
                }
            } else {
                System.err.println("[OpenRouter API] Trả về mã lỗi HTTP: " + response.statusCode() + " - Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[OpenRouter API] Ngoại lệ khi gọi mô hình AI: " + e.getMessage());
        }

        return null; // Fallback sang bộ rule-based nội bộ
    }
}
