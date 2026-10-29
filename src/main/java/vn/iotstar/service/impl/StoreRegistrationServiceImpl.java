package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.StoreRegistrationRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.IStoreRegistrationService;
import vn.iotstar.util.SlugUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreRegistrationServiceImpl implements IStoreRegistrationService {

    @Autowired
    private StoreRegistrationRepository registrationRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private vn.iotstar.service.INotificationService notificationService;

    @Override
    @Transactional
    public StoreRegistration submitRegistration(StoreRegistration registration, User user) {
        registration.setUser(user);
        registration.setStatus(StoreRegistration.Status.PENDING);
        registration.setRejectReason(null);
        return registrationRepository.save(registration);
    }

    @Override
    public StoreRegistration getLatestByUser(Long userId) {
        return registrationRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
    }

    @Override
    public List<StoreRegistration> findAll() {
        return registrationRepository.findAll();
    }

    @Override
    public List<StoreRegistration> findByStatus(StoreRegistration.Status status) {
        return registrationRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public StoreRegistration findById(Long id) {
        return registrationRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Store approveRegistration(Long registrationId) {
        StoreRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Hồ sơ đăng ký không tồn tại: " + registrationId));

        if (reg.getStatus() == StoreRegistration.Status.APPROVED) {
            throw new IllegalStateException("Hồ sơ này đã được duyệt trước đó!");
        }

        User applicant = reg.getUser();

        // 1. Tạo chi nhánh cửa hàng mới (Store)
        Store store = new Store();
        store.setStoreName(reg.getStoreName());
        
        // Sinh slug an toàn, không trùng lặp
        String baseSlug = SlugUtil.toSlug(reg.getStoreName());
        if (baseSlug == null || baseSlug.isBlank()) {
            baseSlug = "cua-hang-" + System.currentTimeMillis();
        }
        String uniqueSlug = baseSlug;
        int counter = 1;
        while (storeRepository.findBySlug(uniqueSlug).isPresent()) {
            uniqueSlug = baseSlug + "-" + (++counter);
        }
        store.setSlug(uniqueSlug);

        store.setPhone(reg.getPhone());
        store.setEmail(reg.getEmail() != null ? reg.getEmail() : applicant.getEmail());
        store.setAddress(reg.getAddress());
        store.setProvince(reg.getProvince());
        store.setDistrict(reg.getDistrict());
        store.setOpenTime(reg.getOpenTime());
        store.setCloseTime(reg.getCloseTime());
        store.setImage(reg.getImage() != null && !reg.getImage().isBlank() ? reg.getImage() : "/images/hero-books-vintage.jpg");
        store.setIsActive(true);
        store.setAiReplyEnabled(false);
        store.setManager(applicant);

        Store savedStore = storeRepository.save(store);

        // 2. Nâng quyền người dùng thành ROLE_VENDOR (Chủ gian hàng / Người bán đối tác theo chuẩn UTEx)
        Role vendorRole = roleRepository.findByName("ROLE_VENDOR")
                .orElseGet(() -> roleRepository.save(new Role(5, "ROLE_VENDOR", "Đối tác bán hàng / Chủ gian hàng")));
        applicant.setRole(vendorRole);
        userRepository.save(applicant);

        // 3. Cập nhật trạng thái hồ sơ thành APPROVED
        reg.setStatus(StoreRegistration.Status.APPROVED);
        reg.setRejectReason(null);
        reg.setUpdatedAt(LocalDateTime.now());
        registrationRepository.save(reg);

        if (notificationService != null) {
            try {
                notificationService.notifyStoreRegistrationResult(reg);
            } catch (Exception ignored) {}
        }

        return savedStore;
    }

    @Override
    @Transactional
    public StoreRegistration rejectRegistration(Long registrationId, String reason) {
        StoreRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Hồ sơ đăng ký không tồn tại: " + registrationId));

        reg.setStatus(StoreRegistration.Status.REJECTED);
        reg.setRejectReason(reason != null && !reason.isBlank() ? reason.trim() : "Hồ sơ chưa đạt tiêu chuẩn quy định của sàn ReBook.");
        reg.setUpdatedAt(LocalDateTime.now());
        StoreRegistration saved = registrationRepository.save(reg);

        if (notificationService != null) {
            try {
                notificationService.notifyStoreRegistrationResult(saved);
            } catch (Exception ignored) {}
        }
        return saved;
    }

    @Override
    public long countPending() {
        return registrationRepository.countByStatus(StoreRegistration.Status.PENDING);
    }
}
