package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.ShippingUnit;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingUnitRepository extends JpaRepository<ShippingUnit, Integer> {
    Optional<ShippingUnit> findByCode(String code);
    List<ShippingUnit> findByIsActiveTrue();
}
