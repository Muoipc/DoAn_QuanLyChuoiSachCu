package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    Page<Order> findByShipperIdOrderByCreatedAtDesc(Long shipperId, Pageable pageable);

    Page<Order> findByOrderStatus(Order.OrderStatus status, Pageable pageable);

    List<Order> findByStoreIdAndOrderStatus(Long storeId, Order.OrderStatus status);

    long countByOrderStatus(Order.OrderStatus status);

    long countByStoreIdAndOrderStatus(Long storeId, Order.OrderStatus status);
}
