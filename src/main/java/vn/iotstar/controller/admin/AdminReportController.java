package vn.iotstar.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.service.IReportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping({"/admin", "/admin/reports"})
public class AdminReportController {

    @Autowired
    private IReportService reportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Dashboard Quản Trị Hệ Thống & Báo Cáo Doanh Thu Trực Quan Với Chart.js
     */
    @GetMapping({"", "/", "/dashboard", "/reports", "/analytics"})
    public String dashboard(
            @RequestParam(name = "year", required = false) Integer yearParam,
            Model model
    ) throws JsonProcessingException {
        int year = (yearParam != null) ? yearParam : LocalDate.now().getYear();

        // 1. Thẻ tổng quan
        Map<String, Object> summary = reportService.getDashboardSummary();
        model.addAttribute("summary", summary);
        model.addAttribute("selectedYear", year);

        // 2. Doanh thu 12 tháng phục vụ Biểu đồ đường (Line Chart)
        Map<Integer, BigDecimal> monthly = reportService.getMonthlyRevenue(year);
        List<BigDecimal> monthlyData = new ArrayList<>(monthly.values());
        model.addAttribute("monthlyRevenueJson", objectMapper.writeValueAsString(monthlyData));

        // 3. Doanh thu theo từng chi nhánh phục vụ Biểu đồ cột / Bar Chart
        Map<String, BigDecimal> storeRevenue = reportService.getStoreRevenue();
        model.addAttribute("storeLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(storeRevenue.keySet())));
        model.addAttribute("storeDataJson", objectMapper.writeValueAsString(new ArrayList<>(storeRevenue.values())));

        // 4. Cơ cấu đơn hàng theo trạng thái phục vụ Biểu đồ tròn / Doughnut Chart
        Map<String, Long> statusDist = reportService.getOrderStatusDistribution();
        model.addAttribute("statusLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(statusDist.keySet())));
        model.addAttribute("statusDataJson", objectMapper.writeValueAsString(new ArrayList<>(statusDist.values())));

        // 5. Top sách cũ bán chạy
        model.addAttribute("topBooks", reportService.getTopSellingBooks(5));

        return "admin/reports/dashboard";
    }

    /**
     * Xuất toàn bộ báo cáo đơn hàng & doanh thu ra file Excel (.xlsx)
     */
    @GetMapping("/reports/export-excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        reportService.exportOrdersToExcel(response);
    }
}
