package vn.iotstar.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.entity.Store;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IReportService;
import vn.iotstar.service.IStoreService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping({"/admin", "/admin/reports"})
public class AdminReportController {

    @Autowired
    private IReportService reportService;

    @Autowired
    private IStoreService storeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Dashboard Quản Trị Hệ Thống & Báo Cáo Doanh Thu Trực Quan Với Chart.js:
     * - Admin: Toàn quyền xem tổng quan chuỗi, cơ cấu doanh thu tất cả chi nhánh.
     * - Store Manager: Chỉ xem báo cáo, doanh thu và đơn hàng của chi nhánh mình phụ trách.
     */
    @GetMapping({"", "/", "/dashboard", "/reports", "/analytics"})
    public String dashboard(
            @RequestParam(name = "year", required = false) Integer yearParam,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) throws JsonProcessingException {
        int year = (yearParam != null) ? yearParam : LocalDate.now().getYear();
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        Map<String, Object> summary;
        Map<Integer, BigDecimal> monthly;
        Map<String, BigDecimal> storeRevenue = new LinkedHashMap<>();
        Map<String, Long> statusDist;

        if (isAdmin) {
            // ADMIN: Dữ liệu toàn chuỗi
            summary = reportService.getDashboardSummary();
            monthly = reportService.getMonthlyRevenue(year);
            storeRevenue = reportService.getStoreRevenue();
            statusDist = reportService.getOrderStatusDistribution();
            model.addAttribute("topBooks", reportService.getTopSellingBooks(5));
        } else {
            // STORE MANAGER: Dữ liệu duy nhất của chi nhánh được phân công
            Long storeId = (managedStore != null) ? managedStore.getId() : -1L;
            summary = reportService.getDashboardSummaryByStore(storeId);
            monthly = reportService.getMonthlyRevenueByStore(year, storeId);
            String stName = (managedStore != null) ? managedStore.getStoreName() : "Chi Nhánh Của Bạn";
            storeRevenue.put(stName, (BigDecimal) summary.getOrDefault("totalRevenue", BigDecimal.ZERO));
            statusDist = reportService.getOrderStatusDistributionByStore(storeId);
            model.addAttribute("topBooks", reportService.getTopSellingBooksByStore(storeId, 5));
        }

        // 1. Thẻ tổng quan
        model.addAttribute("summary", summary);
        model.addAttribute("selectedYear", year);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("managedStore", managedStore);

        // 2. Doanh thu 12 tháng phục vụ Biểu đồ đường (Line Chart)
        List<BigDecimal> monthlyData = new ArrayList<>(monthly.values());
        model.addAttribute("monthlyRevenueJson", objectMapper.writeValueAsString(monthlyData));

        // 3. Doanh thu theo chi nhánh phục vụ Biểu đồ cột / Bar Chart
        model.addAttribute("storeLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(storeRevenue.keySet())));
        model.addAttribute("storeDataJson", objectMapper.writeValueAsString(new ArrayList<>(storeRevenue.values())));

        // 4. Cơ cấu đơn hàng theo trạng thái phục vụ Biểu đồ tròn / Doughnut Chart
        model.addAttribute("statusLabelsJson", objectMapper.writeValueAsString(new ArrayList<>(statusDist.keySet())));
        model.addAttribute("statusDataJson", objectMapper.writeValueAsString(new ArrayList<>(statusDist.values())));

        return "admin/reports/dashboard";
    }

    /**
     * Xuất báo cáo đơn hàng ra file Excel (.xlsx):
     * - Admin: Xuất toàn bộ đơn hàng của toàn hệ thống.
     * - Store Manager: Chỉ xuất các đơn hàng thuộc chi nhánh phụ trách.
     */
    @GetMapping("/reports/export-excel")
    public void exportExcel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) throws IOException {
        boolean isAdmin = userDetails != null && "ROLE_ADMIN".equals(userDetails.getRoleName());
        Store managedStore = (userDetails != null) ? storeService.findByManagerId(userDetails.getId()) : null;

        if (isAdmin) {
            reportService.exportOrdersToExcel(response);
        } else if (managedStore != null) {
            reportService.exportStoreOrdersToExcel(response, managedStore.getId());
        }
    }
}
