package vn.iotstar.service;

import vn.iotstar.entity.Inventory;
import java.util.List;

public interface IInventoryService {
    List<Inventory> findByStoreId(Long storeId);
    List<Inventory> findByBookId(Long bookId);
    Inventory findByStoreAndBook(Long storeId, Long bookId);
    Inventory updateQuantity(Long storeId, Long bookId, int quantity);
    void transferStock(Long fromStoreId, Long toStoreId, Long bookId, int quantity, String notes);
    int getTotalQuantityByBook(Long bookId);
    List<Inventory> findLowStock(Long storeId, int threshold);
}
