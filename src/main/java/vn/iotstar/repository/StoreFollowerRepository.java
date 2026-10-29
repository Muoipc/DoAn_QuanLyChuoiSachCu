package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.StoreFollower;

@Repository
public interface StoreFollowerRepository extends JpaRepository<StoreFollower, Long> {

    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StoreFollower sf WHERE sf.user.id = :userId AND sf.store.id = :storeId")
    void deleteByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);

    long countByStoreId(Long storeId);
}
