package vn.iotstar.dto;

import java.io.Serializable;

/**
 * DTO chứa thông số cấu hình Trợ lý AI riêng của từng chi nhánh/quầy sách.
 */
public class StoreAiConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long storeId;
    private String storeName;
    private String assistantName;
    private String tone;
    private String shopDescription;
    private String customFaq;
    private Boolean aiReplyEnabled;

    public StoreAiConfigDTO() {}

    public StoreAiConfigDTO(Long storeId, String storeName, String assistantName, String tone,
                            String shopDescription, String customFaq, Boolean aiReplyEnabled) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.assistantName = assistantName;
        this.tone = tone;
        this.shopDescription = shopDescription;
        this.customFaq = customFaq;
        this.aiReplyEnabled = aiReplyEnabled;
    }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getAssistantName() { return assistantName; }
    public void setAssistantName(String assistantName) { this.assistantName = assistantName; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public String getShopDescription() { return shopDescription; }
    public void setShopDescription(String shopDescription) { this.shopDescription = shopDescription; }

    public String getCustomFaq() { return customFaq; }
    public void setCustomFaq(String customFaq) { this.customFaq = customFaq; }

    public Boolean getAiReplyEnabled() { return aiReplyEnabled; }
    public void setAiReplyEnabled(Boolean aiReplyEnabled) { this.aiReplyEnabled = aiReplyEnabled; }
}
