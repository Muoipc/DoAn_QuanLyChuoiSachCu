package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Voucher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng vouchers.
 * Quản lý các voucher khuyến mãi của hệ thống chuỗi cửa hàng sách cũ.
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    /**
     * Tìm voucher theo mã code chính xác.
     *
     * @param code Mã code voucher (ví dụ: FREESHIP15K)
     * @return Optional chứa Voucher nếu tồn tại
     */
    Optional<Voucher> findByCode(String code);

    /**
     * Tìm voucher theo mã code không phân biệt chữ hoa chữ thường.
     *
     * @param code Mã code voucher
     * @return Optional chứa Voucher nếu tồn tại
     */
    Optional<Voucher> findByCodeIgnoreCase(String code);

    /**
     * Tìm voucher theo mã code và trạng thái đang kích hoạt (isActive = true).
     *
     * @param code Mã code voucher
     * @return Optional chứa Voucher nếu tồn tại và đang hoạt động
     */
    Optional<Voucher> findByCodeAndIsActiveTrue(String code);

    /**
     * Tìm voucher theo mã code (không phân biệt hoa thường) và đang hoạt động.
     *
     * @param code Mã code voucher
     * @return Optional chứa Voucher nếu tồn tại và đang hoạt động
     */
    Optional<Voucher> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    /**
     * Kiểm tra sự tồn tại của voucher theo mã code.
     *
     * @param code Mã voucher
     * @return true nếu mã đã tồn tại trên hệ thống
     */
    boolean existsByCode(String code);

    /**
     * Lấy danh sách tất cả các voucher đang được kích hoạt.
     *
     * @return Danh sách voucher có isActive = true
     */
    List<Voucher> findByIsActiveTrue();

    /**
     * Lấy danh sách tất cả các voucher đang khả dụng cho khách hàng tại thời điểm hiện tại:
     * 1. Đang kích hoạt (isActive = true).
     * 2. Trong khoảng thời gian hiệu lực (startDate <= now <= endDate).
     * 3. Chưa vượt quá giới hạn số lượt sử dụng tối đa (usedCount < usageLimit).
     * Sắp xếp theo ngày kết thúc tăng dần để người dùng ưu tiên voucher sắp hết hạn.
     *
     * @param now Thời điểm kiểm tra hiện tại
     * @return Danh sách voucher khả dụng
     */
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND v.startDate <= :now AND v.endDate >= :now " +
           "AND v.usedCount < v.usageLimit " +
           "ORDER BY v.endDate ASC")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now);
}
