package vn.iotstar.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookAdminDTO {
    private Long id;

    @NotBlank(message = "Tên sách không được để trống")
    private String title;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer categoryId;

    @NotBlank(message = "Tên tác giả không được để trống")
    private String author;

    private String publisher;
    private Integer publishYear;
    private String isbn;

    @NotNull(message = "Vui lòng nhập độ mới của sách")
    @Min(value = 80, message = "Độ mới tối thiểu là 80%")
    @Max(value = 99, message = "Độ mới sách cũ tối đa là 99%")
    private Integer conditionPercent = 90;

    private String conditionNotes;
    private String description;

    private BigDecimal originalPrice = BigDecimal.ZERO;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán không được âm")
    private BigDecimal price;

    private BigDecimal discountPrice = BigDecimal.ZERO;

    private Boolean isActive = true;
    private Boolean isFeatured = false;

    // Ảnh chính (Bìa sách)
    private MultipartFile primaryImage;

    // Bộ ảnh góc cạnh (Gáy sách, bìa sau, trang sách, ấn bản)
    private List<MultipartFile> angleImages = new ArrayList<>();
    private List<String> angleDescriptions = new ArrayList<>();

    // Tồn kho ban đầu theo từng chi nhánh: Map<storeId, quantity>
    private Map<Long, Integer> storeQuantities = new HashMap<>();

    public BookAdminDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getConditionPercent() { return conditionPercent; }
    public void setConditionPercent(Integer conditionPercent) { this.conditionPercent = conditionPercent; }
    public String getConditionNotes() { return conditionNotes; }
    public void setConditionNotes(String conditionNotes) { this.conditionNotes = conditionNotes; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(BigDecimal discountPrice) { this.discountPrice = discountPrice; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public MultipartFile getPrimaryImage() { return primaryImage; }
    public void setPrimaryImage(MultipartFile primaryImage) { this.primaryImage = primaryImage; }
    public List<MultipartFile> getAngleImages() { return angleImages; }
    public void setAngleImages(List<MultipartFile> angleImages) { this.angleImages = angleImages; }
    public List<String> getAngleDescriptions() { return angleDescriptions; }
    public void setAngleDescriptions(List<String> angleDescriptions) { this.angleDescriptions = angleDescriptions; }
    public Map<Long, Integer> getStoreQuantities() { return storeQuantities; }
    public void setStoreQuantities(Map<Long, Integer> storeQuantities) { this.storeQuantities = storeQuantities; }
}
