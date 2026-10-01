package vn.iotstar.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.BookConsignment;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.*;
import vn.iotstar.service.IReportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements IReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BookConsignmentRepository consignmentRepository;

    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getOrderStatus() == Order.OrderStatus.DELIVERED || o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = allOrders.size();
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long pendingConsignments = consignmentRepository.countByStatus(BookConsignment.ConsignmentStatus.PENDING);

        summary.put("totalRevenue", totalRevenue);
        summary.put("totalOrders", totalOrders);
        summary.put("totalBooks", totalBooks);
        summary.put("totalUsers", totalUsers);
        summary.put("pendingConsignments", pendingConsignments);

        return summary;
    }

    @Override
    public Map<Integer, BigDecimal> getMonthlyRevenue(int year) {
        Map<Integer, BigDecimal> monthly = new TreeMap<>();
        for (int m = 1; m <= 12; m++) {
            monthly.put(m, BigDecimal.ZERO);
        }

        List<Order> orders = orderRepository.findAll();
        for (Order o : orders) {
            if (o.getOrderStatus() == Order.OrderStatus.DELIVERED || o.getPaymentStatus() == Order.PaymentStatus.PAID) {
                if (o.getCreatedAt() != null && o.getCreatedAt().getYear() == year) {
                    int month = o.getCreatedAt().getMonthValue();
                    BigDecimal current = monthly.get(month);
                    monthly.put(month, current.add(o.getFinalAmount()));
                }
            }
        }
        return monthly;
    }

    @Override
    public Map<String, BigDecimal> getStoreRevenue() {
        Map<String, BigDecimal> storeMap = new LinkedHashMap<>();
        List<Store> stores = storeRepository.findAll();
        for (Store s : stores) {
            storeMap.put(s.getStoreName(), BigDecimal.ZERO);
        }

        List<Order> orders = orderRepository.findAll();
        for (Order o : orders) {
            if (o.getStore() != null && (o.getOrderStatus() == Order.OrderStatus.DELIVERED || o.getPaymentStatus() == Order.PaymentStatus.PAID)) {
                String storeName = o.getStore().getStoreName();
                BigDecimal current = storeMap.getOrDefault(storeName, BigDecimal.ZERO);
                storeMap.put(storeName, current.add(o.getFinalAmount()));
            }
        }
        return storeMap;
    }

    @Override
    public Map<String, Long> getOrderStatusDistribution() {
        Map<String, Long> dist = new LinkedHashMap<>();
        for (Order.OrderStatus st : Order.OrderStatus.values()) {
            dist.put(st.name(), orderRepository.countByOrderStatus(st));
        }
        return dist;
    }

    @Override
    public List<Book> getTopSellingBooks(int limit) {
        return bookRepository.findTopSoldWithImages(1);
    }

    @Override
    public void exportOrdersToExcel(HttpServletResponse response) throws IOException {
        List<Order> orders = orderRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo Cáo Đơn Hàng");

            // Tạo Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BROWN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Dòng tiêu đề
            String[] headers = {
                    "STT", "Mã Đơn Hàng", "Ngày Đặt", "Người Nhận", "SĐT",
                    "Chi Nhánh", "Hình Thức Nhận", "Thanh Toán", "Trạng Thái Đơn",
                    "Tiền Sách (VNĐ)", "Cước Ship (VNĐ)", "Giảm Giá (VNĐ)", "Tổng Thu (VNĐ)"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Định dạng tiền tệ
            DataFormat format = workbook.createDataFormat();
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            int rowIdx = 1;
            for (Order o : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(o.getOrderCode());
                row.createCell(2).setCellValue(o.getCreatedAt() != null ? o.getCreatedAt().format(dtf) : "");
                row.createCell(3).setCellValue(o.getReceiverName() != null ? o.getReceiverName() : "");
                row.createCell(4).setCellValue(o.getReceiverPhone() != null ? o.getReceiverPhone() : "");
                row.createCell(5).setCellValue(o.getStore() != null ? o.getStore().getStoreName() : "");
                row.createCell(6).setCellValue(o.getDeliveryMethod() == Order.DeliveryMethod.STORE_PICKUP ? "Tại Cửa Hàng" : "Giao Tận Nơi");
                row.createCell(7).setCellValue(o.getPaymentMethod().name() + " (" + o.getPaymentStatus().name() + ")");
                row.createCell(8).setCellValue(o.getOrderStatus().name());

                Cell cSub = row.createCell(9);
                cSub.setCellValue(o.getSubtotal() != null ? o.getSubtotal().doubleValue() : 0);
                cSub.setCellStyle(currencyStyle);

                Cell cShip = row.createCell(10);
                cShip.setCellValue(o.getShippingFee() != null ? o.getShippingFee().doubleValue() : 0);
                cShip.setCellStyle(currencyStyle);

                Cell cDisc = row.createCell(11);
                cDisc.setCellValue(o.getDiscountAmount() != null ? o.getDiscountAmount().doubleValue() : 0);
                cDisc.setCellStyle(currencyStyle);

                Cell cFin = row.createCell(12);
                cFin.setCellValue(o.getFinalAmount() != null ? o.getFinalAmount().doubleValue() : 0);
                cFin.setCellStyle(currencyStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "BaoCao_DonHang_ChuoiSachCu_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            workbook.write(response.getOutputStream());
        }
    }
}
