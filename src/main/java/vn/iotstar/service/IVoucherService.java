package vn.iotstar.service;

import vn.iotstar.dto.VoucherResponseDTO;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserVoucher;
import vn.iotstar.entity.Voucher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ xử lý Voucher và Kho Voucher cá nhân của người dùng.
 */
public interface IVoucherService {

    /**
     * Lấy danh sách toàn bộ voucher còn hạn, còn lượt sử dụng và đang kích hoạt trên hệ thống.
     * Tự động kiểm tra và đánh dấu xem người dùng hiện tại đã lưu voucher vào ví cá nhân chưa.
     *
     * @param user Người dùng hiện tại (có thể null nếu khách chưa đăng nhập)
     * @return Danh sách VoucherResponseDTO có kèm cờ isSaved
     */
    List<VoucherResponseDTO> getAllAvailableVouchers(User user);

    /**
     * Lấy danh sách voucher trong ví của người dùng theo trạng thái sử dụng.
     *
     * @param user   Người dùng sở hữu ví voucher
     * @param isUsed Trạng thái đã sử dụng (null: lấy toàn bộ, true: đã dùng, false: chưa dùng)
     * @return Danh sách các UserVoucher tương ứng
     */
    List<UserVoucher> getUserSavedVouchers(User user, Boolean isUsed);

    /**
     * Lưu một voucher vào ví cá nhân của người dùng.
     * Kiểm tra chặt chẽ các điều kiện hợp lệ:
     * 1. Mã voucher tồn tại và đang kích hoạt.
     * 2. Voucher đang trong thời gian hiệu lực (startDate <= now <= endDate).
     * 3. Voucher chưa vượt quá số lượt dùng toàn hệ thống (usedCount < usageLimit).
     * 4. Người dùng chưa từng lưu voucher này vào ví trước đó.
     *
     * @param user        Người dùng thực hiện lưu
     * @param voucherCode Mã code của voucher cần lưu
     * @return Bản ghi UserVoucher mới được tạo
     * @throws IllegalArgumentException nếu mã không tồn tại, hết hạn, hết lượt hoặc đã được lưu
     */
    UserVoucher saveVoucherForUser(User user, String voucherCode);

    /**
     * Kiểm tra tính hợp lệ của mã voucher đối với giá trị đơn hàng cụ thể.
     * Kiểm tra trạng thái kích hoạt, thời hạn, số lượt dùng còn lại, và giá trị đơn hàng tối thiểu (minOrderAmount).
     *
     * @param code        Mã voucher cần kiểm tra
     * @param orderAmount Tổng giá trị đơn hàng trước khi áp mã
     * @return Thực thể Voucher hợp lệ sẵn sàng để áp dụng
     * @throws IllegalArgumentException nếu voucher không hợp lệ hoặc đơn hàng không đủ điều kiện
     */
    Voucher validateVoucher(String code, BigDecimal orderAmount);

    /**
     * Tính toán số tiền được giảm giá chính xác dựa trên cấu hình voucher và giá trị đơn hàng.
     * Hỗ trợ 2 hình thức:
     * - FIXED_AMOUNT: Giảm số tiền cố định (tối đa bằng giá trị đơn hàng).
     * - PERCENT: Giảm theo tỷ lệ %, có áp dụng mức giảm tối đa (maxDiscount) nếu được cấu hình.
     *
     * @param voucher     Thực thể Voucher được áp dụng
     * @param orderAmount Tổng giá trị đơn hàng trước khi giảm
     * @return Số tiền thực tế được giảm giá (BigDecimal)
     */
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount);

    /**
     * Đếm tổng số voucher chưa sử dụng trong ví của người dùng.
     *
     * @param user Người dùng cần kiểm tra
     * @return Số lượng voucher khả dụng trong ví
     */
    long countUnusedVouchers(User user);

    /**
     * Tìm kiếm thông tin voucher theo mã code.
     *
     * @param code Mã voucher
     * @return Optional chứa Voucher
     */
    Optional<Voucher> findByCode(String code);

    /**
     * Đánh dấu voucher trong ví của người dùng đã được sử dụng sau khi đặt hàng thành công.
     * Đồng thời tăng số lượt đã sử dụng (usedCount) của voucher trên hệ thống.
     *
     * @param user    Người dùng thực hiện mua hàng
     * @param voucher Voucher đã được áp dụng
     */
    void markVoucherAsUsed(User user, Voucher voucher);
}
