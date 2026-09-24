package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    /**
     * Ghi chú cho Cường: Lấy chi tiết đơn hàng kèm danh sách OrderItem, Book, Store và ShippingUnit
     * phục vụ trang xác nhận đặt hàng thành công (checkout-success) và trang xem lịch sử đơn.
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book b LEFT JOIN FETCH o.store s LEFT JOIN FETCH o.shippingUnit su WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeWithDetails(@Param("orderCode") String orderCode);

    /**
     * Ghi chú cho Cường: Lấy toàn bộ đơn hàng của khách hàng kèm danh sách sách và chi nhánh phục vụ
     * cho trang Lịch sử đơn hàng (/orders).
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book b LEFT JOIN FETCH o.store s WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Lọc đơn hàng theo trạng thái (NEW, CONFIRMED, SHIPPING, DELIVERED, CANCELLED).
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book b LEFT JOIN FETCH o.store s WHERE o.user.id = :userId AND o.orderStatus = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndOrderStatusWithItems(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    Page<Order> findByShipperIdOrderByCreatedAtDesc(Long shipperId, Pageable pageable);

    Page<Order> findByOrderStatus(Order.OrderStatus status, Pageable pageable);

    List<Order> findByStoreIdAndOrderStatus(Long storeId, Order.OrderStatus status);

    long countByOrderStatus(Order.OrderStatus status);

    long countByStoreIdAndOrderStatus(Long storeId, Order.OrderStatus status);
}
