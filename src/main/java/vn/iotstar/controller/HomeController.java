package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.entity.Book;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.BookRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.StoreRepository;

import java.util.List;

/**
 * Controller điều hướng trang chủ và các trang công cộng dành cho khách (Guest).
 * Ghi chú cho Cường: Controller này sử dụng Spring Data JPA Repository để truy vấn danh sách
 * 20 cuốn sách cũ đã thẩm định, sách bán chạy (> 10 cuốn), danh mục và 3 chi nhánh kho từ MySQL lên Thymeleaf.
 */
@Controller
public class HomeController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    /**
     * Điều hướng trang chủ hệ thống chuỗi sách cũ.
     * Cung cấp dữ liệu động cho cả khối Flash Sale và toàn bộ 20 cuốn sách tại 'Gợi ý hôm nay'.
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // 1. Lấy danh sách sách bán chạy trên 10 cuốn cho khu vực Flash Sale (Top 5 cuốn)
        List<Book> flashSaleBooks = bookRepository.findTopSoldWithImages(10);
        if (flashSaleBooks.size() > 5) {
            flashSaleBooks = flashSaleBooks.subList(0, 5);
        }

        // 2. Lấy toàn bộ danh sách 20 cuốn sách cũ đang hoạt động
        List<Book> dailyBooks = bookRepository.findAllActiveWithImages();

        // 3. Lấy danh sách danh mục thể loại và hệ thống 3 chi nhánh
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        List<Store> stores = storeRepository.findByIsActiveTrue();

        model.addAttribute("pageTitle", "Hệ Thống Chuỗi Cửa Hàng Sách Cũ - HCMUTE");
        model.addAttribute("flashSaleBooks", flashSaleBooks);
        model.addAttribute("dailyBooks", dailyBooks);
        model.addAttribute("categories", categories);
        model.addAttribute("stores", stores);

        return "home";
    }
}
