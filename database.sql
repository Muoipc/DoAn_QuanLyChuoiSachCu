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
    order_id BIGINT NULL,
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

-- 20. BẢNG KHO VOUCHER CỦA NGƯỜI DÙNG (USER_VOUCHERS)
CREATE TABLE user_vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    is_used TINYINT(1) NOT NULL DEFAULT 0,
    saved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at DATETIME NULL DEFAULT NULL,
    UNIQUE KEY uk_user_voucher (user_id, voucher_id),
    CONSTRAINT fk_user_vouchers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_vouchers_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- DỮ LIỆU KHỞI TẠO BAN ĐẦU (SEED DATA MẪU PHỤC VỤ TEST ĐỒ ÁN)
-- ====================================================================

-- 1. Nạp vai trò
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên toàn hệ thống'),
(2, 'ROLE_MANAGER', 'Quản lý chi nhánh cửa hàng sách'),
(3, 'ROLE_USER', 'Khách hàng mua sách và ký gửi'),
(4, 'ROLE_SHIPPER', 'Nhân viên giao nhận đơn hàng');

-- 2. Nạp người dùng mẫu (Mật khẩu mặc định: 123456 -> băm BCrypt: $2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i)
INSERT INTO users (id, role_id, username, email, password, full_name, phone, avatar, enabled) VALUES
(1, 1, 'admin', 'admin@oldbookstore.vn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Quản Trị Viên Hệ Thống', '0901234567', '/images/avatars/avatar-admin.png', TRUE),
(2, 2, 'manager_td', 'thu_duc@oldbookstore.vn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Quản Lý CN Thủ Đức', '0912345678', '/images/avatars/avatar-default.png', TRUE),
(3, 2, 'manager_q1', 'quan1@oldbookstore.vn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Quản Lý CN Quận 1', '0923456789', '/images/avatars/avatar-default.png', TRUE),
(4, 3, 'phuc_customer', 'phuc@student.hcmute.edu.vn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Nguyễn Song Hoàng Phúc', '0934567890', '/images/avatars/avatar-phuc.png', TRUE),
(5, 3, 'cuong_customer', 'cuong@student.hcmute.edu.vn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Thái Duy Cường', '0945678901', '/images/avatars/avatar-cuong.png', TRUE),
(6, 4, 'shipper_hung', 'shipper_hung@oldbookstore.vn', '$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i', 'Trần Văn Hùng (Shipper)', '0956789012', '/images/avatars/avatar-default.png', TRUE);

-- 3. Nạp địa chỉ giao hàng mẫu
INSERT INTO addresses (id, user_id, receiver_name, phone, province, district, ward, street_address, is_default) VALUES
(1, 4, 'Nguyễn Song Hoàng Phúc', '0934567890', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', 'Linh Chiểu', 'Số 1 Võ Văn Ngân', TRUE),
(2, 4, 'Hoàng Phúc (Nhà riêng)', '0934567890', 'TP. Hồ Chí Minh', 'Quận Bình Thạnh', 'Phường 25', '125 Điện Biên Phủ', FALSE),
(3, 5, 'Thái Duy Cường', '0945678901', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', 'Hiệp Phú', 'Khu Công Nghệ Cao Q9', TRUE);

-- 4. Nạp 5 chi nhánh chuỗi cửa hàng sách cũ tại TP. Hồ Chí Minh
INSERT INTO stores (id, manager_id, store_name, slug, phone, email, address, province, district, open_time, close_time, image, is_active) VALUES
(1, 2, 'Chi Nhánh 1: Thủ Đức (Gần HCMUTE)', 'chi-nhanh-thu-duc', '02837221223', 'thuduc@oldbookstore.vn', 'Số 48 Võ Văn Ngân, Phường Linh Chiểu', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', '08:00', '21:30', 'https://images.unsplash.com/photo-1507842229451-7f01be777a26?w=800', TRUE),
(2, 3, 'Chi Nhánh 2: Đường Sách Quận 1', 'chi-nhanh-quan-1', '02838229988', 'quan1@oldbookstore.vn', 'Đường Sách Nguyễn Văn Bình, Phường Bến Nghé', 'TP. Hồ Chí Minh', 'Quận 1', '08:30', '22:00', 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800', TRUE),
(3, NULL, 'Chi Nhánh 3: Làng Đại Học Quốc Gia', 'chi-nhanh-lang-dai-hoc', '02837244555', 'langdaihoc@oldbookstore.vn', 'Khu Đô Thị ĐHQG-HCM, Phường Linh Trung', 'TP. Hồ Chí Minh', 'TP. Thủ Đức', '07:30', '21:30', 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800', TRUE),
(4, NULL, 'Chi Nhánh 4: Phố Sách Cũ Phú Nhuận', 'chi-nhanh-phu-nhuan', '02838447766', 'phunhuan@oldbookstore.vn', 'Số 120 Trần Huy Liệu, Phường 15', 'TP. Hồ Chí Minh', 'Quận Phú Nhuận', '08:00', '21:00', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800', TRUE),
(5, NULL, 'Chi Nhánh 5: Khu Học Thuật Quận 5', 'chi-nhanh-quan-5', '02838351122', 'quan5@oldbookstore.vn', 'Số 280 An Dương Vương, Phường 4', 'TP. Hồ Chí Minh', 'Quận 5', '08:00', '21:30', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', TRUE);

-- 5. Nạp danh mục thể loại sách
INSERT INTO `categories` (`id`, `parent_id`, `category_name`, `slug`, `description`, `icon`, `is_active`) VALUES
(1, NULL, 'Văn Học & Tiểu Thuyết', 'van-hoc-tieu-thuyet', 'Các tác phẩm văn học kinh điển Việt Nam và thế giới', 'fa-book-open', 1),
(2, NULL, 'Công Nghệ Thông Tin & Kỹ Thuật', 'cntt-ky-thuat', 'Sách giáo trình, lập trình viên, mạng máy tính', 'fa-laptop-code', 1),
(3, NULL, 'Kinh Tế & Quản Trị', 'kinh-te-quan-tri', 'Sách kinh doanh, khởi nghiệp, tài chính, đầu tư', 'fa-chart-line', 1),
(4, NULL, 'Lịch Sử & Văn Hóa', 'lich-su-van-hoa', 'Sách nghiên cứu sử liệu, văn hóa cổ truyền', 'fa-landmark', 1),
(5, NULL, 'Ngoại Ngữ & Từ Điển', 'ngoai-ngu-tu-dien', 'Giáo trình tiếng Anh, TOEIC, từ điển cổ xưa', 'fa-language', 1),
(6, NULL, 'Kỹ Năng Sống & Tâm Lý', 'ky-nang-tam-ly', 'Sách phát triển bản thân, tâm lý học và tư duy', 'fa-lightbulb', 1),
(7, NULL, 'Giáo Trình ĐH & HCMUTE', 'giao-trinh-hcmute', 'Giáo trình kỹ thuật, công nghệ và bài giảng chuyên ngành', 'fa-graduation-cap', 1),
(8, NULL, 'Truyện Tranh & Manga Cổ', 'truyen-tranh-manga', 'Bộ truyện tranh gắn liền tuổi thơ và ấn bản sưu tầm', 'fa-palette', 1);

-- 6. Nạp danh sách 20 cuốn sách cũ mẫu chuẩn bìa thật 100% (Khớp ảnh bìa thực tế)
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (1, 1, 'Yêu Những Điều Không Hoàn Hảo', 'yeu-nhung-dieu-khong-hoan-hao', 'Hae Min', 'NXB Thế Giới', 2018, '9786047748123', 95, 'Bản in nguyên vẹn 95%, ruột sạch đẹp không ghi chú, đã niêm phong màng co bảo quản.', 'Tác phẩm tản văn chữa lành tâm hồn sâu sắc của Đại đức Hae Min, giúp người đọc bao dung và thấu hiểu chính mình giữa dòng đời vội vã.', 120000, 68000, 65000, 48, 1250, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (1, '/images/books/book-1.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 1, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 1, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 1, 0);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 1, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 1, 4);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (2, 1, 'Nhà Giả Kim (The Alchemist)', 'nha-gia-kim-paulo-coelho', 'Paulo Coelho', 'NXB Nhã Nam', 2015, '9780062315007', 90, 'Sách nguyên vẹn, gáy chắc, mép sách hơi ố vàng nhẹ theo thời gian, không thiếu trang.', 'Kiệt tác vượt thời gian về hành trình theo đuổi vận mệnh của chàng chăn cừu Santiago qua sa mạc.', 79000, 45000, 42000, 35, 980, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (2, '/images/books/book-2.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 2, 6);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 2, 0);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 2, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 2, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 2, 5);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (3, 3, 'Thay Đổi Tí Hon (Atomic Habits)', 'thay-doi-ti-hon-atomic-habits', 'James Clear', 'NXB Thế Giới', 2019, '9780735211292', 98, 'Sách mới 98%, không nếp gấp, hệ thống phương pháp xây dựng thói quen tốt vượt trội.', 'Cẩm nang hướng dẫn từng bước thay đổi những thói quen nhỏ để đạt được kết quả phi thường.', 189000, 95000, 90000, 32, 1100, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (3, '/images/books/book-3.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 3, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 3, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 3, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 3, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 3, 2);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (4, 3, 'Đắc Nhân Tâm (How to Win Friends)', 'dac-nhan-tam-dale-carnegie', 'Dale Carnegie', 'First News - NXB Tổng Hợp', 2016, '9780671027032', 85, 'Bìa cứng sang trọng, gáy vững chắc, ruột sách sạch sẽ, cẩm nang giao tiếp kinh điển.', 'Cẩm nang giao tiếp, đối nhân xử thế kinh điển có ảnh hưởng lớn nhất mọi thời đại.', 86000, 40000, 38000, 22, 760, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (4, '/images/books/book-4.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 4, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 4, 0);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 4, 8);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 4, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 4, 3);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (5, 3, 'Dạy Con Làm Giàu (Rich Dad Poor Dad)', 'day-con-lam-giau-robert-kiyosaki', 'Robert T. Kiyosaki', 'NXB Trẻ', 2012, '9781612680194', 88, 'Sách đọc tốt, các bài học tư duy phân biệt tài sản và tiêu sản còn nguyên giá trị.', 'Bộ sách giáo dục tài chính cá nhân bán chạy nhất hành tinh, thay đổi tư duy làm giàu.', 95000, 48000, 45000, 33, 790, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (5, '/images/books/book-5.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 5, 6);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 5, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 5, 0);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 5, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 5, 4);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (6, 3, 'Tư Duy Nhanh Và Chậm', 'tu-duy-nhanh-va-cham-kahneman', 'Daniel Kahneman', 'NXB Thế Giới', 2018, '9780374533557', 92, 'Ấn bản đẹp, giải thưởng Nobel Kinh tế, phân tích sâu sắc hệ thống tư duy 1 và 2.', 'Kiệt tác tâm lý học hành vi vạch trần những sai lầm có hệ thống trong cách con người phán đoán.', 220000, 110000, 105000, 26, 890, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (6, '/images/books/book-6.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 6, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 6, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 6, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 6, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 6, 5);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (7, 3, 'Thấu Hiểu Sang Chấn (The Body Keeps the Score)', 'thau-hieu-sang-chan', 'Bessel van der Kolk', 'NXB Lao Động', 2021, '9780143127741', 95, 'Sách như mới 95%, chuyên khảo y khoa tâm lý sâu sắc về chữa lành sang chấn tâm lý.', 'Công trình khoa học đột phá về cách sang chấn định hình lại não bộ và phương pháp chữa lành.', 250000, 125000, 120000, 28, 940, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (7, '/images/books/book-7.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 7, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 7, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 7, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 7, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 7, 2);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (8, 1, 'Những Thứ Ba Với Thầy Morrie', 'nhung-thu-ba-voi-thay-morrie', 'Mitch Albom', 'NXB Trẻ', 2014, '9780767905923', 90, 'Sách nguyên vẹn, các bài học về tình yêu, công việc, tuổi già và sự tha thứ lay động tâm can.', 'Hồi ký cảm động về những buổi trò chuyện thứ ba đầy minh triết giữa một học trò và người thầy sắp qua đời.', 85000, 45000, 42000, 24, 710, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (8, '/images/books/book-8.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 8, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 8, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 8, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 8, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 8, 3);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (9, 1, 'Tội Ác Và Trừng Phạt (Crime and Punishment)', 'toi-ac-va-trung-phat-crime-and-punishment', 'Fyodor Dostoevsky', 'NXB Văn Học', 2016, '9780679734772', 85, 'Tiểu thuyết kinh điển nước Nga, độ dày hơn 700 trang giữ nguyên vẹn gáy keo chỉ chắc chắn.', 'Đỉnh cao phân tích tâm lý tội phạm và sự cứu chuộc lương tâm con người của Dostoevsky.', 165000, 85000, 80000, 19, 650, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (9, '/images/books/book-9.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 9, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 9, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 9, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 9, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 9, 4);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (10, 1, 'Hoàng Tử Bé (The Little Prince)', 'hoang-tu-be-saint-exupery', 'Antoine de Saint-Exupéry', 'NXB Kim Đồng', 2019, '9780140283334', 95, 'Ấn bản minh họa màu đẹp mắt của NXB Kim Đồng, gáy vuông vắn, không nhăn mép.', 'Tác phẩm văn học kỳ diệu về tình bạn, bông hồng duy nhất và cái nhìn thuần khiết của trẻ thơ.', 75000, 38000, 35000, 31, 920, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (10, '/images/books/book-10.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 10, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 10, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 10, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 10, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 10, 5);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (11, 1, 'Một Chín Tám Bốn (1984)', 'mot-chin-tam-bon-1984-orwell', 'George Orwell', 'NXB Nhã Nam', 2017, '9780451524935', 90, 'Kiệt tác phản địa đàng thế kỷ 20, sách giữ gìn cẩn thận, ruột giấy vàng cổ điển.', 'Bức tranh tiên tri rùng mình về một xã hội bị giám sát tuyệt đối và sự tha hóa của quyền lực.', 110000, 55000, 50000, 27, 880, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (11, '/images/books/book-11.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 11, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 11, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 11, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 11, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 11, 2);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (12, 1, 'Trại Súc Vật (Animal Farm)', 'trai-suc-vat-animal-farm', 'George Orwell', 'NXB Nhã Nam', 2016, '9780451526342', 92, 'Sách nhỏ gọn, gáy phẳng phiu, truyện ngụ ngôn chính trị sắc sảo bậc nhất.', 'Truyện ngụ ngôn trào phúng kinh điển về cuộc nổi dậy của muông thú và sự biến chất của lý tưởng.', 68000, 35000, 32000, 25, 780, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (12, '/images/books/book-12.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 12, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 12, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 12, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 12, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 12, 3);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (13, 1, 'Gatsby Vĩ Đại (The Great Gatsby)', 'gatsby-vi-dai-fitzgerald', 'F. Scott Fitzgerald', 'NXB Văn Học', 2018, '9780743273565', 88, 'Bản dịch kinh điển, bìa đẹp, trang sách sạch không vết bẩn, khắc họa thời kỳ Jazz lộng lẫy.', 'Bi kịch tình yêu và giấc mơ Mỹ tan vỡ của triệu phú bí ẩn Jay Gatsby bên bờ vịnh Long Island.', 95000, 48000, 45000, 21, 670, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (13, '/images/books/book-13.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 13, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 13, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 13, 0);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 13, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 13, 4);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (14, 1, '451 Độ F (Fahrenheit 451)', '451-do-f-fahrenheit-451', 'Ray Bradbury', 'NXB Nhã Nam', 2017, '9781451673319', 90, 'Sách nguyên vẹn, câu chuyện về chàng lính cứu hỏa Montag đốt sách và sự thức tỉnh.', 'Tác phẩm viễn tưởng kinh điển cảnh báo về một thế giới mà tri thức và sách vở bị cấm đoán.', 105000, 52000, 48000, 23, 740, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (14, '/images/books/book-14.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 14, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 14, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 14, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 14, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 14, 5);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (15, 1, 'Người Đàn Ông Mang Tên Ove', 'nguoi-dan-ong-mang-ten-ove', 'Fredrik Backman', 'NXB Trẻ', 2018, '9781476738024', 95, 'Sách gần như mới 95%, câu chuyện ấm áp về sự tử tế và tình người trong xóm nhỏ.', 'Tiểu thuyết cảm động hài hước về một ông lão khó tính nhưng mang trái tim ấm áp nhất thế giới.', 135000, 68000, 65000, 38, 1150, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (15, '/images/books/book-15.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 15, 6);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 15, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 15, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 15, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 15, 2);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (16, 1, 'Chúa Ruồi (Lord of the Flies)', 'chua-ruoi-william-golding', 'William Golding', 'NXB Hội Nhà Văn', 2015, '9780425274866', 88, 'Bản in tốt, tác phẩm đoạt giải Nobel văn học, giải mã bản tính con người khi cô lập.', 'Câu chuyện rùng mình về một nhóm học sinh mắc kẹt trên đảo hoang và sự thoái hóa nhân tính.', 110000, 55000, 50000, 17, 590, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (16, '/images/books/book-16.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 16, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 16, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 16, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 16, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 16, 3);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (17, 1, 'Kiêu Hãnh Và Định Kiến', 'kieu-hanh-va-dinh-kien-jane-austen', 'Jane Austen', 'NXB Văn Học', 2019, '9780141439518', 90, 'Bìa hoa văn cổ điển nước Anh, gáy sách chắc chắn, thiên tình sử Elizabeth và Darcy.', 'Kiệt tác lãng mạn bất hủ về tình yêu vượt qua định kiến xã hội và tự ái cá nhân của Jane Austen.', 120000, 60000, 58000, 25, 680, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (17, '/images/books/book-17.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 17, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 17, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 17, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 17, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 17, 4);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (18, 1, 'Đồi Gió Hú (Wuthering Heights)', 'doi-gio-hu-emily-bronte', 'Emily Brontë', 'NXB Văn Học', 2016, '9780141439600', 85, 'Bản in giữ gìn tốt, thiên tiểu thuyết tình yêu và hận thù dữ dội trên đồng hoang Yorkshire.', 'Bi tình sử cuồng nhiệt và ám ảnh nhất lịch sử văn học giữa Heathcliff và Catherine Earnshaw.', 115000, 58000, 55000, 16, 530, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (18, '/images/books/book-18.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 18, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 18, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 18, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 18, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 18, 5);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (19, 3, 'Học Cách Mặc Kệ (The Let Them Theory)', 'hoc-cach-mac-ke-mel-robbins', 'Mel Robbins', 'NXB Dân Trí', 2024, '9781401971366', 98, 'Sách gần như mới 98%, cuốn sách tâm lý thực hành tạo cơn sốt toàn cầu của Mel Robbins.', 'Phương pháp giải phóng năng lượng tinh thần bằng cách buông bỏ nhu cầu kiểm soát người khác.', 195000, 98000, 95000, 36, 1280, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (19, '/images/books/book-19.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 19, 6);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 19, 3);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 19, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 19, 5);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 19, 2);
INSERT INTO books (id, category_id, title, slug, author, publisher, publish_year, isbn, condition_percent, condition_notes, description, original_price, price, discount_price, total_sold, views_count, is_active, is_featured)
VALUES (20, 1, 'Bảy Người Chồng Của Evelyn Hugo', 'bay-nguoi-chong-cua-evelyn-hugo', 'Taylor Jenkins Reid', 'NXB Trẻ', 2022, '9781501161933', 95, 'Sách mới 95%, bản in bìa áo xanh ngọc tuyệt đẹp, tác phẩm bán chạy hàng đầu thời gian qua.', 'Hồi ký rực rỡ và bi thương của huyền thoại điện ảnh Hollywood Evelyn Hugo và bí mật cuộc đời bà.', 168000, 85000, 80000, 30, 1020, 1, 1)
ON DUPLICATE KEY UPDATE
category_id=VALUES(category_id), title=VALUES(title), slug=VALUES(slug), author=VALUES(author), publisher=VALUES(publisher),
publish_year=VALUES(publish_year), isbn=VALUES(isbn), condition_percent=VALUES(condition_percent), condition_notes=VALUES(condition_notes),
description=VALUES(description), original_price=VALUES(original_price), price=VALUES(price), discount_price=VALUES(discount_price),
total_sold=VALUES(total_sold), views_count=VALUES(views_count), is_active=1, is_featured=1;
INSERT INTO book_images (book_id, image_url, angle_description, is_primary, display_order)
VALUES (20, '/images/books/book-20.jpg', 'Ảnh bìa thực tế nguyên bản', 1, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (1, 20, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (2, 20, 4);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (3, 20, 1);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (4, 20, 2);
INSERT INTO inventory (store_id, book_id, quantity)
VALUES (5, 20, 3);

-- 9. Nạp đơn vị vận chuyển
INSERT INTO shipping_units (id, name, code, contact_phone, base_fee, estimated_days, is_active) VALUES
(1, 'Giao Hàng Tiết Kiệm (GHTK)', 'GHTK', '18006092', 22000.00, '1-2 ngày', TRUE),
(2, 'Giao Hàng Nhanh (GHN Express)', 'GHN', '1900636677', 28000.00, 'Trong ngày hoặc 24h', TRUE),
(3, 'Viettel Post', 'VIETTEL', '19008095', 25000.00, '2-3 ngày', TRUE);

-- 10. Nạp mã giảm giá Voucher
INSERT INTO vouchers (id, code, description, discount_type, discount_value, min_order_amount, max_discount, start_date, end_date, usage_limit, used_count, is_active) VALUES
(1, 'SACHCU20K', 'Giảm trực tiếp 20.000đ cho đơn từ 100.000đ', 'FIXED_AMOUNT', 20000.00, 100000.00, 20000.00, '2026-09-01 00:00:00', '2026-11-30 23:59:59', 200, 15, TRUE),
(2, 'FREESHIP50K', 'Giảm 100% phí vận chuyển cho đơn sách cũ từ 200.000đ', 'FIXED_AMOUNT', 30000.00, 20000.00, 30000.00, '2026-09-01 00:00:00', '2026-11-30 23:59:59', 100, 28, TRUE),
(3, 'HOISINHSACH10', 'Giảm 10% tổng giá trị đơn hàng mừng ngày hội sách', 'PERCENT', 10.00, 150000.00, 50000.00, '2026-09-01 00:00:00', '2026-12-31 23:59:59', 500, 42, TRUE),
(4, 'FREESHIP15K', 'Giảm 15.000đ phí vận chuyển cho đơn hàng sách cũ từ 99.000đ', 'FIXED_AMOUNT', 15000.00, 99000.00, 15000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 0, TRUE),
(5, 'FREESHIP30K', 'Miễn phí giao hàng tối đa 30.000đ cho đơn từ 250.000đ toàn quốc', 'FIXED_AMOUNT', 30000.00, 250000.00, 30000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 0, TRUE),
(6, 'SACHCU50K', 'Giảm trực tiếp 50.000đ cho đơn hàng mua sách cũ từ 300.000đ', 'FIXED_AMOUNT', 50000.00, 30000.00, 50000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 0, TRUE),
(7, 'HOISINH20', 'Ưu đãi tái sinh tri thức: Giảm 20% tối đa 80.000đ cho đơn từ 150.000đ', 'PERCENT', 20.00, 150000.00, 80000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 150, 0, TRUE),
(8, 'TRIANVIP', 'Tri ân bạn đọc thân thiết: Giảm 15% tối đa 100.000đ cho đơn từ 200.000đ', 'PERCENT', 15.00, 200000.00, 100000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 0, TRUE),
(9, 'CHAOBANMOI', 'Món quà độc giả mới: Giảm ngay 25.000đ cho đơn hàng đầu tiên từ 80.000đ', 'FIXED_AMOUNT', 25000.00, 80000.00, 25000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000, 0, TRUE),
(10, 'YEUSACH10', 'Giảm 10% giá trị đơn hàng cho cộng đồng yêu sách cũ từ 100.000đ', 'PERCENT', 10.00, 100000.00, 50000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 0, TRUE),
(11, 'MEGA100K', 'Đại tiệc sách cũ: Giảm sốc 100.000đ cho đơn hàng giá trị cao từ 500.000đ', 'FIXED_AMOUNT', 100000.00, 500000.00, 100000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50, 0, TRUE);

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

-- 16. Nạp kho voucher mẫu cho người dùng (user_vouchers)
INSERT INTO user_vouchers (user_id, voucher_id, is_used, saved_at, used_at) VALUES
(4, 1, 1, '2026-09-15 10:00:00', '2026-09-20 09:15:00'),
(4, 4, 0, '2026-09-21 08:30:00', NULL),
(4, 7, 0, '2026-09-22 14:20:00', NULL),
(5, 6, 0, '2026-09-22 11:00:00', NULL),
(5, 9, 0, '2026-09-23 09:00:00', NULL);
