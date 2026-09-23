package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_consignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookConsignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "book_title", nullable = false, length = 255)
    private String bookTitle;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(length = 150)
    private String publisher;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(name = "condition_percent", nullable = false)
    private Integer conditionPercent;

    @Column(name = "condition_description", nullable = false, columnDefinition = "TEXT")
    private String conditionDescription;

    @Column(name = "photos_json", columnDefinition = "TEXT")
    private String photosJson;

    @Column(name = "proposed_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal proposedPrice;

    @Column(name = "agreed_price", precision = 14, scale = 2)
    private BigDecimal agreedPrice;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConsignmentStatus status = ConsignmentStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ConsignmentStatus {
        PENDING, APPROVED, REJECTED, STORED
    }
}
