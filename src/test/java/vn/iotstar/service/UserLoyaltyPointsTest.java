package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoyaltyPointsTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Tích lũy 1% điểm thành viên khi đơn hàng chuyển sang trạng thái DELIVERED")
    void testAwardLoyaltyPoints_WhenOrderDelivered() {
        Long orderId = 100L;
        User user = new User();
        user.setId(4L);
        user.setLoyaltyPoints(500);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setFinalAmount(new BigDecimal("200000")); // 200.000 đ
        order.setOrderStatus(Order.OrderStatus.SHIPPING);
        order.setPaymentMethod(Order.PaymentMethod.COD);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateOrderStatus(orderId, Order.OrderStatus.DELIVERED, "Giao thành công");

        // 1% của 200.000 = 2.000 điểm; ban đầu có 500 -> tổng 2.500
        assertEquals(2500, user.getLoyaltyPoints());
        verify(userRepository, times(1)).save(user);
    }
}
