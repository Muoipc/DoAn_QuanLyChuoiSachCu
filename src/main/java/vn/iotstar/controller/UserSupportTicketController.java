package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.iotstar.entity.SupportTicket;
import vn.iotstar.entity.User;
import vn.iotstar.repository.SupportTicketRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.List;
import java.util.Optional;

/**
 * Quản lý phiếu hỗ trợ cá nhân của người dùng (Customer Support Ticket History)
 * URL: /user/support-tickets
 */
@Controller
@RequestMapping("/user/support-tickets")
public class UserSupportTicketController {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"", "/"})
    public String myTickets(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?redirectURL=/user/support-tickets";
        }

        Long userId = userDetails.getId();
        User user = userRepository.findById(userId).orElse(null);
        List<SupportTicket> tickets = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long countOpen = supportTicketRepository.countByUserIdAndStatus(userId, SupportTicket.TicketStatus.OPEN);
        long countResolved = supportTicketRepository.countByUserIdAndStatus(userId, SupportTicket.TicketStatus.RESOLVED);

        model.addAttribute("user", user);
        model.addAttribute("tickets", tickets);
        model.addAttribute("countOpen", countOpen);
        model.addAttribute("countResolved", countResolved);
        model.addAttribute("activeTab", "support-tickets");
        model.addAttribute("pageTitle", "Lịch Sử Phiếu Hỗ Trợ — ReBook");

        return "user/support-tickets";
    }

    @GetMapping("/{id}")
    public String viewTicketDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login?redirectURL=/user/support-tickets/" + id;
        }

        Optional<SupportTicket> opt = supportTicketRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/user/support-tickets";
        }

        SupportTicket ticket = opt.get();
        // Kiểm tra quyền sở hữu IDOR
        boolean isAdmin = "ROLE_ADMIN".equals(userDetails.getRoleName());
        if (!isAdmin && (ticket.getUser() == null || !ticket.getUser().getId().equals(userDetails.getId()))) {
            return "redirect:/user/support-tickets";
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("pageTitle", "Chi Tiết Phiếu Hỗ Trợ #" + ticket.getId() + " — ReBook");

        return "user/support-ticket-detail";
    }
}
