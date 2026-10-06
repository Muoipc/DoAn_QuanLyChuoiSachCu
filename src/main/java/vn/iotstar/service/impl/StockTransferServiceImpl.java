package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.IStockTransferService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockTransferServiceImpl implements IStockTransferService {

    @Autowired
    private StockTransferRepository stockTransferRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public StockTransfer createTransferRequest(Long fromStoreId, Long toStoreId, Long bookId, int quantity, String notes, User requestedBy) {
        if (fromStoreId == null || toStoreId == null) {
            throw new IllegalArgumentException("Vui lòng chọn cả chi nhánh xuất và chi nhánh nhận.");
        }
        if (fromStoreId.equals(toStoreId)) {
            throw new IllegalArgumentException("Chi nhánh xuất và chi nhánh nhận không được trùng nhau.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng điều chuyển phải lớn hơn 0.");
        }

        Store fromStore = storeRepository.findById(fromStoreId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh xuất hàng."));
        Store toStore = storeRepository.findById(toStoreId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh nhận hàng."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách cần điều chuyển."));

        // Kiểm tra tồn kho tại chi nhánh xuất
        Inventory fromInv = inventoryRepository.findByStoreIdAndBookId(fromStoreId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Sách này không có trong kho chi nhánh xuất."));

        if (fromInv.getQuantity() < quantity) {
            throw new IllegalArgumentException("Kho xuất không đủ số lượng (Hiện còn: " + fromInv.getQuantity() + " cuốn).");
        }

        StockTransfer transfer = new StockTransfer();
        String code = "TRF" + System.currentTimeMillis();
        transfer.setTransferCode(code);
        transfer.setFromStore(fromStore);
        transfer.setToStore(toStore);
        transfer.setBook(book);
        transfer.setQuantity(quantity);
        transfer.setNotes(notes);
        transfer.setRequestedBy(requestedBy);
        transfer.setStatus(StockTransfer.TransferStatus.PENDING);

        StockTransfer saved = stockTransferRepository.save(transfer);

        // Bắn thông báo real-time qua WebSocket cho Admin nếu có
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/admin-transfers", 
                        "Có yêu cầu điều chuyển kho mới #" + code + " từ " + fromStore.getStoreName());
            } catch (Exception ignored) {}
        }

        return saved;
    }

    @Override
    public StockTransfer approveTransfer(Long transferId, User adminUser) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chuyển với ID: " + transferId));

        if (transfer.getStatus() != StockTransfer.TransferStatus.PENDING) {
            throw new IllegalStateException("Phiếu điều chuyển này không ở trạng thái chờ duyệt (Hiện tại: " + transfer.getStatus() + ").");
        }

        Long fromStoreId = transfer.getFromStore().getId();
        Long toStoreId = transfer.getToStore().getId();
        Long bookId = transfer.getBook().getId();
        int qty = transfer.getQuantity();

        // 1. Kiểm tra tồn kho kho xuất tại thời điểm duyệt
        Inventory fromInv = inventoryRepository.findByStoreIdAndBookId(fromStoreId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Kho xuất không còn cuốn sách này trong hệ thống."));

        if (fromInv.getQuantity() < qty) {
            throw new IllegalArgumentException("Kho xuất '" + transfer.getFromStore().getStoreName() + 
                    "' hiện chỉ còn " + fromInv.getQuantity() + " cuốn, không đủ số lượng để duyệt " + qty + " cuốn.");
        }

        // 2. Trừ tồn kho chi nhánh xuất
        fromInv.setQuantity(fromInv.getQuantity() - qty);
        inventoryRepository.save(fromInv);

        // 3. Cộng tồn kho chi nhánh nhận
        Optional<Inventory> toInvOpt = inventoryRepository.findByStoreIdAndBookId(toStoreId, bookId);
        Inventory toInv;
        if (toInvOpt.isPresent()) {
            toInv = toInvOpt.get();
            toInv.setQuantity((toInv.getQuantity() != null ? toInv.getQuantity() : 0) + qty);
        } else {
            toInv = new Inventory();
            toInv.setStore(transfer.getToStore());
            toInv.setBook(transfer.getBook());
            toInv.setQuantity(qty);
        }
        inventoryRepository.save(toInv);

        // 4. Cập nhật phiếu điều chuyển
        transfer.setStatus(StockTransfer.TransferStatus.APPROVED);
        transfer.setApprovedBy(adminUser);
        transfer.setApprovedAt(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    @Override
    public StockTransfer rejectTransfer(Long transferId, String reason, User adminUser) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chuyển với ID: " + transferId));

        if (transfer.getStatus() != StockTransfer.TransferStatus.PENDING) {
            throw new IllegalStateException("Phiếu điều chuyển này không ở trạng thái chờ duyệt.");
        }

        transfer.setStatus(StockTransfer.TransferStatus.REJECTED);
        transfer.setAdminFeedback(reason);
        transfer.setApprovedBy(adminUser);
        transfer.setApprovedAt(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    @Override
    public StockTransfer cancelTransfer(Long transferId, User currentUser) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chuyển với ID: " + transferId));

        if (transfer.getStatus() != StockTransfer.TransferStatus.PENDING) {
            throw new IllegalStateException("Không thể hủy phiếu điều chuyển đã được xử lý.");
        }

        // Chỉ người tạo hoặc Admin mới được hủy
        boolean isAdmin = currentUser.getRole() != null && "ROLE_ADMIN".equals(currentUser.getRole().getName());
        boolean isCreator = transfer.getRequestedBy() != null && transfer.getRequestedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator) {
            throw new SecurityException("Bạn không có quyền hủy phiếu điều chuyển này.");
        }

        transfer.setStatus(StockTransfer.TransferStatus.CANCELLED);
        transfer.setAdminFeedback("Đã hủy bởi người tạo (" + currentUser.getFullName() + ")");
        return stockTransferRepository.save(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransfer> searchTransfers(StockTransfer.TransferStatus status, Long storeId) {
        return stockTransferRepository.searchTransfers(status, storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransfer findById(Long id) {
        return stockTransferRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingTransfers() {
        return stockTransferRepository.countByStatus(StockTransfer.TransferStatus.PENDING);
    }
}
