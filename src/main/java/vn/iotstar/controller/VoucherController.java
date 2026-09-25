package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.VoucherResponseDTO;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserVoucher;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.IVoucherService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller và API xử lý Kho Voucher khuyến mãi và Ví Voucher cá nhân.
 */
@Controller
public class VoucherController {

    private static final Long DEFAULT_GUEST_USER_ID = 4L;

    private final IVoucherService voucherService;
    private final UserRepository userRepository;

    @Autowired
    public VoucherController(IVoucherService voucherService, UserRepository userRepository) {
        this.voucherService = voucherService;
        this.userRepository = userRepository;
    }

    /**
     * Lấy đối tượng User từ CustomUserDetails đang đăng nhập (hoặc fallback tài khoản demo).
     */
    private User resolveUser(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return userRepository.findById(userDetails.getId()).orElse(null);
        }
        return userRepository.findById(DEFAULT_GUEST_USER_ID).orElse(null);
    }

    /**
     * GET /vouchers: Trang Kho Voucher công khai.
     * Nhận param ?tab=all | freeship | book | exclusive.
     * Đưa vào Model: danh sách voucher, tab đang chọn, số lượng voucher đã lưu nếu user đã đăng nhập.
     * Trả về view 'vouchers'.
     */
    @GetMapping("/vouchers")
    public String viewVouchers(
            @RequestParam(value = "tab", defaultValue = "all") String tab,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        User user = resolveUser(userDetails);
        List<VoucherResponseDTO> allVouchers = voucherService.getAllAvailableVouchers(user);

        // Lọc danh sách voucher theo từng tab
        List<VoucherResponseDTO> filteredVouchers;
        String normalizedTab = (tab != null) ? tab.trim().toLowerCase() : "all";

        switch (normalizedTab) {
            case "freeship":
                filteredVouchers = allVouchers.stream()
                        .filter(v -> isFreeshipVoucher(v))
                        .collect(Collectors.toList());
                break;
            case "book":
                filteredVouchers = allVouchers.stream()
                        .filter(v -> isBookVoucher(v))
                        .collect(Collectors.toList());
                break;
            case "exclusive":
                filteredVouchers = allVouchers.stream()
                        .filter(v -> isExclusiveVoucher(v))
                        .collect(Collectors.toList());
                break;
            case "all":
            default:
                filteredVouchers = allVouchers;
                normalizedTab = "all";
                break;
        }

        long savedCount = (user != null) ? voucherService.countUnusedVouchers(user) : 0L;
        long countAll = allVouchers.size();
        long countShipping = allVouchers.stream().filter(this::isFreeshipVoucher).count();
        long countBook = allVouchers.stream().filter(this::isBookVoucher).count();
        long countExclusive = allVouchers.stream().filter(this::isExclusiveVoucher).count();

        model.addAttribute("vouchers", filteredVouchers);
        model.addAttribute("allVouchers", allVouchers);
        model.addAttribute("tab", normalizedTab);
        model.addAttribute("savedCount", savedCount);
        model.addAttribute("countAll", countAll);
        model.addAttribute("countShipping", countShipping);
        model.addAttribute("countBook", countBook);
        model.addAttribute("countExclusive", countExclusive);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Kho Voucher Khuyến Mãi - Chuỗi Cửa Hàng Sách Cũ TP.HCM");

        return "vouchers";
    }

    /**
     * POST /vouchers/save: Form submit lưu mã voucher.
     * Nhận mã voucher từ ô nhập liệu, lưu vào ví của user,
     * redirect về /vouchers kèm flash message (success/error).
     */
    @PostMapping("/vouchers/save")
    public String saveVoucher(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String targetCode = (code != null && !code.isBlank()) ? code : voucherCode;

        User user = resolveUser(userDetails);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để lưu mã voucher vào ví cá nhân!");
            return "redirect:/vouchers";
        }

        if (targetCode == null || targetCode.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mã giảm giá cần lưu!");
            return "redirect:/vouchers";
        }

        try {
            voucherService.saveVoucherForUser(user, targetCode.trim());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lưu mã voucher " + targetCode.trim().toUpperCase() + " vào ví thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu mã voucher: " + e.getMessage());
        }

        return "redirect:/vouchers";
    }

    /**
     * GET /user/vouchers: Xem danh sách voucher trong ví cá nhân của user đang đăng nhập.
     */
    @GetMapping("/user/vouchers")
    public String viewUserVouchers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        User user = resolveUser(userDetails);
        if (user == null) {
            return "redirect:/login";
        }

        List<UserVoucher> userVouchers = voucherService.getUserSavedVouchers(user, null);
        long savedCount = voucherService.countUnusedVouchers(user);

        // Đồng thời ánh xạ sang VoucherResponseDTO để linh hoạt cho template
        List<VoucherResponseDTO> voucherDtos = userVouchers.stream()
                .map(uv -> new VoucherResponseDTO(uv.getVoucher(), true, uv.getIsUsed(), uv.getId()))
                .collect(Collectors.toList());

        model.addAttribute("userVouchers", userVouchers);
        model.addAttribute("vouchers", voucherDtos);
        model.addAttribute("savedCount", savedCount);
        model.addAttribute("tab", "wallet");
        model.addAttribute("isWallet", true);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Ví Voucher Của Tôi - Chuỗi Sách Cũ TP.HCM");

        return "vouchers";
    }

    /**
     * POST /api/vouchers/save-ajax: Endpoint AJAX nhận { code: "..." } (JSON hoặc Form-urlencoded),
     * trả về JSON { success: true/false, message: "..." } để hỗ trợ nút bấm 'Lưu' tức thì không tải lại trang.
     */
    @PostMapping(value = "/api/vouchers/save-ajax", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveVoucherAjaxJson(
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String code = null;
        if (payload != null) {
            Object codeObj = payload.get("code");
            if (codeObj == null) {
                codeObj = payload.get("voucherCode");
            }
            if (codeObj != null) {
                code = codeObj.toString().trim();
            }
        }
        return handleSaveVoucherInternal(code, userDetails);
    }

    @PostMapping(value = "/api/vouchers/save-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveVoucherAjaxForm(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String targetCode = (code != null && !code.isBlank()) ? code : voucherCode;
        return handleSaveVoucherInternal(targetCode, userDetails);
    }

    private ResponseEntity<Map<String, Object>> handleSaveVoucherInternal(
            String code,
            CustomUserDetails userDetails) {

        Map<String, Object> response = new LinkedHashMap<>();

        User user = resolveUser(userDetails);
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để lưu mã giảm giá vào ví cá nhân!");
            return ResponseEntity.ok(response);
        }

        if (code == null || code.isBlank()) {
            response.put("success", false);
            response.put("message", "Mã voucher không được để trống!");
            return ResponseEntity.ok(response);
        }

        try {
            voucherService.saveVoucherForUser(user, code.trim());
            response.put("success", true);
            response.put("message", "Lưu mã voucher " + code.trim().toUpperCase() + " vào ví thành công!");
            response.put("savedCount", voucherService.countUnusedVouchers(user));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * POST /api/vouchers/check: Endpoint AJAX kiểm tra mã voucher và tính tiền giảm giá cho giỏ hàng / thanh toán.
     * Nhận { code: "...", orderAmount: 150000 },
     * trả về JSON { valid: true/false, discountAmount: ..., finalAmount: ..., message: "..." }.
     */
    @PostMapping("/api/vouchers/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkVoucherAjax(
            @RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> response = new LinkedHashMap<>();

        String code = "";
        BigDecimal orderAmount = BigDecimal.ZERO;

        if (payload != null) {
            Object codeObj = payload.get("code");
            if (codeObj == null) {
                codeObj = payload.get("voucherCode");
            }
            if (codeObj != null) {
                code = codeObj.toString().trim();
            }

            Object amtObj = payload.get("orderAmount");
            if (amtObj instanceof Number) {
                orderAmount = BigDecimal.valueOf(((Number) amtObj).doubleValue());
            } else if (amtObj != null && !amtObj.toString().isBlank()) {
                try {
                    orderAmount = new BigDecimal(amtObj.toString().trim());
                } catch (Exception ignored) {
                    orderAmount = BigDecimal.ZERO;
                }
            }
        }

        if (code.isBlank()) {
            response.put("valid", false);
            response.put("discountAmount", BigDecimal.ZERO);
            response.put("finalAmount", orderAmount);
            response.put("message", "Vui lòng nhập mã giảm giá");
            return ResponseEntity.ok(response);
        }

        try {
            Voucher voucher = voucherService.validateVoucher(code, orderAmount);
            BigDecimal discountAmount = voucherService.calculateDiscount(voucher, orderAmount);
            BigDecimal finalAmount = orderAmount.subtract(discountAmount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            response.put("valid", true);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", finalAmount);
            response.put("code", voucher.getCode());
            response.put("description", voucher.getDescription());
            response.put("discountType", voucher.getDiscountType().name());
            response.put("discountValue", voucher.getDiscountValue());
            response.put("message", "Áp dụng mã giảm giá thành công! Giảm " + String.format("%,d", discountAmount.longValue()) + " đ");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("valid", false);
            response.put("discountAmount", BigDecimal.ZERO);
            response.put("finalAmount", orderAmount);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("discountAmount", BigDecimal.ZERO);
            response.put("finalAmount", orderAmount);
            response.put("message", "Mã voucher không hợp lệ: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // Helper phân loại tab voucher
    private boolean isFreeshipVoucher(VoucherResponseDTO v) {
        String code = v.getCode() != null ? v.getCode().toUpperCase() : "";
        String desc = v.getDescription() != null ? v.getDescription().toLowerCase() : "";
        return code.contains("SHIP") || desc.contains("vận chuyển") || desc.contains("giao hàng") || desc.contains("freeship");
    }

    private boolean isBookVoucher(VoucherResponseDTO v) {
        String code = v.getCode() != null ? v.getCode().toUpperCase() : "";
        String desc = v.getDescription() != null ? v.getDescription().toLowerCase() : "";
        return code.contains("SACH") || code.contains("BOOK") || desc.contains("sách");
    }

    private boolean isExclusiveVoucher(VoucherResponseDTO v) {
        String code = v.getCode() != null ? v.getCode().toUpperCase() : "";
        String desc = v.getDescription() != null ? v.getDescription().toLowerCase() : "";
        return code.contains("HOISINH") || code.contains("VIP") || code.contains("DOCQUYEN")
                || code.contains("EXCLUSIVE") || desc.contains("độc quyền") || desc.contains("hội sách")
                || v.getDiscountType() == Voucher.DiscountType.PERCENT;
    }
}
