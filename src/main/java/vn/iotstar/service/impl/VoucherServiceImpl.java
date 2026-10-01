package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.VoucherRepository;
import vn.iotstar.service.IVoucherService;

import java.util.List;

@Service
@Transactional
public class VoucherServiceImpl implements IVoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher findById(Long id) {
        return voucherRepository.findById(id).orElse(null);
    }

    @Override
    public Voucher save(Voucher voucher) {
        if (voucher.getCode() != null) {
            voucher.setCode(voucher.getCode().trim().toUpperCase());
        }
        return voucherRepository.save(voucher);
    }

    @Override
    public void deleteById(Long id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Long id) {
        voucherRepository.findById(id).ifPresent(v -> {
            v.setIsActive(!Boolean.TRUE.equals(v.getIsActive()));
            voucherRepository.save(v);
        });
    }
}
