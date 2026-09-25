package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.VoucherResponseDTO;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserVoucher;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.UserVoucherRepository;
import vn.iotstar.repository.VoucherRepository;
import vn.iotstar.service.IVoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lớp triển khai các dịch vụ nghiệp vụ Voucher và Kho Voucher cá nhân.
 */
@Service
public class VoucherServiceImpl implements IVoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Autowired
    public VoucherServiceImpl(VoucherRepository voucherRepository, UserVoucherRepository userVoucherRepository) {
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    /**
     * Lấy toàn bộ danh sách voucher còn hạn, còn lượt sử dụng và đang kích hoạt.
     * Đánh dấu voucher nào người dùng đã lưu vào ví (isSaved).
     *
     * @param user Người dùng hiện tại (có thể null nếu chưa đăng nhập)
     * @return Danh sách VoucherResponseDTO
     */
    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponseDTO> getAllAvailableVouchers(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> availableVouchers = voucherRepository.findAvailableVouchers(now);

        if (availableVouchers.isEmpty()) {
            return Collections.emptyList();
        }

        // Nếu người dùng chưa đăng nhập, trả về danh sách với trạng thái chưa lưu
        if (user == null || user.getId() == null) {
            return availableVouchers.stream()
                    .map(v -> new VoucherResponseDTO(v, false, false, null))
                    .collect(Collectors.toList());
        }

        // Nếu người dùng đã đăng nhập, lấy toàn bộ voucher trong ví để ánh xạ trạng thái
        List<UserVoucher> savedList = userVoucherRepository.findByUser(user);
        Map<Long, UserVoucher> savedVoucherMap = savedList.stream()
                .filter(uv -> uv.getVoucher() != null && uv.getVoucher().getId() != null)
                .collect(Collectors.toMap(
                        uv -> uv.getVoucher().getId(),
                        uv -> uv,
                        (existing, replacement) -> existing
                ));

        return availableVouchers.stream().map(v -> {
            UserVoucher userVoucher = savedVoucherMap.get(v.getId());
            boolean isSaved = (userVoucher != null);
            Boolean isUsed = (userVoucher != null ? userVoucher.getIsUsed() : false);
            Long userVoucherId = (userVoucher != null ? userVoucher.getId() : null);
            return new VoucherResponseDTO(v, isSaved, isUsed, userVoucherId);
        }).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách voucher trong ví của người dùng theo trạng thái sử dụng.
     *
     * @param user   Người dùng sở hữu ví voucher
     * @param isUsed Trạng thái đã sử dụng (null: lấy tất cả, true: đã dùng, false: chưa dùng)
     * @return Danh sách UserVoucher
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserVoucher> getUserSavedVouchers(User user, Boolean isUsed) {
        if (user == null || user.getId() == null) {
            return Collections.emptyList();
        }

        if (isUsed == null) {
            return userVoucherRepository.findByUserOrderBySavedAtDesc(user);
        }

        return userVoucherRepository.findByUserAndIsUsedOrderBySavedAtDesc(user, isUsed);
    }

    /**
     * Lưu một voucher vào ví cá nhân của người dùng.
     * Kiểm tra chặt chẽ các điều kiện hợp lệ: tồn tại, còn hạn, còn lượt, chưa từng lưu.
     *
     * @param user        Người dùng thực hiện lưu
     * @param voucherCode Mã code của voucher
     * @return UserVoucher đã được lưu
     */
    @Override
    @Transactional
    public UserVoucher saveVoucherForUser(User user, String voucherCode) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để lưu voucher vào ví cá nhân");
        }

        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }

        String cleanCode = voucherCode.trim().toUpperCase();

        // 1. Kiểm tra tồn tại và trạng thái kích hoạt của voucher
        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã voucher '" + cleanCode + "' không tồn tại hoặc đã ngừng áp dụng"));

        LocalDateTime now = LocalDateTime.now();

        // 2. Kiểm tra thời gian hiệu lực
        if (now.isBefore(voucher.getStartDate())) {
            throw new IllegalArgumentException("Mã voucher chưa đến đợt áp dụng");
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Mã voucher đã hết hạn sử dụng");
        }

        // 3. Kiểm tra giới hạn số lượt sử dụng toàn hệ thống
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Mã voucher đã hết lượt sử dụng trên hệ thống");
        }

        // 4. Kiểm tra người dùng đã lưu mã này chưa
        boolean alreadySaved = userVoucherRepository.existsByUserAndVoucher(user, voucher);
        if (alreadySaved) {
            throw new IllegalArgumentException("Bạn đã lưu voucher '" + cleanCode + "' trong ví rồi");
        }

        // 5. Lưu vào bảng user_vouchers
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucher.setIsUsed(false);
        userVoucher.setSavedAt(now);

        return userVoucherRepository.save(userVoucher);
    }

    /**
     * Kiểm tra mã voucher có áp dụng được cho đơn hàng hay không.
     *
     * @param code        Mã voucher cần kiểm tra
     * @param orderAmount Tổng giá trị đơn hàng trước khi giảm giá
     * @return Thực thể Voucher nếu hợp lệ
     */
    @Override
    @Transactional(readOnly = true)
    public Voucher validateVoucher(String code, BigDecimal orderAmount) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã giảm giá");
        }

        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng không hợp lệ");
        }

        String cleanCode = code.trim().toUpperCase();

        // 1. Kiểm tra tồn tại & trạng thái active
        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá '" + cleanCode + "' không tồn tại hoặc đã bị khóa"));

        LocalDateTime now = LocalDateTime.now();

        // 2. Kiểm tra ngày bắt đầu
        if (now.isBefore(voucher.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày có hiệu lực");
        }

        // 3. Kiểm tra ngày hết hạn
        if (now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng");
        }

        // 4. Kiểm tra số lượt sử dụng
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng trên hệ thống");
        }

        // 5. Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderAmount() != null && orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException(String.format(
                    "Đơn hàng chưa đạt giá trị tối thiểu %,d đ để áp dụng mã giảm giá này",
                    voucher.getMinOrderAmount().longValue()
            ));
        }

        return voucher;
    }

    /**
     * Tính số tiền giảm giá chính xác cho đơn hàng dựa theo cấu hình của Voucher.
     *
     * @param voucher     Thực thể Voucher
     * @param orderAmount Tổng giá trị đơn hàng
     * @return Số tiền được giảm giá (BigDecimal)
     */
    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        if (voucher == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Kiểm tra đơn hàng có đạt giá trị tối thiểu không
        if (voucher.getMinOrderAmount() != null && orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            // Giảm số tiền cố định
            discount = voucher.getDiscountValue();
            // Số tiền giảm không được vượt quá tổng giá trị đơn hàng
            if (discount.compareTo(orderAmount) > 0) {
                discount = orderAmount;
            }
        } else if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {
            // Giảm theo phần trăm
            BigDecimal percent = voucher.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discount = orderAmount.multiply(percent);

            // Giới hạn mức giảm tối đa nếu maxDiscount được quy định và lớn hơn 0
            if (voucher.getMaxDiscount() != null && voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                if (discount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discount = voucher.getMaxDiscount();
                }
            }

            // Số tiền giảm không được vượt quá đơn hàng
            if (discount.compareTo(orderAmount) > 0) {
                discount = orderAmount;
            }
        }

        // Làm tròn 2 chữ số thập phân phù hợp định dạng tiền tệ lưu trữ trong cơ sở dữ liệu
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Đếm tổng số voucher chưa sử dụng trong ví của người dùng.
     *
     * @param user Người dùng cần kiểm tra
     * @return Số lượng voucher khả dụng trong ví
     */
    @Override
    @Transactional(readOnly = true)
    public long countUnusedVouchers(User user) {
        if (user == null || user.getId() == null) {
            return 0L;
        }
        return userVoucherRepository.countByUserAndIsUsedFalse(user);
    }

    /**
     * Tìm kiếm thông tin voucher theo mã code.
     *
     * @param code Mã voucher
     * @return Optional chứa Voucher
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return voucherRepository.findByCodeIgnoreCase(code.trim());
    }

    /**
     * Đánh dấu voucher trong ví của người dùng đã sử dụng và cập nhật lượt dùng của voucher.
     *
     * @param user    Người dùng thực hiện mua hàng
     * @param voucher Voucher đã được áp dụng
     */
    @Override
    @Transactional
    public void markVoucherAsUsed(User user, Voucher voucher) {
        if (voucher == null) {
            return;
        }

        // Tăng usedCount của voucher
        int currentUsed = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
        voucher.setUsedCount(currentUsed + 1);
        voucherRepository.save(voucher);

        // Nếu người dùng có trong ví thì đánh dấu đã dùng
        if (user != null && user.getId() != null) {
            Optional<UserVoucher> uvOpt = userVoucherRepository.findByUserAndVoucher(user, voucher);
            if (uvOpt.isPresent()) {
                UserVoucher uv = uvOpt.get();
                uv.setIsUsed(true);
                uv.setUsedAt(LocalDateTime.now());
                userVoucherRepository.save(uv);
            }
        }
    }
}
