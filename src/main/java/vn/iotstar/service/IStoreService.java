package vn.iotstar.service;

import vn.iotstar.entity.Store;
import java.util.List;

public interface IStoreService {
    List<Store> findAll();
    List<Store> findActiveStores();
    Store findById(Long id);
    Store save(Store store);
    void toggleActive(Long id);
    long countTotalStores();
}
