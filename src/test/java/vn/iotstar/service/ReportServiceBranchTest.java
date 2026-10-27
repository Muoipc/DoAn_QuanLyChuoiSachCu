package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.*;
import vn.iotstar.service.impl.ReportServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử nghiệp vụ Báo cáo phân quyền theo từng Chi nhánh (Store Manager Isolation)
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceBranchTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private BookConsignmentRepository consignmentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("Báo cáo tổng quan chỉ tính doanh thu và đơn hàng của chi nhánh được phân công")
    void testGetDashboardSummaryByStore() {
        Long storeId = 1L;
        Store store = new Store();
        store.setId(storeId);
        store.setStoreName("Tổng Kho Sách Cũ TP.HCM");

        Order order1 = new Order();
        order1.setId(101L);
        order1.setStore(store);
        order1.setOrderStatus(Order.OrderStatus.DELIVERED);
        order1.setPaymentStatus(Order.PaymentStatus.PAID);
        order1.setFinalAmount(new BigDecimal("150000.00"));

        Order order2 = new Order();
        order2.setId(102L);
        order2.setStore(store);
        order2.setOrderStatus(Order.OrderStatus.CANCELLED);
        order2.setPaymentStatus(Order.PaymentStatus.UNPAID);
        order2.setFinalAmount(new BigDecimal("90000.00"));

        when(orderRepository.findByStoreId(storeId)).thenReturn(List.of(order1, order2));
        when(consignmentRepository.countByStoreIdAndStatus(storeId, vn.iotstar.entity.BookConsignment.ConsignmentStatus.PENDING)).thenReturn(3L);

        Map<String, Object> summary = reportService.getDashboardSummaryByStore(storeId);

        assertNotNull(summary);
        assertEquals(2L, summary.get("totalOrders"));
        assertEquals(new BigDecimal("150000.00"), summary.get("totalRevenue"));
        assertEquals(1L, summary.get("completedOrders"));
        assertEquals(1L, summary.get("cancelledOrders"));
        assertEquals(3L, summary.get("pendingConsignments"));
    }

    @Test
    @DisplayName("Báo cáo doanh thu 12 tháng chỉ tính đơn hàng thuộc chi nhánh phụ trách")
    void testGetMonthlyRevenueByStore() {
        Long storeId = 1L;
        Store store = new Store();
        store.setId(storeId);

        Order order = new Order();
        order.setStore(store);
        order.setOrderStatus(Order.OrderStatus.DELIVERED);
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setFinalAmount(new BigDecimal("200000.00"));
        order.setCreatedAt(LocalDateTime.of(2026, 10, 8, 10, 0));

        when(orderRepository.findByStoreId(storeId)).thenReturn(List.of(order));

        Map<Integer, BigDecimal> monthly = reportService.getMonthlyRevenueByStore(2026, storeId);

        assertNotNull(monthly);
        assertEquals(12, monthly.size());
        assertEquals(0, new BigDecimal("200000.00").compareTo(monthly.get(10)));
        assertEquals(BigDecimal.ZERO, monthly.get(1));
    }
}
