package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
