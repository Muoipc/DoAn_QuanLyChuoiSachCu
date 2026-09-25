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
            @RequestParam(value = "cond", required = false) Integer minCondition,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "store", required = false) Long storeId,
            @RequestParam(value = "sort", defaultValue = "popular") String sort,
            Model model) {

        // Hỗ trợ cả 2 tham số: ?keyword=... (chuẩn Shopee) và ?q=... (chuẩn tìm kiếm thông dụng)
        String effectiveQuery = (keyword != null && !keyword.isBlank()) ? keyword : query;

        List<Book> allBooks = bookRepository.findAllActiveWithImages();

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
        if (categoryId != null) {
            allBooks = allBooks.stream().filter(b ->
                    b.getCategory() != null && categoryId.equals(b.getCategory().getId())
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
            allBooks = allBooks.stream().filter(b ->
                    b.getPrice() != null && b.getPrice().compareTo(minPrice) >= 0
            ).collect(Collectors.toList());
        }
        if (maxPrice != null) {
            allBooks = allBooks.stream().filter(b ->
                    b.getPrice() != null && b.getPrice().compareTo(maxPrice) <= 0
            ).collect(Collectors.toList());
        }

        // 5. Lọc theo chi nhánh kho còn hàng
        if (storeId != null) {
            List<Inventory> storeInventory = inventoryRepository.findByStoreId(storeId);
            Set<Long> inStockBookIds = storeInventory.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0)
                    .map(inv -> inv.getBook().getId())
                    .collect(Collectors.toSet());

            allBooks = allBooks.stream().filter(b ->
                    inStockBookIds.contains(b.getId())
            ).collect(Collectors.toList());
        }

        // 6. Sắp xếp kết quả
        if ("sales".equalsIgnoreCase(sort)) {
            allBooks.sort((b1, b2) -> Integer.compare(
                    b2.getTotalSold() != null ? b2.getTotalSold() : 0,
                    b1.getTotalSold() != null ? b1.getTotalSold() : 0
            ));
        } else if ("price-asc".equalsIgnoreCase(sort)) {
            allBooks.sort(Comparator.comparing(Book::getPrice));
        } else if ("price-desc".equalsIgnoreCase(sort)) {
            allBooks.sort((b1, b2) -> b2.getPrice().compareTo(b1.getPrice()));
        } else if ("latest".equalsIgnoreCase(sort)) {
            allBooks.sort((b1, b2) -> {
                if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
                return b2.getCreatedAt().compareTo(b1.getCreatedAt());
            });
        } else {
            // "popular": Sắp xếp theo lượt xem
            allBooks.sort((b1, b2) -> Integer.compare(
                    b2.getViewsCount() != null ? b2.getViewsCount() : 0,
                    b1.getViewsCount() != null ? b1.getViewsCount() : 0
            ));
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

        model.addAttribute("books", allBooks);
        model.addAttribute("totalFound", allBooks.size());
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
