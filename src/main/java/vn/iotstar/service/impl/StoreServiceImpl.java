package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.IStoreService;

import java.util.List;

@Service
@Transactional
public class StoreServiceImpl implements IStoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findActiveStores() {
        return storeRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Store findById(Long id) {
        return storeRepository.findById(id).orElse(null);
    }

    @Override
    public Store save(Store store) {
        return storeRepository.save(store);
    }

    @Override
    public void toggleActive(Long id) {
        storeRepository.findById(id).ifPresent(store -> {
            store.setIsActive(!Boolean.TRUE.equals(store.getIsActive()));
            storeRepository.save(store);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalStores() {
        return storeRepository.count();
    }
}
