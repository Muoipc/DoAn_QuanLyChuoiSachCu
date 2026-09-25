package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.UserVoucherRepository;
import vn.iotstar.repository.VoucherRepository;
import vn.iotstar.service.impl.VoucherServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử nghiệp vụ tính toán và xác thực Voucher.
 */
@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private UserVoucherRepository userVoucherRepository;

    private IVoucherService voucherService;

    @BeforeEach
    void setUp() {
        voucherService = new VoucherServiceImpl(voucherRepository, userVoucherRepository);
    }

    @Test
    @DisplayName("Tính giảm giá tiền mặt FIXED_AMOUNT chuẩn xác")
    void testCalculateDiscountFixedAmount() {
        Voucher voucher = new Voucher();
        voucher.setDiscountType(Voucher.DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("30000.00"));
        voucher.setMinOrderAmount(new BigDecimal("100000.00"));

        // Đơn hàng đủ điều kiện: 150k -> giảm 30k
        BigDecimal discount = voucherService.calculateDiscount(voucher, new BigDecimal("150000.00"));
        assertEquals(0, new BigDecimal("30000.00").compareTo(discount));

        // Đơn hàng nhỏ hơn mức giảm: 20k -> chỉ giảm tối đa bằng đơn hàng 20k
        voucher.setMinOrderAmount(BigDecimal.ZERO);
        BigDecimal discountCap = voucherService.calculateDiscount(voucher, new BigDecimal("20000.00"));
        assertEquals(0, new BigDecimal("20000.00").compareTo(discountCap));

        // Đơn hàng chưa đạt mức tối thiểu: 80k < 100k -> giảm 0đ
        voucher.setMinOrderAmount(new BigDecimal("100000.00"));
        BigDecimal zeroDiscount = voucherService.calculateDiscount(voucher, new BigDecimal("80000.00"));
        assertEquals(BigDecimal.ZERO, zeroDiscount);
    }

    @Test
    @DisplayName("Tính giảm giá phần trăm PERCENT có trần tối đa maxDiscount")
    void testCalculateDiscountPercent() {
        Voucher voucher = new Voucher();
        voucher.setDiscountType(Voucher.DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("20.00")); // 20%
        voucher.setMinOrderAmount(new BigDecimal("100000.00"));
        voucher.setMaxDiscount(new BigDecimal("50000.00")); // trần 50k

        // Đơn hàng 200k -> 20% là 40k (< 50k max) -> giảm 40k
        BigDecimal discount = voucherService.calculateDiscount(voucher, new BigDecimal("200000.00"));
        assertEquals(0, new BigDecimal("40000.00").compareTo(discount));

        // Đơn hàng 500k -> 20% là 100k (> 50k max) -> trần giảm 50k
        BigDecimal cappedDiscount = voucherService.calculateDiscount(voucher, new BigDecimal("500000.00"));
        assertEquals(0, new BigDecimal("50000.00").compareTo(cappedDiscount));
    }

    @Test
    @DisplayName("Xác thực voucher hợp lệ và ném biệt lệ khi đơn hàng không đạt giá trị tối thiểu")
    void testValidateVoucher() {
        Voucher voucher = new Voucher();
        voucher.setCode("SACHCU50K");
        voucher.setIsActive(true);
        voucher.setStartDate(LocalDateTime.now().minusDays(1));
        voucher.setEndDate(LocalDateTime.now().plusDays(10));
        voucher.setUsageLimit(100);
        voucher.setUsedCount(10);
        voucher.setMinOrderAmount(new BigDecimal("300000.00"));

        when(voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue("SACHCU50K")).thenReturn(Optional.of(voucher));

        // Hợp lệ khi đơn hàng 350.000đ >= 300.000đ
        Voucher validated = voucherService.validateVoucher("SACHCU50K", new BigDecimal("350000.00"));
        assertNotNull(validated);
        assertEquals("SACHCU50K", validated.getCode());

        // Ném lỗi khi đơn hàng chỉ có 200.000đ < 300.000đ
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> voucherService.validateVoucher("SACHCU50K", new BigDecimal("200000.00"))
        );
        assertTrue(ex.getMessage().contains("chưa đạt giá trị tối thiểu"));
    }
}
