package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.impl.CartServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử nghiệp vụ Giỏ hàng chuỗi sách cũ (CartService).
 */
@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("Lấy giỏ hàng theo userId khi đã tồn tại")
    void testGetCartByUserExists() {
        Cart cart = new Cart();
        cart.setId(10L);

        when(cartRepository.findByUserId(4L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCartForUser(4L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(cartRepository, times(1)).findByUserId(4L);
    }

    @Test
    @DisplayName("Tự động tạo giỏ hàng mới khi userId chưa có giỏ")
    void testGetCartByUserCreateNew() {
        User user = new User();
        user.setId(5L);

        when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Cart result = cartService.getOrCreateCartForUser(5L);
        assertNotNull(result);
        assertEquals(99L, result.getId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Tính tổng số lượng sản phẩm trong giỏ hàng hiển thị badge")
    void testGetCartTotalCount() {
        Cart cart = new Cart();
        cart.setId(1L);

        CartItem item1 = new CartItem();
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setQuantity(3);

        when(cartRepository.findByUserId(4L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item1, item2));

        int totalCount = cartService.getCartTotalCount(4L);
        assertEquals(5, totalCount);
    }

    @Test
    @DisplayName("Xóa một sản phẩm khỏi giỏ hàng thành công")
    void testRemoveCartItem() {
        doNothing().when(cartItemRepository).deleteById(15L);

        assertDoesNotThrow(() -> cartService.removeCartItem(15L));
        verify(cartItemRepository, times(1)).deleteById(15L);
    }

    @Test
    @DisplayName("Xóa toàn bộ giỏ hàng theo cartId sau khi đặt hàng")
    void testClearCart() {
        doNothing().when(cartItemRepository).deleteByCartId(20L);

        assertDoesNotThrow(() -> cartService.clearCart(20L));
        verify(cartItemRepository, times(1)).deleteByCartId(20L);
    }
}
