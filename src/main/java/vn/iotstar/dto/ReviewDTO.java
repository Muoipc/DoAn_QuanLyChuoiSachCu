package vn.iotstar.dto;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) truyền tải dữ liệu Đánh giá sách ra Client và REST API.
 * Ghi chú cho Cường:
 * - Tránh lỗi LazyInitializationException và tuần hoàn JSON khi serialize thực thể Review / User / Order.
 * - Chuẩn hóa sẵn ngày tạo (dd/MM/yyyy HH:mm), nhãn đánh giá và ký tự viết tắt Avatar (userInitial) cho giao diện.
 */
public class ReviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String userInitial;
    private Integer rating;
    private String ratingLabel;
    private String comment;
    private String mediaUrl;
    private String mediaType;
    private String createdAt;
    private boolean isPurchased;

    public ReviewDTO() {}

    public ReviewDTO(Long id, Long bookId, Long userId, String userName, String userAvatar,
                     String userInitial, Integer rating, String ratingLabel, String comment,
                     String mediaUrl, String mediaType, String createdAt, boolean isPurchased) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.userInitial = userInitial;
        this.rating = rating;
        this.ratingLabel = ratingLabel;
        this.comment = comment;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.createdAt = createdAt;
        this.isPurchased = isPurchased;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserInitial() {
        return userInitial;
    }

    public void setUserInitial(String userInitial) {
        this.userInitial = userInitial;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRatingLabel() {
        return ratingLabel;
    }

    public void setRatingLabel(String ratingLabel) {
        this.ratingLabel = ratingLabel;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }
}
