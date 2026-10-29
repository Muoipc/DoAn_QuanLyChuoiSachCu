package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreFollower;
import vn.iotstar.entity.User;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.StoreFollowerRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IVoucherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller xử lý trang Cửa Hàng / Chi Nhánh Sách Cũ TP.HCM (Chuẩn Shopee Storefront).
 * Hỗ trợ tham số URL chuẩn Shopee:
 * /stores/{id}?categoryId={catId}&entryPoint=ShopByPDP&itemId={itemId}
 */
@Controller
@RequestMapping("/stores")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private IVoucherService voucherService;

    @Autowired
    private StoreFollowerRepository storeFollowerRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Danh sách 5 chi nhánh cửa hàng sách cũ TP.HCM.
     */
    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = storeRepository.findByIsActiveTrue();
        model.addAttribute("stores", stores);
        model.addAttribute("pageTitle", "Hệ Thống 5 Chi Nhánh Sách Cũ TP.HCM");
        return "stores";
    }

    /**
     * Chi tiết cửa hàng / chi nhánh theo ID (Shopee Shop Storefront).
     * Hỗ trợ bộ lọc categoryId từ PDP: /stores/{id}?categoryId=...&entryPoint=ShopByPDP&itemId=...
     */
    @GetMapping("/{id:[0-9]+}")
    public String storeDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "cat", required = false) Integer catParam,
            @RequestParam(value = "cond", required = false) Integer minCondition,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "entryPoint", required = false) String entryPoint,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "sort", defaultValue = "popular") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            jakarta.servlet.http.HttpServletRequest request,
            Model model) {

        Store store = storeRepository.findById(id).orElse(null);
        if (store == null || !Boolean.TRUE.equals(store.getIsActive())) {
            List<Store> all = storeRepository.findByIsActiveTrue();
            if (!all.isEmpty()) {
                store = all.get(0);
            } else {
                return "redirect:/";
            }
        }

        Integer effectiveCatId = (categoryId != null) ? categoryId : catParam;

        // 1. Lấy toàn bộ sách có tồn kho tại chi nhánh này
        List<Inventory> storeInventory = inventoryRepository.findByStoreIdWithBooks(store.getId());
        List<Book> booksInStore = storeInventory.stream()
                .map(Inventory::getBook)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Book> allStoreBooks = new ArrayList<>(booksInStore);

        // Danh sách sách bán chạy nhất của chi nhánh (Top 6)
        List<Book> bestSellerBooks = new ArrayList<>(allStoreBooks);
        bestSellerBooks.sort((b1, b2) -> {
            int s1 = b1.getTotalSold() != null ? b1.getTotalSold() : 0;
            int s2 = b2.getTotalSold() != null ? b2.getTotalSold() : 0;
            return Integer.compare(s2, s1);
        });
        if (bestSellerBooks.size() > 6) {
            bestSellerBooks = bestSellerBooks.subList(0, 6);
        }

        // Danh sách gợi ý cho bạn (Top 6)
        List<Book> recommendedBooks = new ArrayList<>(allStoreBooks);
        recommendedBooks.sort((b1, b2) -> {
            int v1 = b1.getViewsCount() != null ? b1.getViewsCount() : 0;
            int v2 = b2.getViewsCount() != null ? b2.getViewsCount() : 0;
            return Integer.compare(v2, v1);
        });
        if (recommendedBooks.size() > 6) {
            recommendedBooks = recommendedBooks.subList(0, 6);
        }

        // 2. Lấy danh sách thể loại có sách tại chi nhánh này kèm số lượng
        Map<Category, Long> categoryCountMap = booksInStore.stream()
                .filter(b -> b.getCategory() != null)
                .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));

        List<Category> storeCategories = new ArrayList<>(categoryCountMap.keySet());
        storeCategories.sort(Comparator.comparing(Category::getCategoryName));

        // 3. Lọc theo Category nếu có tham số categoryId
        Category currentCategory = null;
        if (effectiveCatId != null) {
            final Integer filterCatId = effectiveCatId;
            currentCategory = categoryRepository.findById(filterCatId).orElse(null);
            booksInStore = booksInStore.stream()
                    .filter(b -> b.getCategory() != null && b.getCategory().getId().equals(filterCatId))
                    .collect(Collectors.toList());
        }

        // 4. Lọc theo tình trạng sách cũ (cond)
        if (minCondition != null) {
            booksInStore = booksInStore.stream()
                    .filter(b -> b.getConditionPercent() != null && b.getConditionPercent() >= minCondition)
                    .collect(Collectors.toList());
        }

        // 5. Lọc theo khoảng giá (minPrice, maxPrice)
        if (minPrice != null) {
            booksInStore = booksInStore.stream()
                    .filter(b -> b.getMinPrice() != null && b.getMinPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            booksInStore = booksInStore.stream()
                    .filter(b -> b.getMinPrice() != null && b.getMinPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        // 6. Sắp xếp sách
        if ("newest".equals(sort)) {
            booksInStore.sort(Comparator.comparing(Book::getId).reversed());
        } else if ("sales".equals(sort)) {
            booksInStore.sort((b1, b2) -> {
                int s1 = b1.getTotalSold() != null ? b1.getTotalSold() : 0;
                int s2 = b2.getTotalSold() != null ? b2.getTotalSold() : 0;
                return Integer.compare(s2, s1);
            });
        } else if ("price_asc".equals(sort)) {
            booksInStore.sort(Comparator.comparing(Book::getMinPrice));
        } else if ("price_desc".equals(sort)) {
            booksInStore.sort(Comparator.comparing(Book::getMinPrice).reversed());
        } else {
            // popular
            booksInStore.sort((b1, b2) -> {
                int v1 = b1.getViewsCount() != null ? b1.getViewsCount() : 0;
                int v2 = b2.getViewsCount() != null ? b2.getViewsCount() : 0;
                return Integer.compare(v2, v1);
            });
        }

        // 7. Phân trang 20 sách 1 trang cho chi nhánh
        int pageSize = 20;
        int totalItems = booksInStore.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Book> pagedBooks = (totalItems > 0) ? booksInStore.subList(fromIndex, toIndex) : Collections.emptyList();

        // 8. Lấy voucher ưu đãi của cửa hàng
        try {
            model.addAttribute("vouchers", voucherService.getAllAvailableVouchers(null));
        } catch (Exception ignored) {}

        model.addAttribute("store", store);
        model.addAttribute("books", pagedBooks);
        model.addAttribute("totalBooks", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("bestSellerBooks", bestSellerBooks);
        model.addAttribute("recommendedBooks", recommendedBooks);
        model.addAttribute("storeCategories", storeCategories);
        boolean isLoggedIn = (userDetails != null && userDetails.getId() != null);
        model.addAttribute("isLoggedIn", isLoggedIn);

        boolean isOnline = StoreChatController.isStoreOnline(store);
        model.addAttribute("isStoreOnline", isOnline);

        boolean isFollowed = false;
        if (isLoggedIn) {
            isFollowed = storeFollowerRepository.existsByUserIdAndStoreId(userDetails.getId(), id);
        }
        model.addAttribute("isFollowed", isFollowed);

        long followersCount = storeFollowerRepository.countByStoreId(store.getId());
        String joinYear = store.getCreatedAt() != null ? String.valueOf(store.getCreatedAt().getYear()) : "2024";
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("joinYear", joinYear);

        model.addAttribute("categoryCountMap", categoryCountMap);
        model.addAttribute("selectedCategoryId", effectiveCatId);
        model.addAttribute("selectedCategory", currentCategory);
        model.addAttribute("minCondition", minCondition);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentSort", sort);
        model.addAttribute("entryPoint", entryPoint);
        model.addAttribute("itemId", itemId);
        model.addAttribute("pageTitle", store.getStoreName() + " — Chuỗi Sách Cũ TP.HCM");

        return "store-detail";
    }

    /**
     * Chi tiết cửa hàng theo slug.
     */
    @GetMapping("/slug/{slug}")
    public String storeDetailBySlug(
            @PathVariable("slug") String slug,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "cond", required = false) Integer minCondition,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "entryPoint", required = false) String entryPoint,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "sort", defaultValue = "popular") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            jakarta.servlet.http.HttpServletRequest request,
            Model model) {
        Store store = storeRepository.findBySlug(slug).orElse(null);
        if (store != null) {
            return storeDetail(store.getId(), categoryId, null, minCondition, minPrice, maxPrice, entryPoint, itemId, sort, page, userDetails, request, model);
        }
        return "redirect:/stores";
    }

    /**
     * API Toggle Theo Dõi chi nhánh cửa hàng bền vững trong CSDL MySQL.
     * URL: POST /stores/api/toggle-follow/{id}
     */
    @PostMapping("/api/toggle-follow/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> toggleFollowStore(
            @PathVariable("id") Long storeId,
            jakarta.servlet.http.HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> resp = new HashMap<>();
        if (userDetails == null || userDetails.getId() == null) {
            resp.put("success", false);
            resp.put("requireLogin", true);
            resp.put("message", "Vui lòng đăng nhập để theo dõi chi nhánh!");
            return org.springframework.http.ResponseEntity.status(401).body(resp);
        }

        Long userId = userDetails.getId();
        boolean exists = storeFollowerRepository.existsByUserIdAndStoreId(userId, storeId);
        boolean isNowFollowed;
        if (exists) {
            storeFollowerRepository.deleteByUserIdAndStoreId(userId, storeId);
            isNowFollowed = false;
        } else {
            User user = userRepository.findById(userId).orElse(null);
            Store store = storeRepository.findById(storeId).orElse(null);
            if (user != null && store != null) {
                storeFollowerRepository.save(new StoreFollower(user, store));
            }
            isNowFollowed = true;
        }

        long updatedFollowersCount = storeFollowerRepository.countByStoreId(storeId);
        resp.put("success", true);
        resp.put("followed", isNowFollowed);
        resp.put("followersCount", updatedFollowersCount);
        resp.put("message", isNowFollowed ? "Đã theo dõi chi nhánh thành công!" : "Đã hủy theo dõi chi nhánh!");
        return org.springframework.http.ResponseEntity.ok(resp);
    }
}
