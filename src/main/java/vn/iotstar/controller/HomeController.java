package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller điều hướng trang chủ và các trang công cộng dành cho khách (Guest).
 */
@Controller
public class HomeController {

    /**
     * Điều hướng trang chủ hệ thống chuỗi sách cũ.
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Hệ Thống Chuỗi Cửa Hàng Sách Cũ - HCMUTE");
        return "home";
    }
}
