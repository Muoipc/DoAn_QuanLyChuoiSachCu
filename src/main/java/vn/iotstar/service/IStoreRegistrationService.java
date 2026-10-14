package vn.iotstar.service;

import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.entity.User;

import java.util.List;

public interface IStoreRegistrationService {
    StoreRegistration submitRegistration(StoreRegistration registration, User user);
    StoreRegistration getLatestByUser(Long userId);
    List<StoreRegistration> findAll();
    List<StoreRegistration> findByStatus(StoreRegistration.Status status);
    StoreRegistration findById(Long id);
    Store approveRegistration(Long registrationId);
    StoreRegistration rejectRegistration(Long registrationId, String reason);
    long countPending();
}
