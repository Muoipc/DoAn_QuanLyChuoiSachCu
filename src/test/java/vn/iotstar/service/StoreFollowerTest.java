package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import vn.iotstar.controller.StoreController;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.StoreFollower;
import vn.iotstar.entity.User;
import vn.iotstar.repository.StoreFollowerRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreFollowerTest {

    @Mock
    private StoreFollowerRepository storeFollowerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreController storeController;

    @Test
    @DisplayName("Toggle Follow: Khi chưa theo dõi, lưu vào DB và trả về followed = true")
    void testToggleFollow_WhenNotFollowed_ShouldFollow() {
        Long userId = 4L;
        Long storeId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Store store = new Store();
        store.setId(storeId);
        store.setStoreName("Tổng Kho Thủ Đức");

        vn.iotstar.entity.Role role = new vn.iotstar.entity.Role();
        role.setName("ROLE_USER");
        user.setRole(role);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(storeFollowerRepository.existsByUserIdAndStoreId(userId, storeId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeFollowerRepository.countByStoreId(storeId)).thenReturn(10L);

        ResponseEntity<Map<String, Object>> response = storeController.toggleFollowStore(storeId, null, userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
        assertTrue((Boolean) response.getBody().get("followed"));
        assertEquals(10L, response.getBody().get("followersCount"));
        verify(storeFollowerRepository, times(1)).save(any(StoreFollower.class));
    }

    @Test
    @DisplayName("Toggle Follow: Khi đã theo dõi, xóa khỏi DB và trả về followed = false")
    void testToggleFollow_WhenAlreadyFollowed_ShouldUnfollow() {
        Long userId = 4L;
        Long storeId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        vn.iotstar.entity.Role role = new vn.iotstar.entity.Role();
        role.setName("ROLE_USER");
        user.setRole(role);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(storeFollowerRepository.existsByUserIdAndStoreId(userId, storeId)).thenReturn(true);
        when(storeFollowerRepository.countByStoreId(storeId)).thenReturn(9L);

        ResponseEntity<Map<String, Object>> response = storeController.toggleFollowStore(storeId, null, userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
        assertFalse((Boolean) response.getBody().get("followed"));
        assertEquals(9L, response.getBody().get("followersCount"));
        verify(storeFollowerRepository, times(1)).deleteByUserIdAndStoreId(userId, storeId);
    }
}
