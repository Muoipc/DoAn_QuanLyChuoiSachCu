package vn.iotstar.service;

import vn.iotstar.entity.ShippingUnit;
import java.util.List;

public interface IShippingUnitService {
    List<ShippingUnit> findAll();
    ShippingUnit findById(Integer id);
    ShippingUnit save(ShippingUnit unit);
    void deleteById(Integer id);
    void toggleActive(Integer id);
}
