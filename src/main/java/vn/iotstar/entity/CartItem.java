package vn.iotstar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private Integer quantity = 1;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    public CartItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    /**
     * Ghi chú cho Cường: Tính thành tiền cho dòng sản phẩm sách cũ này (Đơn giá x Số lượng).
     */
    public BigDecimal getItemTotal() {
        if (book != null && book.getPrice() != null && quantity != null) {
            return book.getPrice().multiply(new BigDecimal(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Ghi chú cho Cường: Tính tổng số tiền tiết kiệm được so với giá bìa gốc.
     */
    public BigDecimal getSavings() {
        if (book != null && book.getOriginalPrice() != null && book.getPrice() != null && quantity != null) {
            BigDecimal diff = book.getOriginalPrice().subtract(book.getPrice());
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                return diff.multiply(new BigDecimal(quantity));
            }
        }
        return BigDecimal.ZERO;
    }
}
