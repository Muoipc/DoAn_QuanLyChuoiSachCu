package vn.iotstar.service;

import vn.iotstar.dto.VoucherResponseDTO;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserVoucher;
import vn.iotstar.entity.Voucher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ xử lý Voucher:
 * - Phân hệ Client: Kho voucher người dùng, kiểm tra & tính giảm giá khi checkout.
 * - Phân hệ Admin: Quản lý CRUD danh mục khuyến mãi, bật/tắt kích hoạt voucher.
 */
public interface IVoucherService {

    // ================= CLIENT METHODS =================

    /**
     * Lấy danh sách toàn bộ voucher còn hạn, còn lượt sử dụng và đang kích hoạt trên hệ thống.
     * Tự động kiểm tra và đánh dấu xem người dùng hiện tại đã lưu voucher vào ví cá nhân chưa.
     */
    List<VoucherResponseDTO> getAllAvailableVouchers(User user);

    /**
     * Lấy danh sách voucher trong ví của người dùng theo trạng thái sử dụng.
     */
    List<UserVoucher> getUserSavedVouchers(User user, Boolean isUsed);

    /**
     * Lưu một voucher vào ví cá nhân của người dùng.
     */
    UserVoucher saveVoucherForUser(User user, String voucherCode);

    /**
     * Kiểm tra tính hợp lệ của mã voucher đối với giá trị đơn hàng cụ thể.
     */
    Voucher validateVoucher(String code, BigDecimal orderAmount);

    /**
     * Tính toán số tiền được giảm giá chính xác dựa trên cấu hình voucher và giá trị đơn hàng.
     */
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount);

    /**
     * Đếm tổng số voucher chưa sử dụng trong ví của người dùng.
     */
    long countUnusedVouchers(User user);

    /**
     * Tìm kiếm thông tin voucher theo mã code.
     */
    Optional<Voucher> findByCode(String code);

    /**
     * Đánh dấu voucher trong ví của người dùng đã được sử dụng sau khi đặt hàng thành công.
     */
    void markVoucherAsUsed(User user, Voucher voucher);

    // ================= ADMIN METHODS =================

    /**
     * Lấy danh sách tất cả voucher phục vụ phân hệ quản trị.
     */
    List<Voucher> findAll();

    /**
     * Tìm chi tiết voucher theo ID.
     */
    Voucher findById(Long id);

    /**
     * Lưu hoặc cập nhật thông tin voucher.
     */
    Voucher save(Voucher voucher);

    /**
     * Xóa voucher theo ID.
     */
    void deleteById(Long id);

    /**
     * Chuyển đổi trạng thái kích hoạt (Active / Inactive) của voucher.
     */
    void toggleActive(Long id);
}
