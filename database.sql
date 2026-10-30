-- MySQL dump 10.13  Distrib 26.7.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: old_book_store_db
-- ------------------------------------------------------
-- Server version	26.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '93a6a734-9bce-11f1-8e14-10cbca927bb2:1-5259';

--
-- Current Database: `old_book_store_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `old_book_store_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `old_book_store_db`;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ward` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `street_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_addresses_users` (`user_id`),
  CONSTRAINT `fk_addresses_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (1,4,'Nguyễn Song Hoàng Phúc','0912345678','TP. Hồ Chí Minh','TP. Thủ Đức','Phường Linh Chiểu','Số 48 Võ Văn Ngân',1,'2026-09-25 07:30:53'),(2,4,'Hoàng Phúc (Nhà riêng)','0934567890','TP. Hồ Chí Minh','Quận Bình Thạnh','Phường 25','125 Điện Biên Phủ',0,'2026-09-25 07:30:53'),(3,5,'Thái Duy Cường','0945678901','TP. Hồ Chí Minh','TP. Thủ Đức','Hiệp Phú','Khu Công Nghệ Cao Q9',1,'2026-09-25 07:30:53');
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_consignments`
--

DROP TABLE IF EXISTS `book_consignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_consignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `book_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `author` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `publisher` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `publish_year` int DEFAULT NULL,
  `condition_percent` int NOT NULL,
  `condition_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `photos_json` text COLLATE utf8mb4_unicode_ci,
  `proposed_price` decimal(14,2) NOT NULL,
  `agreed_price` decimal(14,2) DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED','STORED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `admin_notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_consignments_users` (`user_id`),
  KEY `fk_consignments_stores` (`store_id`),
  CONSTRAINT `fk_consignments_stores` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_consignments_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_consignments`
--

LOCK TABLES `book_consignments` WRITE;
/*!40000 ALTER TABLE `book_consignments` DISABLE KEYS */;
INSERT INTO `book_consignments` VALUES (1,8,1,'Kính Vạn Hoa (Trọn bộ 54 tập bản cũ)','Nguyễn Nhật Ánh','NXB Kim Đồng',2002,85,'Bộ truyện tuổi thơ giữ được 50/54 cuốn, bìa có nếp gấp nhẹ nhưng gáy đều nguyên vẹn không long trang.','[\"https://res.cloudinary.com/demo/image/upload/v1/samples/books/kinh-van-hoa-1.jpg\",\"https://res.cloudinary.com/demo/image/upload/v1/samples/books/kinh-van-hoa-2.jpg\"]',600000.00,550000.00,'APPROVED','Đã thẩm định hình ảnh đạt yêu cầu, hẹn bạn Phúc mang sách ra chi nhánh Thủ Đức để nhận tiền.\n[Khách hàng đã đồng ý mức giá thẩm định]','2026-09-25 07:30:53','2026-10-28 17:36:29'),(2,2,1,'sách test','test','test',2021,90,'Bìa và gáy sách còn nguyên vẹn, ruột sách sạch sẽ không rách hay viết vẽ.','/images/books/book_1.jpg',65000.00,NULL,'PENDING',NULL,'2026-10-05 05:03:43','2026-10-05 06:43:03'),(3,8,1,'Dế Mèn Phiêu Lưu Ký (Ấn bản Kim Đồng 1986)','Tô Hoài','NXB Kim Đồng',1986,90,'Bìa gốc còn nguyên, giấy ngả vàng tự nhiên, không rách trang',NULL,180000.00,160000.00,'APPROVED','Sách cổ hiếm, tình trạng bảo quản tốt','2026-10-28 17:36:29','2026-10-28 17:36:29'),(4,8,2,'Giáo Trình Cấu Trúc Dữ Liệu & Giải Thuật HCMUTE','Nhiều Tác Giả','NXB ĐHQG',2021,95,'Sách còn như mới, không gạch xóa',NULL,75000.00,NULL,'PENDING','Đang chờ quản lý quầy thẩm định giá','2026-10-28 17:36:29','2026-10-28 17:36:29');
/*!40000 ALTER TABLE `book_consignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_images`
--

DROP TABLE IF EXISTS `book_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `book_id` bigint NOT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `public_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `angle_description` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_primary` tinyint(1) DEFAULT '0',
  `display_order` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_book_images_books` (`book_id`),
  CONSTRAINT `fk_book_images_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=221 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_images`
--

LOCK TABLES `book_images` WRITE;
/*!40000 ALTER TABLE `book_images` DISABLE KEYS */;
INSERT INTO `book_images` VALUES (1,1,'/images/books/book-1.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(2,2,'/images/books/book-2.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(3,3,'/images/books/book-3.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(4,4,'/images/books/book-4.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(5,5,'/images/books/book-5.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(6,6,'/images/books/book-6.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(7,7,'/images/books/book-7.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(8,8,'/images/books/book-8.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(9,9,'/images/books/book-9.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(10,10,'/images/books/book-10.jpg',NULL,'Ảnh bìa thực tế nguyên bản',0,1),(11,11,'/images/books/book-11.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(12,12,'/images/books/book-12.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(13,13,'/images/books/book-13.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(14,14,'/images/books/book-14.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(15,15,'/images/books/book-15.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(16,16,'/images/books/book-16.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(17,17,'/images/books/book-17.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(18,18,'/images/books/book-18.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(19,19,'/images/books/book-19.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(20,20,'/images/books/book-20.jpg',NULL,'Ảnh bìa thực tế nguyên bản',1,1),(21,6,'https://i.pinimg.com/736x/81/98/1b/81981bef1b19acb0dda9a287cd4f5872.jpg',NULL,'Bìa trước sách - Ấn bản bìa chuẩn biểu tượng bút chì đặc trưng',0,2),(22,6,'https://i.pinimg.com/736x/71/e2/6a/71e26a7f77ea6ebb16fce62154864b1e.jpg',NULL,'Trang ruột sách - Sơ đồ tư duy trực quan hệ thống nhận thức 1 và 2',0,3),(23,6,'https://i.pinimg.com/736x/e7/a9/69/e7a969ca159dca85be51cc571157ed95.jpg',NULL,'Gáy sách và độ dày - Cạnh sách trên kệ cùng các tác phẩm kinh điển',0,4),(24,6,'https://i.pinimg.com/736x/40/4f/35/404f3592983138c951519bd05e29aea5.jpg',NULL,'Góc đọc sách aesthetic - Không gian đọc thư giãn cùng tách cà phê',0,5),(25,6,'https://i.pinimg.com/736x/e2/68/9c/e2689c9cc269bfbeb368f347623892d9.jpg',NULL,'Mặt sau và toàn cảnh sách - Góc nhìn toàn thể cuốn sách thực tế',0,6),(26,7,'https://i.pinimg.com/736x/83/5d/72/835d72e9e4dbeaa3fb5427e7e4590eee.jpg',NULL,'Bìa trước sách - Ấn bản The Body Keeps The Score nguyên gốc sắc nét',0,2),(27,7,'https://i.pinimg.com/736x/d3/bc/73/d3bc73560412d3d7a174a531ab061967.jpg',NULL,'Trang ruột sách - Trang sách mở thực tế kèm ghi chú sâu sắc',0,3),(28,7,'https://i.pinimg.com/736x/c0/c7/29/c0c72985c9905516d01c419883aa76ce.jpg',NULL,'Gáy sách và độ dày - Góc nghiêng thể hiện độ dày của cuốn sách',0,4),(29,7,'https://i.pinimg.com/736x/1f/df/38/1fdf38e77bf4888144ec38f2bc0894be.jpg',NULL,'Góc đọc sách aesthetic - Không gian đọc sách chữa lành ấm cúng',0,5),(30,7,'https://i.pinimg.com/736x/ef/7f/03/ef7f03367101864b2e7bf83ba675dbe1.jpg',NULL,'Mặt sau sách - Bìa sau với trích dẫn thông điệp và lời giới thiệu',0,6),(31,8,'https://i.pinimg.com/736x/a5/42/2a/a5422a0aa310aa814910f44640016c97.jpg',NULL,'Bìa trước sách - Ấn bản bìa kinh điển Tuesdays with Morrie ấm áp',0,2),(32,8,'https://i.pinimg.com/736x/14/09/49/140949d88fdfaf29e3bc3bf066ea9bd8.jpg',NULL,'Trang ruột sách - Trang mở với trích dẫn bài học cuộc đời ý nghĩa',0,3),(33,8,'https://i.pinimg.com/736x/76/94/10/769410340dfc71e45f680fc1dde9cb4a.jpg',NULL,'Gáy sách và cạnh sách - Góc cầm sách cận cảnh thể hiện gáy và giấy',0,4),(34,8,'https://i.pinimg.com/736x/a1/ea/10/a1ea10c527868302a5bfa614ca2311c8.jpg',NULL,'Góc đọc sách aesthetic - Bàn đọc thanh bình đón nắng mai cùng tách trà',0,5),(35,8,'https://i.pinimg.com/736x/00/a5/1b/00a51bdbc18408edbc3f9d4c2fb2311f.jpg',NULL,'Mặt sau và áo bìa - Thiết kế hoàn chỉnh gồm bìa trước gáy và bìa sau',0,6),(36,9,'https://i.pinimg.com/736x/98/19/91/98199175448136eb3c3f18ea7e8a4f27.jpg',NULL,'Bìa trước sách - Ấn bản bìa cổ điển Tội Ác Và Trừng Phạt Dostoevsky',0,2),(37,9,'https://i.pinimg.com/736x/29/ad/8d/29ad8dbdf1937f4d044b98e84af211ef.jpg',NULL,'Trang ruột sách - Trang sách mở với ghi chú phân tích tâm lý nhân vật',0,3),(38,9,'https://i.pinimg.com/736x/04/cd/6d/04cd6dedd06db68683577c6d3601ab16.jpg',NULL,'Gáy sách và độ dày - Gáy cứng vintage phong cách văn học kinh điển',0,4),(39,9,'https://i.pinimg.com/736x/2c/fa/f4/2cfaf4dbbf6350be63ce2dfce03ee47c.jpg',NULL,'Góc đọc sách aesthetic - Không gian dark academia cùng tách cà phê',0,5),(40,9,'https://i.pinimg.com/736x/0f/0e/9a/0f0e9a177f9e5590b830f63aa0cf6ec1.jpg',NULL,'Mặt sau sách - Bìa sau và tổng thể phong cách sách vintage cổ điển',0,6),(41,10,'https://i.pinimg.com/736x/24/89/69/248969d3e57727d05e67497bf43995c3.jpg',NULL,'Bìa trước sách - Hình ảnh bìa Hoàng Tử Bé kinh điển mộng mơ',1,2),(42,10,'https://i.pinimg.com/736x/2d/3b/b5/2d3bb5a3b2905809442bcfa4b5144133.jpg',NULL,'Trang ruột sách - Trang mở kèm minh họa màu nước gốc và trích dẫn',0,3),(43,10,'https://i.pinimg.com/736x/eb/7b/e6/eb7be6a6583dd168185442a38d9e20f0.jpg',NULL,'Gáy sách và cạnh sách - Ấn bản vintage tiếng Pháp Le Petit Prince',0,4),(44,10,'https://i.pinimg.com/736x/9c/85/b9/9c85b923eaacb0bf945131b5683f1b6d.jpg',NULL,'Góc đọc sách aesthetic - Không gian tiệm sách hoài niệm tại Pháp',0,5),(45,10,'https://i.pinimg.com/736x/3f/a1/67/3fa16789a4e03804f9342d082cc8ae1e.jpg',NULL,'Mặt sau và toàn bộ bìa - Bản vẽ trải dài bìa trước gáy và bìa sau',0,6),(46,1,'https://i.pinimg.com/736x/63/8b/8e/638b8e3e6bd61f87fab3ee6eed489894.jpg',NULL,'Bìa trước cận cảnh chi tiết',0,2),(47,1,'https://i.pinimg.com/736x/08/63/95/0863958088ef49841eb85f4c10190ec4.jpg',NULL,'Trang ruột minh họa và đọc thử',0,3),(48,1,'https://i.pinimg.com/736x/f3/2c/c2/f32cc2441205c2a8ae0ed24d3630853c.jpg',NULL,'Gáy sách và góc chụp nghiêng',0,4),(49,1,'https://i.pinimg.com/736x/9b/98/c7/9b98c7a8a0923c49a07594de806c26e5.jpg',NULL,'Góc đọc sách aesthetic không gian ấm cúng',0,5),(50,1,'https://i.pinimg.com/736x/1c/82/d4/1c82d448cdaf7e220dd4206c55dd165f.jpg',NULL,'Mặt sau sách và đoạn trích cảm hứng',0,6),(51,2,'https://i.pinimg.com/736x/7e/91/9b/7e919bbb520e905a1ab43041b138184f.jpg',NULL,'Bìa trước cận cảnh tác phẩm',0,2),(52,2,'https://i.pinimg.com/736x/da/98/42/da98427bb421ffc59b8e52e978ee1ab8.jpg',NULL,'Trang ruột và lời tựa kinh điển',0,3),(53,2,'https://i.pinimg.com/736x/74/0b/e7/740be718485d930252a22749a0956b83.jpg',NULL,'Gáy sách và độ dày trang giấy',0,4),(54,2,'https://i.pinimg.com/736x/95/b7/43/95b743bec59bd0763b1fac7e52118a7d.jpg',NULL,'Góc đọc sách thư giãn cùng cà phê',0,5),(55,2,'https://i.pinimg.com/736x/d2/75/53/d275535ff37ddf902a3bf0b44731b66e.jpg',NULL,'Mặt sau sách và góc nhìn tổng thể',0,6),(56,3,'https://i.pinimg.com/736x/ba/8e/71/ba8e71589d752cbc2a04ef1623715a9d.jpg',NULL,'Bìa trước thiết kế tối giản đặc trưng',0,2),(57,3,'https://i.pinimg.com/736x/4d/b5/02/4db50213599c06aec88b23fe48c05a62.jpg',NULL,'Trang ruột và sơ đồ phương pháp nguyên tử',0,3),(58,3,'https://i.pinimg.com/736x/55/4a/0d/554a0daba5abf1eff7364c4e2261245c.jpg',NULL,'Gáy sách và các góc cạnh thực tế',0,4),(59,3,'https://i.pinimg.com/736x/7c/f5/4e/7cf54e2e3818514b1525ac8435bc2598.jpg',NULL,'Góc đọc sách thẩm mỹ ban đêm (cozy night)',0,5),(60,3,'https://i.pinimg.com/736x/6e/d8/5d/6ed85d30789cc439d197db4045d28b4a.jpg',NULL,'Mặt sau sách kết hợp sổ tay ứng dụng',0,6),(61,4,'https://i.pinimg.com/736x/ee/7d/61/ee7d61ab878bada116691d3338917311.jpg',NULL,'Bìa trước sách ấn bản chuẩn',0,2),(62,4,'https://i.pinimg.com/736x/41/79/ab/4179ab7921a25b5c678dc49439890df0.jpg',NULL,'Trang ruột và các nguyên tắc giao tiếp cốt lõi',0,3),(63,4,'https://i.pinimg.com/736x/88/b2/c2/88b2c2cdb520610233ff15ef76f4b8a7.jpg',NULL,'Gáy sách và cạnh sách nhìn nghiêng',0,4),(64,4,'https://i.pinimg.com/736x/13/39/80/1339805d9f7a1aac037b2010d7ab3fe5.jpg',NULL,'Góc đọc sách thẩm mỹ trên bàn gỗ vintage',0,5),(65,4,'https://i.pinimg.com/736x/e0/f6/20/e0f62096666510b98adbd96ec12ab4af.jpg',NULL,'Mặt sau sách và tóm lược nội dung',0,6),(66,5,'https://i.pinimg.com/736x/c4/f1/27/c4f127110bf00069606aae71ad5691a8.jpg',NULL,'Bìa trước cuốn sách nổi tiếng',0,2),(67,5,'https://i.pinimg.com/736x/e7/9a/95/e79a95f8e44dccff754d5be8d201d13f.jpg',NULL,'Trang ruột và sơ đồ tư duy tài chính',0,3),(68,5,'https://i.pinimg.com/736x/c9/1c/1b/c91c1b54c0b37406ee924a3930ded3fe.jpg',NULL,'Gáy sách và các trang sách mở rộng',0,4),(69,5,'https://i.pinimg.com/736x/43/b8/79/43b87983509cf664070f3c9e79333782.jpg',NULL,'Góc đọc sách aesthetic góc bàn làm việc',0,5),(70,5,'https://i.pinimg.com/736x/f2/79/bf/f279bfb96f51baaeeb5f6227fc7c3fa9.jpg',NULL,'Mặt sau sách và thông điệp gửi độc giả',0,6),(71,16,'https://i.pinimg.com/736x/ee/34/8f/ee348f305e6e9a2696de54e0929bce21.jpg',NULL,'Ảnh bìa trước nghệ thuật',0,2),(72,16,'https://i.pinimg.com/736x/16/cb/4e/16cb4e6bba49474cd1307db5519c2b2c.jpg',NULL,'Trang ruột và ghi chú đọc thử',0,3),(73,16,'https://i.pinimg.com/736x/83/6c/b5/836cb59f1362200e4f555ffdd81ddceb.jpg',NULL,'Gáy sách và thiết kế sống sách',0,4),(74,16,'https://i.pinimg.com/736x/da/ed/5f/daed5f64e1689492746ee863d0774f16.jpg',NULL,'Góc đọc sách thẩm mỹ kèm bookmark',0,5),(75,16,'https://i.pinimg.com/736x/14/3b/62/143b62645290d15978a1451d8320715a.jpg',NULL,'Bìa sau và tóm tắt nội dung',0,6),(76,17,'https://i.pinimg.com/736x/c1/53/03/c153034bf76547efbd749d10d0b45554.jpg',NULL,'Bìa trước phiên bản bìa cứng cổ điển',0,2),(77,17,'https://i.pinimg.com/736x/67/0e/de/670edeb60d0137e48382cf6f4a0b95d3.jpg',NULL,'Trang ruột kèm phong thư đọc thử',0,3),(78,17,'https://i.pinimg.com/736x/60/d4/26/60d426da28cca40f517d14115bc937f5.jpg',NULL,'Gáy sách bộ sưu tập văn học kinh điển',0,4),(79,17,'https://i.pinimg.com/736x/d9/eb/41/d9eb41014fc1ce257a1a4c515f8249c5.jpg',NULL,'Góc đọc sách thư giãn cùng tách trà',0,5),(80,17,'https://i.pinimg.com/736x/29/62/e3/2962e38e1eb9c401fad44a2cd592a0e1.jpg',NULL,'Bìa sau và tổng thể ấn bản đặc biệt',0,6),(81,18,'https://i.pinimg.com/736x/a4/7f/2f/a47f2f29b317072785cc2f9e70e37316.jpg',NULL,'Bìa trước minh họa nghệ thuật Gothic',0,2),(82,18,'https://i.pinimg.com/736x/89/f5/42/89f542b7513f2e033fc23f0d33f8849b.jpg',NULL,'Trang ruột và các đoạn trích dẫn cảm xúc',0,3),(83,18,'https://i.pinimg.com/736x/72/99/9a/72999a31ccdea9a016c71e6df3bdd1db.jpg',NULL,'Gáy sách phong cách cổ điển phương Tây',0,4),(84,18,'https://i.pinimg.com/736x/61/f0/41/61f04190f51809b5690b88f8187471c0.jpg',NULL,'Góc đọc sách aesthetic mùa thu hoài niệm',0,5),(85,18,'https://i.pinimg.com/736x/0a/d6/40/0ad6403837a403dfabb2c466299066c2.jpg',NULL,'Mặt sau và ấn bản minh họa đặc biệt',0,6),(86,19,'https://i.pinimg.com/736x/22/be/2f/22be2fd357e7763d2485e8053e7193b7.jpg',NULL,'Bìa trước sách ấn bản bán chạy số 1',0,2),(87,19,'https://i.pinimg.com/736x/23/02/c9/2302c95b24326b005135ddb8006ec6bb.jpg',NULL,'Trang ruột và quy tắc tâm lý thực hành',0,3),(88,19,'https://i.pinimg.com/736x/b3/d7/95/b3d7953eece2108109a9bd6a19116db2.jpg',NULL,'Gáy sách và dáng sách bìa cứng hiện đại',0,4),(89,19,'https://i.pinimg.com/736x/17/fc/60/17fc60e44d84945216d7aca1fe76206e.jpg',NULL,'Góc đọc sách truyền cảm hứng sống tích cực',0,5),(90,19,'https://i.pinimg.com/736x/5f/7d/a0/5f7da0f34ccd94abbc74a580944563e9.jpg',NULL,'Bìa sau và tóm lược thông điệp cốt lõi',0,6),(91,20,'https://i.pinimg.com/736x/20/80/7a/20807a9a3999fd9f4e672e4a8c86e97d.jpg',NULL,'Bìa trước biểu tượng chiếc đầm xanh lộng lẫy',0,2),(92,20,'https://i.pinimg.com/736x/96/d1/74/96d1746eb0fd1363e7d1ff4566ea5048.jpg',NULL,'Trang ruột và ghi chép đọc thử giàu cảm xúc',0,3),(93,20,'https://i.pinimg.com/736x/3d/1c/57/3d1c575ea3b61698e75beff2cbff52a9.jpg',NULL,'Gáy sách và cạnh sách mạ màu bản giới hạn',0,4),(94,20,'https://i.pinimg.com/736x/c5/07/9a/c5079ac91b6af5f4c28b5f927b488865.jpg',NULL,'Góc đọc sách phong cách vintage quyến rũ',0,5),(95,20,'https://i.pinimg.com/736x/bf/97/51/bf97513416d0964e3420e3321895660a.jpg',NULL,'Toàn cảnh bìa sau và sống sách trải rộng',0,6),(96,11,'https://i.pinimg.com/736x/16/15/8b/16158bf2b669ea4ae0d64178c10447a6.jpg',NULL,'Bìa trước minh họa biểu tượng Big Brother kinh điển',0,2),(97,11,'https://i.pinimg.com/736x/e8/2f/19/e82f19430e14807233d5353a561b3095.jpg',NULL,'Trang ruột trích dẫn thông điệp tác phẩm',0,3),(98,11,'https://i.pinimg.com/736x/bc/5c/8d/bc5c8deb1425ca7aa79a5061f4b4c4c6.jpg',NULL,'Gáy sách thiết kế nghệ thuật cổ điển',0,4),(99,11,'https://i.pinimg.com/736x/5a/77/28/5a77286b69840e036f3c7ffabb0873ff.jpg',NULL,'Góc đọc sách thẩm mỹ cozy không gian chill',0,5),(100,11,'https://i.pinimg.com/736x/2d/53/3d/2d533db269bd4b94201f3955fb36e0d8.jpg',NULL,'Mặt sau và toàn cảnh ấn bản Penguin cổ điển',0,6),(101,12,'https://i.pinimg.com/736x/30/0e/4e/300e4e0aa0e7f524d4e01bd72612c7fe.jpg',NULL,'Bìa trước minh họa châm biếm kinh điển',0,2),(102,12,'https://i.pinimg.com/736x/07/30/6f/07306f8bab929c5775d67180ea612be7.jpg',NULL,'Trang ruột trích dẫn 7 điều răn của nông trại',0,3),(103,12,'https://i.pinimg.com/736x/fc/e1/23/fce123a45c8978b52f4cd1c64e82a37d.jpg',NULL,'Gáy sách và góc nghiêng cuốn tiểu thuyết cổ điển',0,4),(104,12,'https://i.pinimg.com/736x/20/07/bf/2007bfc120bfe692c448ef4bf62a279d.jpg',NULL,'Góc đọc sách thư thái bên bàn cafe',0,5),(105,12,'https://i.pinimg.com/736x/0a/0b/80/0a0b80d9d3c7b85b2e3692b60b01ff97.jpg',NULL,'Mặt sau và toàn cảnh ấn phẩm đặc biệt',0,6),(106,13,'https://i.pinimg.com/736x/0d/de/cd/0ddecd77668d911913e6d430ab33b4a3.jpg',NULL,'Bìa trước nghệ thuật phong cách Art Deco kinh điển',0,2),(107,13,'https://i.pinimg.com/736x/f8/dc/38/f8dc38b182bb8dde2ffd7197109e70c4.jpg',NULL,'Trang ruột trích dẫn ánh sáng xanh và giấc mơ Gatsby',0,3),(108,13,'https://i.pinimg.com/736x/b2/4d/37/b24d373c15a90b7efd803e6e75a9f3d5.jpg',NULL,'Gáy sách ấn bản Vintage Penguin cổ điển',0,4),(109,13,'https://i.pinimg.com/736x/29/59/d9/2959d925117cced3b72a549c46cbf08e.jpg',NULL,'Góc đọc sách thẩm mỹ bên tách cà phê',0,5),(110,13,'https://i.pinimg.com/736x/a4/8f/5a/a48f5adba3fe999440c5fbf8fe4f8b65.jpg',NULL,'Mặt sau và toàn cảnh ấn phẩm hoài niệm',0,6),(111,14,'https://i.pinimg.com/736x/bd/9b/ac/bd9bacf0a92bce2d7975b399b26e3291.jpg',NULL,'Bìa trước minh họa người lính phóng hỏa và ngọn lửa',0,2),(112,14,'https://i.pinimg.com/736x/54/b0/d9/54b0d9f520721e6d192de8e7af22d40d.jpg',NULL,'Trang ruột mở trang 134 đoạn trích triết lý sâu sắc',0,3),(113,14,'https://i.pinimg.com/736x/fa/09/18/fa09189f9df0479bd7720ac1210182fb.jpg',NULL,'Gáy sách thiết kế dải quẹt diêm độc đáo',0,4),(114,14,'https://i.pinimg.com/736x/a5/23/d4/a523d41e832af7c5f152d9c0d64e5ef5.jpg',NULL,'Góc đọc sách thư thái và không gian chiêm nghiệm',0,5),(115,14,'https://i.pinimg.com/736x/47/0f/9d/470f9d2fd83a58c6c7722e6758792c99.jpg',NULL,'Mặt sau và ấn bản kỷ niệm kinh điển bìa cứng',0,6),(116,15,'https://i.pinimg.com/736x/ec/a3/0a/eca30aa1983656204e662d55a2b6354d.jpg',NULL,'Bìa trước minh họa nghệ thuật Ove và chú mèo',0,2),(117,15,'https://i.pinimg.com/736x/bb/dc/4b/bbdc4b398a90e73b97ebd17837637258.jpg',NULL,'Trang ruột trích dẫn câu chuyện cảm động về tình yêu',0,3),(118,15,'https://i.pinimg.com/736x/c2/38/80/c238804c07eb1d64469dd689a123d3a1.jpg',NULL,'Gáy sách và góc nhìn nghiêng ấn bản bỏ túi',0,4),(119,15,'https://i.pinimg.com/736x/03/3d/ad/033dadee55ec340e1bcbd40a5590d9f3.jpg',NULL,'Góc đọc sách ấm cúng ấn bản tiếng Việt Nhã Nam',0,5),(120,15,'https://i.pinimg.com/736x/4d/62/e0/4d62e027fc59483714add24a625052f0.jpg',NULL,'Mặt sau và toàn cảnh cuốn sách ấn bản quốc tế',0,6),(171,100,'/images/books/book_100.jpg',NULL,NULL,1,1),(172,101,'/images/books/book_101.jpg',NULL,NULL,1,1),(173,102,'/images/books/book_102.jpg',NULL,NULL,1,1),(174,103,'/images/books/book_103.jpg',NULL,NULL,1,1),(175,104,'/images/books/book_104.jpg',NULL,NULL,1,1),(176,105,'/images/books/book_105.jpg',NULL,NULL,1,1),(177,106,'/images/books/book_106.jpg',NULL,NULL,1,1),(178,107,'/images/books/book_107.jpg',NULL,NULL,1,1),(179,108,'/images/books/book_108.jpg',NULL,NULL,1,1),(180,109,'/images/books/book_109.jpg',NULL,NULL,1,1),(181,110,'/images/books/book_110.jpg',NULL,NULL,1,1),(182,111,'/images/books/book_111.jpg',NULL,NULL,1,1),(183,112,'/images/books/book_112.jpg',NULL,NULL,1,1),(184,113,'/images/books/book_113.jpg',NULL,NULL,1,1),(185,114,'/images/books/book_114.jpg',NULL,NULL,1,1),(186,115,'/images/books/book_115.jpg',NULL,NULL,1,1),(187,116,'/images/books/book_116.jpg',NULL,NULL,1,1),(188,117,'/images/books/book_117.jpg',NULL,NULL,1,1),(189,118,'/images/books/book_118.jpg',NULL,NULL,1,1),(190,119,'/images/books/book_119.jpg',NULL,NULL,1,1),(191,120,'/images/books/book_120.jpg',NULL,NULL,1,1),(192,121,'/images/books/book_121.jpg',NULL,NULL,1,1),(193,122,'/images/books/book_122.jpg',NULL,NULL,1,1),(194,123,'/images/books/book_123.jpg',NULL,NULL,1,1),(195,124,'/images/books/book_124.jpg',NULL,NULL,1,1),(196,125,'/images/books/book_125.jpg',NULL,NULL,1,1),(197,126,'/images/books/book_126.jpg',NULL,NULL,1,1),(198,127,'/images/books/book_127.jpg',NULL,NULL,1,1),(199,128,'/images/books/book_128.jpg',NULL,NULL,1,1),(200,129,'/images/books/book_129.jpg',NULL,NULL,1,1),(201,130,'/images/books/book_130.jpg',NULL,NULL,1,1),(202,131,'/images/books/book_131.jpg',NULL,NULL,1,1),(203,132,'/images/books/book_132.jpg',NULL,NULL,1,1),(204,133,'/images/books/book_133.jpg',NULL,NULL,1,1),(205,134,'/images/books/book_134.jpg',NULL,NULL,1,1),(206,135,'/images/books/book_135.jpg',NULL,NULL,1,1),(207,136,'/images/books/book_136.jpg',NULL,NULL,1,1),(208,137,'/images/books/book_137.jpg',NULL,NULL,1,1),(209,138,'/images/books/book_138.jpg',NULL,NULL,1,1),(210,139,'/images/books/book_139.jpg',NULL,NULL,1,1),(211,140,'/images/books/book_140.jpg',NULL,NULL,1,1),(212,141,'/images/books/book_141.jpg',NULL,NULL,1,1),(213,142,'/images/books/book_142.jpg',NULL,NULL,1,1),(214,143,'/images/books/book_143.jpg',NULL,NULL,1,1),(215,144,'/images/books/book_144.jpg',NULL,NULL,1,1),(216,145,'/images/books/book_145.jpg',NULL,NULL,1,1),(217,146,'/images/books/book_146.jpg',NULL,NULL,1,1),(218,147,'/images/books/book_147.jpg',NULL,NULL,1,1),(219,148,'/images/books/book_148.jpg',NULL,NULL,1,1),(220,149,'/images/books/book_149.jpg',NULL,NULL,1,1);
/*!40000 ALTER TABLE `book_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(280) COLLATE utf8mb4_unicode_ci NOT NULL,
  `author` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `publisher` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `publish_year` int DEFAULT NULL,
  `isbn` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `condition_percent` int NOT NULL DEFAULT '90',
  `condition_notes` text COLLATE utf8mb4_unicode_ci,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `original_price` decimal(14,2) DEFAULT '0.00',
  `price` decimal(14,2) NOT NULL,
  `discount_price` decimal(14,2) DEFAULT '0.00',
  `total_sold` int DEFAULT '0',
  `views_count` int DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `is_featured` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `fk_books_categories` (`category_id`),
  CONSTRAINT `fk_books_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES (1,1,'Yêu Những Điều Không Hoàn Hảo','yeu-nhung-dieu-khong-hoan-hao','Hae Min','NXB Thế Giới',2018,'9786047748123',95,'Bản in nguyên vẹn 95%, ruột sạch đẹp không ghi chú, đã niêm phong màng co bảo quản.','Tác phẩm tản văn chữa lành tâm hồn sâu sắc của Đại đức Hae Min, giúp người đọc bao dung và thấu hiểu chính mình giữa dòng đời vội vã.',120000.00,68000.00,65000.00,76,1420,1,1,'2026-09-25 07:30:53','2026-10-29 08:52:19'),(2,1,'Nhà Giả Kim (The Alchemist)','nha-gia-kim-paulo-coelho','Paulo Coelho','NXB Nhã Nam',2015,'9780062315007',90,'Sách nguyên vẹn, gáy chắc, mép sách hơi ố vàng nhẹ theo thời gian, không thiếu trang.','Kiệt tác vượt thời gian về hành trình theo đuổi vận mệnh của chàng chăn cừu Santiago qua sa mạc.',79000.00,45000.00,42000.00,38,1117,1,1,'2026-09-25 07:30:53','2026-10-29 08:42:05'),(3,3,'Thay Đổi Tí Hon (Atomic Habits)','thay-doi-ti-hon-atomic-habits','James Clear','NXB Thế Giới',2019,'9780735211292',98,'Sách mới 98%, không nếp gấp, hệ thống phương pháp xây dựng thói quen tốt vượt trội.','Cẩm nang hướng dẫn từng bước thay đổi những thói quen nhỏ để đạt được kết quả phi thường.',189000.00,95000.00,90000.00,32,1110,1,1,'2026-09-25 07:30:53','2026-10-12 07:19:00'),(4,3,'Đắc Nhân Tâm (How to Win Friends)','dac-nhan-tam-dale-carnegie','Dale Carnegie','First News - NXB Tổng Hợp',2016,'9780671027032',85,'Bìa cứng sang trọng, gáy vững chắc, ruột sách sạch sẽ, cẩm nang giao tiếp kinh điển.','Cẩm nang giao tiếp, đối nhân xử thế kinh điển có ảnh hưởng lớn nhất mọi thời đại.',86000.00,40000.00,38000.00,22,763,1,1,'2026-09-25 07:30:53','2026-10-05 07:55:36'),(5,3,'Dạy Con Làm Giàu (Rich Dad Poor Dad)','day-con-lam-giau-robert-kiyosaki','Robert T. Kiyosaki','NXB Trẻ',2012,'9781612680194',88,'Sách đọc tốt, các bài học tư duy phân biệt tài sản và tiêu sản còn nguyên giá trị.','Bộ sách giáo dục tài chính cá nhân bán chạy nhất hành tinh, thay đổi tư duy làm giàu.',95000.00,48000.00,45000.00,33,794,1,1,'2026-09-25 07:30:53','2026-10-02 01:09:43'),(6,3,'Tư Duy Nhanh Và Chậm','tu-duy-nhanh-va-cham-kahneman','Daniel Kahneman','NXB Thế Giới',2018,'9780374533557',92,'Ấn bản đẹp, giải thưởng Nobel Kinh tế, phân tích sâu sắc hệ thống tư duy 1 và 2.','Kiệt tác tâm lý học hành vi vạch trần những sai lầm có hệ thống trong cách con người phán đoán.',220000.00,110000.00,105000.00,25,893,1,1,'2026-09-25 07:30:53','2026-10-11 15:07:08'),(7,3,'Thấu Hiểu Sang Chấn (The Body Keeps the Score)','thau-hieu-sang-chan','Bessel van der Kolk','NXB Lao Động',2021,'9780143127741',95,'Sách như mới 95%, chuyên khảo y khoa tâm lý sâu sắc về chữa lành sang chấn tâm lý.','Công trình khoa học đột phá về cách sang chấn định hình lại não bộ và phương pháp chữa lành.',250000.00,125000.00,120000.00,28,944,1,1,'2026-09-25 07:30:53','2026-10-11 14:18:28'),(8,1,'Những Thứ Ba Với Thầy Morrie','nhung-thu-ba-voi-thay-morrie','Mitch Albom','NXB Trẻ',2014,'9780767905923',90,'Sách nguyên vẹn, các bài học về tình yêu, công việc, tuổi già và sự tha thứ lay động tâm can.','Hồi ký cảm động về những buổi trò chuyện thứ ba đầy minh triết giữa một học trò và người thầy sắp qua đời.',85000.00,45000.00,42000.00,24,711,1,1,'2026-09-25 07:30:53','2026-09-29 07:00:54'),(9,1,'Tội Ác Và Trừng Phạt (Crime and Punishment)','toi-ac-va-trung-phat-crime-and-punishment','Fyodor Dostoevsky','NXB Văn Học',2016,'9780679734772',85,'Tiểu thuyết kinh điển nước Nga, độ dày hơn 700 trang giữ nguyên vẹn gáy keo chỉ chắc chắn.','Đỉnh cao phân tích tâm lý tội phạm và sự cứu chuộc lương tâm con người của Dostoevsky.',165000.00,85000.00,80000.00,19,655,1,1,'2026-09-25 07:30:53','2026-10-01 07:20:57'),(10,1,'Hoàng Tử Bé (The Little Prince)','hoang-tu-be-saint-exupery','Antoine de Saint-Exupéry','NXB Kim Đồng',2019,'9780140283334',95,'Ấn bản minh họa màu đẹp mắt của NXB Kim Đồng, gáy vuông vắn, không nhăn mép.','Tác phẩm văn học kỳ diệu về tình bạn, bông hồng duy nhất và cái nhìn thuần khiết của trẻ thơ.',75000.00,38000.00,35000.00,31,922,1,1,'2026-09-25 07:30:53','2026-09-29 07:00:54'),(11,1,'Một Chín Tám Bốn (1984)','mot-chin-tam-bon-1984-orwell','George Orwell','NXB Nhã Nam',2017,'9780451524935',90,'Kiệt tác phản địa đàng thế kỷ 20, sách giữ gìn cẩn thận, ruột giấy vàng cổ điển.','Bức tranh tiên tri rùng mình về một xã hội bị giám sát tuyệt đối và sự tha hóa của quyền lực.',110000.00,55000.00,50000.00,27,885,1,1,'2026-09-25 07:30:53','2026-10-06 14:30:18'),(12,1,'Trại Súc Vật (Animal Farm)','trai-suc-vat-animal-farm','George Orwell','NXB Nhã Nam',2016,'9780451526342',92,'Sách nhỏ gọn, gáy phẳng phiu, truyện ngụ ngôn chính trị sắc sảo bậc nhất.','Truyện ngụ ngôn trào phúng kinh điển về cuộc nổi dậy của muông thú và sự biến chất của lý tưởng.',68000.00,35000.00,32000.00,25,782,1,1,'2026-09-25 07:30:53','2026-10-28 02:43:24'),(13,1,'Gatsby Vĩ Đại (The Great Gatsby)','gatsby-vi-dai-fitzgerald','F. Scott Fitzgerald','NXB Văn Học',2018,'9780743273565',88,'Bản dịch kinh điển, bìa đẹp, trang sách sạch không vết bẩn, khắc họa thời kỳ Jazz lộng lẫy.','Bi kịch tình yêu và giấc mơ Mỹ tan vỡ của triệu phú bí ẩn Jay Gatsby bên bờ vịnh Long Island.',95000.00,48000.00,45000.00,21,671,1,1,'2026-09-25 07:30:53','2026-09-29 07:00:55'),(14,1,'451 Độ F (Fahrenheit 451)','451-do-f-fahrenheit-451','Ray Bradbury','NXB Nhã Nam',2017,'9781451673319',90,'Sách nguyên vẹn, câu chuyện về chàng lính cứu hỏa Montag đốt sách và sự thức tỉnh.','Tác phẩm viễn tưởng kinh điển cảnh báo về một thế giới mà tri thức và sách vở bị cấm đoán.',105000.00,52000.00,48000.00,23,742,1,1,'2026-09-25 07:30:53','2026-10-02 06:01:02'),(15,1,'Người Đàn Ông Mang Tên Ove','nguoi-dan-ong-mang-ten-ove','Fredrik Backman','NXB Trẻ',2018,'9781476738024',95,'Sách gần như mới 95%, câu chuyện ấm áp về sự tử tế và tình người trong xóm nhỏ.','Tiểu thuyết cảm động hài hước về một ông lão khó tính nhưng mang trái tim ấm áp nhất thế giới.',135000.00,68000.00,65000.00,89,1272,1,1,'2026-09-25 07:30:53','2026-10-29 07:22:00'),(16,1,'Chúa Ruồi (Lord of the Flies)','chua-ruoi-william-golding','William Golding','NXB Hội Nhà Văn',2015,'9780425274866',88,'Bản in tốt, tác phẩm đoạt giải Nobel văn học, giải mã bản tính con người khi cô lập.','Câu chuyện rùng mình về một nhóm học sinh mắc kẹt trên đảo hoang và sự thoái hóa nhân tính.',110000.00,55000.00,50000.00,17,591,1,1,'2026-09-25 07:30:53','2026-09-29 07:00:55'),(17,1,'Kiêu Hãnh Và Định Kiến','kieu-hanh-va-dinh-kien-jane-austen','Jane Austen','NXB Văn Học',2019,'9780141439518',90,'Bìa hoa văn cổ điển nước Anh, gáy sách chắc chắn, thiên tình sử Elizabeth và Darcy.','Kiệt tác lãng mạn bất hủ về tình yêu vượt qua định kiến xã hội và tự ái cá nhân của Jane Austen.',120000.00,60000.00,58000.00,25,686,1,1,'2026-09-25 07:30:53','2026-10-01 07:23:16'),(18,1,'Đồi Gió Hú (Wuthering Heights)','doi-gio-hu-emily-bronte','Emily Brontë','NXB Văn Học',2016,'9780141439600',85,'Bản in giữ gìn tốt, thiên tiểu thuyết tình yêu và hận thù dữ dội trên đồng hoang Yorkshire.','Bi tình sử cuồng nhiệt và ám ảnh nhất lịch sử văn học giữa Heathcliff và Catherine Earnshaw.',115000.00,58000.00,55000.00,17,532,1,1,'2026-09-25 07:30:53','2026-10-02 05:47:11'),(19,3,'Học Cách Mặc Kệ (The Let Them Theory)','hoc-cach-mac-ke-mel-robbins','Mel Robbins','NXB Dân Trí',2024,'9781401971366',98,'Sách gần như mới 98%, cuốn sách tâm lý thực hành tạo cơn sốt toàn cầu của Mel Robbins.','Phương pháp giải phóng năng lượng tinh thần bằng cách buông bỏ nhu cầu kiểm soát người khác.',195000.00,98000.00,95000.00,38,1324,1,1,'2026-09-25 07:30:53','2026-10-29 08:49:41'),(20,1,'Bảy Người Chồng Của Evelyn Hugo','bay-nguoi-chong-cua-evelyn-hugo','Taylor Jenkins Reid','NXB Trẻ',2022,'9781501161933',95,'Sách mới 95%, bản in bìa áo xanh ngọc tuyệt đẹp, tác phẩm bán chạy hàng đầu thời gian qua.','Hồi ký rực rỡ và bi thương của huyền thoại điện ảnh Hollywood Evelyn Hugo và bí mật cuộc đời bà.',168000.00,85000.00,80000.00,30,1023,1,1,'2026-09-25 07:30:53','2026-10-01 07:20:34'),(100,2,'Đắc Nhân Tâm','c-nhn-tm-1445','Dale Carnegie','NXB Trẻ',2018,'ISBN100',85,'Sách cũ chất lượng tốt','Cuốn sách Đắc Nhân Tâm của tác giả Dale Carnegie là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',119600.00,92000.00,0.00,32,35,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(101,4,'Nhà Giả Kim','nh-gi-kim-4518','Paulo Coelho','NXB Trẻ',2018,'ISBN101',80,'Sách cũ chất lượng tốt','Cuốn sách Nhà Giả Kim của tác giả Paulo Coelho là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',159900.00,123000.00,0.00,36,40,1,1,'2026-10-02 05:56:40','2026-10-29 05:55:35'),(102,3,'Tuổi Trẻ Đáng Giá Bao Nhiêu','tui-tr-ng-gi-bao-nhiu-2207','Rosie Nguyễn','NXB Trẻ',2018,'ISBN102',95,'Sách cũ chất lượng tốt','Cuốn sách Tuổi Trẻ Đáng Giá Bao Nhiêu của tác giả Rosie Nguyễn là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',40300.00,31000.00,0.00,33,61,1,1,'2026-10-02 05:56:40','2026-10-06 12:54:42'),(103,2,'Cho Tôi Xin Một Vé Đi Tuổi Thơ','cho-ti-xin-mt-v-i-tui-th-7872','Nguyễn Nhật Ánh','NXB Trẻ',2018,'ISBN103',99,'Sách cũ chất lượng tốt','Cuốn sách Cho Tôi Xin Một Vé Đi Tuổi Thơ của tác giả Nguyễn Nhật Ánh là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',159900.00,123000.00,0.00,8,64,1,1,'2026-10-02 05:56:40','2026-10-05 08:05:19'),(104,3,'Mắt Biếc','mt-bic-4756','Nguyễn Nhật Ánh','NXB Trẻ',2018,'ISBN104',80,'Sách cũ chất lượng tốt','Cuốn sách Mắt Biếc của tác giả Nguyễn Nhật Ánh là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',41600.00,32000.00,0.00,107,70,1,1,'2026-10-02 05:56:40','2026-10-29 07:41:12'),(105,3,'Bắt Trẻ Đồng Xanh','bt-tr-ng-xanh-3157','J.D. Salinger','NXB Trẻ',2018,'ISBN105',90,'Sách cũ chất lượng tốt','Cuốn sách Bắt Trẻ Đồng Xanh của tác giả J.D. Salinger là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',75400.00,58000.00,0.00,49,81,1,1,'2026-10-02 05:56:40','2026-10-29 03:14:24'),(106,1,'Tôi Thấy Hoa Vàng Trên Cỏ Xanh','ti-thy-hoa-vng-trn-c-xanh-3533','Nguyễn Nhật Ánh','NXB Trẻ',2018,'ISBN106',80,'Sách cũ chất lượng tốt','Cuốn sách Tôi Thấy Hoa Vàng Trên Cỏ Xanh của tác giả Nguyễn Nhật Ánh là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',153400.00,118000.00,0.00,40,50,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(107,4,'Ông Già Và Biển Cả','ng-gi-v-bin-c-7081','Ernest Hemingway','NXB Trẻ',2018,'ISBN107',99,'Sách cũ chất lượng tốt','Cuốn sách Ông Già Và Biển Cả của tác giả Ernest Hemingway là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',45500.00,35000.00,0.00,19,17,1,1,'2026-10-02 05:56:40','2026-10-05 05:24:17'),(108,3,'Suối Nguồn','sui-ngun-3551','Ayn Rand','NXB Trẻ',2018,'ISBN108',80,'Sách cũ chất lượng tốt','Cuốn sách Suối Nguồn của tác giả Ayn Rand là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',192400.00,148000.00,0.00,24,11,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(109,4,'Sapiens: Lược Sử Loài Người','sapiens-lc-s-loi-ngi-9960','Yuval Noah Harari','NXB Trẻ',2018,'ISBN109',95,'Sách cũ chất lượng tốt','Cuốn sách Sapiens: Lược Sử Loài Người của tác giả Yuval Noah Harari là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',59800.00,46000.00,0.00,35,16,1,1,'2026-10-02 05:56:40','2026-10-05 05:24:30'),(110,1,'Muôn Kiếp Nhân Sinh','mun-kip-nhn-sinh-7736','Nguyên Phong','NXB Trẻ',2018,'ISBN110',99,'Sách cũ chất lượng tốt','Cuốn sách Muôn Kiếp Nhân Sinh của tác giả Nguyên Phong là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',185900.00,143000.00,0.00,11,73,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(111,4,'Thiết Kế Cuộc Đời Thịnh Vượng','thit-k-cuc-i-thnh-vng-4029','Thái Phạm','NXB Trẻ',2018,'ISBN111',85,'Sách cũ chất lượng tốt','Cuốn sách Thiết Kế Cuộc Đời Thịnh Vượng của tác giả Thái Phạm là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',40300.00,31000.00,0.00,26,85,1,1,'2026-10-02 05:56:40','2026-10-13 12:02:24'),(112,1,'Think And Grow Rich','think-and-grow-rich-3203','Napoleon Hill','NXB Trẻ',2018,'ISBN112',80,'Sách cũ chất lượng tốt','Cuốn sách Think And Grow Rich của tác giả Napoleon Hill là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',195000.00,150000.00,0.00,9,13,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(113,3,'Dạy Con Làm Giàu (Tập 1)','dy-con-lm-giu-tp-1-3371','Robert T. Kiyosaki','NXB Trẻ',2018,'ISBN113',85,'Sách cũ chất lượng tốt','Cuốn sách Dạy Con Làm Giàu (Tập 1) của tác giả Robert T. Kiyosaki là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',154700.00,119000.00,0.00,36,69,1,1,'2026-10-02 05:56:40','2026-10-02 07:29:09'),(114,1,'Sức Mạnh Của Thói Quen','sc-mnh-ca-thi-quen-4841','Charles Duhigg','NXB Trẻ',2018,'ISBN114',95,'Sách cũ chất lượng tốt','Cuốn sách Sức Mạnh Của Thói Quen của tác giả Charles Duhigg là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',118300.00,91000.00,0.00,8,96,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(115,3,'Người Giàu Có Nhất Thành Babylon','ngi-giu-c-nht-thnh-babylon-9803','George S. Clason','NXB Trẻ',2018,'ISBN115',90,'Sách cũ chất lượng tốt','Cuốn sách Người Giàu Có Nhất Thành Babylon của tác giả George S. Clason là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',162500.00,125000.00,0.00,33,34,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(116,4,'Đọc Vị Bất Kỳ Ai','c-v-bt-k-ai-2544','David J. Lieberman','NXB Trẻ',2018,'ISBN116',95,'Sách cũ chất lượng tốt','Cuốn sách Đọc Vị Bất Kỳ Ai của tác giả David J. Lieberman là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',140400.00,108000.00,0.00,54,147,1,1,'2026-10-02 05:56:40','2026-10-29 06:07:47'),(117,2,'Tâm Lý Học Tội Phạm','tm-l-hc-ti-phm-4265','Stanton E. Samenow','NXB Trẻ',2018,'ISBN117',85,'Sách cũ chất lượng tốt','Cuốn sách Tâm Lý Học Tội Phạm của tác giả Stanton E. Samenow là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',133900.00,103000.00,0.00,19,65,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(118,1,'Tâm Lý Học Đám Đông','tm-l-hc-m-ng-3360','Gustave Le Bon','NXB Trẻ',2018,'ISBN118',85,'Sách cũ chất lượng tốt','Cuốn sách Tâm Lý Học Đám Đông của tác giả Gustave Le Bon là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',193700.00,149000.00,0.00,32,82,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(119,3,'Nghệ Thuật Tinh Tế Của Việc Đếch Quan Tâm','ngh-thut-tinh-t-ca-vic-ch-quan-tm-4164','Mark Manson','NXB Trẻ',2018,'ISBN119',85,'Sách cũ chất lượng tốt','Cuốn sách Nghệ Thuật Tinh Tế Của Việc Đếch Quan Tâm của tác giả Mark Manson là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',122200.00,94000.00,0.00,13,83,1,1,'2026-10-02 05:56:40','2026-10-02 06:25:14'),(120,3,'Mình Là Cá, Việc Của Mình Là Bơi','mnh-l-c-vic-ca-mnh-l-bi-7714','Takeshi Furukawa','NXB Trẻ',2018,'ISBN120',95,'Sách cũ chất lượng tốt','Cuốn sách Mình Là Cá, Việc Của Mình Là Bơi của tác giả Takeshi Furukawa là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',48100.00,37000.00,0.00,45,139,1,1,'2026-10-02 05:56:40','2026-10-29 03:10:10'),(121,2,'Bạn Đắt Giá Bao Nhiêu?','bn-t-gi-bao-nhiu-4616','Vãn Tình','NXB Trẻ',2018,'ISBN121',95,'Sách cũ chất lượng tốt','Cuốn sách Bạn Đắt Giá Bao Nhiêu? của tác giả Vãn Tình là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',123500.00,95000.00,0.00,25,87,1,1,'2026-10-02 05:56:40','2026-10-05 08:48:33'),(122,1,'Lối Sống Tối Giản Của Người Nhật','li-sng-ti-gin-ca-ngi-nht-4194','Sasaki Fumio','NXB Trẻ',2018,'ISBN122',80,'Sách cũ chất lượng tốt','Cuốn sách Lối Sống Tối Giản Của Người Nhật của tác giả Sasaki Fumio là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',130000.00,100000.00,0.00,37,94,1,1,'2026-10-02 05:56:40','2026-10-06 02:24:57'),(123,2,'Sự Im Lặng Của Bầy Cừu','s-im-lng-ca-by-cu-1819','Thomas Harris','NXB Trẻ',2018,'ISBN123',90,'Sách cũ chất lượng tốt','Cuốn sách Sự Im Lặng Của Bầy Cừu của tác giả Thomas Harris là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',91000.00,70000.00,0.00,42,54,1,1,'2026-10-02 05:56:40','2026-10-12 11:05:38'),(124,3,'Hai Số Phận','hai-s-phn-1930','Jeffrey Archer','NXB Trẻ',2018,'ISBN124',90,'Sách cũ chất lượng tốt','Cuốn sách Hai Số Phận của tác giả Jeffrey Archer là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',80600.00,62000.00,0.00,22,93,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(125,3,'Bố Già','b-gi-6505','Mario Puzo','NXB Trẻ',2018,'ISBN125',90,'Sách cũ chất lượng tốt','Cuốn sách Bố Già của tác giả Mario Puzo là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',187200.00,144000.00,0.00,47,113,1,1,'2026-10-02 05:56:40','2026-10-05 05:41:04'),(126,1,'Bên Nhau Trọn Đời','bn-nhau-trn-i-7255','Cố Mạn','NXB Trẻ',2018,'ISBN126',85,'Sách cũ chất lượng tốt','Cuốn sách Bên Nhau Trọn Đời của tác giả Cố Mạn là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',141700.00,109000.00,0.00,12,13,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(127,2,'Thất Tịch Không Mưa','tht-tch-khng-ma-7021','Lâu Vũ Tình','NXB Trẻ',2018,'ISBN127',99,'Sách cũ chất lượng tốt','Cuốn sách Thất Tịch Không Mưa của tác giả Lâu Vũ Tình là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',115700.00,89000.00,0.00,37,28,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(128,2,'Phía Sau Nghi Can X','pha-sau-nghi-can-x-3971','Higashino Keigo','NXB Trẻ',2018,'ISBN128',90,'Sách cũ chất lượng tốt','Cuốn sách Phía Sau Nghi Can X của tác giả Higashino Keigo là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',122200.00,94000.00,0.00,25,80,1,1,'2026-10-02 05:56:40','2026-10-12 11:45:46'),(129,3,'Bạch Dạ Hành','bch-d-hnh-8380','Higashino Keigo','NXB Trẻ',2018,'ISBN129',90,'Sách cũ chất lượng tốt','Cuốn sách Bạch Dạ Hành của tác giả Higashino Keigo là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',193700.00,149000.00,0.00,35,80,1,1,'2026-10-02 05:56:40','2026-10-06 08:44:13'),(130,3,'Điều Kỳ Diệu Của Tiệm Tạp Hóa Namiya','iu-k-diu-ca-tim-tp-ha-namiya-1618','Higashino Keigo','NXB Trẻ',2018,'ISBN130',95,'Sách cũ chất lượng tốt','Cuốn sách Điều Kỳ Diệu Của Tiệm Tạp Hóa Namiya của tác giả Higashino Keigo là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',74100.00,57000.00,0.00,42,21,1,1,'2026-10-02 05:56:40','2026-10-05 08:49:47'),(131,2,'Rừng Na Uy','rng-na-uy-6442','Haruki Murakami','NXB Trẻ',2018,'ISBN131',80,'Sách cũ chất lượng tốt','Cuốn sách Rừng Na Uy của tác giả Haruki Murakami là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',137800.00,106000.00,0.00,13,64,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(132,4,'Kafka Bên Bờ Biển','kafka-bn-b-bin-2514','Haruki Murakami','NXB Trẻ',2018,'ISBN132',85,'Sách cũ chất lượng tốt','Cuốn sách Kafka Bên Bờ Biển của tác giả Haruki Murakami là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',52000.00,40000.00,0.00,37,86,1,1,'2026-10-02 05:56:40','2026-10-14 04:31:32'),(133,4,'Cuốn Theo Chiều Gió','cun-theo-chiu-gi-1094','Margaret Mitchell','NXB Trẻ',2018,'ISBN133',90,'Sách cũ chất lượng tốt','Cuốn sách Cuốn Theo Chiều Gió của tác giả Margaret Mitchell là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',107900.00,83000.00,0.00,35,19,1,1,'2026-10-02 05:56:40','2026-10-05 05:35:10'),(134,4,'Những Người Khốn Khổ','nhng-ngi-khn-kh-9857','Victor Hugo','NXB Trẻ',2018,'ISBN134',90,'Sách cũ chất lượng tốt','Cuốn sách Những Người Khốn Khổ của tác giả Victor Hugo là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',71500.00,55000.00,0.00,20,21,1,1,'2026-10-02 05:56:40','2026-10-13 12:02:30'),(135,1,'Không Gia Đình','khng-gia-nh-3961','Hector Malot','NXB Trẻ',2018,'ISBN135',80,'Sách cũ chất lượng tốt','Cuốn sách Không Gia Đình của tác giả Hector Malot là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',174200.00,134000.00,0.00,14,87,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(136,1,'Đại Gia Gatsby','i-gia-gatsby-8820','F. Scott Fitzgerald','NXB Trẻ',2018,'ISBN136',95,'Sách cũ chất lượng tốt','Cuốn sách Đại Gia Gatsby của tác giả F. Scott Fitzgerald là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',162500.00,125000.00,0.00,45,24,1,1,'2026-10-02 05:56:40','2026-10-06 13:01:28'),(137,4,'Tội Ác Và Hình Phạt','ti-c-v-hnh-pht-9041','Fyodor Dostoevsky','NXB Trẻ',2018,'ISBN137',95,'Sách cũ chất lượng tốt','Cuốn sách Tội Ác Và Hình Phạt của tác giả Fyodor Dostoevsky là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',107900.00,83000.00,0.00,19,78,1,1,'2026-10-02 05:56:40','2026-10-02 06:17:21'),(138,3,'Anna Karenina','anna-karenina-4122','Leo Tolstoy','NXB Trẻ',2018,'ISBN138',95,'Sách cũ chất lượng tốt','Cuốn sách Anna Karenina của tác giả Leo Tolstoy là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',189800.00,146000.00,0.00,18,59,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(139,4,'Hai Vạn Dặm Dưới Đáy Biển','hai-vn-dm-di-y-bin-2059','Jules Verne','NXB Trẻ',2018,'ISBN139',80,'Sách cũ chất lượng tốt','Cuốn sách Hai Vạn Dặm Dưới Đáy Biển của tác giả Jules Verne là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',146900.00,113000.00,0.00,10,13,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(140,1,'Harry Potter Và Hòn Đá Phù Thủy','harry-potter-v-hn--ph-thy-1628','J.K. Rowling','NXB Trẻ',2018,'ISBN140',90,'Sách cũ chất lượng tốt','Cuốn sách Harry Potter Và Hòn Đá Phù Thủy của tác giả J.K. Rowling là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 90%, không rách trang, gáy sách nguyên vẹn.',113100.00,87000.00,0.00,30,63,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(141,4,'Hoàng Tử Bé','hong-t-b-4411','Antoine de Saint-Exupéry','NXB Trẻ',2018,'ISBN141',85,'Sách cũ chất lượng tốt','Cuốn sách Hoàng Tử Bé của tác giả Antoine de Saint-Exupéry là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',102700.00,79000.00,0.00,20,86,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(142,4,'Chuyện Con Mèo Dạy Hải Âu Bay','chuyn-con-mo-dy-hi-u-bay-1152','Luis Sepúlveda','NXB Trẻ',2018,'ISBN142',95,'Sách cũ chất lượng tốt','Cuốn sách Chuyện Con Mèo Dạy Hải Âu Bay của tác giả Luis Sepúlveda là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',106600.00,82000.00,0.00,33,86,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(143,1,'Hành Trình Về Phương Đông','hnh-trnh-v-phng-ng-5853','Baird T. Spalding','NXB Trẻ',2018,'ISBN143',90,'Sách cũ chất lượng tốt','Cuốn sách Hành Trình Về Phương Đông của tác giả Baird T. Spalding là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',84500.00,65000.00,0.00,40,44,1,1,'2026-10-02 05:56:40','2026-10-29 06:05:37'),(144,1,'Bí Mật Tư Duy Triệu Phú','b-mt-t-duy-triu-ph-2909','T. Harv Eker','NXB Trẻ',2018,'ISBN144',99,'Sách cũ chất lượng tốt','Cuốn sách Bí Mật Tư Duy Triệu Phú của tác giả T. Harv Eker là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',45500.00,35000.00,0.00,26,88,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(145,4,'Chiến Tranh Tiền Tệ','chin-tranh-tin-t-8314','Song Hongbing','NXB Trẻ',2018,'ISBN145',90,'Sách cũ chất lượng tốt','Cuốn sách Chiến Tranh Tiền Tệ của tác giả Song Hongbing là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',137800.00,106000.00,0.00,17,20,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(146,1,'Tâm Lý Học Hành Vi','tm-l-hc-hnh-vi-5117','John B. Watson','NXB Trẻ',2018,'ISBN146',85,'Sách cũ chất lượng tốt','Cuốn sách Tâm Lý Học Hành Vi của tác giả John B. Watson là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',119600.00,92000.00,0.00,14,12,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(147,3,'Sống Mòn','sng-mn-5484','Nam Cao','NXB Trẻ',2018,'ISBN147',99,'Sách cũ chất lượng tốt','Cuốn sách Sống Mòn của tác giả Nam Cao là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 95%, không rách trang, gáy sách nguyên vẹn.',63700.00,49000.00,0.00,42,46,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(148,3,'Tắt Đèn','tt-n-7554','Ngô Tất Tố','NXB Trẻ',2018,'ISBN148',85,'Sách cũ chất lượng tốt','Cuốn sách Tắt Đèn của tác giả Ngô Tất Tố là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 85%, không rách trang, gáy sách nguyên vẹn.',114400.00,88000.00,0.00,11,69,1,1,'2026-10-02 05:56:40','2026-10-02 05:56:40'),(149,4,'Số Đỏ','s-1402','Vũ Trọng Phụng','NXB Trẻ',2018,'ISBN149',85,'Sách cũ chất lượng tốt','Cuốn sách Số Đỏ của tác giả Vũ Trọng Phụng là một kiệt tác đáng đọc, thuộc hàng kinh điển trong nền văn học. Sách cũ độ mới 80%, không rách trang, gáy sách nguyên vẹn.',85800.00,66000.00,0.00,38,73,1,1,'2026-10-02 05:56:40','2026-10-29 05:55:39');
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_cart_items_carts` (`cart_id`),
  KEY `fk_cart_items_books` (`book_id`),
  KEY `fk_cart_items_stores` (`store_id`),
  CONSTRAINT `fk_cart_items_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_items_carts` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_items_stores` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (15,8,105,1,1,'2026-10-28 17:30:36'),(22,2,116,1,1,'2026-10-29 06:12:30'),(24,1,1,1,2,'2026-10-29 08:45:46');
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_carts_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,4,'2026-09-25 07:31:17'),(2,1,'2026-09-29 03:36:37'),(4,2,'2026-10-05 04:52:18'),(5,6,'2026-10-05 08:18:30'),(6,11,'2026-10-13 14:57:26'),(7,5,'2026-10-15 09:29:21'),(8,8,'2026-10-28 17:12:25'),(9,24,'2026-10-29 08:58:30');
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int DEFAULT NULL,
  `category_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `fk_categories_parent` (`parent_id`),
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,NULL,'Văn Học & Tiểu Thuyết','van-hoc-tieu-thuyet','Các tác phẩm văn học kinh điển Việt Nam và thế giới','fa-book-open',1),(2,NULL,'Công Nghệ Thông Tin & Kỹ Thuật','cntt-ky-thuat','Sách giáo trình, lập trình viên, mạng máy tính','fa-laptop-code',1),(3,NULL,'Kinh Tế & Quản Trị','kinh-te-quan-tri','Sách kinh doanh, khởi nghiệp, tài chính, đầu tư','fa-chart-line',1),(4,NULL,'Lịch Sử & Văn Hóa','lich-su-van-hoa','Sách nghiên cứu sử liệu, văn hóa cổ truyền','fa-landmark',1),(5,NULL,'Ngoại Ngữ & Từ Điển','ngoai-ngu-tu-dien','Giáo trình tiếng Anh, TOEIC, từ điển cổ xưa','fa-language',1),(6,NULL,'Kỹ Năng Sống & Tâm Lý','ky-nang-tam-ly','Sách phát triển bản thân, tâm lý học và tư duy','fa-lightbulb',1),(7,NULL,'Giáo Trình ĐH & HCMUTE','giao-trinh-hcmute','Giáo trình kỹ thuật, công nghệ và bài giảng chuyên ngành','fa-graduation-cap',1),(8,NULL,'Truyện Tranh & Manga Cổ','truyen-tranh-manga','Bộ truyện tranh gắn liền tuổi thơ và ấn bản sưu tầm','fa-palette',1);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `book_id` bigint DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sender_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CUSTOMER',
  `sender_name` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `needs_attention` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_pinned` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_chat_user` (`user_id`),
  KEY `fk_chat_store` (`store_id`),
  KEY `fk_chat_book` (`book_id`),
  CONSTRAINT `fk_chat_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_chat_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=282 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` VALUES (34,4,4,NULL,'chào shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:49',0,0),(35,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:49',0,0),(36,4,4,NULL,'hú',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:52',0,0),(37,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:52',0,0),(38,4,4,NULL,'shdflàd',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:53',0,0),(39,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:53',0,0),(40,4,4,NULL,'ad',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:53',0,0),(41,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:53',0,0),(42,4,4,NULL,'ád',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:53',0,0),(43,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:53',0,0),(44,4,4,NULL,'fa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:54',0,0),(45,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:54',0,0),(46,4,4,NULL,'df',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:54',0,0),(47,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:54',0,0),(48,4,4,NULL,'ádf',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:54',0,0),(49,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:54',0,0),(50,4,4,NULL,'ád',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:54',0,0),(51,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:54',0,0),(52,4,4,NULL,'fa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:54',0,0),(53,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:54',0,0),(54,4,4,NULL,'sdf',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:54',0,0),(55,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:54',0,0),(56,4,4,NULL,'ád',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:55',0,0),(57,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:55',0,0),(58,4,4,NULL,'fá',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:55',0,0),(59,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:55',0,0),(60,4,4,NULL,'dfa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:55',0,0),(61,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:55',0,0),(62,4,4,NULL,'s',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:55',0,0),(63,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:55',0,0),(64,4,4,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:10:59',0,0),(65,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:10:59',0,0),(66,4,4,NULL,'sà',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:00',0,0),(67,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:00',0,0),(68,4,4,NULL,'sdf',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:01',0,0),(69,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:01',0,0),(70,4,4,NULL,'a',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:01',0,0),(71,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:01',0,0),(72,4,4,NULL,'sfa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:01',0,0),(73,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:01',0,0),(74,4,4,NULL,'dsfa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:01',0,0),(75,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:01',0,0),(76,4,4,NULL,'sd',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:01',0,0),(77,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:01',0,0),(78,4,4,NULL,'fá',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:02',0,0),(79,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:02',0,0),(80,4,4,NULL,'df',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-07 12:11:02',0,0),(81,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-07 12:11:02',0,0),(82,4,4,NULL,'chòa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-08 02:10:43',0,0),(83,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-08 02:10:43',0,0),(84,4,1,NULL,'[Video thực tế]','/uploads/chat/chat_vid_1791468018004_8f00eea8.mp4','VIDEO','CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-08 14:00:22',0,0),(85,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-08 14:00:22',0,0),(86,4,1,NULL,'[Video thực tế]','/uploads/chat/chat_vid_1791468203223_55635cdd.mp4','VIDEO','CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-08 14:03:27',0,0),(87,4,1,NULL,'Dạ chi nhánh đã nhận được video kiểm tra sách từ bạn ạ! Thủ kho / Quản lý Tổng Kho Sách Cũ TP.HCM sẽ xem video và phản hồi cho bạn ngay ít phút nữa nhé! 📹✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-08 14:03:27',0,0),(88,1,1,NULL,'Dạ chi nhánh mình còn cuốn này tại quầy không ạ?',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',1,0,'2026-10-12 03:55:04',0,0),(89,1,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',0,0,'2026-10-12 03:55:04',0,0),(90,1,2,NULL,'chòa',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:38',0,0),(91,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:38',0,0),(92,1,2,NULL,'sadfádf',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:40',0,0),(93,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:40',0,0),(94,1,2,NULL,'df',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:40',0,0),(95,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:40',0,0),(96,1,2,NULL,'sdf',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:41',0,0),(97,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:41',0,0),(98,1,2,NULL,'ssf',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:41',0,0),(99,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:41',0,0),(100,1,2,NULL,'dfs',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:41',0,0),(101,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:41',0,0),(102,1,2,NULL,'df',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:42',0,0),(103,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:42',0,0),(104,1,2,NULL,'s',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:42',0,0),(105,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:42',0,0),(106,1,2,NULL,'fs',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:42',0,0),(107,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:42',0,0),(108,1,2,NULL,'fsd',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:42',0,0),(109,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:42',0,0),(110,1,2,NULL,'fs',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,0,'2026-10-13 13:37:43',0,0),(111,1,2,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 2: Đường Sách Quận 1. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 13:37:43',0,0),(112,4,4,NULL,'chào',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-13 14:11:24',0,0),(113,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-13 14:11:25',0,0),(114,4,4,NULL,'chào',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-13 14:11:26',0,0),(115,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-13 14:11:26',0,0),(116,4,4,NULL,'chào',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-13 14:11:27',0,0),(117,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-13 14:11:27',0,0),(118,4,4,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-13 14:11:28',0,0),(119,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-13 14:11:28',0,0),(120,4,4,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,0,'2026-10-13 14:11:29',0,0),(121,4,4,NULL,'Dạ em là Trợ lý AI của Chi Nhánh 4: Phố Sách Cũ Phú Nhuận. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 4: Phố Sách Cũ Phú Nhuận',1,0,'2026-10-13 14:11:29',0,0),(122,4,1,NULL,'chào',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 14:58:16',0,0),(123,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 14:58:20',0,0),(124,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 14:58:22',0,0),(125,4,1,NULL,'hả',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 14:58:26',0,0),(126,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 14:58:28',0,0),(127,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,0,'2026-10-13 14:58:28',0,0),(128,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 14:58:31',0,0),(129,4,1,NULL,'xin chao ReBook test',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:06:05',0,0),(130,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:07:11',0,0),(131,4,1,NULL,'chào ní',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:07:13',0,0),(132,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:08:30',0,0),(133,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:08:33',0,0),(134,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,0,'2026-10-13 15:08:33',0,0),(135,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:08:36',0,0),(136,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:37:44',0,0),(137,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:37:46',0,0),(138,4,1,NULL,'ji',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:38:08',0,0),(139,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:38:08',0,0),(140,4,1,NULL,'chào',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:38:11',0,0),(141,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:38:12',0,0),(142,4,1,NULL,'ê',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:38:13',0,0),(143,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:38:13',0,0),(144,4,1,NULL,'ủa',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:38:15',0,0),(145,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:38:15',0,0),(146,4,1,NULL,'ní',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:38:16',0,0),(147,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:38:16',0,0),(148,4,1,NULL,'chào ní',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:38:42',0,0),(149,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:38:42',0,0),(150,4,1,NULL,'sao ní',NULL,NULL,'STORE_STAFF','Quản Lý CN Thủ Đức',1,0,'2026-10-13 15:38:47',0,0),(151,4,1,NULL,'có vấn đề gì',NULL,NULL,'STORE_STAFF','Quản Lý CN Thủ Đức',1,0,'2026-10-13 15:38:59',0,0),(152,4,1,NULL,'hàng lỗi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:39:11',0,0),(153,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:39:11',0,0),(154,4,1,NULL,'Dạ em đã giữ sách ở quầy thu ngân cho mình rồi ạ!',NULL,NULL,'STORE_STAFF','Quản Lý CN Thủ Đức',1,0,'2026-10-13 15:40:57',0,0),(155,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,0,'2026-10-13 15:41:02',0,0),(156,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,0,'2026-10-13 15:41:02',0,0),(157,4,1,NULL,'Dạ cuốn sách này hiện đang còn trên kệ tại quầy của chi nhánh ạ! Anh/chị có thể ghé xem hoặc đặt mua trên web nhé!',NULL,NULL,'STORE_STAFF','Quản Lý CN Thủ Đức',1,0,'2026-10-13 15:41:10',0,0),(158,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:14',0,0),(159,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-13 15:41:14',0,0),(160,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:14',0,0),(161,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-13 15:41:14',0,0),(162,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:15',0,0),(163,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-13 15:41:15',0,0),(164,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:15',0,0),(165,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-13 15:41:15',0,0),(166,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:15',0,0),(167,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-13 15:41:15',0,0),(168,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:15',0,0),(169,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-13 15:41:15',0,0),(170,4,1,NULL,'[Hình ảnh thực tế]','/uploads/chat/chat_img_1791906085774_032ba23f.png','IMAGE','CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:41:26',0,0),(171,4,1,NULL,'Dạ em là Trợ lý AI của Tổng Kho Sách Cũ TP.HCM. Em đã ghi nhận câu hỏi của mình. Nếu anh/chị cần kiểm tra tồn kho cuốn sách nào hoặc cần thủ kho chụp ảnh thực tế tại quầy, anh/chị cứ nhắn tựa sách hoặc gửi ảnh cho em nhé ạ! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:41:26',0,0),(172,4,1,NULL,'chao shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:46:13',0,0),(173,4,1,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Tổng Kho Sách Cũ TP.HCM** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:46:13',0,0),(174,4,1,NULL,'co dac nhan tam khong',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:46:13',0,0),(175,4,1,100,'Dạ cuốn sách **\"Đắc Nhân Tâm\"** (Dale Carnegie) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **180 cuốn** trên kệ.\n• Tình trạng: Độ mới **85%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **92.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:46:13',0,0),(176,4,1,NULL,'cuon the alchemist con khong',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:46:13',0,0),(177,4,1,NULL,'Dạ hiện tại em chưa tìm thấy sách khớp chính xác với từ khóa **\"cuon the alchemist con khong\"** tại quầy Tổng Kho Sách Cũ TP.HCM.\n\nBạn có thể thử:\n1. Nhắn tên tựa sách cụ thể (ví dụ: *Đắc Nhân Tâm*, *Nhà Giả Kim*, *Hoàng Tử Bé*...)\n2. Nhắn thể loại bạn quan tâm (ví dụ: *sách kinh tế*, *sách kỹ năng*, *văn học*...)\n3. Hoặc bấm nút **[👨‍💼 Gặp nhân viên quầy]** bên dưới để Quản lý chi nhánh hỗ trợ tra cứu kho sách giúp mình nhé! 📖✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:46:13',0,0),(178,4,1,NULL,'goi y sach hay tai quay',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:46:13',0,0),(179,4,1,149,'Dạ tại quầy **Tổng Kho Sách Cũ TP.HCM** đang có các đầu sách nổi bật được bạn đọc yêu thích:\n📖 **Số Đỏ** - 66.000₫ (mới 85%)\n📖 **Tắt Đèn** - 88.000₫ (mới 85%)\n📖 **Sống Mòn** - 49.000₫ (mới 99%)\n\nBạn có thể nhắn tên tựa sách cụ thể để em kiểm tra chi tiết độ mới và tồn kho kệ sách nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:46:13',0,0),(180,4,1,NULL,'cuon the alchemist con khong',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:48:38',0,0),(181,4,1,2,'Dạ cuốn sách **\"Nhà Giả Kim (The Alchemist)\"** (Paulo Coelho) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **50 cuốn** trên kệ.\n• Tình trạng: Độ mới **90%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **45.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:48:38',0,0),(182,4,1,NULL,'sach cua paulo coelho',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:48:38',0,0),(183,4,1,2,'Dạ cuốn sách **\"Nhà Giả Kim (The Alchemist)\"** (Paulo Coelho) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **50 cuốn** trên kệ.\n• Tình trạng: Độ mới **90%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **45.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:48:38',0,0),(184,4,1,NULL,'dia chi o dau vay shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-13 15:48:38',0,0),(185,4,1,NULL,'Dạ chi nhánh **Tổng Kho Sách Cũ TP.HCM** có địa chỉ tại: 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh (TP. Hồ Chí Minh). Cửa hàng có chỗ để xe máy miễn phí và bàn đọc sách trải nghiệm, rất mong được đón tiếp bạn ghé quầy ạ! 📍',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-13 15:48:38',0,0),(186,4,2,NULL,'chao shop, chi nhanh minh o dau va mo cua den may gio',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-13 15:48:50',0,0),(187,4,2,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Chi Nhánh 2: Đường Sách Quận 1** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 15:48:50',0,0),(188,4,2,NULL,'dia chi quan 1 o dau',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-13 15:49:00',0,0),(189,4,2,NULL,'Dạ chi nhánh **Chi Nhánh 2: Đường Sách Quận 1** có địa chỉ tại: Đường Sách Nguyễn Văn Bình, Phường Bến Nghé (Quận 1). Cửa hàng có chỗ để xe máy miễn phí và bàn đọc sách trải nghiệm, rất mong được đón tiếp bạn ghé quầy ạ! 📍',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 15:49:00',0,0),(190,4,2,NULL,'co dac nhan tam khong',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-13 15:49:08',0,0),(191,4,2,100,'Dạ cuốn sách **\"Đắc Nhân Tâm\"** tại quầy Chi Nhánh 2: Đường Sách Quận 1 vừa tạm hết trên kệ ạ.\nTuy nhiên, hệ thống ReBook kiểm tra thấy vẫn còn tại:\n• Tổng Kho Sách Cũ TP.HCM (còn 180 cuốn)\nBạn có thể đặt trực tiếp trên web để chuỗi điều chuyển giao nhanh 2-4h nhé!',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-13 15:49:08',0,0),(192,4,2,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-14 04:44:28',0,0),(193,4,2,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Chi Nhánh 2: Đường Sách Quận 1 sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-14 04:44:28',0,0),(194,4,2,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-28 02:41:41',0,0),(195,4,2,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Chi Nhánh 2: Đường Sách Quận 1** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-28 02:41:41',0,0),(196,1,3,NULL,'hi',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,1,'2026-10-28 02:53:39',0,0),(197,1,3,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Chi Nhánh 3: Làng Đại Học Quốc Gia** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 3: Làng Đại Học Quốc Gia',1,0,'2026-10-28 02:53:39',0,0),(198,1,3,NULL,'hi',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,1,'2026-10-28 02:53:43',0,0),(199,1,3,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Chi Nhánh 3: Làng Đại Học Quốc Gia** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 3: Làng Đại Học Quốc Gia',0,0,'2026-10-28 02:53:43',0,0),(200,1,2,NULL,'hi',NULL,NULL,'CUSTOMER','Quản Trị Viên Hệ Thống',0,1,'2026-10-28 02:53:46',0,0),(201,1,2,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Chi Nhánh 2: Đường Sách Quận 1** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',0,0,'2026-10-28 02:53:46',0,0),(202,4,1,NULL,'Xin chao shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:02:28',0,0),(203,4,1,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Tổng Kho Sách Cũ TP.HCM** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:02:28',0,0),(204,4,1,NULL,'Shop có cuốn sách nào hay về tâm lý không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:02:34',0,0),(205,4,1,149,'Dạ tại quầy **Tổng Kho Sách Cũ TP.HCM** đang có các đầu sách nổi bật được bạn đọc yêu thích:\n📖 **Số Đỏ** - 66.000₫ (mới 85%)\n📖 **Tắt Đèn** - 88.000₫ (mới 85%)\n📖 **Sống Mòn** - 49.000₫ (mới 99%)\n\nBạn có thể nhắn tên tựa sách cụ thể để em kiểm tra chi tiết độ mới và tồn kho kệ sách nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:02:34',0,0),(206,4,1,NULL,'tôi muốn tìm sách về tài chính cá nhân',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:02:40',0,0),(207,4,1,110,'Dạ cuốn sách **\"Muôn Kiếp Nhân Sinh\"** (Nguyên Phong) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **159 cuốn** trên kệ.\n• Tình trạng: Độ mới **99%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **143.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:02:40',0,0),(208,4,1,NULL,'Shop có cuốn sách nào dạy đầu tư chứng khoán không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:02:46',0,0),(209,4,1,NULL,'Dạ hiện tại em chưa tìm thấy sách khớp chính xác với từ khóa **\"Shop có cuốn sách nào dạy đầu tư chứng khoán không?\"** tại quầy Tổng Kho Sách Cũ TP.HCM.\n\nBạn có thể thử:\n1. Nhắn tên tựa sách cụ thể (ví dụ: *Đắc Nhân Tâm*, *Nhà Giả Kim*, *Hoàng Tử Bé*...)\n2. Nhắn thể loại bạn quan tâm (ví dụ: *sách kinh tế*, *sách kỹ năng*, *văn học*...)\n3. Hoặc bấm nút **[👨‍💼 Gặp nhân viên quầy]** bên dưới để Quản lý chi nhánh hỗ trợ tra cứu kho sách giúp mình nhé! 📖✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:02:46',0,0),(210,4,1,NULL,'Shop ơi chi nhánh Thủ Đức có cuốn Đắc Nhân Tâm không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:09:35',0,0),(211,4,1,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Tổng Kho Sách Cũ TP.HCM** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:09:44',0,0),(212,4,1,NULL,'Tư vấn cho mình cuốn sách nào đọc để bớt suy nghĩ tiêu cực với',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:09:49',0,0),(213,4,1,NULL,'Dạ hiện tại em chưa tìm thấy sách khớp chính xác với từ khóa **\"Tư vấn cho mình cuốn sách nào đọc để bớt suy nghĩ tiêu cực với\"** tại quầy Tổng Kho Sách Cũ TP.HCM.\n\nBạn có thể thử:\n1. Nhắn tên tựa sách cụ thể (ví dụ: *Đắc Nhân Tâm*, *Nhà Giả Kim*, *Hoàng Tử Bé*...)\n2. Nhắn thể loại bạn quan tâm (ví dụ: *sách kinh tế*, *sách kỹ năng*, *văn học*...)\n3. Hoặc bấm nút **[👨‍💼 Gặp nhân viên quầy]** bên dưới để Quản lý chi nhánh hỗ trợ tra cứu kho sách giúp mình nhé! 📖✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:09:58',0,0),(214,4,1,NULL,'Shop tư vấn giúp mình một cuốn sách giúp rèn luyện thói quen tốt với',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:15:01',0,0),(215,4,1,106,'Dạ cuốn sách **\"Tôi Thấy Hoa Vàng Trên Cỏ Xanh\"** (Nguyễn Nhật Ánh) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **138 cuốn** trên kệ.\n• Tình trạng: Độ mới **80%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **118.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:15:10',0,0),(216,4,1,NULL,'Shop có mở cửa vào ngày Chủ Nhật không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:15:17',0,0),(217,4,1,NULL,'Dạ chi nhánh **Tổng Kho Sách Cũ TP.HCM** mở cửa phục vụ từ 08:00 sáng đến 21:30 tối hàng ngày (kể cả Thứ 7, Chủ Nhật và ngày lễ) ạ! Bạn có thể ghé bất cứ lúc nào trong khung giờ này nhé! ⏰',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:15:26',0,0),(218,4,1,NULL,'Shop ơi bớt giá cuốn này cho mình 20k được không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:15:34',0,0),(219,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-28 03:15:34',0,0),(220,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:22:27',0,0),(221,4,1,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Tổng Kho Sách Cũ TP.HCM** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:22:27',0,0),(222,4,1,NULL,'Dạo này mình thấy stress quá, shop có cuốn nào chữa lành tâm trạng không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:28:08',0,0),(223,4,1,1,'Chào bạn, mình rất hiểu cảm giác stress khi cuộc sống có nhiều áp lực. Đọc sách là một cách tuyệt vời để tìm lại sự cân bằng và chữa lành tâm trạng. Shop mình có một vài cuốn sách có thể giúp bạn cảm thấy nhẹ nhàng và tích cực hơn đấy:\n\n*   **\"Yêu Những Điều Không Hoàn Hảo\"** của Hae Min: Cuốn sách này như một người bạn tâm giao, mang đến những lời khuyên chân thành, nhẹ nhàng về cách chấp nhận bản thân, đối diện với những khuyết điểm và tìm thấy hạnh phúc trong những điều giản dị. Nó giúp bạn nhận ra rằng không ai là hoàn hảo và điều đó hoàn toàn ổn.\n*   **\"Mình Là Cá, Việc Của Mình Là Bơi\"** của Takeshi Furukawa: Cuốn sách mang một thông điệp đầy tích cực về việc hãy cứ là chính mình và làm tốt việc của mình, đừng quá lo lắng về những gì người khác nghĩ hay những áp lực bên ngoài. Cách tiếp cận nhẹ nhàng, đôi khi hài hước này sẽ giúp bạn thư giãn và tìm lại sự tự tin.\n*   **\"Nhà Giả Kim\"** của Paulo Coelho: Đây là một câu chuyện ngụ ngôn kinh điển về hành trình theo đuổi ước mơ và khám phá ý nghĩa cuộc sống. Cuốn sách truyền cảm hứng mạnh mẽ, giúp bạn nhìn nhận lại những gì mình đang tìm kiếm và khuyến khích bạn tin vào bản thân trên con đường của mình.\n*   **\"Tuổi Trẻ Đáng Giá Bao Nhiêu\"** của Rosie Nguyễn: Nếu bạn đang cảm thấy lạc lõng hay hoang mang về con đường phía trước, cuốn sách này sẽ là một nguồn động viên tuyệt vời. Rosie Nguyễn chia sẻ những trải nghiệm và suy ngẫm sâu sắc về cách sống một tuổi trẻ ý nghĩa,\n\n📍 Cuốn **\"Yêu Những Điều Không Hoàn Hảo\"** hiện **CÒN 32 CUỐN TRÊN KỆ** tại Tổng Kho Sách Cũ TP.HCM (48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh). Bạn có thể bấm nút **Mua** bên dưới để giữ sách ngay nhé! 📚✨',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:28:13',0,0),(224,4,1,NULL,'Em là sinh viên HCMUTE muốn tìm sách học lập trình Java và Spring Boot',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:28:26',0,0),(225,4,1,NULL,'Chào bạn, rất vui được hỗ trợ bạn là sinh viên HCMUTE tìm sách!\n\nHiện tại, danh mục sách mà chúng tôi có thông tin tồn kho chủ yếu tập trung vào các thể loại văn học, kinh doanh, phát triển bản thân và kỹ năng mềm. Vì vậy, chúng tôi chưa có các đầu sách chuyên sâu về **lập trình Java và Spring Boot** trong kho dữ liệu thời gian thực này.\n\nTuy nhiên, với vị trí chi nhánh **Thủ Đức (gần ĐH SPKT HCMUTE)**, đây sẽ là địa điểm rất thuận tiện để bạn ghé thăm. Bạn có thể đến trực tiếp chi nhánh này để tham khảo và hỏi nhân viên cửa hàng xem liệu có những đầu sách lập trình nào phù hợp hoặc có thể đặt hàng theo nhu cầu của bạn không nhé.\n\n**Thông tin về chi nhánh Thủ Đức và các ưu đãi của Chuỗi Cửa Hàng Sách Cũ TP.HCM:**\n\n*   **Chi nhánh Thủ Đức:** Nằm gần ĐH SPKT HCMUTE, rất tiện lợi cho sinh viên.\n*   **Giờ mở cửa:** Tất cả các chi nhánh, bao gồm cả Thủ Đức, đều mở cửa từ **08:00 đến 21:30** hàng ngày.\n*   **Chất lượng sách:** Chúng tôi cam kết sách có độ mới từ **80% đến 99',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:28:30',0,0),(226,4,1,NULL,'Alo shop ơi cho mình gặp nhân viên trực quầy với',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:28:41',0,0),(227,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-28 03:28:41',0,0),(228,4,1,NULL,'chào shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:31:44',0,0),(229,4,1,NULL,'Chào bạn, tôi là trợ lý AI của Chuỗi Cửa Hàng Sách Cũ TP.HCM đây ạ! Rất vui được hỗ trợ bạn hôm nay.\n\nBạn đang tìm kiếm cuốn sách nào hay cần tư vấn về thể loại sách nào ạ?',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:31:47',0,0),(230,4,1,NULL,'Quầy mình có cuốn sách nào hay về kinh tế hoặc phát triển bản thân không?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:34:40',0,0),(231,4,1,NULL,'Chào Bạn, em là nhân viên quầy **Tổng Kho Sách Cũ TP.HCM** đây ạ!\n\nTại quầy mình, em có một vài tựa sách rất hay về kinh tế và phát triển bản thân mà chắc chắn sẽ mang lại nhiều kiến thức bổ ích cho Bạn đó ạ.\n\n**Về mảng kinh tế và tài chính:**\n\n*   **\"Chiến Tranh Tiền Tệ\"** của Song Hongbing: Cuốn này phân tích sâu sắc về lịch sử các cuộc khủng hoảng tài chính, rất đáng đọc để hiểu rõ hơn về dòng chảy tiền tệ toàn cầu. Em còn **197 cuốn**, độ mới **90%**, giá chỉ **106.000₫**.\n\n**Về phát triển bản thân, tư duy và tâm lý:**\n\n*   **\"Bí Mật Tư Duy Triệu Phú\"** của T. Harv Eker: Cuốn sách kinh điển giúp thay đổi tư duy về tiền bạc và sự giàu có. Em còn tới **221 cuốn**, độ mới **99%** (gần như mới tinh), giá chỉ **35.000₫**.\n*   **\"Tâm Lý Học Hành Vi\"** của John B. Watson: Nếu Bạn muốn tìm hiểu sâu hơn về cách con người suy nghĩ và hành động, đây là một tài liệu rất giá trị. Em còn **197 cuốn**, độ mới **85%**, giá **92.000₫**.\n*   **\"Hành Trình Về Phương Đông\"** của Baird T. Spalding: Một tác phẩm đầy cảm hứng về hành trình khám phá bản thân và những bí ẩn tâm linh. Em còn **204 cuốn**, độ mới **90%**, giá **65.000₫**.\n*   **\"Chuyện Con Mèo Dạy Hải Âu Bay\"** của Luis Sepúlveda: Một câu chuyện ngụ ngôn nhẹ nhàng nhưng sâu sắc về tình bạn',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:34:45',0,0),(232,4,2,NULL,'Quầy mình ở Quận 1 có những sách gì hay đang có sẵn vậy bạn?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-28 03:34:56',0,0),(233,4,2,NULL,'Dạ, chào Bạn! Em là nhân viên tại Chi Nhánh 2 Đường Sách Quận 1 đây ạ. Rất vui được tư vấn cho Bạn ạ!\n\nHiện tại, quầy em có rất nhiều đầu sách hay và đang có sẵn trên kệ ạ. Để em điểm qua một vài tựa sách nổi bật mà nhiều bạn đọc yêu thích nhé:\n\n*   **Sống Mòn** của Nam Cao: Cuốn này có độ mới lên tới 99% luôn ạ, giá chỉ 49.000₫. Là một tác phẩm kinh điển khắc họa sâu sắc cuộc sống và tâm lý con người.\n*   **Bí Mật Tư Duy Triệu Phú** của T. Harv Eker: Tựa sách này cũng đạt độ mới 99%, giá 35.000₫. Rất phù hợp nếu Bạn muốn tìm hiểu về tư duy làm giàu và thay đổi tài chính cá nhân.\n*   **Chuyện Con Mèo Dạy Hải Âu Bay** của Luis Sepúlveda: Độ mới 95%, giá 82.000₫. Một câu chuyện đầy cảm hứng và ý nghĩa về tình bạn, trách nhiệm và sự sẻ chia.\n*   **Hoàng Tử Bé** của Antoine de Saint-Exupéry: Cuốn này có độ mới 85%, giá 79.0',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-28 03:35:00',0,0),(234,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',1,1,'2026-10-28 03:40:21',0,0),(235,4,1,NULL,'Dạ, em chào Anh/Chị ạ! Em là nhân viên tại quầy **Tổng Kho Sách Cũ TP.HCM** ở địa chỉ 48 Võ Văn Ngân, Thủ Đức đây ạ.\n\nAnh/Chị cần em hỗ trợ tìm kiếm sách hay có câu hỏi gì về sách cũ không ạ? Em sẵn lòng giúp ạ!',NULL,NULL,'STORE_STAFF','Trợ lý AI - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-28 03:40:25',0,0),(236,4,2,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-28 03:40:37',0,0),(237,4,2,NULL,'Dạ, chào Bạn! Em là nhân viên tại Chi Nhánh 2: Đường Sách Quận 1 của Chuỗi Cửa Hàng Sách Cũ TP.HCM.\n\nBạn cần em hỗ trợ tìm kiếm cuốn sách nào hay có câu hỏi gì về các đầu sách cũ bên em không ạ? Em sẵn sàng giúp đỡ ạ!',NULL,NULL,'STORE_STAFF','Trợ lý AI - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-28 03:40:40',0,0),(238,4,2,NULL,'chaof shop nhen',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-28 04:04:09',0,0),(239,4,2,NULL,'Dạ, chào bạn ạ! Em là Cậu Ba Đường Sách, rất hân hạnh được tiếp đón Quý bạn ghé thăm không gian sách cũ hoài cổ của em tại Đường Sách Nguyễn Văn Bình này.\n\nBạn đang tìm kiếm một tựa sách cũ hay có điều gì muốn em chia sẻ, tư vấn không ạ? Em ở đây để phục vụ bạn ạ.',NULL,NULL,'STORE_STAFF','Cậu Ba Đường Sách - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-28 04:04:14',0,0),(240,4,2,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-28 04:11:19',0,0),(241,4,2,NULL,'Dạ, chào Bạn! Em là Cậu Ba Đường Sách, hân hạnh được đón tiếp Bạn tại quầy sách cũ thân thương này, nằm giữa lòng Đường Sách Nguyễn Văn Bình nhộn nhịp.\n\nQuầy em đây chuyên về những trang sách văn học kinh điển và những bản in quý giá từ trước năm 1975, mang một chút hơi thở hoài cổ giữa lòng Sài Gòn.\n\nBạn ghé qua chơi, hay đang tìm kiếm một tựa sách nào đó ạ? Em sẵn lòng lắng nghe và sẻ chia.',NULL,NULL,'STORE_STAFF','Cậu Ba Đường Sách - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-28 04:11:22',0,0),(242,5,1,NULL,'Chào quầy, mình là Cường cần tìm sách cũ',NULL,NULL,'CUSTOMER','Thái Duy Cường',1,1,'2026-10-28 04:27:50',0,0),(243,5,1,NULL,'Chào Anh Cường ạ! Em là Bé Mọt Thủ Đức đây ạ, rất vui được đón tiếp Anh/Chị ghé quầy hôm nay.\n\nAnh Cường đang tìm thể loại sách nào hay tác giả nào cụ thể không ạ? Để em dễ dàng hỗ trợ mình tìm được cuốn sách ưng ý nhất ạ! Quầy em có rất nhiều đầu sách hay đang chờ Anh/Chị khám phá đó ạ.',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',0,0,'2026-10-28 04:27:55',0,0),(244,4,2,NULL,'hi shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 03:17:35',0,0),(245,4,2,NULL,'Dạ, chào Bạn! Em là Cậu Ba Đường Sách, Trợ lý AI kiêm nhân viên trực quầy tại Chi Nhánh 2 - Đường Sách Quận 1 của \'Chuỗi Cửa Hàng Sách Cũ TP.HCM\' đây ạ.\n\nRất vui được đón tiếp Bạn ghé thăm không gian hoài cổ của em giữa lòng Đường Sách Nguyễn Văn Bình này. Quầy em mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí đó ạ.\n\nBạn có đang tìm kiếm tựa sách nào đặc biệt hay muốn em giới thiệu vài cuốn hay không? Em luôn sẵn lòng lắng nghe và chia sẻ ạ.',NULL,NULL,'STORE_STAFF','Cậu Ba Đường Sách - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-29 03:17:39',0,0),(246,4,2,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 03:17:44',0,0),(247,4,2,NULL,'Dạ, chào Bạn! Em là Cậu Ba Đường Sách, rất vui được đón tiếp Quý bạn ghé thăm chi nhánh Đường Sách Quận 1 của chúng em.\n\nNơi đây là một góc nhỏ hoài cổ giữa lòng Sài Gòn nhộn nhịp, nơi những trang sách cũ kỹ mang trong mình bao câu chuyện và dấu ấn thời gian.\n\nKhông biết hôm nay Quý bạn đang tìm kiếm điều gì, hay muốn em giới thiệu chút gì về những đầu sách đang có trên kệ ạ?',NULL,NULL,'STORE_STAFF','Cậu Ba Đường Sách - Chi Nhánh 2: Đường Sách Quận 1',1,0,'2026-10-29 03:17:47',0,0),(248,4,2,NULL,'chào bạn',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 04:44:20',0,0),(249,4,2,NULL,'Dạ, chào Bạn. Em là Cậu Ba Đường Sách, rất vui được đón tiếp Bạn ghé thăm quầy sách cũ của chúng em tại Đường Sách Nguyễn Văn Bình này ạ.\n\nQuầy em là một góc nhỏ hoài cổ, chuyên về những áng văn học kinh điển và các bản in quý giá trước năm 1975, giữa lòng Sài Gòn nhộn nhịp.\n\nBạn đang tìm kiếm điều gì đặc biệt hôm nay ạ? Em sẵn lòng lắng nghe và hỗ trợ Bạn tìm được những trang sách ưng ý.',NULL,NULL,'STORE_STAFF','Cậu Ba Đường Sách - Chi Nhánh 2: Đường Sách Quận 1',0,0,'2026-10-29 04:44:23',0,0),(250,8,1,NULL,'Dạ shop còn cuốn Mắt Biếc không ạ?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 04:46:37',0,0),(251,8,1,NULL,'Dạ chào Bạn ạ! Em đã kiểm tra trên kệ của quầy em ở Thủ Đức rồi, hiện tại thì cuốn **Mắt Biếc** bên em vừa hết ạ.\n\nTuy nhiên, hệ thống chuỗi nhà em có nhiều chi nhánh lắm, có thể bên em sẽ điều chuyển về cho Bạn được ạ. Hoặc nếu Bạn thích các tác phẩm văn học Việt Nam với giọng văn sâu lắng, tình cảm tương tự, em có thể gợi ý một vài cuốn khác đang có sẵn ở quầy em nhé:\n\n*   **Số Đỏ** của Vũ Trọng Phụng (85%, 66.000₫)\n*   **Tắt Đèn** của Ngô Tất Tố (85%, 88.000₫)\n*   **Sống Mòn** của Nam Cao (99%, 49.000₫)\n\nBạn xem sao ạ? Em luôn sẵn sàng hỗ trợ Bạn tìm được cuốn sách ưng ý!',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',0,0,'2026-10-29 04:46:41',0,0),(252,4,1,NULL,'Dạ shop còn cuốn Chiến Tranh Tiền Tệ không ạ?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 04:52:17',0,0),(253,4,1,145,'Dạ chào Bạn! Em kiểm tra rồi ạ, cuốn **Chiến Tranh Tiền Tệ** của Song Hongbing bên em còn tới 197 cuốn trên kệ luôn ạ! Sách còn mới 90%, giá chỉ 106.000₫ thôi ạ.\n\nBạn ghé quầy em ở 48 Võ Văn Ngân trải nghiệm hoặc bấm nút Mua để em giao nhanh nha. Nếu Bạn mua trên 3 cuốn, em có tặng kèm bookmark da kỷ niệm nữa đó ạ!',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 04:52:22',0,0),(254,4,1,NULL,'Dạ shop còn cuốn Chiến Tranh Tiền Tệ không ạ?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 04:54:17',0,0),(255,4,1,145,'Dạ chào Bạn! Em là Bé Mọt Thủ Đức đây ạ!\n\nVề cuốn **\"Chiến Tranh Tiền Tệ\"** của tác giả Song Hongbing, em kiểm tra trên kệ thì vẫn còn ạ! Hiện tại quầy em còn đến **197 cuốn** với độ mới **90%**, giá bán cũ là **106.000₫**.\n\nBạn ghé quầy em trải nghiệm sách hoặc có thể bấm nút \"Mua\" để em chuẩn bị giao nhanh cho Bạn nha! À, nếu Bạn mua trên 3 cuốn, em sẽ tặng kèm một chiếc bookmark da kỷ niệm xinh xắn nữa đó ạ! 😊',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 04:54:21',0,0),(256,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 04:55:11',0,0),(257,4,1,NULL,'Chào bạn! Em là Bé Mọt Thủ Đức đây ạ, rất vui được đón tiếp Bạn ghé thăm \"Tổng Kho Sách Cũ TP.HCM\" nhà em ở 48 Võ Văn Ngân, Thủ Đức.\n\nQuầy em có bãi đỗ xe máy rộng rãi miễn phí và bàn đọc sách thoải mái lắm ạ. Bạn cần tìm sách hay có câu hỏi gì cứ tự nhiên hỏi em nhé! Em sẵn sàng hỗ trợ ạ. 😊',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 04:55:14',0,0),(258,4,1,NULL,'hi',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 04:58:40',0,0),(259,4,1,NULL,'Chào Bạn! Em là Bé Mọt Thủ Đức đây ạ. Rất vui được chào đón Bạn ghé thăm quầy sách cũ Tổng Kho của tụi em ở 48 Võ Văn Ngân, TP. Thủ Đức nè.\n\nBạn cần em hỗ trợ tìm kiếm hay tư vấn gì không ạ?',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 04:58:43',0,0),(260,4,1,NULL,'chào ní',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 05:08:42',0,0),(261,4,1,NULL,'Chào Bạn! Em là Bé Mọt Thủ Đức đây ạ, rất vui được đón tiếp Bạn ghé thăm quầy sách của em ở Tổng Kho Sách Cũ TP.HCM.\n\nBạn cần em tìm kiếm hay tư vấn tựa sách nào không ạ? Em sẵn sàng hỗ trợ hết mình nha! 😊',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 05:08:45',0,0),(262,4,1,NULL,'bạn có gay ko',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 05:08:53',0,0),(263,4,1,NULL,'Em chào Bạn! Em là Bé Mọt Thủ Đức, một trợ lý AI của Chuỗi Cửa Hàng Sách Cũ TP.HCM nè. Em không có giới tính hay xu hướng cá nhân đâu ạ, em chỉ là một chương trình máy tính được lập trình để giúp Bạn tìm kiếm và tư vấn về sách thôi ạ.\n\nBạn có cần em hỗ trợ tìm cuốn sách nào hoặc có câu hỏi gì về sách không nè? Em luôn sẵn lòng giúp đỡ ạ! 😊',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 05:08:57',0,0),(264,4,1,NULL,'Dạ em muốn hỏi Tổng Kho Sách Cũ TP.HCM về tình trạng và tồn kho của cuốn sách \"Người Đàn Ông Mang Tên Ove\" ạ!',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 05:59:46',0,0),(265,4,1,NULL,'Dạ chào Bạn! Em là Bé Mọt Thủ Đức đây ạ.\n\nEm vừa kiểm tra nhanh trên kệ \"Tổng Kho Sách Cũ TP.HCM\" của mình thì hiện tại em chưa thấy có cuốn **\"Người Đàn Ông Mang Tên Ove\"** ạ. Cuốn này đang tạm hết hàng tại quầy em rồi ạ.\n\nTuy nhiên, hệ thống chuỗi cửa hàng của bên em có nhiều chi nhánh, có thể sách đang có ở một trong các địa điểm khác. Nếu Bạn cần gấp, em có thể hỗ trợ kiểm tra xem có thể điều chuyển về đây cho Bạn không ạ?\n\nHoặc nếu Bạn muốn tham khảo thêm sách cùng thể loại đang có sẵn tại quầy em, em xin gợi ý một vài tựa rất hay và bán chạy nè:\n*   **\"Chuyện Con Mèo Dạy Hải Âu Bay\"** (Tác giả: Luis Sepúlveda) - Độ mới 95%, giá 82.000₫.\n*   **\"Hoàng Tử Bé\"** (Tác giả: Antoine de Saint-Exupéry) - Độ mới 85%, giá 79.000₫.\n*   **\"Kafka Bên Bờ Biển\"** (Tác giả: Haruki Murakami) - Độ mới 85%, giá 40.000₫',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 05:59:51',0,0),(266,4,1,NULL,'chào shop',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 06:00:23',0,0),(267,4,1,NULL,'Chào Bạn! Em là Bé Mọt Thủ Đức đây ạ. Rất vui được chào đón Bạn đến với \"Tổng Kho Sách Cũ TP.HCM\" của chúng em!\n\nQuầy em ở ngay **48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức**, gần ĐH Sư phạm Kỹ thuật (HCMUTE) đó ạ. Quầy em có bãi đỗ xe máy rộng rãi miễn phí và bàn đọc sách thoải mái để Bạn ghé qua xem sách và thư giãn nha.\n\nChúng em mở cửa từ **08:00 đến 21:30 mỗi ngày**.\n\nBạn cần tìm cuốn sách nào hay muốn em tư vấn gì không ạ? Em sẵn sàng hỗ trợ mình nè! 😊',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 06:00:27',0,0),(268,4,1,NULL,'tìm sách về tình yêu',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 06:00:39',0,0),(269,4,1,NULL,'Chào Bạn, em là Bé Mọt Thủ Đức đây ạ! Rất vui được hỗ trợ Bạn tìm sách.\n\nVề chủ đề tình yêu, quầy em hiện có một vài tựa sách rất hay và kinh điển, có lẽ sẽ hợp với sở thích của Bạn đó ạ:\n\n*   **Anna Karenina** của Leo Tolstoy: Đây là một tác phẩm kinh điển về tình yêu, đam mê và những bi kịch trong xã hội Nga thế kỷ 19. Sách có độ mới 95%, giá bán cũ là 146.000₫, còn trên kệ em là 200 cuốn ạ.\n*   **Cuốn Theo Chiều Gió** của Margaret Mitchell: Câu chuyện tình yêu lãng mạn và đầy bi tráng trong bối cảnh Nội chiến Hoa Kỳ. Sách còn mới 90%, giá 83.000₫, em có sẵn 198 cuốn ạ.\n*   **Rừng Na Uy** của Haruki Murakami: Một câu chuyện đầy chất thơ về tình yêu, sự mất mát và trưởng thành của tuổi trẻ Nhật Bản. Sách có độ mới 80%, giá 106.000₫, em có 200 cuốn ạ.\n*   **Hoàng Tử Bé** của Antoine de Saint-Exupéry: Tuy không hẳn là tiểu thuyết tình yêu theo nghĩa truyền thống, nhưng tác phẩm này chứa đựng những bài học sâu sắc về tình yêu, tình bạn và ý nghĩa cuộc sống. Độ mới 85%, giá 79.000₫, còn 182 cuốn ạ.\n*   **Đại Gia Gatsby** của F. Scott Fitzgerald: Một bức tranh về nước Mỹ những năm 1920, với những khát vọng tình yêu, sự giàu sang và cả những bi kịch. Sách mới 95%, giá 12',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 06:00:43',0,0),(270,4,1,NULL,'chào',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 08:49:10',0,0),(271,4,1,NULL,'Dạ ReBook xin chào bạn! Em là Trợ lý AI của **Tổng Kho Sách Cũ TP.HCM** 📚✨\nHiện tại quầy đang có sẵn rất nhiều đầu sách cũ tuyển chọn (Văn học, Kinh tế, Kỹ năng, Ngoại ngữ, CNTT...). Bạn đang tìm tựa sách, tác giả hay cần em gợi ý sách hay tại quầy ạ?',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',1,0,'2026-10-29 08:49:12',0,0),(272,4,1,NULL,'[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 08:49:28',0,0),(273,4,1,NULL,'Dạ chi nhánh đã nhận được yêu cầu gặp nhân viên! Quản lý quầy Tổng Kho Sách Cũ TP.HCM sẽ tiếp quản cuộc trò chuyện và phản hồi trực tiếp cho bạn ngay nhé ạ! 👨‍💼✨',NULL,NULL,'STORE_STAFF','Hệ thống chuyển giao',1,1,'2026-10-29 08:49:28',0,0),(274,4,1,NULL,'Dạ em muốn hỏi Tổng Kho Sách Cũ TP.HCM về tình trạng và tồn kho của cuốn sách \"Học Cách Mặc Kệ (The Let Them Theory)\" ạ!',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 08:49:54',0,0),(275,4,1,19,'Dạ cuốn sách **\"Học Cách Mặc Kệ (The Let Them Theory)\"** (Mel Robbins) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **54 cuốn** trên kệ.\n• Tình trạng: Độ mới **98%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **98.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',0,0,'2026-10-29 08:49:56',0,0),(276,4,1,NULL,'Dạ em muốn hỏi Tổng Kho Sách Cũ TP.HCM về tình trạng và tồn kho của cuốn sách \"Học Cách Mặc Kệ (The Let Them Theory)\" ạ!',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 08:50:03',0,0),(277,4,1,19,'Dạ cuốn sách **\"Học Cách Mặc Kệ (The Let Them Theory)\"** (Mel Robbins) hiện **ĐANG CÓ SẴN** tại Tổng Kho Sách Cũ TP.HCM ạ!\n• Tồn kho quầy: **54 cuốn** trên kệ.\n• Tình trạng: Độ mới **98%**, gáy bìa nguyên vẹn, trang giấy sạch đẹp.\n• Giá bán cũ: **98.000₫**.\nBạn có thể bấm nút **Mua** ngay bên dưới để đặt giao tận nơi hoặc ghé quầy đọc thử nhé! ✨',NULL,NULL,'STORE_STAFF','Bé Mọt Thủ Đức - Tổng Kho Sách Cũ TP.HCM',0,0,'2026-10-29 08:50:04',0,0),(278,4,3,NULL,'Dạ chi nhánh mình còn cuốn này tại quầy không ạ?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 08:54:09',0,0),(279,4,3,NULL,'Chào Bạn! ✨ Em là Mọt Làng Đại Học đây ạ!\n\nBạn ơi, bạn cho em xin tên cuốn sách mà Bạn đang tìm để em kiểm tra trên kệ của Chi Nhánh 3: Làng Đại Học Quốc Gia mình nha! Em sẵn sàng hỗ trợ Bạn hết mình ạ! 📚💖',NULL,NULL,'STORE_STAFF','Mọt Làng Đại Học - Chi Nhánh 3: Làng Đại Học Quốc Gia',0,0,'2026-10-29 08:54:12',0,0),(280,4,3,NULL,'Dạ shop có thể chụp ảnh thực tế tình trạng sách giúp mình được không ạ?',NULL,NULL,'CUSTOMER','Nguyễn Song Hoàng Phúc',0,1,'2026-10-29 08:54:17',0,0),(281,4,3,NULL,'Dạ hiện tại em chưa tìm thấy sách khớp chính xác với từ khóa **\"Dạ shop có thể chụp ảnh thực tế tình trạng sách giúp mình được không ạ?\"** tại quầy Chi Nhánh 3: Làng Đại Học Quốc Gia.\n\nBạn có thể thử:\n1. Nhắn tên tựa sách cụ thể (ví dụ: *Đắc Nhân Tâm*, *Nhà Giả Kim*, *Hoàng Tử Bé*...)\n2. Nhắn thể loại bạn quan tâm (ví dụ: *sách kinh tế*, *sách kỹ năng*, *văn học*...)\n3. Hoặc bấm nút **[👨‍💼 Gặp nhân viên quầy]** bên dưới để Quản lý chi nhánh hỗ trợ tra cứu kho sách giúp mình nhé! 📖✨',NULL,NULL,'STORE_STAFF','Mọt Làng Đại Học - Chi Nhánh 3: Làng Đại Học Quốc Gia',0,0,'2026-10-29 08:54:19',0,0);
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT '0',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_book` (`store_id`,`book_id`),
  KEY `fk_inventory_books` (`book_id`),
  CONSTRAINT `fk_inventory_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_inventory_stores` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=856 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,1,1,28,'2026-10-29 08:52:30'),(6,1,2,50,'2026-10-11 15:07:30'),(11,1,3,51,'2026-10-05 06:43:03'),(16,1,4,48,'2026-10-05 06:43:03'),(21,1,5,44,'2026-10-05 06:43:03'),(26,1,6,49,'2026-10-11 15:07:08'),(31,1,7,45,'2026-10-05 06:43:03'),(36,1,8,41,'2026-10-05 06:43:03'),(41,1,9,44,'2026-10-05 06:43:03'),(46,1,10,50,'2026-10-05 06:43:03'),(51,1,11,47,'2026-10-05 06:43:03'),(56,1,12,36,'2026-10-05 06:43:03'),(61,1,13,43,'2026-10-05 06:43:03'),(66,1,14,54,'2026-10-05 06:43:03'),(71,1,15,39,'2026-10-05 08:49:19'),(76,1,16,36,'2026-10-05 06:43:03'),(81,1,17,46,'2026-10-05 06:43:03'),(86,1,18,45,'2026-10-05 06:43:03'),(91,1,19,54,'2026-10-05 06:43:03'),(96,1,20,38,'2026-10-05 06:43:03'),(194,1,100,180,'2026-10-05 06:43:03'),(202,1,101,166,'2026-10-05 06:43:03'),(210,1,102,179,'2026-10-05 06:43:03'),(218,1,103,122,'2026-10-05 06:43:03'),(226,1,104,191,'2026-10-29 04:07:38'),(234,1,105,163,'2026-10-29 03:14:24'),(242,1,106,138,'2026-10-05 06:43:03'),(250,1,107,176,'2026-10-05 06:43:03'),(258,1,108,191,'2026-10-05 06:43:03'),(266,1,109,207,'2026-10-05 06:43:03'),(274,1,110,159,'2026-10-05 06:43:03'),(282,1,111,186,'2026-10-05 06:43:03'),(290,1,112,202,'2026-10-05 06:43:03'),(298,1,113,150,'2026-10-05 06:43:03'),(306,1,114,166,'2026-10-05 06:43:03'),(314,1,115,206,'2026-10-05 06:43:03'),(322,1,116,180,'2026-10-29 06:07:47'),(330,1,117,179,'2026-10-05 06:43:03'),(338,1,118,199,'2026-10-05 06:43:03'),(346,1,119,210,'2026-10-05 06:43:03'),(354,1,120,181,'2026-10-05 06:43:03'),(362,1,121,203,'2026-10-05 06:43:03'),(370,1,122,203,'2026-10-05 06:43:03'),(378,1,123,187,'2026-10-05 06:43:03'),(386,1,124,209,'2026-10-05 06:43:03'),(394,1,125,198,'2026-10-05 06:43:03'),(402,1,126,214,'2026-10-05 06:43:03'),(410,1,127,130,'2026-10-05 06:43:03'),(418,1,128,171,'2026-10-05 06:43:03'),(426,1,129,224,'2026-10-05 06:43:03'),(434,1,130,228,'2026-10-05 06:43:03'),(442,1,131,200,'2026-10-05 06:43:03'),(450,1,132,206,'2026-10-05 06:43:03'),(458,1,133,198,'2026-10-05 06:43:03'),(466,1,134,186,'2026-10-05 06:43:03'),(474,1,135,176,'2026-10-05 06:43:03'),(482,1,136,216,'2026-10-05 06:43:03'),(490,1,137,224,'2026-10-05 06:43:03'),(498,1,138,200,'2026-10-05 06:43:03'),(506,1,139,196,'2026-10-05 06:43:03'),(514,1,140,148,'2026-10-05 06:43:03'),(522,1,141,182,'2026-10-05 06:43:03'),(530,1,142,194,'2026-10-05 06:43:03'),(538,1,143,204,'2026-10-05 06:43:03'),(546,1,144,221,'2026-10-05 06:43:03'),(554,1,145,197,'2026-10-05 06:43:03'),(562,1,146,197,'2026-10-05 06:43:03'),(570,1,147,164,'2026-10-05 06:43:03'),(578,1,148,165,'2026-10-05 06:43:03'),(586,1,149,156,'2026-10-05 06:43:03'),(594,2,1,2,'2026-10-15 06:26:15'),(595,2,2,2,'2026-10-15 06:26:15'),(596,2,3,2,'2026-10-15 06:26:15'),(597,2,4,2,'2026-10-15 06:26:15'),(598,2,5,2,'2026-10-15 06:26:15'),(599,2,6,2,'2026-10-15 06:26:15'),(600,2,7,2,'2026-10-15 06:26:15'),(601,2,8,2,'2026-10-15 06:26:15'),(602,2,9,2,'2026-10-15 06:26:15'),(603,2,10,2,'2026-10-15 06:26:15'),(604,2,11,2,'2026-10-15 06:26:15'),(605,2,12,2,'2026-10-15 06:26:15'),(606,2,13,2,'2026-10-15 06:26:15'),(607,2,14,2,'2026-10-15 06:26:15'),(608,2,15,2,'2026-10-15 06:26:15'),(609,2,16,2,'2026-10-15 06:26:15'),(610,2,17,2,'2026-10-15 06:26:15'),(611,2,18,2,'2026-10-15 06:26:15'),(612,2,19,2,'2026-10-15 06:26:15'),(613,2,20,2,'2026-10-15 06:26:15'),(614,2,101,2,'2026-10-15 06:26:15'),(615,2,102,2,'2026-10-15 06:26:15'),(616,2,104,2,'2026-10-15 06:26:15'),(617,2,105,2,'2026-10-15 06:26:15'),(618,2,106,2,'2026-10-15 06:26:15'),(619,2,107,2,'2026-10-15 06:26:15'),(620,2,108,2,'2026-10-15 06:26:15'),(621,2,109,2,'2026-10-15 06:26:15'),(622,2,110,2,'2026-10-15 06:26:15'),(623,2,111,2,'2026-10-15 06:26:15'),(624,2,112,2,'2026-10-15 06:26:15'),(625,2,113,2,'2026-10-15 06:26:15'),(626,2,114,2,'2026-10-15 06:26:15'),(627,2,115,2,'2026-10-15 06:26:15'),(628,2,116,2,'2026-10-15 06:26:15'),(629,2,118,2,'2026-10-15 06:26:15'),(630,2,119,2,'2026-10-15 06:26:15'),(631,2,120,2,'2026-10-15 06:26:15'),(632,2,122,2,'2026-10-15 06:26:15'),(633,2,124,2,'2026-10-15 06:26:15'),(634,2,125,2,'2026-10-15 06:26:15'),(635,2,126,2,'2026-10-15 06:26:15'),(636,2,129,2,'2026-10-15 06:26:15'),(637,2,130,2,'2026-10-15 06:26:15'),(638,2,132,2,'2026-10-15 06:26:15'),(639,2,133,2,'2026-10-15 06:26:15'),(640,2,134,2,'2026-10-15 06:26:15'),(641,2,135,2,'2026-10-15 06:26:15'),(642,2,136,2,'2026-10-15 06:26:15'),(643,2,137,2,'2026-10-15 06:26:15'),(644,2,138,2,'2026-10-15 06:26:15'),(645,2,139,2,'2026-10-15 06:26:15'),(646,2,140,2,'2026-10-15 06:26:15'),(647,2,141,2,'2026-10-15 06:26:15'),(648,2,142,2,'2026-10-15 06:26:15'),(649,2,143,2,'2026-10-15 06:26:15'),(650,2,144,2,'2026-10-15 06:26:15'),(651,2,145,2,'2026-10-15 06:26:15'),(652,2,146,2,'2026-10-15 06:26:15'),(653,2,147,2,'2026-10-15 06:26:15'),(654,2,148,2,'2026-10-15 06:26:15'),(655,2,149,2,'2026-10-15 06:26:15'),(657,3,100,5,'2026-10-15 06:26:35'),(658,3,103,8,'2026-10-15 06:26:35'),(659,3,117,6,'2026-10-15 06:26:35'),(660,3,121,8,'2026-10-15 06:26:15'),(661,3,123,6,'2026-10-15 06:26:35'),(662,3,127,8,'2026-10-15 06:26:35'),(663,3,128,7,'2026-10-15 06:26:35'),(664,3,131,4,'2026-10-15 06:26:35'),(672,4,1,5,'2026-10-15 06:26:15'),(673,4,2,2,'2026-10-15 06:26:15'),(674,4,3,5,'2026-10-15 06:26:15'),(675,4,4,2,'2026-10-15 06:26:15'),(676,4,5,5,'2026-10-15 06:26:15'),(677,4,6,2,'2026-10-15 06:26:15'),(678,4,7,5,'2026-10-15 06:26:15'),(679,4,8,2,'2026-10-15 06:26:15'),(680,4,9,5,'2026-10-15 06:26:15'),(681,4,10,2,'2026-10-15 06:26:15'),(682,4,11,5,'2026-10-15 06:26:15'),(683,4,12,2,'2026-10-15 06:26:15'),(684,4,13,5,'2026-10-15 06:26:15'),(685,4,14,2,'2026-10-15 06:26:15'),(686,4,15,5,'2026-10-15 06:26:15'),(687,4,16,2,'2026-10-15 06:26:15'),(688,4,17,5,'2026-10-15 06:26:15'),(689,4,18,2,'2026-10-15 06:26:15'),(690,4,19,5,'2026-10-15 06:26:15'),(691,4,20,2,'2026-10-15 06:26:15'),(692,4,101,5,'2026-10-15 06:26:15'),(693,4,102,2,'2026-10-15 06:26:15'),(694,4,104,2,'2026-10-15 06:26:15'),(695,4,105,5,'2026-10-15 06:26:15'),(696,4,106,2,'2026-10-15 06:26:15'),(697,4,107,5,'2026-10-15 06:26:15'),(698,4,108,2,'2026-10-15 06:26:15'),(699,4,109,5,'2026-10-15 06:26:15'),(700,4,110,2,'2026-10-15 06:26:15'),(701,4,111,5,'2026-10-15 06:26:15'),(702,4,112,2,'2026-10-15 06:26:15'),(703,4,113,5,'2026-10-15 06:26:15'),(704,4,114,2,'2026-10-15 06:26:15'),(705,4,115,5,'2026-10-15 06:26:15'),(706,4,116,2,'2026-10-15 06:26:15'),(707,4,118,2,'2026-10-15 06:26:15'),(708,4,119,5,'2026-10-15 06:26:15'),(709,4,120,2,'2026-10-15 06:26:15'),(710,4,122,2,'2026-10-15 06:26:15'),(711,4,124,2,'2026-10-15 06:26:15'),(712,4,125,5,'2026-10-15 06:26:15'),(713,4,126,2,'2026-10-15 06:26:15'),(714,4,129,5,'2026-10-15 06:26:15'),(715,4,130,2,'2026-10-15 06:26:15'),(716,4,132,2,'2026-10-15 06:26:15'),(717,4,133,5,'2026-10-15 06:26:15'),(718,4,134,2,'2026-10-15 06:26:15'),(719,4,135,5,'2026-10-15 06:26:15'),(720,4,136,2,'2026-10-15 06:26:15'),(721,4,137,5,'2026-10-15 06:26:15'),(722,4,138,2,'2026-10-15 06:26:15'),(723,4,139,5,'2026-10-15 06:26:15'),(724,4,140,2,'2026-10-15 06:26:15'),(725,4,141,5,'2026-10-15 06:26:15'),(726,4,142,2,'2026-10-15 06:26:15'),(727,4,143,5,'2026-10-15 06:26:15'),(728,4,144,2,'2026-10-15 06:26:15'),(729,4,145,5,'2026-10-15 06:26:15'),(730,4,146,2,'2026-10-15 06:26:15'),(731,4,147,5,'2026-10-15 06:26:15'),(732,4,148,2,'2026-10-15 06:26:15'),(733,4,149,5,'2026-10-15 06:26:15'),(735,5,1,6,'2026-10-15 06:26:15'),(736,5,2,3,'2026-10-15 06:26:15'),(737,5,3,7,'2026-10-15 06:26:15'),(738,5,4,4,'2026-10-15 06:26:15'),(739,5,5,8,'2026-10-15 06:26:15'),(740,5,6,5,'2026-10-15 06:26:15'),(741,5,7,2,'2026-10-15 06:26:15'),(742,5,8,6,'2026-10-15 06:26:15'),(743,5,9,3,'2026-10-15 06:26:15'),(744,5,10,7,'2026-10-15 06:26:15'),(745,5,11,4,'2026-10-15 06:26:15'),(746,5,12,8,'2026-10-15 06:26:15'),(747,5,13,5,'2026-10-15 06:26:15'),(748,5,14,2,'2026-10-15 06:26:15'),(749,5,15,6,'2026-10-15 06:26:15'),(750,5,16,3,'2026-10-15 06:26:15'),(751,5,17,7,'2026-10-15 06:26:15'),(752,5,18,4,'2026-10-15 06:26:15'),(753,5,19,8,'2026-10-15 06:26:15'),(754,5,20,5,'2026-10-15 06:26:15'),(755,5,102,4,'2026-10-15 06:26:15'),(756,5,104,5,'2026-10-15 06:26:15'),(757,5,105,2,'2026-10-15 06:26:15'),(758,5,106,6,'2026-10-15 06:26:15'),(759,5,108,7,'2026-10-15 06:26:15'),(760,5,110,8,'2026-10-15 06:26:15'),(761,5,112,2,'2026-10-15 06:26:15'),(762,5,113,6,'2026-10-15 06:26:15'),(763,5,114,3,'2026-10-15 06:26:15'),(764,5,115,7,'2026-10-15 06:26:15'),(765,5,118,5,'2026-10-15 06:26:15'),(766,5,119,2,'2026-10-15 06:26:15'),(767,5,120,6,'2026-10-15 06:26:15'),(768,5,122,7,'2026-10-15 06:26:15'),(769,5,124,8,'2026-10-15 06:26:15'),(770,5,125,5,'2026-10-15 06:26:15'),(771,5,126,2,'2026-10-15 06:26:15'),(772,5,129,7,'2026-10-15 06:26:15'),(773,5,130,4,'2026-10-15 06:26:15'),(774,5,135,3,'2026-10-15 06:26:15'),(775,5,136,7,'2026-10-15 06:26:15'),(776,5,138,8,'2026-10-15 06:26:15'),(777,5,140,2,'2026-10-15 06:26:15'),(778,5,143,7,'2026-10-15 06:26:15'),(779,5,144,4,'2026-10-15 06:26:15'),(780,5,146,5,'2026-10-15 06:26:15'),(781,5,147,2,'2026-10-15 06:26:15'),(782,5,148,6,'2026-10-15 06:26:15'),(798,9,1,3,'2026-10-15 06:26:15'),(799,9,2,4,'2026-10-15 06:26:15'),(800,9,8,5,'2026-10-15 06:26:15'),(801,9,9,6,'2026-10-15 06:26:15'),(802,9,10,2,'2026-10-15 06:26:15'),(803,9,11,3,'2026-10-15 06:26:15'),(804,9,12,4,'2026-10-15 06:26:15'),(805,9,13,5,'2026-10-15 06:26:15'),(806,9,14,6,'2026-10-15 06:26:15'),(807,9,15,2,'2026-10-15 06:26:15'),(808,9,16,3,'2026-10-15 06:26:15'),(809,9,17,4,'2026-10-15 06:26:15'),(810,9,18,5,'2026-10-15 06:26:15'),(811,9,20,2,'2026-10-15 06:26:15'),(812,9,106,3,'2026-10-15 06:26:15'),(813,9,110,2,'2026-10-15 06:26:15'),(814,9,112,4,'2026-10-15 06:26:15'),(815,9,114,6,'2026-10-15 06:26:15'),(816,9,118,5,'2026-10-15 06:26:15'),(817,9,122,4,'2026-10-15 06:26:15'),(818,9,126,3,'2026-10-15 06:26:15'),(819,9,135,2,'2026-10-15 06:26:15'),(820,9,136,3,'2026-10-15 06:26:15'),(821,9,140,2,'2026-10-15 06:26:15'),(822,9,143,5,'2026-10-15 06:26:15'),(823,9,144,6,'2026-10-15 06:26:15'),(824,9,146,3,'2026-10-15 06:26:15'),(829,3,1,8,'2026-10-15 06:26:35'),(830,3,2,7,'2026-10-15 06:26:35'),(831,3,8,7,'2026-10-15 06:26:35'),(832,3,9,6,'2026-10-15 06:26:35'),(833,3,10,5,'2026-10-15 06:26:35'),(834,3,11,4,'2026-10-15 06:26:35'),(835,3,12,3,'2026-10-15 06:26:35'),(836,3,13,8,'2026-10-15 06:26:35'),(837,3,14,7,'2026-10-15 06:26:35'),(838,3,15,6,'2026-10-15 06:26:35'),(839,3,16,5,'2026-10-15 06:26:35'),(840,3,17,4,'2026-10-15 06:26:35'),(841,3,18,3,'2026-10-15 06:26:35'),(842,3,20,7,'2026-10-15 06:26:35'),(843,3,106,5,'2026-10-15 06:26:35'),(844,3,110,7,'2026-10-15 06:26:35'),(845,3,112,5,'2026-10-15 06:26:35'),(846,3,114,3,'2026-10-15 06:26:35'),(847,3,118,5,'2026-10-15 06:26:35'),(848,3,122,7,'2026-10-15 06:26:35'),(849,3,126,3,'2026-10-15 06:26:35'),(850,3,135,6,'2026-10-15 06:26:35'),(851,3,136,5,'2026-10-15 06:26:35'),(852,3,140,7,'2026-10-15 06:26:35'),(853,3,143,4,'2026-10-15 06:26:35'),(854,3,144,3,'2026-10-15 06:26:35'),(855,3,146,7,'2026-10-15 06:26:35');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_role` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('CONSIGNMENT','ORDER','STOCK_TRANSFER','STORE_REGISTRATION','SUPPORT_TICKET','SYSTEM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqqnsjxlwleyjbxlmm213jaj3f` (`recipient_id`),
  KEY `FKinagv63vd03tebhs0mpc9by4` (`store_id`),
  CONSTRAINT `FKinagv63vd03tebhs0mpc9by4` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `FKqqnsjxlwleyjbxlmm213jaj3f` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'2026-10-15 16:30:12.045020',_binary '','Đơn hàng #ORD56612030 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD56612030','Đặt hàng thành công','ORDER',5,1),(2,'2026-10-15 16:30:12.048863',_binary '\0','Có đơn hàng mới #ORD56612030 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/21','Đơn hàng mới tại chi nhánh','ORDER',2,1),(3,'2026-10-15 16:30:43.237147',_binary '','Đơn hàng #ORD56612030 đang trên đường giao tới bạn.','ROLE_USER','/orders/ORD56612030','Cập nhật đơn hàng #ORD56612030','ORDER',5,1),(4,'2026-10-15 16:31:49.380361',_binary '\0','Ban quản trị ReBook đã phản hồi yêu cầu về [Bảo hành ]: Da lien he va san sang doi sach 99% cho quy khach','ROLE_USER','/user/account/profile?tab=support','Phản hồi yêu cầu hỗ trợ #2','SUPPORT_TICKET',5,NULL),(5,'2026-10-28 11:20:59.815463',_binary '\0','Hồ sơ mở gian hàng \'Hiệu Sách Cũ Thủ Đức 1793161233\' đã được phê duyệt. Bạn đã là Quản lý chi nhánh!','ROLE_USER','/admin/inventory','Chúc mừng! Gian hàng đã được duyệt','STORE_REGISTRATION',14,NULL),(6,'2026-10-29 10:14:23.931588',_binary '','Đơn hàng #ORD243663861 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD243663861','Đặt hàng thành công','ORDER',4,1),(7,'2026-10-29 10:14:23.934086',_binary '\0','Có đơn hàng mới #ORD243663861 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/22','Đơn hàng mới tại chi nhánh','ORDER',2,1),(8,'2026-10-29 10:19:40.825568',_binary '\0','Hồ sơ mở gian hàng \'Tiệm Sách Cũ Bình Thạnh Xưa 1793243951\' đã được phê duyệt. Bạn đã là Quản lý chi nhánh!','ROLE_USER','/admin/inventory','Chúc mừng! Gian hàng đã được duyệt','STORE_REGISTRATION',15,NULL),(9,'2026-10-29 11:07:38.151646',_binary '\0','Đơn hàng #ORD246858089 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD246858089','Đặt hàng thành công','ORDER',8,1),(10,'2026-10-29 11:07:38.155129',_binary '\0','Có đơn hàng mới #ORD246858089 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/23','Đơn hàng mới tại chi nhánh','ORDER',2,1),(11,'2026-10-29 11:19:35.424400',_binary '\0','Đơn hàng #ORD247575412 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD247575412','Đặt hàng thành công','ORDER',4,1),(12,'2026-10-29 11:19:35.425889',_binary '\0','Có đơn hàng mới #ORD247575412 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/24','Đơn hàng mới tại chi nhánh','ORDER',2,1),(13,'2026-10-29 11:20:01.454246',_binary '\0','Đơn hàng #ORD247601445 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD247601445','Đặt hàng thành công','ORDER',4,1),(14,'2026-10-29 11:20:01.458251',_binary '\0','Có đơn hàng mới #ORD247601445 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/25','Đơn hàng mới tại chi nhánh','ORDER',2,1),(15,'2026-10-29 13:06:30.191088',_binary '\0','Đơn hàng #ORD253990160 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD253990160','Đặt hàng thành công','ORDER',4,1),(16,'2026-10-29 13:06:30.212876',_binary '\0','Có đơn hàng mới #ORD253990160 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/26','Đơn hàng mới tại chi nhánh','ORDER',2,1),(17,'2026-10-29 13:07:46.732806',_binary '\0','Đơn hàng #ORD254066722 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD254066722','Đặt hàng thành công','ORDER',4,1),(18,'2026-10-29 13:07:46.737762',_binary '\0','Có đơn hàng mới #ORD254066722 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/27','Đơn hàng mới tại chi nhánh','ORDER',2,1),(19,'2026-10-29 13:19:08.437689',_binary '\0','Đơn hàng #ORD254748408 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD254748408','Đặt hàng thành công','ORDER',1,1),(20,'2026-10-29 13:19:08.439282',_binary '\0','Có đơn hàng mới #ORD254748408 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/28','Đơn hàng mới tại chi nhánh','ORDER',2,1),(21,'2026-10-29 13:26:20.312466',_binary '\0','Đơn hàng #ORD254748408 đã được chi nhánh xác nhận và đang đóng gói.','ROLE_USER','/orders/ORD254748408','Cập nhật đơn hàng #ORD254748408','ORDER',1,1),(22,'2026-10-29 13:33:30.389176',_binary '\0','Đơn hàng #ORD255610343 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD255610343','Đặt hàng thành công','ORDER',1,1),(23,'2026-10-29 13:33:30.391162',_binary '\0','Có đơn hàng mới #ORD255610343 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/29','Đơn hàng mới tại chi nhánh','ORDER',2,1),(24,'2026-10-29 15:46:52.137587',_binary '\0','Đơn hàng #ORD263612116 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD263612116','Đặt hàng thành công','ORDER',4,1),(25,'2026-10-29 15:46:52.139474',_binary '\0','Có đơn hàng mới #ORD263612116 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/30','Đơn hàng mới tại chi nhánh','ORDER',2,1),(26,'2026-10-29 15:46:58.659422',_binary '','Đơn hàng #ORD263618649 đã được ghi nhận vào hệ thống ReBook. Chi nhánh đang chuẩn bị sách cho bạn.','ROLE_USER','/orders/ORD263618649','Đặt hàng thành công','ORDER',4,1),(27,'2026-10-29 15:46:58.660605',_binary '\0','Có đơn hàng mới #ORD263618649 (0 cuốn sách). Vui lòng xác nhận và đóng gói.','ROLE_MANAGER','/admin/orders/31','Đơn hàng mới tại chi nhánh','ORDER',2,1);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `book_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `condition_percent` int NOT NULL,
  `price` decimal(14,2) NOT NULL,
  `quantity` int NOT NULL,
  `total_price` decimal(14,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_items_orders` (`order_id`),
  KEY `fk_order_items_books` (`book_id`),
  CONSTRAINT `fk_order_items_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_items_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,'Số Đỏ (Bản in NXB Văn Học 1996)',85,45000.00,1,45000.00),(2,1,4,'Lập Trình Web Với Java & Spring (Giáo trình)',92,80000.00,1,80000.00),(3,2,6,'Đắc Nhân Tâm (Bản dịch Nguyễn Hiến Lê 1974)',80,150000.00,1,150000.00),(4,3,4,'Lập Trình Web Với Java & Spring (Giáo trình)',92,80000.00,1,80000.00),(5,4,2,'Nhà Giả Kim (Bản bìa mềm 2015)',95,45000.00,1,45000.00),(6,5,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(7,5,19,'Học Cách Mặc Kệ (The Let Them Theory)',98,98000.00,1,98000.00),(8,6,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(9,7,19,'Học Cách Mặc Kệ (The Let Them Theory)',98,98000.00,1,98000.00),(10,8,15,'Người Đàn Ông Mang Tên Ove',95,68000.00,48,3264000.00),(11,9,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,19,1292000.00),(12,10,2,'Nhà Giả Kim (The Alchemist)',90,45000.00,1,45000.00),(13,11,2,'Nhà Giả Kim (The Alchemist)',90,45000.00,1,45000.00),(14,12,18,'Đồi Gió Hú (Wuthering Heights)',85,58000.00,1,58000.00),(15,13,2,'Nhà Giả Kim (The Alchemist)',90,45000.00,1,45000.00),(16,14,2,'Nhà Giả Kim (The Alchemist)',90,45000.00,1,45000.00),(17,15,104,'Mắt Biếc',80,32000.00,100,3200000.00),(18,16,15,'Người Đàn Ông Mang Tên Ove',95,68000.00,1,68000.00),(19,17,15,'Người Đàn Ông Mang Tên Ove',95,68000.00,2,136000.00),(20,18,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(21,19,105,'Bắt Trẻ Đồng Xanh',90,58000.00,1,58000.00),(22,20,116,'Đọc Vị Bất Kỳ Ai',95,108000.00,1,108000.00),(23,21,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(24,22,105,'Bắt Trẻ Đồng Xanh',90,58000.00,1,58000.00),(25,23,104,'Mắt Biếc',80,32000.00,1,32000.00),(26,24,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(27,25,116,'Đọc Vị Bất Kỳ Ai',95,108000.00,1,108000.00),(28,26,116,'Đọc Vị Bất Kỳ Ai',95,108000.00,1,108000.00),(29,27,116,'Đọc Vị Bất Kỳ Ai',95,108000.00,1,108000.00),(30,28,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(31,29,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(32,30,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00),(33,31,1,'Yêu Những Điều Không Hoàn Hảo',95,68000.00,1,68000.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `shipping_unit_id` int DEFAULT NULL,
  `shipper_id` bigint DEFAULT NULL,
  `voucher_id` bigint DEFAULT NULL,
  `receiver_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `delivery_method` enum('HOME_DELIVERY','STORE_PICKUP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method` enum('BANK_TRANSFER','COD','MOMO','VNPAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` enum('PAID','REFUNDED','UNPAID') COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_status` enum('CANCELLED','CONFIRMED','DELIVERED','NEW','PICKED_UP','RETURNED','SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `subtotal` decimal(14,2) NOT NULL,
  `shipping_fee` decimal(12,2) DEFAULT '0.00',
  `discount_amount` decimal(12,2) DEFAULT '0.00',
  `final_amount` decimal(14,2) NOT NULL,
  `customer_notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_code` (`order_code`),
  KEY `fk_orders_users` (`user_id`),
  KEY `fk_orders_stores` (`store_id`),
  KEY `fk_orders_shipping` (`shipping_unit_id`),
  KEY `fk_orders_shipper` (`shipper_id`),
  KEY `fk_orders_voucher` (`voucher_id`),
  CONSTRAINT `fk_orders_shipper` FOREIGN KEY (`shipper_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_orders_shipping` FOREIGN KEY (`shipping_unit_id`) REFERENCES `shipping_units` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_orders_stores` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_orders_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_orders_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'ORD-20260920-001',8,1,1,6,1,'Nguyễn Song Hoàng Phúc','0934567890','Số 1 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức','HOME_DELIVERY','VNPAY','PAID','DELIVERED',125000.00,22000.00,20000.00,127000.00,'Giao giờ hành chính giúp em.','2026-09-20 02:15:00','2026-10-28 17:30:41'),(2,'ORD-20260921-002',4,1,2,6,NULL,'Nguyễn Song Hoàng Phúc','0934567890','125 Điện Biên Phủ, P. 25, Q. Bình Thạnh','HOME_DELIVERY','COD','UNPAID','RETURNED',150000.00,28000.00,0.00,178000.00,'Bọc chống sốc kỹ cuốn cổ này nhé shop. | [Lý do Shipper]: Khách hẹn giao lại ngày/giờ khác','2026-09-21 07:30:00','2026-10-11 15:07:08'),(3,'ORD-20260922-003',5,1,NULL,NULL,NULL,'Thái Duy Cường','0945678901','Nhận tại Chi Nhánh Thủ Đức','STORE_PICKUP','COD','UNPAID','NEW',80000.00,0.00,0.00,80000.00,'Chiều mai mình ghé quán nhận sách.','2026-09-22 03:00:00','2026-09-25 07:30:53'),(4,'ORD-20260922-004',5,1,1,6,NULL,'Thái Duy Cường','0945678901','Khu Công Nghệ Cao Q9, TP. Thủ Đức','HOME_DELIVERY','COD','UNPAID','RETURNED',45000.00,22000.00,0.00,67000.00,'Đã xác nhận đơn hàng chuẩn bị đóng gói. | [Lý do Shipper]: Khách không nghe máy sau 3 cuộc gọi','2026-09-22 09:45:00','2026-10-11 15:07:30'),(5,'ORD578360670',4,1,1,NULL,2,'Nguyễn Song Hoàng Phúc','0934567890','Số 1 Võ Văn Ngân, Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',166000.00,0.00,30000.00,136000.00,'','2026-09-28 06:52:41','2026-09-28 06:52:41'),(6,'ORD667291220',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',68000.00,22000.00,0.00,90000.00,'','2026-09-29 07:34:51','2026-09-29 07:34:51'),(7,'ORD839831492',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',98000.00,22000.00,0.00,120000.00,'','2026-10-01 07:30:31','2026-10-01 07:30:31'),(8,'ORD911420918',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',3264000.00,0.00,0.00,3264000.00,'','2026-10-02 03:23:41','2026-10-02 03:23:41'),(9,'ORD911839265',1,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',1292000.00,0.00,0.00,1292000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-02 03:30:39','2026-10-02 03:30:39'),(10,'ORD919854740',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',45000.00,22000.00,0.00,67000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-02 05:44:15','2026-10-02 05:44:15'),(11,'ORD919902081',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',45000.00,22000.00,0.00,67000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-02 05:45:02','2026-10-02 05:45:02'),(12,'ORD920031117',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',58000.00,22000.00,0.00,80000.00,'[Phân loại: Bản Sưu Tầm (99% Mới + Bookmark)]','2026-10-02 05:47:11','2026-10-02 05:47:11'),(13,'ORD920042880',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',45000.00,22000.00,0.00,67000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-02 05:47:23','2026-10-02 05:47:23'),(14,'ORD920050541',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',45000.00,22000.00,0.00,67000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-02 05:47:31','2026-10-02 05:47:31'),(15,'ORD922963163',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',3200000.00,0.00,0.00,3200000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-02 06:36:03','2026-10-02 06:36:03'),(16,'ORD183205778',4,1,1,NULL,2,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',68000.00,22000.00,30000.00,60000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-05 06:53:26','2026-10-05 06:53:26'),(17,'ORD190159375',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',136000.00,22000.00,0.00,158000.00,'','2026-10-05 08:49:19','2026-10-05 08:49:19'),(18,'ORD276232884',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',68000.00,22000.00,0.00,90000.00,'','2026-10-06 08:43:53','2026-10-06 08:43:53'),(19,'ORD952310564',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',58000.00,22000.00,0.00,80000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-14 04:31:51','2026-10-14 04:31:51'),(20,'ORD45815781',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','BANK_TRANSFER','PAID','CONFIRMED',108000.00,22000.00,0.00,130000.00,'[Phân loại: Bản Tiết Kiệm (85% Mới)]','2026-10-15 06:30:16','2026-10-15 06:34:20'),(21,'ORD56612030',5,1,1,NULL,NULL,'Thái Duy Cường','0945678901','Số 1 Võ Văn Ngân, TP. Thủ Đức','HOME_DELIVERY','COD','UNPAID','SHIPPING',68000.00,22000.00,0.00,90000.00,'Kiểm tra giao sách nhanh | [Lý do Shipper]: Da giao cho shipper','2026-10-15 09:30:12','2026-10-15 09:30:43'),(22,'ORD243663861',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','BANK_TRANSFER','UNPAID','NEW',58000.00,22000.00,0.00,80000.00,'','2026-10-29 03:14:24','2026-10-29 03:14:24'),(23,'ORD246858089',8,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0901234567','','HOME_DELIVERY','COD','UNPAID','NEW',32000.00,22000.00,0.00,54000.00,'','2026-10-29 04:07:38','2026-10-29 04:07:38'),(24,'ORD247575412',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',68000.00,22000.00,0.00,90000.00,'','2026-10-29 04:19:35','2026-10-29 04:19:35'),(25,'ORD247601445',4,1,1,NULL,NULL,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','VNPAY','UNPAID','NEW',108000.00,22000.00,0.00,130000.00,'','2026-10-29 04:20:01','2026-10-29 04:20:01'),(26,'ORD253990160',4,1,2,NULL,1,'Hoàng Phúc (Nhà riêng)','0934567890','125 Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP. Hồ Chí Minh','HOME_DELIVERY','BANK_TRANSFER','UNPAID','CANCELLED',108000.00,28000.00,20000.00,116000.00,'','2026-10-29 06:06:30','2026-10-29 06:07:39'),(27,'ORD254066722',4,1,1,NULL,NULL,'Hoàng Phúc (Nhà riêng)','0934567890','125 Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',108000.00,22000.00,0.00,130000.00,'','2026-10-29 06:07:47','2026-10-29 06:07:47'),(28,'ORD254748408',1,1,1,NULL,NULL,'Quản Trị Viên Hệ Thống','0901234567','Số 1 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','COD','PAID','CONFIRMED',68000.00,22000.00,0.00,90000.00,'','2026-10-29 06:19:08','2026-10-29 06:26:20'),(29,'ORD255610343',1,1,1,NULL,NULL,'Quản Trị Viên Hệ Thống','0901234567','Số 1 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','MOMO','UNPAID','NEW',68000.00,22000.00,0.00,90000.00,'','2026-10-29 06:33:30','2026-10-29 06:33:30'),(30,'ORD263612116',4,1,3,NULL,2,'Hoàng Phúc (Nhà riêng)','0934567890','125 Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP. Hồ Chí Minh','HOME_DELIVERY','COD','UNPAID','NEW',68000.00,25000.00,30000.00,63000.00,'','2026-10-29 08:46:52','2026-10-29 08:46:52'),(31,'ORD263618649',4,1,1,NULL,2,'Nguyễn Song Hoàng Phúc','0912345678','Số 48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','HOME_DELIVERY','BANK_TRANSFER','UNPAID','CANCELLED',68000.00,22000.00,30000.00,60000.00,'','2026-10-29 08:46:59','2026-10-29 08:52:30');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otp_tokens`
--

DROP TABLE IF EXISTS `otp_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `otp_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_type` enum('CHANGE_PASSWORD','FORGOT_PASSWORD','REGISTER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` timestamp NOT NULL,
  `is_used` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_tokens`
--

LOCK TABLES `otp_tokens` WRITE;
/*!40000 ALTER TABLE `otp_tokens` DISABLE KEYS */;
INSERT INTO `otp_tokens` VALUES (11,'nguyensonghoangphuc0125@gmail.com','335690','FORGOT_PASSWORD','2026-09-29 13:41:28',0,'2026-09-29 13:36:28'),(12,'nguyensonghoangphuc0125@gmail.com','661626','FORGOT_PASSWORD','2026-09-29 13:48:48',0,'2026-09-29 13:43:48'),(13,'nguyensonghoangphuc0125@gmail.com','989321','FORGOT_PASSWORD','2026-09-29 13:55:28',0,'2026-09-29 13:50:28'),(15,'nguyensonghoangphuc@gmail.com','902750','REGISTER','2026-09-29 14:21:58',0,'2026-09-29 14:16:58'),(16,'nguyensonghoangphuc@gmail.com','358331','REGISTER','2026-09-29 14:23:09',1,'2026-09-29 14:18:09'),(17,'phuc@student.hcmute.edu.vn','450832','CHANGE_PASSWORD','2026-10-01 08:19:38',0,'2026-10-01 08:14:38'),(18,'phuc@student.hcmute.edu.vn','364071','CHANGE_PASSWORD','2026-10-01 08:20:10',1,'2026-10-01 08:15:10'),(19,'phuc@student.hcmute.edu.vn','347680','CHANGE_PASSWORD','2026-10-01 08:20:14',1,'2026-10-01 08:15:14'),(20,'muoipc.spa4@gmail.com','765805','REGISTER','2026-10-29 07:50:42',0,'2026-10-29 07:45:42'),(21,'nguyensonghoangphuc@gmail.com','469188','REGISTER','2026-10-29 08:41:30',0,'2026-10-29 08:36:30'),(22,'nguyensonghoangphuc@gmail.com','532069','REGISTER','2026-10-29 09:00:09',0,'2026-10-29 08:55:09'),(23,'nguyensonghoangphuc@gmail.com','592693','REGISTER','2026-10-29 09:01:37',0,'2026-10-29 08:56:37'),(24,'nguyensonghoangphuc@gmail.com','308340','REGISTER','2026-10-29 09:02:06',1,'2026-10-29 08:57:06'),(25,'nguyensonghoangphuc@gmail.com','771465','FORGOT_PASSWORD','2026-10-29 09:04:01',0,'2026-10-29 08:59:01'),(26,'nguyensonghoangphuc@gmail.com','220603','FORGOT_PASSWORD','2026-10-29 09:04:02',0,'2026-10-29 08:59:02');
/*!40000 ALTER TABLE `otp_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `rating` int NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_type` enum('IMAGE','NONE','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_reviews_users` (`user_id`),
  KEY `fk_reviews_books` (`book_id`),
  KEY `fk_reviews_orders` (`order_id`),
  CONSTRAINT `fk_reviews_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,4,1,1,5,'Sách cũ nhưng được bảo quản rất cẩn thận, bìa tuy có ố nhẹ đúng chất thời gian năm 1996 nhưng các trang bên trong hoàn toàn không bị mối mọt, chữ in rõ nét, đọc rất thích!','https://res.cloudinary.com/demo/image/upload/v1/samples/books/review-sodo-real.jpg','IMAGE','2026-09-25 07:30:53'),(2,4,4,1,5,'Giáo trình Lập trình Web của trường bản này còn rất mới, các phần ghi chú của anh chị khóa trước để lại rất hữu ích cho kỳ thi cuối kỳ môn này. Đóng gói rất chắc chắn!','https://res.cloudinary.com/demo/image/upload/v1/samples/books/review-java-real.jpg','IMAGE','2026-09-25 07:30:53'),(3,4,15,NULL,5,'hi',NULL,'NONE','2026-09-25 07:32:45'),(4,4,15,NULL,5,'sách ổn','/images/reviews/review_1790322160725_e9199182.jpeg','IMAGE','2026-09-25 07:42:46'),(5,4,2,NULL,5,'Bản in Nhà Giả Kim bìa mềm rất đẹp, các trang giấy vàng ngà đọc êm mắt, sách được bọc màng co cẩn thận!',NULL,'NONE','2026-10-05 07:56:23'),(6,4,15,NULL,5,'sản phẩm này tối ;alsìd;aknsdnẳon9winônnondôíijfọdội','/images/reviews/review_1793253496489_bf03e2de.png','IMAGE','2026-10-29 05:58:27'),(7,4,15,NULL,5,'Cuốn sách cũ được đóng gói rất cẩn thận, các trang giấy còn rất sạch sẽ và nguyên vẹn. Gáy sách phẳng, chất lượng vượt mong đợi của mình!',NULL,'NONE','2026-10-29 05:59:02'),(8,4,19,NULL,5,'Cuốn sách cũ được đóng gói rất cẩn thận, các trang giấy còn rất sạch sẽ và nguyên vẹn. Gáy sách phẳng, chất lượng vượt mong đợi của mình!','/images/reviews/review_1793263857006_7a29b709.png','IMAGE','2026-10-29 08:50:59');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ROLE_ADMIN','Quản trị viên toàn hệ thống'),(2,'ROLE_MANAGER','Quản lý chi nhánh cửa hàng sách'),(3,'ROLE_USER','Khách hàng mua sách và ký gửi'),(4,'ROLE_SHIPPER','Nhân viên giao nhận đơn hàng'),(5,'ROLE_VENDOR','Đối tác bán hàng / Chủ gian hàng');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipping_units`
--

DROP TABLE IF EXISTS `shipping_units`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipping_units` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `base_fee` decimal(12,2) DEFAULT '25000.00',
  `estimated_days` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '2-3 ngày',
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipping_units`
--

LOCK TABLES `shipping_units` WRITE;
/*!40000 ALTER TABLE `shipping_units` DISABLE KEYS */;
INSERT INTO `shipping_units` VALUES (1,'Giao Hàng Tiết Kiệm (GHTK)','GHTK','18006092',22000.00,'1-2 ngày',1),(2,'Giao Hàng Nhanh (GHN Express)','GHN','1900636677',28000.00,'Trong ngày hoặc 24h',1),(3,'Viettel Post','VIETTEL','19008095',25000.00,'2-3 ngày',1);
/*!40000 ALTER TABLE `shipping_units` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_transfers`
--

DROP TABLE IF EXISTS `stock_transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transfers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_feedback` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int NOT NULL,
  `status` enum('APPROVED','CANCELLED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `transfer_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `approved_by_id` bigint DEFAULT NULL,
  `book_id` bigint NOT NULL,
  `from_store_id` bigint NOT NULL,
  `requested_by_id` bigint NOT NULL,
  `to_store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKel3lrfowfromgwcvxlve9xvow` (`transfer_code`),
  KEY `FK66lj3md62kup4e54oqjgd4f6j` (`approved_by_id`),
  KEY `FKl1kif70iuduhpbrlgapvkg3g8` (`book_id`),
  KEY `FKhie1nkgu03ig472r1fwa9svw2` (`from_store_id`),
  KEY `FKa9fhurp2hnqm1b28v4ncb4yl1` (`requested_by_id`),
  KEY `FKfxtjn7rcfoxwnh3w0s9o854ok` (`to_store_id`),
  CONSTRAINT `FK66lj3md62kup4e54oqjgd4f6j` FOREIGN KEY (`approved_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKa9fhurp2hnqm1b28v4ncb4yl1` FOREIGN KEY (`requested_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfxtjn7rcfoxwnh3w0s9o854ok` FOREIGN KEY (`to_store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `FKhie1nkgu03ig472r1fwa9svw2` FOREIGN KEY (`from_store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `FKl1kif70iuduhpbrlgapvkg3g8` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_transfers`
--

LOCK TABLES `stock_transfers` WRITE;
/*!40000 ALTER TABLE `stock_transfers` DISABLE KEYS */;
INSERT INTO `stock_transfers` VALUES (1,NULL,NULL,'2026-10-29 10:20:50.108859','',1,'PENDING','TRF1793244050107',NULL,5,2,1,3);
/*!40000 ALTER TABLE `stock_transfers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_followers`
--

DROP TABLE IF EXISTS `store_followers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_followers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_store_follow` (`user_id`,`store_id`),
  KEY `fk_sf_store` (`store_id`),
  CONSTRAINT `fk_sf_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sf_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_followers`
--

LOCK TABLES `store_followers` WRITE;
/*!40000 ALTER TABLE `store_followers` DISABLE KEYS */;
INSERT INTO `store_followers` VALUES (2,4,2,'2026-10-26 04:24:28'),(3,5,1,'2026-10-26 04:24:28'),(12,4,1,'2026-10-29 08:48:09');
/*!40000 ALTER TABLE `store_followers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_registrations`
--

DROP TABLE IF EXISTS `store_registrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_registrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bank_account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bank_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `close_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `district` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `open_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `owner_id_card` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `owner_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `store_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp87cwx6luygjw0jxp9i6wwtg1` (`user_id`),
  CONSTRAINT `FKp87cwx6luygjw0jxp9i6wwtg1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_registrations`
--

LOCK TABLES `store_registrations` WRITE;
/*!40000 ALTER TABLE `store_registrations` DISABLE KEYS */;
INSERT INTO `store_registrations` VALUES (1,'123 Quang Trung, Phường 10','0123456789','Vietcombank','21:30','2026-10-13 22:46:22.069926','Chuyên giáo trình ĐH SPKT, sách văn học cổ và truyện tranh.','Quận Gò Vấp','phuc@student.hcmute.edu.vn','/images/hero-books-vintage.jpg','08:00','079204001234','Nguyễn Hoàng Phúc','0909123456','TP. Hồ Chí Minh',NULL,'APPROVED','Tiệm Sách Cũ Gò Vấp','2026-10-13 22:46:44.983990',4),(2,'45 Võ Văn Ngân, Linh Chiểu','999888777666','Vietcombank','21:30','2026-10-28 11:20:33.832960','','TP. Thủ Đức',NULL,'/images/hero-books-vintage.jpg','08:00','079204001999','Nguyễn Văn Test','0988776655','TP. Hồ Chí Minh',NULL,'APPROVED','Hiệu Sách Cũ Thủ Đức 1793161233','2026-10-28 11:20:59.819781',14),(3,'120 Bạch Đằng, P.24','10123456789','Vietcombank','21:30','2026-10-29 10:19:11.693817','','Quận Bình Thạnh','','/images/hero-books-vintage.jpg','08:00','079204008888','Trần Văn Bình','0908889999','TP. Hồ Chí Minh',NULL,'APPROVED','Tiệm Sách Cũ Bình Thạnh Xưa 1793243951','2026-10-29 10:19:40.831158',15);
/*!40000 ALTER TABLE `store_registrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stores`
--

DROP TABLE IF EXISTS `stores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `manager_id` bigint DEFAULT NULL,
  `store_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(220) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `open_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '08:00',
  `close_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '22:00',
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `commission_rate` decimal(5,2) DEFAULT NULL,
  `ai_reply_enabled` tinyint(1) DEFAULT '0',
  `ai_assistant_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'Trợ lý quầy sách',
  `ai_tone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'FRIENDLY',
  `ai_shop_description` text COLLATE utf8mb4_unicode_ci,
  `ai_custom_faq` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `fk_stores_manager` (`manager_id`),
  CONSTRAINT `fk_stores_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stores`
--

LOCK TABLES `stores` WRITE;
/*!40000 ALTER TABLE `stores` DISABLE KEYS */;
INSERT INTO `stores` VALUES (1,2,'Tổng Kho Sách Cũ TP.HCM','ba-tan-sach-cu-thu-vien-thu-duc','0962.936.310','lebatanth@gmail.com','48 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh','TP. Hồ Chí Minh','TP. Hồ Chí Minh','08:00','21:30','https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800',1,'2026-09-25 07:30:53',NULL,1,'Bé Mọt Thủ Đức','FRIENDLY','Tổng kho trung tâm gần ĐH Sư phạm Kỹ thuật HCMUTE, có bàn đọc sách miễn phí và bãi đỗ xe máy rộng rãi.','Dạ quầy mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí. Khách mua trên 3 cuốn luôn được tặng bookmark da kỷ niệm ạ!'),(2,3,'Chi Nhánh 2: Đường Sách Quận 1','chi-nhanh-quan-1','02838229988','quan1@oldbookstore.vn','Đường Sách Nguyễn Văn Bình, Phường Bến Nghé','TP. Hồ Chí Minh','Quận 1','08:30','22:00','https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800',1,'2026-10-07 08:05:36',NULL,1,'Cậu Ba Đường Sách','VINTAGE','Chi nhánh không gian hoài cổ giữa lòng Đường Sách Nguyễn Văn Bình, chuyên sách văn học kinh điển và bản in trước 1975.','Dạ quầy mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí. Khách mua trên 3 cuốn luôn được tặng bookmark da kỷ niệm ạ!'),(3,11,'Chi Nhánh 3: Làng Đại Học Quốc Gia','chi-nhanh-lang-dai-hoc','02837244555','langdaihoc@oldbookstore.vn','Khu Đô Thị ĐHQG-HCM, Phường Linh Trung','TP. Hồ Chí Minh','TP. Thủ Đức','07:30','21:30','https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800',1,'2026-10-07 08:05:36',NULL,1,'Mọt Làng Đại Học','ENERGETIC','Quầy phục vụ sinh viên KĐT ĐHQG TP.HCM, ưu đãi giáo trình, sách ngoại ngữ và tài liệu kỹ thuật.','Dạ quầy mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí. Khách mua trên 3 cuốn luôn được tặng bookmark da kỷ niệm ạ!'),(4,12,'Chi Nhánh 4: Phố Sách Cũ Phú Nhuận','chi-nhanh-phu-nhuan','02838447766','phunhuan@oldbookstore.vn','Số 120 Trần Huy Liệu, Phường 15','TP. Hồ Chí Minh','Quận Phú Nhuận','08:00','21:00','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=800',1,'2026-10-07 08:05:36',NULL,1,'Cố Nhân Phú Nhuận','VINTAGE','Quầy sách phố Trần Huy Liệu lâu năm, chuyên sách khảo cứu, triết học và văn hóa phương Đông.','Dạ quầy mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí. Khách mua trên 3 cuốn luôn được tặng bookmark da kỷ niệm ạ!'),(5,13,'Chi Nhánh 5: Khu Học Thuật Quận 5','chi-nhanh-quan-5','02838351122','quan5@oldbookstore.vn','Số 280 An Dương Vương, Phường 4','TP. Hồ Chí Minh','Quận 5','08:00','21:30','https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800',1,'2026-10-07 08:05:36',NULL,1,'Thầy Giáo Quận 5','INTELLECTUAL','Quầy học thuật gần ĐH Sư phạm và ĐH Sài Gòn, chuyên sách tâm lý giáo dục và khoa học xã hội.','Dạ quầy mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí. Khách mua trên 3 cuốn luôn được tặng bookmark da kỷ niệm ạ!'),(9,4,'Tiệm Sách Cũ Gò Vấp','tiem-sach-cu-go-vap','0909123456','phuc@student.hcmute.edu.vn','123 Quang Trung, Phường 10','TP. Hồ Chí Minh','Quận Gò Vấp','08:00','21:30','/images/hero-books-vintage.jpg',1,'2026-10-13 15:46:45',10.00,0,'Trợ lý quầy sách','FRIENDLY','Cửa hàng sách cũ uy tín tại TP.HCM.','Dạ quầy mở cửa từ 08:00 đến 21:30 hàng ngày, có bãi đậu xe máy miễn phí. Khách mua trên 3 cuốn luôn được tặng bookmark da kỷ niệm ạ!'),(10,14,'Hiệu Sách Cũ Thủ Đức 1793161233','hieu-sach-cu-thu-duc-1793161233','0988776655','seller_1793161233@rebook.local','45 Võ Văn Ngân, Linh Chiểu','TP. Hồ Chí Minh','TP. Thủ Đức','08:00','21:30','/images/hero-books-vintage.jpg',1,'2026-10-28 04:21:00',10.00,0,'Trợ lý quầy sách','FRIENDLY',NULL,NULL),(11,15,'Tiệm Sách Cũ Bình Thạnh Xưa 1793243951','tiem-sach-cu-binh-thanh-xua-1793243951','0908889999','','120 Bạch Đằng, P.24','TP. Hồ Chí Minh','Quận Bình Thạnh','08:00','21:30','/images/hero-books-vintage.jpg',1,'2026-10-29 03:19:41',10.00,0,'Trợ lý quầy sách','FRIENDLY',NULL,NULL);
/*!40000 ALTER TABLE `stores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `support_tickets`
--

DROP TABLE IF EXISTS `support_tickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `support_tickets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('IN_PROGRESS','OPEN','RESOLVED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `admin_reply` text COLLATE utf8mb4_unicode_ci,
  `resolved_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_support_tickets_user` (`user_id`),
  CONSTRAINT `fk_support_tickets_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `support_tickets`
--

LOCK TABLES `support_tickets` WRITE;
/*!40000 ALTER TABLE `support_tickets` DISABLE KEYS */;
INSERT INTO `support_tickets` VALUES (1,'Vấn đề đơn hàng','2026-10-15 13:31:54.039964','hoang.test@gmail.com','Nguyễn Văn Hoàng','Tôi muốn đổi sách do sách giao nhầm chi nhánh kho Thủ Đức.','ORD20261014','0912345678','RESOLVED','Dạ ReBook đã xác nhận đổi cuốn mới cùng tình trạng độ mới 95% cho bạn tại Chi nhánh Thủ Đức theo chính sách đổi trả 7 ngày ạ.','2026-10-29 00:36:29.000000',8),(2,'Bảo hành ','2026-10-15 16:31:12.518323','cuong@student.hcmute.edu.vn','Thái Duy Cường','Sách có vết ố nhẹ ở trang 20, tôi muốn hỏi cách bảo quản hoặc đổi cuốn khác.','ORD56612030','0945678901','RESOLVED','Chào bạn Phúc, ReBook đã kiểm tra đơn hàng ORD-20260920-001 và điều phối Shipper giao bổ sung quà tặng kèm cho bạn trong hôm nay ạ.','2026-10-29 00:36:29.000000',8),(3,'Đơn hàng & Giao nhận','2026-10-28 13:06:18.427708','test@cskh.vn','Khách hàng Test CSKH','Cần tư vấn thời gian giao sách cũ tới Thủ Đức','ORD-TEST-001','0901234567','OPEN',NULL,NULL,8),(14,'Tư vấn đơn hàng','2026-10-29 15:51:57.310947','abc@gmail.com','phuc','hàng lỗi','ádfdfsá','13423','OPEN',NULL,NULL,4);
/*!40000 ALTER TABLE `support_tickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vouchers`
--

DROP TABLE IF EXISTS `user_vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `voucher_id` bigint NOT NULL,
  `is_used` tinyint(1) NOT NULL DEFAULT '0',
  `saved_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_voucher` (`user_id`,`voucher_id`),
  KEY `fk_user_vouchers_voucher` (`voucher_id`),
  CONSTRAINT `fk_user_vouchers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_vouchers_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vouchers`
--

LOCK TABLES `user_vouchers` WRITE;
/*!40000 ALTER TABLE `user_vouchers` DISABLE KEYS */;
INSERT INTO `user_vouchers` VALUES (1,4,1,1,'2026-09-15 10:00:00','2026-10-29 13:06:30'),(2,4,4,0,'2026-09-21 08:30:00',NULL),(3,4,7,0,'2026-09-22 14:20:00',NULL),(4,5,6,0,'2026-09-22 11:00:00',NULL),(5,5,9,0,'2026-09-23 09:00:00',NULL),(6,4,2,1,'2026-09-29 14:28:59','2026-10-29 15:46:59'),(7,4,5,0,'2026-09-29 14:29:00',NULL);
/*!40000 ALTER TABLE `user_vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `loyalty_points` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_users_roles` (`role_id`),
  CONSTRAINT `fk_users_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,1,'admin','admin@oldbookstore.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Quản Trị Viên Hệ Thống','0901234567','https://res.cloudinary.com/demo/image/upload/v1/samples/people/smiling-man.jpg',1,'2026-09-25 07:30:53','2026-10-05 04:24:06',0),(2,2,'manager_td','thu_duc@oldbookstore.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Quản Lý CN Thủ Đức','0912345678','/images/avatars/avatar-default.png',1,'2026-09-25 07:30:53','2026-09-25 07:30:53',0),(3,2,'manager_q1','quan1@oldbookstore.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Quản Lý CN Quận 1','0923456789','/images/avatars/avatar-default.png',1,'2026-09-25 07:30:53','2026-09-25 07:30:53',0),(4,3,'phuc_customer','phuc@student.hcmute.edu.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Nguyễn Song Hoàng Phúc','0934567890','/images/avatars/avatar_4_1793253892315.png',1,'2026-09-25 07:30:53','2026-10-29 06:04:52',0),(5,3,'cuong_customer','cuong@student.hcmute.edu.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Thái Duy Cường','0945678901','/images/avatars/avatar-cuong.png',1,'2026-09-25 07:30:53','2026-09-25 07:30:53',0),(6,4,'shipper_hung','shipper_hung@oldbookstore.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Trần Văn Hùng (Shipper)','0956789012','/images/avatars/avatar-default.png',1,'2026-09-25 07:30:53','2026-09-25 07:30:53',0),(8,3,'phuc0125','nguyensonghoangphuc0125@gmail.com','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Nguyễn Song Hoàng Phúc','0901234567',NULL,1,'2026-09-29 13:36:11','2026-10-28 17:12:17',0),(10,2,'manager_q7','minhthu.q7@rebook.vn','$2a$10$kR10wEqqLzTLAqIonVNfWubbPBIS67elizDEU9Ongd5bDMY85Ufb.','Nguyễn Thị Minh Thư','0912345678',NULL,1,'2026-10-12 03:43:02','2026-10-12 03:43:56',0),(11,2,'manager_dhqg','dhqg@rebook.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Quản Lý CN Làng ĐHQG','0901234503',NULL,1,'2026-10-13 14:34:16','2026-10-13 14:34:16',0),(12,2,'manager_pn','phunhuan@rebook.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Quản Lý CN Phú Nhuận','0901234504',NULL,1,'2026-10-13 14:34:16','2026-10-13 14:34:16',0),(13,2,'manager_q5','quan5@rebook.vn','$2a$10$Whn3DmaeYo7HxBDwnmX8BOB20XUJ14LXC/rACz8BAE1phMFcAM03i','Quản Lý CN Quận 5','0901234505',NULL,1,'2026-10-13 14:34:16','2026-10-13 14:34:16',0),(14,2,'seller_1793161233','seller_1793161233@rebook.local','$2a$10$wUSpKoQeG9rDKbzOWm8UGe6VdQB/MdSboxj9ISBk12RruySXD1dMm','Nguyễn Văn Test','0988776655',NULL,1,'2026-10-28 04:20:34','2026-10-28 04:21:00',0),(15,5,'binhthanh_1793243951','binhthanh_1793243951@rebook.local','$2a$10$RtLft2/YZJ4lK5sGwhowpuckLxv9ipUiGYlT6FkhUDsXbv37x87My','Trần Văn Bình','0908889999',NULL,1,'2026-10-29 03:19:12','2026-10-29 03:19:41',0),(24,3,'PHuvc','nguyensonghoangphuc@gmail.com','$2a$10$8h7vnSmO7gQDcL8GEBqPkeMw6BEQALs3iwXEQa1dXTBuVppKGP/SW','phuc gu','230792505',NULL,1,'2026-10-29 08:56:37','2026-10-29 08:58:30',0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `viewed_books`
--

DROP TABLE IF EXISTS `viewed_books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `viewed_books` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `viewed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book_viewed` (`user_id`,`book_id`),
  KEY `fk_viewed_books` (`book_id`),
  CONSTRAINT `fk_viewed_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_viewed_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `viewed_books`
--

LOCK TABLES `viewed_books` WRITE;
/*!40000 ALTER TABLE `viewed_books` DISABLE KEYS */;
INSERT INTO `viewed_books` VALUES (1,4,1,'2026-09-25 07:30:53'),(2,4,2,'2026-09-25 07:30:53'),(3,4,4,'2026-09-25 07:30:53'),(4,4,6,'2026-09-25 07:30:53'),(5,5,3,'2026-09-25 07:30:53'),(6,5,4,'2026-09-25 07:30:53'),(7,5,7,'2026-09-25 07:30:53');
/*!40000 ALTER TABLE `viewed_books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `discount_type` enum('FIXED_AMOUNT','FREE_SHIPPING','PERCENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_value` decimal(12,2) NOT NULL,
  `min_order_amount` decimal(14,2) DEFAULT '0.00',
  `max_discount` decimal(14,2) DEFAULT '0.00',
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `usage_limit` int DEFAULT '100',
  `used_count` int DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES (1,'SACHCU20K','Giảm trực tiếp 20.000đ cho đơn từ 100.000đ','FIXED_AMOUNT',20000.00,100000.00,20000.00,'2026-09-01 00:00:00','2026-11-30 23:59:59',200,16,1),(2,'FREESHIP50K','Giảm 100% phí vận chuyển cho đơn sách cũ từ 200.000đ','FIXED_AMOUNT',30000.00,20000.00,30000.00,'2026-09-01 00:00:00','2026-11-30 23:59:59',100,32,1),(3,'HOISINHSACH10','Giảm 10% tổng giá trị đơn hàng mừng ngày hội sách','PERCENT',10.00,150000.00,50000.00,'2026-09-01 00:00:00','2026-12-31 23:59:59',500,42,1),(4,'FREESHIP15K','Giảm 15.000đ phí vận chuyển cho đơn hàng sách cũ từ 99.000đ','FIXED_AMOUNT',15000.00,99000.00,15000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',500,0,1),(5,'FREESHIP30K','Miễn phí giao hàng tối đa 30.000đ cho đơn từ 250.000đ toàn quốc','FIXED_AMOUNT',30000.00,250000.00,30000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',300,0,1),(6,'SACHCU50K','Giảm trực tiếp 50.000đ cho đơn hàng mua sách cũ từ 300.000đ','FIXED_AMOUNT',50000.00,30000.00,50000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',200,0,1),(7,'HOISINH20','Ưu đãi tái sinh tri thức: Giảm 20% tối đa 80.000đ cho đơn từ 150.000đ','PERCENT',20.00,150000.00,80000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',150,0,1),(8,'TRIANVIP','Tri ân bạn đọc thân thiết: Giảm 15% tối đa 100.000đ cho đơn từ 200.000đ','PERCENT',15.00,200000.00,100000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',100,0,1),(9,'CHAOBANMOI','Món quà độc giả mới: Giảm ngay 25.000đ cho đơn hàng đầu tiên từ 80.000đ','FIXED_AMOUNT',25000.00,80000.00,25000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',1000,0,1),(10,'YEUSACH10','Giảm 10% giá trị đơn hàng cho cộng đồng yêu sách cũ từ 100.000đ','PERCENT',10.00,100000.00,50000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',500,0,1),(11,'MEGA100K','Đại tiệc sách cũ: Giảm sốc 100.000đ cho đơn hàng giá trị cao từ 500.000đ','FIXED_AMOUNT',100000.00,500000.00,100000.00,'2026-01-01 00:00:00','2026-12-31 23:59:59',50,0,1);
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlists`
--

DROP TABLE IF EXISTS `wishlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book_wishlist` (`user_id`,`book_id`),
  KEY `fk_wishlists_books` (`book_id`),
  CONSTRAINT `fk_wishlists_books` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wishlists_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlists`
--

LOCK TABLES `wishlists` WRITE;
/*!40000 ALTER TABLE `wishlists` DISABLE KEYS */;
INSERT INTO `wishlists` VALUES (1,4,3,'2026-09-25 07:30:53'),(2,4,5,'2026-09-25 07:30:53'),(3,4,6,'2026-09-25 07:30:53'),(4,5,1,'2026-09-25 07:30:53'),(5,5,4,'2026-09-25 07:30:53'),(12,8,1,'2026-10-28 17:36:29'),(13,8,3,'2026-10-28 17:36:29'),(14,8,15,'2026-10-28 17:36:29'),(15,8,104,'2026-10-28 17:36:29'),(18,4,1,'2026-10-29 08:45:08');
/*!40000 ALTER TABLE `wishlists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'old_book_store_db'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-30  7:55:07
