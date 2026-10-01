package vn.iotstar.service;

import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.Book;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IReportService {
    Map<String, Object> getDashboardSummary();
    Map<Integer, BigDecimal> getMonthlyRevenue(int year);
    Map<String, BigDecimal> getStoreRevenue();
    Map<String, Long> getOrderStatusDistribution();
    List<Book> getTopSellingBooks(int limit);
    void exportOrdersToExcel(HttpServletResponse response) throws IOException;
}
