package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.ICartService;

import java.util.List;
import java.util.Optional;

/**
 * Hiện thực nghiệp vụ Giỏ hàng chuỗi sách cũ lưu trữ trực tiếp vào CSDL MySQL.
 * Ghi chú cho Cường:
 * - Khi khách thêm sách vào giỏ, ta lưu cả storeId để biết khách dự định mua/lấy sách từ chi nhánh nào trong 5 chi nhánh TP.HCM.
 * - Nếu cùng một cuốn sách và cùng một chi nhánh đã có trong giỏ thì ta cộng dồn số lượng.
 */
@Service
@Transactional
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Cart getOrCreateCartForUser(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsWithDetails(Long cartId) {
        return cartItemRepository.findByCartIdWithDetails(cartId);
    }

    @Override
    public void addToCart(Long userId, Long bookId, Long storeId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        Cart cart = getOrCreateCartForUser(userId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + bookId));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + storeId));

        // Kiểm tra xem sản phẩm này tại chi nhánh này đã có trong giỏ chưa
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndBookIdAndStoreId(cart.getId(), bookId, storeId);

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setStore(store);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    @Override
    public void updateQuantity(Long cartItemId, int quantity) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
        }
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartTotalCount(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return 0;
        }
        List<CartItem> items = cartItemRepository.findByCartId(cartOpt.get().getId());
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
