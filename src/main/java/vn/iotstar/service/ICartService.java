package vn.iotstar.service;

import vn.iotstar.entity.Cart;
import vn.iotstar.entity.CartItem;

import java.util.List;

/**
 * Interface nghiệp vụ quản lý Giỏ hàng chuỗi sách cũ.
 * Ghi chú cho Cường: Service này xử lý:
 * 1. Khởi tạo hoặc lấy giỏ hàng CSDL của người dùng theo userId.
 * 2. Thêm sách vào giỏ (gắn với chi nhánh cụ thể trong 5 chi nhánh TP.HCM).
 * 3. Tăng/giảm số lượng sách hoặc xóa khỏi giỏ.
 * 4. Đồng bộ tổng số lượng hiển thị trên biểu tượng giỏ hàng ở Header.
 */
public interface ICartService {

    /**
     * Lấy giỏ hàng của user; nếu chưa có trong DB thì tự động tạo mới một Cart rỗng.
     */
    Cart getOrCreateCartForUser(Long userId);

    /**
     * Lấy toàn bộ danh sách CartItem kèm Book, BookImage và Store.
     */
    List<CartItem> getCartItemsWithDetails(Long cartId);

    /**
     * Thêm sách cũ vào giỏ hàng với số lượng và chi nhánh kho được chọn.
     */
    void addToCart(Long userId, Long bookId, Long storeId, int quantity);

    /**
     * Cập nhật số lượng của một dòng trong giỏ hàng.
     */
    void updateQuantity(Long cartItemId, int quantity);

    /**
     * Xóa một cuốn sách khỏi giỏ hàng.
     */
    void removeCartItem(Long cartItemId);

    /**
     * Xóa toàn bộ giỏ hàng (sau khi đặt hàng thành công).
     */
    void clearCart(Long cartId);

    /**
     * Đếm tổng số cuốn sách hiện có trong giỏ của user để hiển thị badge Header.
     */
    int getCartTotalCount(Long userId);
}
