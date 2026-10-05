package vn.iotstar.service;

import vn.iotstar.entity.StockTransfer;
import vn.iotstar.entity.User;

import java.util.List;

public interface IStockTransferService {

    StockTransfer createTransferRequest(Long fromStoreId, Long toStoreId, Long bookId, int quantity, String notes, User requestedBy);

    StockTransfer approveTransfer(Long transferId, User adminUser);

    StockTransfer rejectTransfer(Long transferId, String reason, User adminUser);

    StockTransfer cancelTransfer(Long transferId, User currentUser);

    List<StockTransfer> searchTransfers(StockTransfer.TransferStatus status, Long storeId);

    StockTransfer findById(Long id);

    long countPendingTransfers();
}
