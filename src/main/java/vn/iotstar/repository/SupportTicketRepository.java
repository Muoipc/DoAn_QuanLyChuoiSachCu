package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.SupportTicket;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(SupportTicket.TicketStatus status);
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
    List<SupportTicket> findByEmailOrderByCreatedAtDesc(String email);
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(SupportTicket.TicketStatus status);
    long countByUserIdAndStatus(Long userId, SupportTicket.TicketStatus status);
}
