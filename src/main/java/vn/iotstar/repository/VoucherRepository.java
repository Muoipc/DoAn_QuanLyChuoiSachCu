package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Voucher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND v.startDate <= :now AND v.endDate >= :now " +
           "AND v.usedCount < v.usageLimit")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now);
}
