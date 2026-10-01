package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.ShippingUnit;
import vn.iotstar.repository.ShippingUnitRepository;
import vn.iotstar.service.IShippingUnitService;

import java.util.List;

@Service
@Transactional
public class ShippingUnitServiceImpl implements IShippingUnitService {

    @Autowired
    private ShippingUnitRepository shippingUnitRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingUnit> findAll() {
        return shippingUnitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingUnit findById(Integer id) {
        return shippingUnitRepository.findById(id).orElse(null);
    }

    @Override
    public ShippingUnit save(ShippingUnit unit) {
        return shippingUnitRepository.save(unit);
    }

    @Override
    public void deleteById(Integer id) {
        shippingUnitRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Integer id) {
        shippingUnitRepository.findById(id).ifPresent(u -> {
            u.setIsActive(!Boolean.TRUE.equals(u.getIsActive()));
            shippingUnitRepository.save(u);
        });
    }
}
