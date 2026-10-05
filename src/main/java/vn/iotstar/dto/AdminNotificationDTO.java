package vn.iotstar.dto;

import java.math.BigDecimal;

/**
 * DTO vận chuyển thông báo thời gian thực qua WebSocket STOMP
 * Hỗ trợ các phân hệ: Ký gửi sách (NEW_CONSIGNMENT), Đơn hàng mới (NEW_ORDER), Điều chuyển kho (STOCK_TRANSFER)
 */
public class AdminNotificationDTO {
    private Long id;
    private String type;            // "NEW_CONSIGNMENT", "NEW_ORDER", "STOCK_TRANSFER"
    private String title;
    private String message;
    private Long storeId;
    private String storeName;
    private String bookTitle;
    private String author;
    private String customerName;
    private BigDecimal proposedPrice;
    private Integer conditionPercent;
    private String targetUrl;
    private String createdAt;

    public AdminNotificationDTO() {}

    public AdminNotificationDTO(Long id, String type, String title, String message, Long storeId,
                                String storeName, String bookTitle, String author, String customerName,
                                BigDecimal proposedPrice, Integer conditionPercent, String targetUrl, String createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.storeId = storeId;
        this.storeName = storeName;
        this.bookTitle = bookTitle;
        this.author = author;
        this.customerName = customerName;
        this.proposedPrice = proposedPrice;
        this.conditionPercent = conditionPercent;
        this.targetUrl = targetUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getProposedPrice() { return proposedPrice; }
    public void setProposedPrice(BigDecimal proposedPrice) { this.proposedPrice = proposedPrice; }

    public Integer getConditionPercent() { return conditionPercent; }
    public void setConditionPercent(Integer conditionPercent) { this.conditionPercent = conditionPercent; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
