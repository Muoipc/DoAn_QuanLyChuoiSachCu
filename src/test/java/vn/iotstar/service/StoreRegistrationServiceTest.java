package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreRegistration;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.StoreRegistrationRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.StoreRegistrationServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreRegistrationServiceTest {

    @Mock
    private StoreRegistrationRepository registrationRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private StoreRegistrationServiceImpl registrationService;

    @Test
    @DisplayName("Nộp hồ sơ mở gian hàng thành công với trạng thái PENDING")
    void testSubmitRegistration() {
        User user = new User();
        user.setId(10L);
        user.setUsername("user_hop_tac");

        StoreRegistration reg = new StoreRegistration();
        reg.setStoreName("Tiệm Sách Cũ Gò Vấp");
        reg.setPhone("0901234567");
        reg.setAddress("123 Quang Trung, Gò Vấp");

        when(registrationRepository.save(any(StoreRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreRegistration saved = registrationService.submitRegistration(reg, user);

        assertNotNull(saved);
        assertEquals(user, saved.getUser());
        assertEquals(StoreRegistration.Status.PENDING, saved.getStatus());
        assertNull(saved.getRejectReason());
        verify(registrationRepository, times(1)).save(reg);
    }

    @Test
    @DisplayName("Admin duyệt hồ sơ: Tạo Store mới, gán manager_id và nâng quyền thành ROLE_MANAGER")
    void testApproveRegistration() {
        User applicant = new User();
        applicant.setId(15L);
        applicant.setUsername("seller_binh_thanh");
        applicant.setEmail("seller@rebook.vn");
        applicant.setRole(new Role(3, "ROLE_USER", "Khách hàng"));

        StoreRegistration reg = new StoreRegistration();
        reg.setId(100L);
        reg.setUser(applicant);
        reg.setStoreName("Gác Sách Xưa Bình Thạnh");
        reg.setPhone("0988776655");
        reg.setAddress("45 Bạch Đằng, Bình Thạnh");
        reg.setProvince("TP. Hồ Chí Minh");
        reg.setDistrict("Quận Bình Thạnh");
        reg.setStatus(StoreRegistration.Status.PENDING);

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(reg));
        when(storeRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(Optional.of(new Role(2, "ROLE_MANAGER", "Quản lý cửa hàng")));

        Store approvedStore = registrationService.approveRegistration(100L);

        assertNotNull(approvedStore);
        assertEquals("Gác Sách Xưa Bình Thạnh", approvedStore.getStoreName());
        assertEquals(applicant, approvedStore.getManager());
        assertTrue(approvedStore.getIsActive());
        assertEquals(StoreRegistration.Status.APPROVED, reg.getStatus());
        assertEquals("ROLE_MANAGER", applicant.getRole().getName());

        verify(storeRepository, times(1)).save(any(Store.class));
        verify(userRepository, times(1)).save(applicant);
        verify(registrationRepository, times(1)).save(reg);
    }

    @Test
    @DisplayName("Admin từ chối hồ sơ: Lưu lý do từ chối, tài khoản giữ nguyên vai trò cũ")
    void testRejectRegistration() {
        User applicant = new User();
        applicant.setId(20L);
        applicant.setRole(new Role(3, "ROLE_USER", "Khách hàng"));

        StoreRegistration reg = new StoreRegistration();
        reg.setId(101L);
        reg.setUser(applicant);
        reg.setStatus(StoreRegistration.Status.PENDING);

        when(registrationRepository.findById(101L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(StoreRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreRegistration rejected = registrationService.rejectRegistration(101L, "Địa chỉ kho sách chưa rõ ràng, vui lòng bổ sung");

        assertNotNull(rejected);
        assertEquals(StoreRegistration.Status.REJECTED, rejected.getStatus());
        assertEquals("Địa chỉ kho sách chưa rõ ràng, vui lòng bổ sung", rejected.getRejectReason());
        assertEquals("ROLE_USER", applicant.getRole().getName());

        verify(registrationRepository, times(1)).save(reg);
        verify(storeRepository, never()).save(any(Store.class));
    }
}
