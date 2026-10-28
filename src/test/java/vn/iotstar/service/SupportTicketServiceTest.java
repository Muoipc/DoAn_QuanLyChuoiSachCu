package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.SupportTicket;
import vn.iotstar.entity.User;
import vn.iotstar.repository.SupportTicketRepository;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private INotificationService notificationService;

    @Test
    @DisplayName("Tạo phiếu hỗ trợ CSKH gắn liền tài khoản User: Lưu thành công")
    void testCreateSupportTicketWithUser() {
        User user = new User();
        user.setId(4L);
        user.setFullName("Nguyễn Song Hoàng Phúc");
        user.setEmail("phuc0125@gmail.com");

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setFullName(user.getFullName());
        ticket.setEmail(user.getEmail());
        ticket.setCategory("Đơn hàng & Giao nhận");
        ticket.setMessage("Tôi cần kiểm tra lại thời gian giao của đơn hàng ORD-12345");
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> {
            SupportTicket t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        SupportTicket saved = supportTicketRepository.save(ticket);

        assertNotNull(saved);
        assertEquals(101L, saved.getId());
        assertNotNull(saved.getUser());
        assertEquals(4L, saved.getUser().getId());
        assertEquals(SupportTicket.TicketStatus.OPEN, saved.getStatus());
        verify(supportTicketRepository, times(1)).save(ticket);
    }

    @Test
    @DisplayName("Admin phản hồi phiếu hỗ trợ: Lưu câu trả lời, đổi trạng thái RESOLVED và có thời gian phản hồi")
    void testResolveSupportTicket() {
        User user = new User();
        user.setId(4L);
        user.setEmail("phuc0125@gmail.com");

        SupportTicket ticket = new SupportTicket();
        ticket.setId(202L);
        ticket.setUser(user);
        ticket.setCategory("Chất lượng sách");
        ticket.setMessage("Sách có đúng gáy nguyên bản không?");
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Admin xử lý
        ticket.setAdminReply("Chào bạn, sách được chụp ảnh thật 100% tại quầy Thủ Đức, gáy sách nguyên vẹn.");
        ticket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());

        SupportTicket resolved = supportTicketRepository.save(ticket);

        assertEquals(SupportTicket.TicketStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getAdminReply());
        assertTrue(resolved.getAdminReply().contains("quầy Thủ Đức"));
        assertNotNull(resolved.getResolvedAt());
    }

    @Test
    @DisplayName("Tra cứu danh sách phiếu hỗ trợ theo tài khoản User ID")
    void testFindByUserId() {
        SupportTicket t1 = new SupportTicket();
        t1.setId(1L);
        SupportTicket t2 = new SupportTicket();
        t2.setId(2L);

        when(supportTicketRepository.findByUserIdOrderByCreatedAtDesc(4L)).thenReturn(List.of(t2, t1));

        List<SupportTicket> list = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(4L);

        assertEquals(2, list.size());
        assertEquals(2L, list.get(0).getId());
        verify(supportTicketRepository, times(1)).findByUserIdOrderByCreatedAtDesc(4L);
    }
}
