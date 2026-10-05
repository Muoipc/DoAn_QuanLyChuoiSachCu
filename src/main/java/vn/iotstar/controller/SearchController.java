package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.StoreRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller Bộ lọc & Tìm kiếm đa tiêu chí chuẩn Shopee cho chuỗi sách cũ.
 * Ghi chú cho Cường:
 * Đáp ứng đầy đủ tiêu chí phân công đồ án:
 * - Lọc theo từ khóa tìm kiếm (tên sách, tác giả, nhà xuất bản, mã ISBN).
 * - Lọc theo Danh mục thể loại sách.
 * - Lọc theo Độ mới thẩm định (80% - 99%).
 * - Lọc theo Khoảng giá (Giá tối thiểu - Giá tối đa).
 * - Lọc theo Chi nhánh kho còn hàng (1 trong 5 chi nhánh TP.HCM).
 * - Sắp xếp đa dạng: Bán chạy nhất, Mới nhất, Phổ biến, Giá tăng dần, Giá giảm dần.
 */
@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public String searchBooks(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "cat", required = false) Integer categoryId,
            @RequestParam(value = "category", required = false) Integer categoryParam,
            @RequestParam(value = "categoryId", required = false) Integer categoryIdShopee,
            @RequestParam(value = "cond", required = false) Integer minCondition,
            @RequestParam(value = "minPrice", required = false) String minPriceStr,
            @RequestParam(value = "maxPrice", required = false) String maxPriceStr,
            @RequestParam(value = "store", required = false) Long storeId,
            @RequestParam(value = "sort", defaultValue = "popular") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        BigDecimal minPrice = null;
        if (minPriceStr != null && !minPriceStr.isBlank()) {
            try { minPrice = new BigDecimal(minPriceStr); } catch (Exception e) {}
        }
        BigDecimal maxPrice = null;
        if (maxPriceStr != null && !maxPriceStr.isBlank()) {
            try { maxPrice = new BigDecimal(maxPriceStr); } catch (Exception e) {}
        }

        // Hỗ trợ cả các tham số: ?keyword=... (chuẩn Shopee), ?q=..., ?categoryId=..., ?cat=..., ?category=...
        String effectiveQuery = (keyword != null && !keyword.isBlank()) ? keyword : query;
        Integer effectiveCategoryId = (categoryId != null) ? categoryId : (categoryIdShopee != null ? categoryIdShopee : categoryParam);

        List<Book> allBooks = new ArrayList<>(bookRepository.findAllActiveWithImages());

        // 1. Lọc theo từ khóa
        if (effectiveQuery != null && !effectiveQuery.isBlank()) {
            String qLower = effectiveQuery.trim().toLowerCase();
            allBooks = allBooks.stream().filter(b ->
                    (b.getTitle() != null && b.getTitle().toLowerCase().contains(qLower)) ||
                    (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(qLower)) ||
                    (b.getPublisher() != null && b.getPublisher().toLowerCase().contains(qLower)) ||
                    (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(qLower))
            ).collect(Collectors.toList());
        }

        // 2. Lọc theo danh mục
        if (effectiveCategoryId != null) {
            allBooks = allBooks.stream().filter(b ->
                    b.getCategory() != null && effectiveCategoryId.equals(b.getCategory().getId())
            ).collect(Collectors.toList());
        }

        // 3. Lọc theo độ mới tối thiểu (%)
        if (minCondition != null) {
            allBooks = allBooks.stream().filter(b ->
                    b.getConditionPercent() != null && b.getConditionPercent() >= minCondition
            ).collect(Collectors.toList());
        }

        // 4. Lọc theo khoảng giá
        if (minPrice != null) {
            BigDecimal finalMinPrice = minPrice;
            allBooks = allBooks.stream().filter(b ->
                    b.getPrice() != null && b.getPrice().compareTo(finalMinPrice) >= 0
            ).collect(Collectors.toList());
        }
        if (maxPrice != null) {
            BigDecimal finalMaxPrice = maxPrice;
            allBooks = allBooks.stream().filter(b ->
                    b.getPrice() != null && b.getPrice().compareTo(finalMaxPrice) <= 0
            ).collect(Collectors.toList());
        }

        // 5. Lọc theo chi nhánh kho còn hàng
        if (storeId != null) {
            List<Inventory> storeInventory = inventoryRepository.findByStoreId(storeId);
            Set<Long> inStockBookIds = storeInventory.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0 && inv.getBook() != null && inv.getBook().getId() != null)
                    .map(inv -> inv.getBook().getId())
                    .collect(Collectors.toSet());

            allBooks = allBooks.stream().filter(b ->
                    inStockBookIds.contains(b.getId())
            ).collect(Collectors.toList());
        }

        // 6. Sắp xếp kết quả (bảo vệ an toàn chống NullPointerException khi giá null)
        if ("sales".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted((b1, b2) -> Integer.compare(
                    b2.getTotalSold() != null ? b2.getTotalSold() : 0,
                    b1.getTotalSold() != null ? b1.getTotalSold() : 0
            )).collect(Collectors.toList());
        } else if ("price-asc".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted(Comparator.comparing(b -> b.getPrice() != null ? b.getPrice() : BigDecimal.ZERO)).collect(Collectors.toList());
        } else if ("price-desc".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted((b1, b2) -> {
                BigDecimal p1 = b1.getPrice() != null ? b1.getPrice() : BigDecimal.ZERO;
                BigDecimal p2 = b2.getPrice() != null ? b2.getPrice() : BigDecimal.ZERO;
                return p2.compareTo(p1);
            }).collect(Collectors.toList());
        } else if ("latest".equalsIgnoreCase(sort)) {
            allBooks = allBooks.stream().sorted((b1, b2) -> {
                if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
                return b2.getCreatedAt().compareTo(b1.getCreatedAt());
            }).collect(Collectors.toList());
        } else {
            // "popular": Sắp xếp theo lượt xem
            allBooks = allBooks.stream().sorted((b1, b2) -> Integer.compare(
                    b2.getViewsCount() != null ? b2.getViewsCount() : 0,
                    b1.getViewsCount() != null ? b1.getViewsCount() : 0
            )).collect(Collectors.toList());
        }

        List<Category> categories = categoryRepository.findByIsActiveTrue();
        List<Store> stores = storeRepository.findByIsActiveTrue();

        // Nếu không có sách khớp từ khóa, nạp sách bán chạy nhất để gợi ý chuẩn Shopee
        if (allBooks.isEmpty()) {
            List<Book> recommendedBooks = bookRepository.findTopSoldWithImages(1);
            if (recommendedBooks.isEmpty()) {
                recommendedBooks = bookRepository.findAllActiveWithImages().stream().limit(8).collect(Collectors.toList());
            }
            model.addAttribute("recommendedBooks", recommendedBooks);
        }

        // Phân trang 20 sách 1 trang
        int pageSize = 20;
        int totalItems = allBooks.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Book> pagedBooks = (totalItems > 0) ? allBooks.subList(fromIndex, toIndex) : allBooks;

        model.addAttribute("books", pagedBooks);
        model.addAttribute("totalFound", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("categories", categories);
        model.addAttribute("stores", stores);

        model.addAttribute("selectedQuery", effectiveQuery);
        model.addAttribute("selectedCat", categoryId);
        model.addAttribute("selectedCond", minCondition);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedStore", storeId);
        model.addAttribute("selectedSort", sort);

        model.addAttribute("pageTitle", "Tìm Kiếm Sách Cũ — Kết Quả Bộ Lọc Chuỗi TP.HCM");

        return "search";
    }
}
