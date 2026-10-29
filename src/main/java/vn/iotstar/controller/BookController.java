package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Review;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.ReviewRepository;
import vn.iotstar.repository.WishlistRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller xử lý chi tiết sách và xem tồn kho chuỗi 5 chi nhánh TP.HCM.
 * Ghi chú cho Cường: Controller này phụ trách:
 * 1. Nạp chi tiết cuốn sách (ảnh chụp thật, độ mới %, tình trạng bìa/gáy/ruột sách theo thẩm định).
 * 2. Nạp tình trạng tồn kho thời gian thực tại 5 chi nhánh TP.HCM từ bảng inventory.
 * 3. Nạp danh sách đánh giá của độc giả và gợi ý các cuốn sách cùng thể loại.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private vn.iotstar.repository.StoreRepository storeRepository;

    @Autowired
    private vn.iotstar.repository.StoreFollowerRepository storeFollowerRepository;

    @Autowired
    private vn.iotstar.repository.VoucherRepository voucherRepository;

    /**
     * Xem chi tiết sách theo ID.
     * URL: /books/{id} (Ví dụ: /books/1)
     */
    @GetMapping("/{id}")
    public String bookDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        // 1. Lấy thông tin sách kèm ảnh và danh mục
        Optional<Book> bookOpt = bookRepository.findDetailById(id);
        if (bookOpt.isEmpty()) {
            bookOpt = bookRepository.findById(id);
        }

        if (bookOpt.isEmpty()) {
            return "redirect:/";
        }

        Book book = bookOpt.get();

        // 2. Tăng lượt xem (views_count)
        book.setViewsCount(book.getViewsCount() != null ? book.getViewsCount() + 1 : 1);
        bookRepository.save(book);

        // 3. Lấy thông tin tồn kho tại 5 chi nhánh TP.HCM
        List<Inventory> inventoryList = inventoryRepository.findByBookIdWithStore(id);
        int totalStock = inventoryList.stream().mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0).sum();

        // 4. Lấy danh sách đánh giá & điểm đánh giá trung bình
        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(id);
        Double avgRating = reviewRepository.getAverageRatingByBookId(id);
        long reviewCount = reviewRepository.countByBookId(id);
        if (avgRating == null || reviewCount == 0) {
            avgRating = 0.0;
        }

        long count5Star = reviewRepository.countByBookIdAndRating(id, 5);
        long count4Star = reviewRepository.countByBookIdAndRating(id, 4);
        long count3Star = reviewRepository.countByBookIdAndRating(id, 3);
        long count2Star = reviewRepository.countByBookIdAndRating(id, 2);
        long count1Star = reviewRepository.countByBookIdAndRating(id, 1);

        // 5. Lấy sách liên quan cùng danh mục
        List<Book> relatedBooks = List.of();
        if (book.getCategory() != null) {
            relatedBooks = bookRepository.findRelatedBooks(
                    book.getCategory().getId(),
                    book.getId(),
                    PageRequest.of(0, 6)
            );
        }

        // 5.1 Sách khác của cùng chi nhánh/shop (CÁC SẢN PHẨM KHÁC CỦA SHOP - Top 6)
        List<Book> otherStoreBooks = List.of();
        if (inventoryList != null && !inventoryList.isEmpty() && inventoryList.get(0).getStore() != null) {
            List<Inventory> storeInvs = inventoryRepository.findByStoreId(inventoryList.get(0).getStore().getId());
            otherStoreBooks = storeInvs.stream()
                    .map(Inventory::getBook)
                    .filter(b -> b != null && !b.getId().equals(id))
                    .distinct()
                    .sorted((b1, b2) -> {
                        int s1 = b1.getTotalSold() != null ? b1.getTotalSold() : 0;
                        int s2 = b2.getTotalSold() != null ? b2.getTotalSold() : 0;
                        return Integer.compare(s2, s1);
                    })
                    .limit(6)
                    .collect(Collectors.toList());
        }
        if (otherStoreBooks.isEmpty()) {
            otherStoreBooks = bookRepository.findAll(PageRequest.of(0, 6)).getContent().stream()
                    .filter(b -> !b.getId().equals(id))
                    .collect(Collectors.toList());
        }

        // 6. Kiểm tra trạng thái yêu thích (Wishlist) và lịch sử mua sách của người dùng hiện tại
        boolean isLiked = false;
        boolean hasPurchased = false;
        boolean hasReviewed = false;
        if (userDetails != null && userDetails.getId() != null) {
            Long currentUserId = userDetails.getId();
            isLiked = wishlistRepository.existsByUserIdAndBookId(currentUserId, id);
            hasPurchased = !reviewRepository.findDeliveredOrdersByUserIdAndBookId(currentUserId, id).isEmpty();
            hasReviewed = reviewRepository.existsByUserIdAndBookId(currentUserId, id);
        }
        long actualLikes = wishlistRepository.countByBookId(id);
        long likesCount = actualLikes;

        // 7. Đưa dữ liệu vào Model cho Thymeleaf render
        Store primaryStore = null;
        if (inventoryList != null && !inventoryList.isEmpty()) {
            primaryStore = inventoryList.get(0).getStore();
        } else {
            List<Store> allActive = storeRepository.findByIsActiveTrue();
            if (!allActive.isEmpty()) {
                primaryStore = allActive.get(0);
            }
        }
        model.addAttribute("store", primaryStore);
        if (primaryStore != null) {
            long storeBooksCount = inventoryRepository.countByStoreId(primaryStore.getId());
            model.addAttribute("storeBookCount", storeBooksCount > 0 ? storeBooksCount : totalStock);
            long storeFollowers = storeFollowerRepository.countByStoreId(primaryStore.getId());
            model.addAttribute("storeFollowers", String.valueOf(storeFollowers));
            model.addAttribute("storeJoinYear", primaryStore.getCreatedAt() != null ? String.valueOf(primaryStore.getCreatedAt().getYear()) : "2024");
            boolean isStoreOnline = StoreChatController.isStoreOnline(primaryStore);
            model.addAttribute("isStoreOnline", isStoreOnline);
        } else {
            model.addAttribute("isStoreOnline", false);
        }
        // 8. Tính toán số lượng tồn kho và giá bán theo từng phân loại tình trạng sách (Tiết kiệm / Tiêu chuẩn / Sưu tầm)
        int cond = book.getConditionPercent() != null ? book.getConditionPercent() : 90;
        String primaryTier = (cond <= 85) ? "budget" : ((cond >= 96) ? "collector" : "standard");

        long seed = (book.getId() != null ? book.getId() : 1L) * 2654435761L + totalStock * 31L;
        java.util.Random rng = new java.util.Random(seed);

        int[] budgetConds = {80, 82, 85};
        int[] standardConds = {90, 92, 95};
        int[] collectorConds = {98, 99};

        int budgetCond = budgetConds[rng.nextInt(budgetConds.length)];
        int standardCond = standardConds[rng.nextInt(standardConds.length)];
        int collectorCond = collectorConds[rng.nextInt(collectorConds.length)];

        java.math.BigDecimal thousand = new java.math.BigDecimal("1000");
        java.math.BigDecimal basePrice = book.getPrice() != null ? book.getPrice() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal budgetPrice;
        java.math.BigDecimal standardPrice;
        java.math.BigDecimal collectorPrice;

        if ("budget".equals(primaryTier)) {
            budgetCond = cond;
            budgetPrice = basePrice;
            standardPrice = basePrice.multiply(new java.math.BigDecimal("1.15")).divide(thousand, 0, java.math.RoundingMode.HALF_UP).multiply(thousand);
            collectorPrice = basePrice.multiply(new java.math.BigDecimal("1.30")).divide(thousand, 0, java.math.RoundingMode.HALF_UP).multiply(thousand);
        } else if ("collector".equals(primaryTier)) {
            collectorCond = cond;
            collectorPrice = basePrice;
            standardPrice = basePrice.multiply(new java.math.BigDecimal("0.90")).divide(thousand, 0, java.math.RoundingMode.HALF_UP).multiply(thousand);
            budgetPrice = basePrice.multiply(new java.math.BigDecimal("0.78")).divide(thousand, 0, java.math.RoundingMode.HALF_UP).multiply(thousand);
        } else {
            standardCond = cond;
            standardPrice = basePrice;
            budgetPrice = basePrice.multiply(new java.math.BigDecimal("0.85")).divide(thousand, 0, java.math.RoundingMode.HALF_UP).multiply(thousand);
            collectorPrice = basePrice.multiply(new java.math.BigDecimal("1.15")).divide(thousand, 0, java.math.RoundingMode.HALF_UP).multiply(thousand);
        }
        if (book.getOriginalPrice() != null && book.getOriginalPrice().compareTo(standardPrice) > 0
                && collectorPrice.compareTo(book.getOriginalPrice()) >= 0) {
            collectorPrice = book.getOriginalPrice().subtract(thousand).max(standardPrice);
        }

        int budgetStock = 0;
        int standardStock = 0;
        int collectorStock = 0;

        if (totalStock <= 0) {
            budgetStock = 0;
            standardStock = 0;
            collectorStock = 0;
        } else if (totalStock == 1) {
            // Chỉ có đúng 1 quyển duy nhất: chỉ mở đúng tình trạng thực tế của quyển đó
            if ("budget".equals(primaryTier)) budgetStock = 1;
            else if ("collector".equals(primaryTier)) collectorStock = 1;
            else standardStock = 1;
        } else if (totalStock == 2) {
            int roll = rng.nextInt(100);
            if (roll < 35) {
                if ("budget".equals(primaryTier)) budgetStock = 2;
                else if ("collector".equals(primaryTier)) collectorStock = 2;
                else standardStock = 2;
            } else {
                if ("budget".equals(primaryTier)) {
                    budgetStock = 1;
                    standardStock = 1;
                } else if ("collector".equals(primaryTier)) {
                    collectorStock = 1;
                    standardStock = 1;
                } else {
                    standardStock = 1;
                    if (rng.nextBoolean()) budgetStock = 1;
                    else collectorStock = 1;
                }
            }
        } else if (totalStock <= 6) {
            int roll = rng.nextInt(100);
            if (roll < 45) {
                // Chia vào 2 tình trạng, 1 tình trạng khóa (0 quyển)
                int mainShare = Math.max(2, Math.round(totalStock * 0.65f));
                int secShare = totalStock - mainShare;
                if ("budget".equals(primaryTier)) {
                    budgetStock = mainShare;
                    standardStock = secShare;
                } else if ("collector".equals(primaryTier)) {
                    collectorStock = mainShare;
                    standardStock = secShare;
                } else {
                    standardStock = mainShare;
                    if (rng.nextInt(100) < 65) budgetStock = secShare;
                    else collectorStock = secShare;
                }
            } else {
                // Chia vào cả 3 tình trạng
                collectorStock = 1;
                budgetStock = Math.max(1, totalStock / 3);
                standardStock = totalStock - budgetStock - collectorStock;
                if ("budget".equals(primaryTier) && budgetStock < standardStock) {
                    int tmp = budgetStock; budgetStock = standardStock; standardStock = tmp;
                } else if ("collector".equals(primaryTier) && collectorStock < standardStock) {
                    int tmp = collectorStock; collectorStock = standardStock; standardStock = tmp;
                }
            }
        } else {
            // Khi có nhiều sách (totalStock >= 7): đa số có đủ 3 tình trạng, một số ít hết hàng 1 phân loại hiếm
            int modeRoll = rng.nextInt(100);
            if (modeRoll < 82) {
                // 82% trường hợp có đủ cả 3 tình trạng với tỷ lệ tự nhiên
                double stdRatio = 0.48 + (rng.nextInt(15) / 100.0); // 48% - 62%
                double budRatio = 0.24 + (rng.nextInt(11) / 100.0); // 24% - 34%
                standardStock = Math.max(1, (int) Math.round(totalStock * stdRatio));
                budgetStock = Math.max(1, (int) Math.round(totalStock * budRatio));
                collectorStock = totalStock - standardStock - budgetStock;
                if (collectorStock < 1) {
                    collectorStock = 1;
                    standardStock = Math.max(1, totalStock - budgetStock - 1);
                }
                if ("budget".equals(primaryTier) && budgetStock < standardStock) {
                    int tmp = budgetStock; budgetStock = standardStock; standardStock = tmp;
                } else if ("collector".equals(primaryTier) && collectorStock < standardStock) {
                    int tmp = collectorStock; collectorStock = standardStock; standardStock = tmp;
                }
            } else if (modeRoll < 93 && !"collector".equals(primaryTier)) {
                // 11% trường hợp hết hàng Bản Sưu Tầm (98-99%), chỉ còn Bản Chuẩn và Bản Tiết Kiệm
                double stdRatio = 0.62 + (rng.nextInt(12) / 100.0);
                standardStock = Math.max(1, (int) Math.round(totalStock * stdRatio));
                budgetStock = Math.max(1, totalStock - standardStock);
                collectorStock = 0;
                if ("budget".equals(primaryTier) && budgetStock < standardStock) {
                    int tmp = budgetStock; budgetStock = standardStock; standardStock = tmp;
                }
            } else if (!"budget".equals(primaryTier)) {
                // 7% trường hợp hết hàng Bản Tiết Kiệm (80-85%), chỉ còn Bản Chuẩn và Bản Sưu Tầm
                double stdRatio = 0.72 + (rng.nextInt(12) / 100.0);
                standardStock = Math.max(1, (int) Math.round(totalStock * stdRatio));
                collectorStock = Math.max(1, totalStock - standardStock);
                budgetStock = 0;
                if ("collector".equals(primaryTier) && collectorStock < standardStock) {
                    int tmp = collectorStock; collectorStock = standardStock; standardStock = tmp;
                }
            } else {
                standardStock = Math.max(1, (int) Math.round(totalStock * 0.35));
                collectorStock = Math.max(1, (int) Math.round(totalStock * 0.15));
                budgetStock = totalStock - standardStock - collectorStock;
            }
        }

        Long budgetBookId = book.getId();
        Long standardBookId = book.getId();
        Long collectorBookId = book.getId();
        model.addAttribute("primaryTier", primaryTier);
        model.addAttribute("budgetStock", budgetStock);
        model.addAttribute("standardStock", standardStock);
        model.addAttribute("collectorStock", collectorStock);
        model.addAttribute("budgetPrice", budgetPrice);
        model.addAttribute("standardPrice", standardPrice);
        model.addAttribute("collectorPrice", collectorPrice);
        model.addAttribute("budgetCond", budgetCond);
        model.addAttribute("standardCond", standardCond);
        model.addAttribute("collectorCond", collectorCond);
        model.addAttribute("budgetBookId", budgetBookId);
        model.addAttribute("standardBookId", standardBookId);
        model.addAttribute("collectorBookId", collectorBookId);

        model.addAttribute("book", book);
        model.addAttribute("inventoryList", inventoryList);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", (reviewCount > 0 && avgRating > 0) ? String.format(java.util.Locale.US, "%.1f", avgRating) : "0.0");
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("count5Star", count5Star);
        model.addAttribute("count4Star", count4Star);
        model.addAttribute("count3Star", count3Star);
        model.addAttribute("count2Star", count2Star);
        model.addAttribute("count1Star", count1Star);
        model.addAttribute("hasPurchased", hasPurchased);
        model.addAttribute("hasReviewed", hasReviewed);
        model.addAttribute("isLoggedIn", userDetails != null);
        model.addAttribute("relatedBooks", relatedBooks);
        model.addAttribute("otherStoreBooks", otherStoreBooks);
        model.addAttribute("bestSellerBooks", otherStoreBooks);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likesCount", likesCount);
        model.addAttribute("vouchers", voucherRepository.findByIsActiveTrue());
        model.addAttribute("pageTitle", book.getTitle() + " - Chuỗi Sách Cũ");

        return "book-detail";
    }

    /**
     * Xem chi tiết sách theo Slug thân thiện SEO.
     * URL: /books/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public String bookDetailBySlug(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        Optional<Book> bookOpt = bookRepository.findDetailBySlug(slug);
        if (bookOpt.isPresent()) {
            return bookDetail(bookOpt.get().getId(), userDetails, model);
        }
        return "redirect:/";
    }
}
