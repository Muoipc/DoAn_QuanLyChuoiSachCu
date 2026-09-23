-- ====================================================================
-- ĐỒ ÁN CUỐI KỲ: XÂY DỰNG WEBSITE QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ
-- MÔN HỌC: LẬP TRÌNH WEB (WEBPR330479) - HCMUTE
-- GVHD: ThS. NGUYỄN HỮU TRUNG
-- SVTH: NGUYỄN SONG HOÀNG PHÚC & THÁI DUY CƯỜNG
-- ====================================================================

DROP DATABASE IF EXISTS old_book_store_db;
CREATE DATABASE old_book_store_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE old_book_store_db;

-- 1. BẢNG VAI TRÒ (ROLES)
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- 2. BẢNG NGƯỜI DÙNG (USERS)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    avatar VARCHAR(500),
    enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 3. BẢNG TOKEN MÃ OTP (XÁC THỰC EMAIL & QUÊN MẬT KHẨU)
CREATE TABLE otp_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    token_type ENUM('REGISTER', 'FORGOT_PASSWORD') NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. BẢNG SỔ ĐỊA CHỈ NHẬN HÀNG (ADDRESSES - KHÁCH CÓ NHIỀU ĐỊA CHỈ)
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. BẢNG CHI NHÁNH CHUỖI CỬA HÀNG SÁCH (STORES)
CREATE TABLE stores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manager_id BIGINT,
    store_name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(255) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    open_time VARCHAR(20) DEFAULT '08:00',
    close_time VARCHAR(20) DEFAULT '22:00',
    image VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stores_manager FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 6. BẢNG DANH MỤC THỂ LOẠI SÁCH (CATEGORIES)
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    parent_id INT NULL,
    category_name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. BẢNG SÁCH CŨ (BOOKS)
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(280) NOT NULL UNIQUE,
    author VARCHAR(150) NOT NULL,
    publisher VARCHAR(150),
    publish_year INT,
    isbn VARCHAR(50),
    condition_percent INT NOT NULL DEFAULT 90, -- 80% đến 99%
    condition_notes TEXT,                      -- Tình trạng giấy, gáy sách, chữ ký tác giả
    description LONGTEXT,
    original_price DECIMAL(14,2) DEFAULT 0.00,
    price DECIMAL(14,2) NOT NULL,
    discount_price DECIMAL(14,2) DEFAULT 0.00,
    total_sold INT DEFAULT 0,                  -- Phục vụ lọc sách bán chạy > 10 cuốn
    views_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_categories FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 8. BẢNG BỘ ẢNH CHI TIẾT CỦA SÁCH (BOOK_IMAGES - CLOUDINARY)
CREATE TABLE book_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(200),
    angle_description VARCHAR(100),            -- 'Bìa trước', 'Gáy sách', 'Trang đầu', 'Bìa sau'
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    CONSTRAINT fk_book_images_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. BẢNG QUẢN LÝ TỒN KHO THEO TỪNG CHI NHÁNH (INVENTORY)
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_store_book (store_id, book_id),
    CONSTRAINT fk_inventory_stores FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. BẢNG GIỎ HÀNG LƯU DATABASE (CARTS)
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. BẢNG CHI TIẾT GIỎ HÀNG (CART_ITEMS)
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,                  -- Khách chọn lấy tại chi nhánh nào
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_carts FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_stores FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. BẢNG ĐƠN VỊ VẬN CHUYỂN (SHIPPING_UNITS)
CREATE TABLE shipping_units (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    contact_phone VARCHAR(20),
    base_fee DECIMAL(12,2) DEFAULT 25000.00,
    estimated_days VARCHAR(50) DEFAULT '2-3 ngày',
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- 13. BẢNG MÃ GIẢM GIÁ (VOUCHERS)
CREATE TABLE vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type ENUM('PERCENT', 'FIXED_AMOUNT') NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    min_order_amount DECIMAL(14,2) DEFAULT 0.00,
    max_discount DECIMAL(14,2) DEFAULT 0.00,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    usage_limit INT DEFAULT 100,
    used_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- 14. BẢNG ĐƠN HÀNG (ORDERS - 6 TRẠNG THÁI)
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,                  -- Chi nhánh chịu trách nhiệm đóng gói/xuất hàng
    shipping_unit_id INT NULL,
    shipper_id BIGINT NULL,
    voucher_id BIGINT NULL,
    receiver_name VARCHAR(150) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    delivery_method ENUM('HOME_DELIVERY', 'STORE_PICKUP') DEFAULT 'HOME_DELIVERY',
    payment_method ENUM('COD', 'VNPAY') DEFAULT 'COD',
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID',
    order_status ENUM(
        'NEW',               -- Đơn hàng mới
        'CONFIRMED',         -- Đã xác nhận
        'SHIPPING',          -- Đang giao
        'DELIVERED',         -- Đã giao
        'CANCELLED',         -- Hủy đơn
        'RETURNED'           -- Trả hàng - Hoàn tiền
    ) DEFAULT 'NEW',
    subtotal DECIMAL(14,2) NOT NULL,
    shipping_fee DECIMAL(12,2) DEFAULT 0.00,
    discount_amount DECIMAL(12,2) DEFAULT 0.00,
    final_amount DECIMAL(14,2) NOT NULL,
    customer_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_stores FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_shipping FOREIGN KEY (shipping_unit_id) REFERENCES shipping_units(id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_shipper FOREIGN KEY (shipper_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 15. BẢNG CHI TIẾT ĐƠN HÀNG (ORDER_ITEMS)
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(255) NOT NULL,
    condition_percent INT NOT NULL,
    price DECIMAL(14,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 16. BẢNG ĐÁNH GIÁ SẢN PHẨM (REVIEWS - TEXT >= 50 KÝ TỰ, ẢNH/VIDEO CLOUDINARY)
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,                     -- Logic kiểm tra độ dài >= 50 ký tự
    media_url VARCHAR(500),                    -- Link ảnh hoặc video trên Cloudinary
    media_type ENUM('IMAGE', 'VIDEO', 'NONE') DEFAULT 'NONE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_orders FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 17. BẢNG YÊU CẦU KÝ GỬI / THANH LÝ SÁCH CŨ (BOOK_CONSIGNMENTS)
CREATE TABLE book_consignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,                  -- Chi nhánh khách chọn gửi thẩm định
    book_title VARCHAR(255) NOT NULL,
    author VARCHAR(150) NOT NULL,
    publisher VARCHAR(150),
    publish_year INT,
    condition_percent INT NOT NULL,
    condition_description TEXT NOT NULL,
    photos_json TEXT,                          -- Danh sách URLs ảnh các góc sách
    proposed_price DECIMAL(14,2) NOT NULL,     -- Giá khách mong muốn
    agreed_price DECIMAL(14,2) NULL,           -- Giá shop thẩm định duyệt
    status ENUM(
        'PENDING',           -- Chờ duyệt
        'APPROVED',          -- Đã chấp nhận, chờ nhận sách
        'REJECTED',          -- Từ chối thu mua
        'STORED'             -- Đã nhập kho bày bán
    ) DEFAULT 'PENDING',
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_consignments_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_consignments_stores FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 18. BẢNG DANH SÁCH YÊU THÍCH (WISHLISTS)
CREATE TABLE wishlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_book_wishlist (user_id, book_id),
    CONSTRAINT fk_wishlists_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 19. BẢNG LỊCH SỬ XEM SÁCH (VIEWED_BOOKS)
CREATE TABLE viewed_books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_book_viewed (user_id, book_id),
    CONSTRAINT fk_viewed_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_viewed_books FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ====================================================================
-- DỮ LIỆU KHỞI TẠO BAN ĐẦU (SEED DATA MẪU PHỤC VỤ TEST ĐỒ ÁN)
-- ====================================================================

-- 1. Nạp vai trò
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên toàn hệ thống'),
(2, 'ROLE_MANAGER', 'Quản lý chi nhánh cửa hàng sách'),
(3, 'ROLE_USER', 'Khách hàng mua sách và ký gửi'),
(4, 'ROLE_SHIPPER', 'Nhân viên giao nhận đơn hàng');

-- 2. Nạp người dùng mẫu (Mật khẩu mặc định: 123456 -> băm BCrypt: $2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6)
INSERT INTO users (id, role_id, username, email, password, full_name, phone, avatar, enabled) VALUES
(1, 1, 'admin', 'admin@oldbookstore.vn', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Quản Trị Viên Hệ Thống', '0901234567', 'https://res.cloudinary.com/demo/image/upload/v1/samples/people/smiling-man.jpg', TRUE),
(2, 2, 'manager_td', 'thu_duc@oldbookstore.vn', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Quản Lý CN Thủ Đức', '0912345678', 'https://res.cloudinary.com/demo/image/upload/v1/samples/people/boy-snow-hoodie.jpg', TRUE),
(3, 2, 'manager_q1', 'quan1@oldbookstore.vn', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Quản Lý CN Quận 1', '0923456789', 'https://res.cloudinary.com/demo/image/upload/v1/samples/people/kitchen-bar.jpg', TRUE),
(4, 3, 'phuc_customer', 'phuc@student.hcmute.edu.vn', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Nguyễn Song Hoàng Phúc', '0934567890', 'https://res.cloudinary.com/demo/image/upload/v1/samples/people/jazz-singer.jpg', TRUE),
(5, 3, 'cuong_customer', 'cuong@student.hcmute.edu.vn', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Thái Duy Cường', '0945678901', 'https://res.cloudinary.com/demo/image/upload/v1/samples/people/musical-duo.jpg', TRUE),
(6, 4, 'shipper_hung', 'shipper_hung@oldbookstore.vn', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Trần Văn Hùng (Shipper)', '0956789012', 'https://res.cloudinary.com/demo/image/upload/v1/samples/people/bicycle.jpg', TRUE);

-- 3. Nạp địa chỉ giao hàng mẫu
INSERT INTO addresses (id, user_id, receiver_name, phone, province, district, ward, street_address, is_default) VALUES
(1, 4, 'Nguyễn Song Hoàng Phúc', '0934567890', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', 'Linh Chiểu', 'Số 1 Võ Văn Ngân', TRUE),
(2, 4, 'Hoàng Phúc (Nhà riêng)', '0934567890', 'TP. Hồ Chí Minh', 'Quận Bình Thạnh', 'Phường 25', '125 Điện Biên Phủ', FALSE),
(3, 5, 'Thái Duy Cường', '0945678901', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', 'Hiệp Phú', 'Khu Công Nghệ Cao Q9', TRUE);

-- 4. Nạp chi nhánh chuỗi cửa hàng sách cũ
INSERT INTO stores (id, manager_id, store_name, slug, phone, email, address, province, district, open_time, close_time, image, is_active) VALUES
(1, 2, 'Chi Nhánh Thủ Đức (Gần HCMUTE)', 'chi-nhanh-thu-duc', '02837221223', 'thu_duc@oldbookstore.vn', 'Số 48 Võ Văn Ngân, Phường Linh Chiểu', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', '08:00', '21:30', 'https://images.unsplash.com/photo-1507842229451-7f01be777a26?w=800', TRUE),
(2, 3, 'Chi Nhánh Đường Sách Quận 1', 'chi-nhanh-quan-1', '02838229988', 'quan1@oldbookstore.vn', 'Đường Sách Nguyễn Văn Bình, Phường Bến Nghé', 'TP. Hồ Chí Minh', 'Quận 1', '08:30', '22:00', 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800', TRUE),
(3, NULL, 'Chi Nhánh Phố Cổ Hà Nội', 'chi-nhanh-ha-noi', '02439268899', 'hanoi@oldbookstore.vn', 'Phố Sách Đinh Lễ, Phường Tràng Tiền', 'Hà Nội', 'Quận Hoàn Kiếm', '08:00', '21:00', 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800', TRUE);

-- 5. Nạp danh mục thể loại sách
INSERT INTO categories (id, parent_id, category_name, slug, description, icon, is_active) VALUES
(1, NULL, 'Văn Học & Tiểu Thuyết', 'van-hoc-tieu-thuyet', 'Các tác phẩm văn học kinh điển Việt Nam và thế giới', 'fa-book-open', TRUE),
(2, NULL, 'Công Nghệ Thông Tin & Kỹ Thuật', 'cntt-ky-thuat', 'Sách giáo trình, lập trình viên, mạng máy tính', 'fa-laptop-code', TRUE),
(3, NULL, 'Kinh Tế & Quản Trị', 'kinh-te-quan-tri', 'Sách kinh doanh, khởi nghiệp, tài chính, đầu tư', 'fa-chart-line', TRUE),
(4, NULL, 'Lịch Sử & Văn Hóa', 'lich-su-van-hoa', 'Sách nghiên cứu sử liệu, văn hóa cổ truyền', 'fa-landmark', TRUE),
(5, NULL, 'Ngoại Ngữ & Từ Điển', 'ngoai-ngu-tu-dien', 'Giáo trình tiếng Anh, TOEIC, từ điển cổ xưa', 'fa-language', TRUE);

-- 6. Nạp danh sách sách cũ mẫu (Có cuốn sold > 10 phục vụ test trang chủ Guest)
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured) VALUES
(1, 1, 'Số Đỏ (Bản in NXB Văn Học 1996)', 'so-do-nxb-van-hoc-1996', 'Vũ Trọng Phụng', 'NXB Văn Học', 1996, '9786046912341', 85, 'Bìa hơi sờn mép theo thời gian, ruột nguyên vẹn đầy đủ trang, giấy ố vàng cổ kính.', 'Tác phẩm hiện thực phê phán trứ danh của văn học Việt Nam trước 1945.', 80000.00, 45000.00, 40000.00, 24, 350, TRUE, TRUE),
(2, 1, 'Nhà Giả Kim (Bản bìa mềm 2015)', 'nha-gia-kim-2015', 'Paulo Coelho', 'NXB Hội Nhà Văn', 2015, '9786045332211', 95, 'Sách còn như mới, không gập góc, không ghi chép.', 'Cuốn sách bán chạy nhất mọi thời đại về hành trình tìm kiếm vận mệnh.', 79000.00, 50000.00, 45000.00, 18, 520, TRUE, TRUE),
(3, 1, 'Rừng Na Uy (Bản dịch Trịnh Lữ)', 'rung-na-uy-trinh-lu', 'Haruki Murakami', 'NXB Nhã Nam', 2011, '9786049281144', 90, 'Gáy sách chắc chắn, mép dưới có chữ ký của chủ cũ.', 'Tiểu thuyết kinh điển của văn học đương đại Nhật Bản.', 115000.00, 70000.00, 65000.00, 15, 410, TRUE, TRUE),
(4, 2, 'Lập Trình Web Với Java & Spring (Giáo trình)', 'lap-trinh-web-java-spring', 'Bộ Môn CNPM HCMUTE', 'NXB Đại Học Quốc Gia', 2021, '9786047388992', 92, 'Sách dùng lướt, vài trang đầu có highlight bút nhớ vàng.', 'Giáo trình cốt lõi cho sinh viên theo học môn Lập trình Web.', 150000.00, 85000.00, 80000.00, 32, 890, TRUE, TRUE),
(5, 2, 'Design Patterns: Elements of Reusable Object-Oriented Software', 'design-patterns-gang-of-four', 'Erich Gamma et al.', 'Addison-Wesley', 2008, '9780201633610', 88, 'Bản gốc tiếng Anh, bìa cứng, góc gáy hơi mòn, trang sách sạch sẽ.', 'Cuốn kinh thánh về 23 mẫu thiết kế phần mềm kinh điển của Gang of Four.', 450000.00, 190000.00, 180000.00, 12, 670, TRUE, TRUE),
(6, 3, 'Đắc Nhân Tâm (Bản dịch Nguyễn Hiến Lê 1974)', 'dac-nhan-tam-nguyen-hien-le-1974', 'Dale Carnegie (Nguyễn Hiến Lê dịch)', 'Cơ sở xuất bản Tao Đàn', 1974, 'N/A-1974', 80, 'Ấn bản hiếm trước 1975, gáy dán băng keo trong bảo vệ, trang ngả nâu cổ điển.', 'Bản dịch huyền thoại giữ trọn vẹn văn phong mộc mạc triết lý.', 250000.00, 160000.00, 150000.00, 11, 820, TRUE, TRUE),
(7, 3, 'Cha Giàu Cha Nghèo (Tập 1 - 2012)', 'cha-giau-cha-ngheo-tap-1', 'Robert T. Kiyosaki', 'NXB Trẻ', 2012, '9786041011223', 90, 'Tình trạng tốt, không rách trang.', 'Bài học cơ bản về tư duy tự do tài chính và đầu tư cá nhân.', 95000.00, 55000.00, 50000.00, 28, 480, TRUE, TRUE),
(8, 4, 'Đại Việt Sử Ký Toàn Thư (Bản trọn bộ 2004)', 'dai-viet-su-ky-toan-thu-2004', 'Ngô Sĩ Liên', 'NXB Khoa Học Xã Hội', 2004, '9786048991201', 95, 'Bìa cứng gáy mạ vàng, bảo quản trong tủ kính, gần như mới 95%.', 'Bộ quốc sử ghi chép đầy đủ từ thời Hồng Bàng đến triều Hậu Lê.', 600000.00, 380000.00, 350000.00, 8, 290, TRUE, FALSE),
(9, 5, 'English Grammar in Use (Raymond Murphy - 4th Edition)', 'english-grammar-in-use-4th', 'Raymond Murphy', 'Cambridge University Press', 2015, '9780521189064', 85, 'Đã làm bài tập bằng bút chì các Unit 1-10, các trang sau mới tinh.', 'Cẩm nang học ngữ pháp tiếng Anh thông dụng và hiệu quả nhất thế giới.', 180000.00, 95000.00, 90000.00, 22, 610, TRUE, TRUE);

-- 7. Nạp ảnh chi tiết nhiều góc cạnh của sách (Cloudinary)
INSERT INTO book_images (id, book_id, image_url, public_id, angle_description, is_primary, display_order) VALUES
(1, 1, 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/so-do-bia.jpg', 'so-do-bia', 'Bìa trước NXB Văn Học 1996', TRUE, 1),
(2, 1, 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/so-do-gay.jpg', 'so-do-gay', 'Gáy sách cổ', FALSE, 2),
(3, 2, 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/nha-gia-kim-bia.jpg', 'nha-gia-kim-bia', 'Bìa trước', TRUE, 1),
(4, 3, 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/rung-na-uy-bia.jpg', 'rung-na-uy-bia', 'Bìa trước Nhã Nam', TRUE, 1),
(5, 4, 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/java-spring-bia.jpg', 'java-spring-bia', 'Bìa giáo trình HCMUTE', TRUE, 1),
(6, 6, 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/dac-nhan-tam-1974.jpg', 'dac-nhan-tam-1974', 'Ấn bản Tao Đàn 1974', TRUE, 1);

-- 8. Nạp số lượng tồn kho theo từng chi nhánh
INSERT INTO inventory (store_id, book_id, quantity) VALUES
(1, 1, 3), (2, 1, 2),                  -- Sách 1 có tại CN Thủ Đức và CN Quận 1
(1, 2, 5), (2, 2, 4), (3, 2, 2),        -- Sách 2 có tại cả 3 chi nhánh
(1, 3, 2), (2, 3, 3),                  -- Sách 3
(1, 4, 15), (2, 4, 8),                 -- Giáo trình Java có nhiều ở CN Thủ Đức
(1, 5, 1), (3, 5, 2),                  -- Design Patterns
(2, 6, 1),                             -- Đắc Nhân Tâm 1974 bản hiếm duy nhất tại CN Quận 1
(1, 7, 6), (2, 7, 4),
(2, 8, 1), (3, 8, 1),
(1, 9, 7), (2, 9, 5);

-- 9. Nạp đơn vị vận chuyển
INSERT INTO shipping_units (id, name, code, contact_phone, base_fee, estimated_days, is_active) VALUES
(1, 'Giao Hàng Tiết Kiệm (GHTK)', 'GHTK', '18006092', 22000.00, '1-2 ngày', TRUE),
(2, 'Giao Hàng Nhanh (GHN Express)', 'GHN', '1900636677', 28000.00, 'Trong ngày hoặc 24h', TRUE),
(3, 'Viettel Post', 'VIETTEL', '19008095', 25000.00, '2-3 ngày', TRUE);

-- 10. Nạp mã giảm giá Voucher
INSERT INTO vouchers (id, code, description, discount_type, discount_value, min_order_amount, max_discount, start_date, end_date, usage_limit, used_count, is_active) VALUES
(1, 'SACHCU20K', 'Giảm trực tiếp 20.000đ cho đơn từ 100.000đ', 'FIXED_AMOUNT', 20000.00, 100000.00, 20000.00, '2026-09-01 00:00:00', '2026-11-30 23:59:59', 200, 15, TRUE),
(2, 'FREESHIP50K', 'Giảm 100% phí vận chuyển cho đơn sách cũ từ 200.000đ', 'FIXED_AMOUNT', 30000.00, 20000.00, 30000.00, '2026-09-01 00:00:00', '2026-11-30 23:59:59', 100, 28, TRUE),
(3, 'HOISINHSACH10', 'Giảm 10% tổng giá trị đơn hàng mừng ngày hội sách', 'PERCENT', 10.00, 150000.00, 50000.00, '2026-09-01 00:00:00', '2026-12-31 23:59:59', 500, 42, TRUE);

-- 11. Nạp đơn hàng mẫu (Đủ 6 trạng thái đơn hàng)
INSERT INTO orders (id, order_code, user_id, store_id, shipping_unit_id, shipper_id, voucher_id, receiver_name, receiver_phone, receiver_address, delivery_method, payment_method, payment_status, order_status, subtotal, shipping_fee, discount_amount, final_amount, customer_notes, created_at) VALUES
(1, 'ORD-20260920-001', 4, 1, 1, 6, 1, 'Nguyễn Song Hoàng Phúc', '0934567890', 'Số 1 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức', 'HOME_DELIVERY', 'VNPAY', 'PAID', 'DELIVERED', 125000.00, 22000.00, 20000.00, 127000.00, 'Giao giờ hành chính giúp em.', '2026-09-20 09:15:00'),
(2, 'ORD-20260921-002', 4, 2, 2, 6, NULL, 'Nguyễn Song Hoàng Phúc', '0934567890', '125 Điện Biên Phủ, P. 25, Q. Bình Thạnh', 'HOME_DELIVERY', 'COD', 'UNPAID', 'SHIPPING', 150000.00, 28000.00, 0.00, 178000.00, 'Bọc chống sốc kỹ cuốn cổ này nhé shop.', '2026-09-21 14:30:00'),
(3, 'ORD-20260922-003', 5, 1, NULL, NULL, NULL, 'Thái Duy Cường', '0945678901', 'Nhận tại Chi Nhánh Thủ Đức', 'STORE_PICKUP', 'COD', 'UNPAID', 'NEW', 80000.00, 0.00, 0.00, 80000.00, 'Chiều mai mình ghé quán nhận sách.', '2026-09-22 10:00:00'),
(4, 'ORD-20260922-004', 5, 1, 1, 6, NULL, 'Thái Duy Cường', '0945678901', 'Khu Công Nghệ Cao Q9, TP. Thủ Đức', 'HOME_DELIVERY', 'COD', 'UNPAID', 'CONFIRMED', 45000.00, 22000.00, 0.00, 67000.00, 'Đã xác nhận đơn hàng chuẩn bị đóng gói.', '2026-09-22 16:45:00');

-- 12. Nạp chi tiết đơn hàng
INSERT INTO order_items (id, order_id, book_id, book_title, condition_percent, price, quantity, total_price) VALUES
(1, 1, 1, 'Số Đỏ (Bản in NXB Văn Học 1996)', 85, 45000.00, 1, 45000.00),
(2, 1, 4, 'Lập Trình Web Với Java & Spring (Giáo trình)', 92, 80000.00, 1, 80000.00),
(3, 2, 6, 'Đắc Nhân Tâm (Bản dịch Nguyễn Hiến Lê 1974)', 80, 150000.00, 1, 150000.00),
(4, 3, 4, 'Lập Trình Web Với Java & Spring (Giáo trình)', 92, 80000.00, 1, 80000.00),
(5, 4, 2, 'Nhà Giả Kim (Bản bìa mềm 2015)', 95, 45000.00, 1, 45000.00);

-- 13. Nạp đánh giá mẫu (Kiểm tra độ dài >= 50 ký tự và có ảnh Cloudinary)
INSERT INTO reviews (id, user_id, book_id, order_id, rating, comment, media_url, media_type) VALUES
(1, 4, 1, 1, 5, 'Sách cũ nhưng được bảo quản rất cẩn thận, bìa tuy có ố nhẹ đúng chất thời gian năm 1996 nhưng các trang bên trong hoàn toàn không bị mối mọt, chữ in rõ nét, đọc rất thích!', 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/review-sodo-real.jpg', 'IMAGE'),
(2, 4, 4, 1, 5, 'Giáo trình Lập trình Web của trường bản này còn rất mới, các phần ghi chú của anh chị khóa trước để lại rất hữu ích cho kỳ thi cuối kỳ môn này. Đóng gói rất chắc chắn!', 'https://res.cloudinary.com/demo/image/upload/v1/samples/books/review-java-real.jpg', 'IMAGE');

-- 14. Nạp đơn đăng ký ký gửi sách cũ mẫu
INSERT INTO book_consignments (id, user_id, store_id, book_title, author, publisher, publish_year, condition_percent, condition_description, photos_json, proposed_price, agreed_price, status, admin_notes) VALUES
(1, 4, 1, 'Kính Vạn Hoa (Trọn bộ 54 tập bản cũ)', 'Nguyễn Nhật Ánh', 'NXB Kim Đồng', 2002, 85, 'Bộ truyện tuổi thơ giữ được 50/54 cuốn, bìa có nếp gấp nhẹ nhưng gáy đều nguyên vẹn không long trang.', '["https://res.cloudinary.com/demo/image/upload/v1/samples/books/kinh-van-hoa-1.jpg","https://res.cloudinary.com/demo/image/upload/v1/samples/books/kinh-van-hoa-2.jpg"]', 600000.00, 550000.00, 'APPROVED', 'Đã thẩm định hình ảnh đạt yêu cầu, hẹn bạn Phúc mang sách ra chi nhánh Thủ Đức để nhận tiền.');

-- 15. Nạp danh sách yêu thích và sách đã xem mẫu
INSERT INTO wishlists (user_id, book_id) VALUES
(4, 3), (4, 5), (4, 6),
(5, 1), (5, 4);

INSERT INTO viewed_books (user_id, book_id) VALUES
(4, 1), (4, 2), (4, 4), (4, 6),
(5, 3), (5, 4), (5, 7);
