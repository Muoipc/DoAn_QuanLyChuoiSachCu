package vn.iotstar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(name = "open_time", length = 20)
    private String openTime = "08:00";

    @Column(name = "close_time", length = 20)
    private String closeTime = "22:00";

    @Column(length = 500)
    private String image;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "ai_reply_enabled")
    private Boolean aiReplyEnabled = false;

    @Column(name = "ai_assistant_name", length = 100)
    private String aiAssistantName = "Trợ lý quầy sách";

    @Column(name = "ai_tone", length = 50)
    private String aiTone = "FRIENDLY";

    @Column(name = "ai_shop_description", columnDefinition = "TEXT")
    private String aiShopDescription;

    @Column(name = "ai_custom_faq", columnDefinition = "TEXT")
    private String aiCustomFaq;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private java.math.BigDecimal commissionRate = new java.math.BigDecimal("10.00");

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Store() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getAiReplyEnabled() { return aiReplyEnabled != null ? aiReplyEnabled : false; }
    public void setAiReplyEnabled(Boolean aiReplyEnabled) { this.aiReplyEnabled = aiReplyEnabled; }
    public String getAiAssistantName() { return (aiAssistantName != null && !aiAssistantName.isBlank()) ? aiAssistantName : "Trợ lý quầy sách"; }
    public void setAiAssistantName(String aiAssistantName) { this.aiAssistantName = aiAssistantName; }
    public String getAiTone() { return aiTone != null ? aiTone : "FRIENDLY"; }
    public void setAiTone(String aiTone) { this.aiTone = aiTone; }
    public String getAiShopDescription() { return aiShopDescription; }
    public void setAiShopDescription(String aiShopDescription) { this.aiShopDescription = aiShopDescription; }
    public String getAiCustomFaq() { return aiCustomFaq; }
    public void setAiCustomFaq(String aiCustomFaq) { this.aiCustomFaq = aiCustomFaq; }
    public java.math.BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(java.math.BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
