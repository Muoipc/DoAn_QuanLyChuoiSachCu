package vn.iotstar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_registrations")
public class StoreRegistration {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String province = "TP. Hồ Chí Minh";

    @Column(nullable = false, length = 100)
    private String district;

    @Column(name = "open_time", length = 20)
    private String openTime = "08:00";

    @Column(name = "close_time", length = 20)
    private String closeTime = "21:30";

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "owner_id_card", nullable = false, length = 30)
    private String ownerIdCard;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "bank_account", nullable = false, length = 50)
    private String bankAccount;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StoreRegistration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
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
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerIdCard() { return ownerIdCard; }
    public void setOwnerIdCard(String ownerIdCard) { this.ownerIdCard = ownerIdCard; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
