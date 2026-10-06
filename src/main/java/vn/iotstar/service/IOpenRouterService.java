package vn.iotstar.service;

import java.util.List;
import java.util.Map;

/**
 * Interface dịch vụ giao tiếp với OpenRouter API cho Trợ Lý AI.
 * Phân hệ: Khách hàng (Hoàng Phúc - 24162096).
 */
public interface IOpenRouterService {
    /**
     * Kiểm tra xem API Key của OpenRouter đã được cấu hình hợp lệ chưa.
     */
    boolean isConfigured();

    /**
     * Gửi truy vấn và RAG Context danh mục sách đến OpenRouter (không có lịch sử hội thoại).
     * @param userMessage Tin nhắn từ khách hàng.
     * @param catalogContext Ngữ cảnh danh mục sách và tồn kho từ CSDL.
     * @return Chuỗi phản hồi từ mô hình ngôn ngữ lớn (hoặc null nếu lỗi / chưa cấu hình).
     */
    String generateReply(String userMessage, String catalogContext);

    /**
     * [TỐI ƯU - Conversation History] Gửi truy vấn kèm lịch sử hội thoại để AI nhớ ngữ cảnh.
     * @param userMessage Tin nhắn hiện tại từ khách hàng.
     * @param catalogContext Ngữ cảnh danh mục sách và tồn kho từ CSDL.
     * @param conversationHistory Lịch sử hội thoại, mỗi phần tử có "role" ("user"/"assistant") và "content".
     * @return Chuỗi phản hồi từ mô hình ngôn ngữ lớn (hoặc null nếu lỗi / chưa cấu hình).
     */
    String generateReplyWithHistory(String userMessage, String catalogContext, List<Map<String, String>> conversationHistory);
}
