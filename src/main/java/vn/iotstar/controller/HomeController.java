package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Hệ Thống Chuỗi Cửa Hàng Sách Cũ - HCMUTE");
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
