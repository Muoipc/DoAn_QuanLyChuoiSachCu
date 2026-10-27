package vn.iotstar.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.SupportTicket;
import vn.iotstar.repository.SupportTicketRepository;
import vn.iotstar.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/support-tickets")
public class AdminSupportTicketController {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private vn.iotstar.repository.UserRepository userRepository;

    @Autowired
    private vn.iotstar.service.INotificationService notificationService;

    /**
     * Danh sách phiếu hỗ trợ & khiếu nại khách hàng
     */
    @GetMapping({"", "/"})
    public String listTickets(
            @RequestParam(value = "status", required = false) String statusFilter,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        List<SupportTicket> tickets;
        if ("OPEN".equalsIgnoreCase(statusFilter)) {
            tickets = supportTicketRepository.findByStatusOrderByCreatedAtDesc(SupportTicket.TicketStatus.OPEN);
        } else if ("RESOLVED".equalsIgnoreCase(statusFilter)) {
            tickets = supportTicketRepository.findByStatusOrderByCreatedAtDesc(SupportTicket.TicketStatus.RESOLVED);
        } else {
            tickets = supportTicketRepository.findAllByOrderByCreatedAtDesc();
        }

        long countOpen = supportTicketRepository.countByStatus(SupportTicket.TicketStatus.OPEN);

        model.addAttribute("tickets", tickets);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter.toUpperCase() : "ALL");
        model.addAttribute("countOpen", countOpen);
        model.addAttribute("pageTitle", "Hỗ Trợ & Khiếu Nại Khách Hàng");

        return "admin/support-tickets/list";
    }

    /**
     * Phản hồi và đóng phiếu hỗ trợ
     */
    @PostMapping("/{id}/resolve")
    public String resolveTicket(
            @PathVariable("id") Long id,
            @RequestParam("reply") String reply,
            RedirectAttributes redirect
    ) {
        SupportTicket ticket = supportTicketRepository.findById(id).orElse(null);
        if (ticket == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy phiếu hỗ trợ #" + id);
            return "redirect:/admin/support-tickets";
        }

        ticket.setAdminReply(reply != null && !reply.isBlank() ? reply.trim() : "Đã xử lý và liên hệ khách hàng qua điện thoại/email.");
        ticket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        SupportTicket savedTicket = supportTicketRepository.save(ticket);

        if (savedTicket.getEmail() != null && notificationService != null) {
            userRepository.findByEmail(savedTicket.getEmail()).ifPresent(u -> {
                try {
                    notificationService.notifySupportTicketResolved(savedTicket, u);
                } catch (Exception ignored) {}
            });
        }

        redirect.addFlashAttribute("successMessage", "Đã phản hồi và hoàn tất giải quyết phiếu #" + id);
        return "redirect:/admin/support-tickets";
    }

    /**
     * Xóa phiếu hỗ trợ
     */
    @PostMapping("/{id}/delete")
    public String deleteTicket(
            @PathVariable("id") Long id,
            RedirectAttributes redirect
    ) {
        if (supportTicketRepository.existsById(id)) {
            supportTicketRepository.deleteById(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa phiếu hỗ trợ #" + id);
        }
        return "redirect:/admin/support-tickets";
    }
}
