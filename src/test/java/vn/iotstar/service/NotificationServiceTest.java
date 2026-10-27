package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import vn.iotstar.entity.*;
import vn.iotstar.repository.NotificationRepository;
import vn.iotstar.service.impl.NotificationServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("Tự động tạo 2 thông báo khi đơn hàng mới được tạo (1 cho khách, 1 cho quản lý)")
    void testNotifyOrderCreated() {
        User user = new User();
        user.setId(5L);
        user.setUsername("customer_cuong");

        Store store = new Store();
        store.setId(1L);
        store.setStoreName("Tổng Kho Thủ Đức");

        Order order = new Order();
        order.setId(10L);
        order.setOrderCode("ORD998877");
        order.setUser(user);
        order.setStore(store);
        order.setItems(new ArrayList<>());

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.notifyOrderCreated(order);

        // Phải lưu 2 thông báo: cho khách (ROLE_USER) và cho quản lý (ROLE_MANAGER)
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Tự động tạo thông báo gửi khách hàng khi đơn hàng đổi trạng thái")
    void testNotifyOrderStatusChanged() {
        User user = new User();
        user.setId(5L);

        Order order = new Order();
        order.setOrderCode("ORD998877");
        order.setUser(user);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.notifyOrderStatusChanged(order, Order.OrderStatus.CONFIRMED, Order.OrderStatus.SHIPPING);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Lấy danh sách thông báo và đếm số lượng chưa đọc của khách hàng")
    void testGetAndCountNotificationsForUser() {
        Long userId = 5L;
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setTitle("Đơn hàng mới");

        when(notificationRepository.findForUser(eq(userId), any(PageRequest.class))).thenReturn(List.of(n1));
        when(notificationRepository.countUnreadForUser(userId)).thenReturn(1L);

        List<Notification> list = notificationService.getNotificationsForUser(userId);
        long unread = notificationService.countUnreadForUser(userId);

        assertEquals(1, list.size());
        assertEquals(1L, unread);
        assertEquals("Đơn hàng mới", list.get(0).getTitle());
    }
}
