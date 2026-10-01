package vn.iotstar.service;

import vn.iotstar.entity.Voucher;
import java.util.List;

public interface IVoucherService {
    List<Voucher> findAll();
    Voucher findById(Long id);
    Voucher save(Voucher voucher);
    void deleteById(Long id);
    void toggleActive(Long id);
}
