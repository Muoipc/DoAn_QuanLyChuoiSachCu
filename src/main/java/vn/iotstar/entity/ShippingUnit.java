package vn.iotstar.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_units")
public class ShippingUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "base_fee", precision = 12, scale = 2)
    private BigDecimal baseFee = new BigDecimal("25000.00");

    @Column(name = "estimated_days", length = 50)
    private String estimatedDays = "2-3 ngày";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public ShippingUnit() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public BigDecimal getBaseFee() { return baseFee; }
    public void setBaseFee(BigDecimal baseFee) { this.baseFee = baseFee; }
    public String getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(String estimatedDays) { this.estimatedDays = estimatedDays; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
