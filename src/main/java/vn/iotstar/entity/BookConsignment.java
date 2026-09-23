package vn.iotstar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_consignments")
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
    private ConsignmentStatus status = ConsignmentStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ConsignmentStatus { PENDING, APPROVED, REJECTED, STORED }

    public BookConsignment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    public Integer getConditionPercent() { return conditionPercent; }
    public void setConditionPercent(Integer conditionPercent) { this.conditionPercent = conditionPercent; }
    public String getConditionDescription() { return conditionDescription; }
    public void setConditionDescription(String conditionDescription) { this.conditionDescription = conditionDescription; }
    public String getPhotosJson() { return photosJson; }
    public void setPhotosJson(String photosJson) { this.photosJson = photosJson; }
    public BigDecimal getProposedPrice() { return proposedPrice; }
    public void setProposedPrice(BigDecimal proposedPrice) { this.proposedPrice = proposedPrice; }
    public BigDecimal getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(BigDecimal agreedPrice) { this.agreedPrice = agreedPrice; }
    public ConsignmentStatus getStatus() { return status; }
    public void setStatus(ConsignmentStatus status) { this.status = status; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
