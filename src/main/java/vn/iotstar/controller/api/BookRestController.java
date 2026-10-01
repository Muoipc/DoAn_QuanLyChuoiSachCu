package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Inventory;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.InventoryRepository;

import java.util.*;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * API Lấy danh sách sách cũ đang mở bán
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<Map<String, Object>>> getAllBooks() {
        List<Book> books = bookRepository.findAllActiveWithImages();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Book b : books) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("title", b.getTitle());
            map.put("author", b.getAuthor());
            map.put("publisher", b.getPublisher());
            map.put("publishYear", b.getPublishYear());
            map.put("isbn", b.getIsbn());
            map.put("conditionPercent", b.getConditionPercent());
            map.put("conditionNotes", b.getConditionNotes());
            map.put("price", b.getPrice());
            map.put("originalPrice", b.getOriginalPrice());
            map.put("category", b.getCategory() != null ? b.getCategory().getCategoryName() : "");
            map.put("primaryImageUrl", b.getPrimaryImageUrl());
            map.put("totalSold", b.getTotalSold());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * API Lấy chi tiết sách theo ID kèm tồn kho tại 5 chi nhánh
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookDetail(@PathVariable("id") Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book b = bookOpt.get();
        Map<String, Object> map = new HashMap<>();
        map.put("id", b.getId());
        map.put("title", b.getTitle());
        map.put("slug", b.getSlug());
        map.put("author", b.getAuthor());
        map.put("publisher", b.getPublisher());
        map.put("publishYear", b.getPublishYear());
        map.put("isbn", b.getIsbn());
        map.put("conditionPercent", b.getConditionPercent());
        map.put("conditionNotes", b.getConditionNotes());
        map.put("description", b.getDescription());
        map.put("price", b.getPrice());
        map.put("originalPrice", b.getOriginalPrice());
        map.put("category", b.getCategory() != null ? b.getCategory().getCategoryName() : "");
        map.put("primaryImageUrl", b.getPrimaryImageUrl());

        // Danh sách tồn kho theo chi nhánh
        List<Inventory> inventories = inventoryRepository.findByBookIdWithStore(b.getId());
        List<Map<String, Object>> stocks = new ArrayList<>();
        for (Inventory inv : inventories) {
            Map<String, Object> s = new HashMap<>();
            s.put("storeId", inv.getStore().getId());
            s.put("storeName", inv.getStore().getStoreName());
            s.put("address", inv.getStore().getAddress());
            s.put("quantity", inv.getQuantity());
            stocks.add(s);
        }
        map.put("inventories", stocks);

        return ResponseEntity.ok(map);
    }
}
