package vn.iotstar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Thực thể ánh xạ bảng user_vouchers - Quản lý kho voucher cá nhân của người dùng.
 * Lưu trữ thông tin voucher mà người dùng đã lưu vào ví và trạng thái sử dụng của từng voucher.
 */
@Entity
@Table(name = "user_vouchers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_voucher", columnNames = {"user_id", "voucher_id"})
})
public class UserVoucher {

    /**
     * Khóa chính tự tăng của bảng user_vouchers.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người dùng sở hữu voucher trong ví (Liên kết Many-to-One với thực thể User).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Voucher được lưu trong ví (Liên kết Many-to-One với thực thể Voucher).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    /**
     * Trạng thái voucher đã được sử dụng hay chưa (mặc định false - chưa sử dụng).
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    /**
     * Thời gian người dùng thực hiện lưu voucher vào ví.
     */
    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    /**
     * Thời gian người dùng áp dụng voucher cho đơn hàng thành công.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Tự động khởi tạo thời gian lưu trước khi persist vào cơ sở dữ liệu nếu chưa được gán.
     */
    @PrePersist
    public void prePersist() {
        if (this.savedAt == null) {
            this.savedAt = LocalDateTime.now();
        }
        if (this.isUsed == null) {
            this.isUsed = false;
        }
    }

    /**
     * Constructor mặc định không tham số.
     */
    public UserVoucher() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param id       Khóa chính
     * @param user     Người dùng sở hữu
     * @param voucher  Mã giảm giá
     * @param isUsed   Trạng thái đã sử dụng
     * @param savedAt  Thời gian lưu vào ví
     * @param usedAt   Thời gian sử dụng
     */
    public UserVoucher(Long id, User user, Voucher voucher, Boolean isUsed, LocalDateTime savedAt, LocalDateTime usedAt) {
        this.id = id;
        this.user = user;
        this.voucher = voucher;
        this.isUsed = isUsed;
        this.savedAt = savedAt;
        this.usedAt = usedAt;
    }

    /**
     * Constructor tiện ích để lưu voucher mới cho người dùng.
     *
     * @param user    Người dùng sở hữu
     * @param voucher Mã giảm giá cần lưu
     */
    public UserVoucher(User user, Voucher voucher) {
        this.user = user;
        this.voucher = voucher;
        this.isUsed = false;
        this.savedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
