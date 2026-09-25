package vn.iotstar.dto;

import vn.iotstar.entity.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) hiển thị thông tin Voucher ra giao diện người dùng.
 * Bao gồm đầy đủ thông tin giảm giá, thời hạn, điều kiện áp dụng,
 * và cờ đánh dấu người dùng hiện tại đã lưu voucher vào ví cá nhân hay chưa.
 */
public class VoucherResponseDTO {

    /**
     * ID của voucher trong hệ thống.
     */
    private Long id;

    /**
     * Mã ký tự của voucher (ví dụ: FREESHIP15K, SACHCU50K).
     */
    private String code;

    /**
     * Mô tả chi tiết ưu đãi của voucher.
     */
    private String description;

    /**
     * Loại giảm giá: FIXED_AMOUNT (tiền mặt) hoặc PERCENT (phần trăm).
     */
    private Voucher.DiscountType discountType;

    /**
     * Giá trị giảm giá (số tiền hoặc số phần trăm tương ứng).
     */
    private BigDecimal discountValue;

    /**
     * Giá trị đơn hàng tối thiểu để được áp dụng voucher.
     */
    private BigDecimal minOrderAmount;

    /**
     * Số tiền giảm giá tối đa (áp dụng cho loại giảm PERCENT).
     */
    private BigDecimal maxDiscount;

    /**
     * Ngày bắt đầu có hiệu lực của voucher.
     */
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc hiệu lực của voucher.
     */
    private LocalDateTime endDate;

    /**
     * Tổng số lượt sử dụng tối đa của voucher trên toàn hệ thống.
     */
    private Integer usageLimit;

    /**
     * Số lượt voucher đã được khách hàng sử dụng thực tế.
     */
    private Integer usedCount;

    /**
     * Trạng thái hoạt động của voucher.
     */
    private Boolean isActive;

    /**
     * Cờ đánh dấu người dùng hiện tại đã lưu voucher này vào ví hay chưa.
     */
    private Boolean isSaved = false;

    /**
     * Trạng thái voucher đã được người dùng sử dụng trong ví chưa (null nếu chưa lưu ví).
     */
    private Boolean isUsed = false;

    /**
     * ID bản ghi UserVoucher tương ứng nếu người dùng đã lưu vào ví.
     */
    private Long userVoucherId;

    /**
     * Số lượt sử dụng còn lại của voucher trên toàn hệ thống.
     */
    private Integer remainingUses;

    /**
     * Constructor mặc định không tham số.
     */
    public VoucherResponseDTO() {
    }

    /**
     * Constructor khởi tạo từ thực thể Voucher và thông tin ví cá nhân.
     *
     * @param voucher       Thực thể Voucher gốc
     * @param isSaved       Đã lưu vào ví hay chưa
     * @param isUsed        Đã dùng trong ví chưa
     * @param userVoucherId ID bản ghi UserVoucher nếu có
     */
    public VoucherResponseDTO(Voucher voucher, Boolean isSaved, Boolean isUsed, Long userVoucherId) {
        if (voucher != null) {
            this.id = voucher.getId();
            this.code = voucher.getCode();
            this.description = voucher.getDescription();
            this.discountType = voucher.getDiscountType();
            this.discountValue = voucher.getDiscountValue();
            this.minOrderAmount = voucher.getMinOrderAmount();
            this.maxDiscount = voucher.getMaxDiscount();
            this.startDate = voucher.getStartDate();
            this.endDate = voucher.getEndDate();
            this.usageLimit = voucher.getUsageLimit();
            this.usedCount = voucher.getUsedCount();
            this.isActive = voucher.getIsActive();
            int limit = voucher.getUsageLimit() != null ? voucher.getUsageLimit() : 0;
            int used = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
            this.remainingUses = Math.max(0, limit - used);
        }
        this.isSaved = isSaved != null ? isSaved : false;
        this.isUsed = isUsed != null ? isUsed : false;
        this.userVoucherId = userVoucherId;
    }

    /**
     * Constructor đầy đủ tham số.
     */
    public VoucherResponseDTO(Long id, String code, String description, Voucher.DiscountType discountType,
                              BigDecimal discountValue, BigDecimal minOrderAmount, BigDecimal maxDiscount,
                              LocalDateTime startDate, LocalDateTime endDate, Integer usageLimit,
                              Integer usedCount, Boolean isActive, Boolean isSaved, Boolean isUsed,
                              Long userVoucherId, Integer remainingUses) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscount = maxDiscount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.isActive = isActive;
        this.isSaved = isSaved;
        this.isUsed = isUsed;
        this.userVoucherId = userVoucherId;
        this.remainingUses = remainingUses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Voucher.DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Voucher.DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSaved() {
        return isSaved;
    }

    public void setIsSaved(Boolean isSaved) {
        this.isSaved = isSaved;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public Long getUserVoucherId() {
        return userVoucherId;
    }

    public void setUserVoucherId(Long userVoucherId) {
        this.userVoucherId = userVoucherId;
    }

    public Integer getRemainingUses() {
        return remainingUses;
    }

    public void setRemainingUses(Integer remainingUses) {
        this.remainingUses = remainingUses;
    }

    public String getCategorySlug() {
        if (code == null) return "book";
        String c = code.toUpperCase();
        String d = (description != null) ? description.toLowerCase() : "";
        if (c.startsWith("FREESHIP") || d.contains("vận chuyển") || d.contains("giao hàng") || d.contains("freeship")) {
            return "shipping";
        }
        if (c.startsWith("TRIAN") || c.startsWith("VIP") || c.startsWith("CHAOBAN") || c.startsWith("MEGA") || d.contains("độc quyền") || d.contains("tri ân") || d.contains("độc giả mới")) {
            return "exclusive";
        }
        return "book";
    }

    public String getCategoryName() {
        String slug = getCategorySlug();
        switch (slug) {
            case "shipping": return "Mã Vận Chuyển";
            case "exclusive": return "Ưu Đãi Độc Quyền";
            default: return "Mã Giảm Giá Sách Cũ";
        }
    }

    public String getDiscountDisplay() {
        if (discountType == Voucher.DiscountType.PERCENT) {
            return (discountValue != null ? discountValue.stripTrailingZeros().toPlainString() : "0") + "%";
        }
        if (discountValue == null) return "0đ";
        long val = discountValue.longValue();
        if (val >= 1000 && val % 1000 == 0) {
            return (val / 1000) + "K";
        }
        return String.format("%,dđ", val);
    }

    public int getPercentUsed() {
        if (usageLimit == null || usageLimit <= 0) return 0;
        int used = (usedCount != null) ? usedCount : 0;
        return (int) Math.min(100, Math.round(((double) used / usageLimit) * 100));
    }

    public String getFormattedMinOrder() {
        if (minOrderAmount == null || minOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Đơn tối thiểu ₫0";
        }
        return String.format("Đơn tối thiểu ₫%,d", minOrderAmount.longValue());
    }

    public String getFormattedEndDate() {
        if (endDate == null) return "Vô thời hạn";
        return endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
