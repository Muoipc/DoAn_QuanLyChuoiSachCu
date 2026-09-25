package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserVoucher;
import vn.iotstar.entity.Voucher;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng user_vouchers.
 * Cung cấp các truy vấn lấy danh sách voucher trong ví của người dùng, kiểm tra trạng thái lưu và đếm voucher khả dụng.
 */
@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    /**
     * Lấy toàn bộ danh sách voucher trong ví của người dùng.
     *
     * @param user Thực thể người dùng
     * @return Danh sách UserVoucher thuộc về user
     */
    List<UserVoucher> findByUser(User user);

    /**
     * Lấy danh sách voucher trong ví của người dùng theo ID, sắp xếp giảm dần theo thời gian lưu.
     *
     * @param userId ID người dùng
     * @return Danh sách UserVoucher
     */
    List<UserVoucher> findByUser_IdOrderBySavedAtDesc(Long userId);

    /**
     * Lấy toàn bộ danh sách voucher trong ví của người dùng, sắp xếp giảm dần theo thời gian lưu.
     *
     * @param user Thực thể người dùng
     * @return Danh sách UserVoucher
     */
    List<UserVoucher> findByUserOrderBySavedAtDesc(User user);

    /**
     * Lấy danh sách voucher trong ví của người dùng theo trạng thái đã dùng hay chưa.
     *
     * @param user   Thực thể người dùng
     * @param isUsed Trạng thái đã sử dụng (true: đã dùng, false: chưa dùng)
     * @return Danh sách UserVoucher tương ứng
     */
    List<UserVoucher> findByUserAndIsUsed(User user, Boolean isUsed);

    /**
     * Lấy danh sách voucher trong ví người dùng theo trạng thái sử dụng, sắp xếp mới nhất lên đầu.
     *
     * @param user   Thực thể người dùng
     * @param isUsed Trạng thái đã sử dụng
     * @return Danh sách UserVoucher
     */
    List<UserVoucher> findByUserAndIsUsedOrderBySavedAtDesc(User user, Boolean isUsed);

    /**
     * Kiểm tra xem người dùng đã từng lưu voucher cụ thể này vào ví hay chưa.
     *
     * @param user    Thực thể người dùng
     * @param voucher Thực thể mã giảm giá
     * @return true nếu đã lưu trong ví, false nếu chưa lưu
     */
    boolean existsByUserAndVoucher(User user, Voucher voucher);

    /**
     * Kiểm tra người dùng đã lưu voucher vào ví qua ID người dùng và ID voucher.
     *
     * @param userId    ID người dùng
     * @param voucherId ID voucher
     * @return true nếu đã lưu, ngược lại false
     */
    boolean existsByUser_IdAndVoucher_Id(Long userId, Long voucherId);

    /**
     * Tìm bản ghi lưu voucher của người dùng theo đối tượng User và Voucher.
     *
     * @param user    Thực thể người dùng
     * @param voucher Thực thể mã giảm giá
     * @return Optional chứa UserVoucher nếu tìm thấy
     */
    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);

    /**
     * Tìm bản ghi lưu voucher trong ví của người dùng theo mã code của voucher.
     *
     * @param user        Thực thể người dùng
     * @param voucherCode Mã code của voucher
     * @return Optional chứa UserVoucher nếu tìm thấy
     */
    Optional<UserVoucher> findByUserAndVoucher_Code(User user, String voucherCode);

    /**
     * Tìm bản ghi lưu voucher trong ví theo ID người dùng và mã code voucher.
     *
     * @param userId      ID người dùng
     * @param voucherCode Mã code của voucher
     * @return Optional chứa UserVoucher nếu tìm thấy
     */
    Optional<UserVoucher> findByUser_IdAndVoucher_Code(Long userId, String voucherCode);

    /**
     * Đếm tổng số voucher chưa sử dụng (isUsed = false) trong ví của một người dùng.
     *
     * @param user Thực thể người dùng
     * @return Số lượng voucher chưa sử dụng
     */
    long countByUserAndIsUsedFalse(User user);

    /**
     * Đếm tổng số voucher chưa sử dụng trong ví theo ID người dùng.
     *
     * @param userId ID người dùng
     * @return Số lượng voucher chưa sử dụng
     */
    long countByUser_IdAndIsUsedFalse(Long userId);
}
