package vn.iotstar.dto;

import java.math.BigDecimal;

public class OrderNotificationDTO {
    private String orderCode;
    private String receiverName;
    private BigDecimal finalAmount;
    private String storeName;
    private String deliveryMethod;
    private String createdAt;
    private String type;
    private String message;

    public OrderNotificationDTO() {}

    public OrderNotificationDTO(String orderCode, String receiverName, BigDecimal finalAmount, String storeName, String deliveryMethod, String createdAt, String type, String message) {
        this.orderCode = orderCode;
        this.receiverName = receiverName;
        this.finalAmount = finalAmount;
        this.storeName = storeName;
        this.deliveryMethod = deliveryMethod;
        this.createdAt = createdAt;
        this.type = type;
        this.message = message;
    }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
