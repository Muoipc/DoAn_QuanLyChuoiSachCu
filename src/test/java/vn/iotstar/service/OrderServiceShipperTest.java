package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Order;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceShipperTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Thống kê số lượng đơn và tổng tiền COD thu hộ của Shipper")
    void testShipperStatsCalculation() {
        Long shipperId = 6L;
        when(orderRepository.countByShipperIdAndOrderStatus(shipperId, Order.OrderStatus.SHIPPING)).thenReturn(5L);
        when(orderRepository.countByShipperIdAndOrderStatus(shipperId, Order.OrderStatus.DELIVERED)).thenReturn(12L);
        when(orderRepository.countByShipperIdAndOrderStatus(shipperId, Order.OrderStatus.RETURNED)).thenReturn(1L);
        when(orderRepository.sumCodCollectedByShipper(shipperId)).thenReturn(new BigDecimal("1850000.00"));

        long shipping = orderService.countOrdersForShipper(shipperId, Order.OrderStatus.SHIPPING);
        long delivered = orderService.countOrdersForShipper(shipperId, Order.OrderStatus.DELIVERED);
        long returned = orderService.countOrdersForShipper(shipperId, Order.OrderStatus.RETURNED);
        BigDecimal codTotal = orderService.sumCodCollectedByShipper(shipperId);

        assertEquals(5L, shipping);
        assertEquals(12L, delivered);
        assertEquals(1L, returned);
        assertEquals(new BigDecimal("1850000.00"), codTotal);
    }

    @Test
    @DisplayName("Ghi nhận lý do khi Shipper báo giao thất bại / hoàn hàng")
    void testShipperReportDeliveryFailureWithNote() {
        Long orderId = 100L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderStatus(Order.OrderStatus.SHIPPING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(orderId, Order.OrderStatus.RETURNED, "Khách không nghe máy sau 3 cuộc gọi");

        assertEquals(Order.OrderStatus.RETURNED, order.getOrderStatus());
        assertTrue(order.getCustomerNotes() != null && order.getCustomerNotes().contains("Khách không nghe máy"));
    }

    @Test
    @DisplayName("Quy trình Shipper lấy hàng từ cửa hàng (PICKED_UP) và bắt đầu đi giao (SHIPPING)")
    void testShipperPickUpAndStartShipping() {
        Long orderId = 101L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Shipper lấy hàng tại kho
        orderService.updateOrderStatus(orderId, Order.OrderStatus.PICKED_UP, "Đã lấy hàng từ chi nhánh");
        assertEquals(Order.OrderStatus.PICKED_UP, order.getOrderStatus());

        // 2. Shipper bắt đầu giao tới khách
        orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPING, "Đang trên đường giao");
        assertEquals(Order.OrderStatus.SHIPPING, order.getOrderStatus());
    }
}
