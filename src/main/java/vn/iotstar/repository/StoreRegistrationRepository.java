package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.StoreRegistration;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRegistrationRepository extends JpaRepository<StoreRegistration, Long> {
    List<StoreRegistration> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<StoreRegistration> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<StoreRegistration> findByStatusOrderByCreatedAtDesc(StoreRegistration.Status status);
    long countByStatus(StoreRegistration.Status status);
}
