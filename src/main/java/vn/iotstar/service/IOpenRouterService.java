package vn.iotstar.service;

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
     * Gửi truy vấn và RAG Context danh mục sách đến OpenRouter.
     * @param userMessage Tin nhắn từ khách hàng.
     * @param catalogContext Ngữ cảnh danh mục sách và tồn kho từ CSDL.
     * @return Chuỗi phản hồi từ mô hình ngôn ngữ lớn (hoặc null nếu lỗi / chưa cấu hình).
     */
    String generateReply(String userMessage, String catalogContext);
}
