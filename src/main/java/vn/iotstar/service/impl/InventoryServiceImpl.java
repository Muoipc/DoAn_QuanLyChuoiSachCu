package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Inventory;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.IInventoryService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements IInventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByStoreId(Long storeId) {
        return inventoryRepository.findByStoreId(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByBookId(Long bookId) {
        return inventoryRepository.findByBookId(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory findByStoreAndBook(Long storeId, Long bookId) {
        return inventoryRepository.findByStoreIdAndBookId(storeId, bookId).orElse(null);
    }

    @Override
    public Inventory updateQuantity(Long storeId, Long bookId, int quantity) {
        if (quantity < 0) {
            quantity = 0;
        }
        Optional<Inventory> opt = inventoryRepository.findByStoreIdAndBookId(storeId, bookId);
        Inventory inv;
        if (opt.isPresent()) {
            inv = opt.get();
            inv.setQuantity(quantity);
        } else {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + storeId));
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + bookId));
            inv = new Inventory();
            inv.setStore(store);
            inv.setBook(book);
            inv.setQuantity(quantity);
        }
        return inventoryRepository.save(inv);
    }

    @Override
    public void transferStock(Long fromStoreId, Long toStoreId, Long bookId, int quantity, String notes) {
        if (fromStoreId == null || toStoreId == null) {
            throw new IllegalArgumentException("Vui lòng chọn cả chi nhánh xuất và chi nhánh nhập");
        }
        if (fromStoreId.equals(toStoreId)) {
            throw new IllegalArgumentException("Chi nhánh gửi và chi nhánh nhận không được trùng nhau");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng chuyển kho phải lớn hơn 0");
        }

        Inventory fromInv = inventoryRepository.findByStoreIdAndBookId(fromStoreId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Sách không tồn tại trong kho xuất"));

        if (fromInv.getQuantity() < quantity) {
            throw new IllegalArgumentException("Số lượng trong kho xuất không đủ (Hiện còn: " + fromInv.getQuantity() + " cuốn)");
        }

        // Giảm kho xuất
        fromInv.setQuantity(fromInv.getQuantity() - quantity);
        inventoryRepository.save(fromInv);

        // Tăng kho nhập
        Optional<Inventory> toInvOpt = inventoryRepository.findByStoreIdAndBookId(toStoreId, bookId);
        Inventory toInv;
        if (toInvOpt.isPresent()) {
            toInv = toInvOpt.get();
            toInv.setQuantity(toInv.getQuantity() + quantity);
        } else {
            Store toStore = storeRepository.findById(toStoreId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh nhận"));
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));
            toInv = new Inventory();
            toInv.setStore(toStore);
            toInv.setBook(book);
            toInv.setQuantity(quantity);
        }
        inventoryRepository.save(toInv);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalQuantityByBook(Long bookId) {
        List<Inventory> list = inventoryRepository.findByBookId(bookId);
        return list.stream().mapToInt(Inventory::getQuantity).sum();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findLowStock(Long storeId, int threshold) {
        List<Inventory> list = (storeId != null) ? inventoryRepository.findByStoreId(storeId) : inventoryRepository.findAll();
        return list.stream()
                .filter(inv -> inv.getQuantity() <= threshold)
                .collect(Collectors.toList());
    }
}
