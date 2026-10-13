# Kế Hoạch Triển Khai: Nâng Cấp Toàn Diện Hệ Thống Icon Sang Chuẩn 3D Claymorphism

## 1. Goal
Thay thế toàn bộ hệ thống icon danh mục thể loại sách trên trang chủ (`image_abd685.png`) sang phong cách **3D Claymorphism** thuần khiết (hình khối đất sét bo tròn căng mọng, chất liệu nhám lì matte, ánh sáng studio mềm mại, màu sắc pastel ấm áp đồng bộ nhận diện ReBook), đảm bảo độ sắc nét cao (Retina 256x256 @2x) và hiển thị mượt mà trên mọi thiết bị.

---

## 2. Current Context & Assumptions
- **Hiện trạng tồn tại qua phân tích trực quan `image_abd685.png`:**
  - Bộ 6 icon danh mục hiện tại (`van-hoc.png`, `cntt.png`, `kinh-te.png`, `ky-nang.png`, `ngoai-ngu.png`, `lich-su.png`) có phong cách đồ họa chưa đồng nhất: icon quả địa cầu là khung lưới rỗng (wireframe), laptop có bề mặt phẳng 2D, đền thờ lịch sử nhiều cạnh thô cứng, thiếu cảm giác phồng dẻo đặc trưng của đất sét 3D (claymorphism).
  - Thanh danh mục trên `home.html` hiện hiển thị 9 mục: 6 mục trên kèm 3 mục bổ trợ (`giao-trinh.png`, `truyen-tranh.png`, `sach-moi.png`).
- **Nơi lưu trữ asset:**
  - Mã nguồn: `src/main/resources/static/images/categories/`
  - Thư mục runtime Tomcat: `target/classes/static/images/categories/`
  - Định dạng chuẩn: PNG 256x256 pixels, kênh trong suốt RGBA 32-bit, tối ưu khử răng cưa Lanczos 4x.
- **Tiêu chuẩn mỹ thuật (Art Direction Standard - 3D Claymorphism):**
  - *Hình khối (Geometry):* Bo tròn tối đa mọi góc cạnh (chubby, pillowy volumes), không có góc nhọn hay nét mảnh dưới 4px.
  - *Ánh sáng (Lighting):* Key light từ góc trên-trái tạo khối nổi sâu, Fill light ấm từ góc dưới-phải, Ambient occlusion mềm mại tại các khe tiếp giáp giữa các khối.
  - *Chất liệu (Material):* Nhám lì (Matte Clay), độ phản quang mở rộng dịu mắt, bóng đổ tiếp xúc (contact shadow) mờ nhạt bên dưới chân icon.
  - *Bảng màu (Palette):* Màu ấm áp pastel kết hợp các điểm nhấn thương hiệu ReBook (Cam san hô `#FF6036`, Tím lavender `#8B5CF6`, Xanh ngọc mint `#10B981`, Vàng hổ phách `#F59E0B`, Xanh dương sky `#0EA5E9`).

---

## 3. Architecture & Proposed Approach
- **Kỹ thuật sinh ảnh (3D Clay Synthesis Engine):**
  - Xây dựng script Python chuyên dụng `scripts/generate_3d_categories.py` sử dụng thuật toán trường khoảng cách định hướng (Signed Distance Fields - SDF) kết hợp mô hình chiếu sáng Phong / Blinn-Phong và đổ bóng mềm (Soft Ambient Occlusion).
  - Render hình ảnh ở độ phân giải siêu nét $1024 \times 1024$ rồi nén mịn (supersampling antialiasing) về chuẩn $256 \times 256$ giúp icon sắc nét tuyệt đối, không vỡ hạt hay răng cưa trên màn hình Retina.
- **Tái thiết kế chi tiết 9 Biểu tượng Thể loại:**
  1. `van-hoc.png` (Văn Học): 3 cuốn sách đất sét bo tròn xếp chồng so le (Hồng phấn - Xanh mint - Cam san hô).
  2. `cntt.png` (CNTT & Kỹ Thuật): Chiếc laptop khối đất sét tròn phồng màu xanh Cyan, màn hình dập nổi ký hiệu mã nguồn `</>` màu trắng sữa.
  3. `kinh-te.png` (Kinh Tế & QTKD): Biểu đồ cột 3D tăng trưởng dốc lên kết hợp đồng xu vàng đất sét dập nổi dấu tiền tệ `₫`.
  4. `ky-nang.png` (Kỹ Năng Sống): Chậu đất tròn màu đất nung với mầm cây 2 lá xanh mọng nước mọc vươn lên.
  5. `ngoai-ngu.png` (Ngoại Ngữ): Quả địa cầu 3D khối đặc tròn trịa màu xanh pastel kết hợp bong bóng thoại chữ `A` xinh xắn.
  6. `lich-su.png` (Lịch Sử & Văn Hóa): Cuộn thư tịch cổ 3D cuộn tròn thắt dải ruy băng đỏ mềm mại kèm ngòi bút lông vũ.
  7. `giao-trinh.png` (Giáo Trình ĐH): Quyển đại từ điển dày bo tròn kèm chiếc mũ cử nhân tốt nghiệp màu xanh navy.
  8. `truyen-tranh.png` (Truyện Tranh): Bong bóng thoại sấm sét vui nhộn / mặt cười biểu tượng Manga đáng yêu.
  9. `sach-moi.png` (Tất Cả Thể Loại): Chiếc hộp kho báu tri thức 3D mở nắp tỏa ánh sáng vàng sao ấm.

---

## 4. Step-by-Step Implementation Tasks

### Task 1: Xây dựng Script Sinh Asset 3D Claymorphism (`scripts/generate_3d_categories.py`)
- **Tệp tạo mới:** `scripts/generate_3d_categories.py`
- **Mục tiêu:** Sinh toàn bộ 9 file ảnh PNG $256 \times 256$ RGBA với ánh sáng studio và chất liệu đất sét mờ.
- **Mã nguồn thực thi:**
```python
import os
import numpy as np
from PIL import Image, ImageFilter

OUTPUT_DIR = 'src/main/resources/static/images/categories'
os.makedirs(OUTPUT_DIR, exist_ok=True)
SIZE = 1024
y_grid, x_grid = np.mgrid[:SIZE, :SIZE].astype(np.float32)

# Vector ánh sáng chính (Góc trên - trái)
light = np.array([-0.45, -0.65, 0.60], dtype=np.float32)
light /= np.linalg.norm(light)

def create_base_canvas():
    return np.zeros((SIZE, SIZE, 4), dtype=np.uint8)

def save_icon(canvas, filename):
    img = Image.fromarray(canvas, mode='RGBA')
    # Contact shadow mềm dưới đáy
    shadow_layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    s_draw = np.zeros((SIZE, SIZE), dtype=np.uint8)
    s_dist = ((x_grid - 512.0)/320.0)**2 + ((y_grid - 840.0)/80.0)**2
    s_draw[s_dist <= 1.0] = 75
    s_img = Image.fromarray(s_draw, mode='L').filter(ImageFilter.GaussianBlur(32))
    shadow_layer.putalpha(s_img)
    
    final = Image.alpha_composite(shadow_layer, img)
    final_256 = final.resize((256, 256), Image.Resampling.LANCZOS)
    out_path = os.path.join(OUTPUT_DIR, filename)
    final_256.save(out_path)
    print(f"Generated {filename}")

# Sẽ định nghĩa các hàm vẽ cho 9 icon theo SDF và Phong Shading
```
- **Lệnh xác thực:**
```bash
python3 scripts/generate_3d_categories.py
ls -lh src/main/resources/static/images/categories/
```
- **Kết quả kỳ vọng:** Đầy đủ 9 tệp ảnh PNG dung lượng từ 25KB - 45KB với nền trong suốt hoàn hảo.

---

### Task 2: Sinh Đợt 1 — 6 Icon Thể Loại Cốt Lõi Khớp `image_abd685.png`
- **Tệp chỉnh sửa / tạo:**
  - `src/main/resources/static/images/categories/van-hoc.png`
  - `src/main/resources/static/images/categories/cntt.png`
  - `src/main/resources/static/images/categories/kinh-te.png`
  - `src/main/resources/static/images/categories/ky-nang.png`
  - `src/main/resources/static/images/categories/ngoai-ngu.png`
  - `src/main/resources/static/images/categories/lich-su.png`
- **Chi tiết kỹ thuật từng hình:**
  1. *van-hoc.png:* Vẽ 3 khối dẹt bo góc lớn xếp nghiêng góc 15°, bìa sách chuyển màu pastel `#F472B6`, `#34D399`, `#FB923C`.
  2. *cntt.png:* Khối đế bàn phím vát tròn dập rãnh trackpad, màn hình nghiêng 30° màu `#38BDF8` có ký hiệu `</>`.
  3. *kinh-te.png:* 3 cột trụ đất sét bậc thang xanh ngọc `#10B981`, mũi tên dốc lên màu cam, đồng xu vàng tròn `#FBBF24`.
  4. *ky-nang.png:* Chậu tròn phồng màu nâu đất `#A16207`, 2 lá mầm dày tròn trịa màu xanh lục `#22C55E`.
  5. *ngoai-ngu.png:* Quả cầu đất sét tròn đặc màu xanh da trời `#60A5FA`, các mảng lục địa xanh lá bo tròn, bong bóng thoại chữ `A`.
  6. *lich-su.png:* Cuộn giấy cổ màu ngà `#FEF3C7` cuộn tròn 2 đầu, dải nơ đỏ `#EF4444`, ngòi bút lông vũ màu nâu da bò `#8C4A27`.
- **Lệnh thực thi:**
```bash
python3 -c "import scripts.generate_3d_categories as gen; gen.build_core_six()"
```

---

### Task 3: Sinh Đợt 2 — 3 Icon Thể Loại Bổ Trợ & Tiện Ích Dịch Vụ
- **Tệp chỉnh sửa / tạo:**
  - `src/main/resources/static/images/categories/giao-trinh.png`
  - `src/main/resources/static/images/categories/truyen-tranh.png`
  - `src/main/resources/static/images/categories/sach-moi.png`
  - `src/main/resources/static/images/categories/ky-gui.png` (Dịch vụ ký gửi)
- **Chi tiết kỹ thuật:**
  1. *giao-trinh.png:* Sách giáo trình xanh đậm dập gáy chỉ, phía trên đặt mũ cử nhân vuông bo tròn chùm tua rua vàng.
  2. *truyen-tranh.png:* Khung truyện tranh Manga bong bóng thoại viền múp míp, hiệu ứng mắt cười anime 3D.
  3. *sach-moi.png:* Hộp rương báu tri thức bo tròn mở hé nắp phát sáng ấm.
  4. *ky-gui.png:* Chiếc hộp carton bưu phẩm đất sét dán băng keo tròn kèm nhãn dán ReBook.
- **Lệnh thực thi:**
```bash
python3 -c "import scripts.generate_3d_categories as gen; gen.build_supplementary()"
```

---

### Task 4: Tinh Chỉnh CSS Thẻ Danh Mục Trên Trang Chủ (`home.html` & `liquid-glass.css`)
- **Tệp chỉnh sửa:**
  - `src/main/resources/templates/home.html` (dòng 1635-1650)
  - `src/main/resources/static/css/liquid-glass.css`
- **Mục tiêu:** Nâng cấp khung hiển thị icon `.chotot-cat-icon-box` và `.chotot-cat-3d-img` để tôn nổi hiệu ứng 3D khối:
  - Tăng kích thước hiển thị từ `38px` lên `44px` để chi tiết đất sét nổi rõ.
  - Thêm hiệu ứng nhún nhảy mềm (spring hover animation): `transform: translateY(-4px) scale(1.10) rotate(-2deg);`.
  - Bộ lọc ánh sáng: `filter: drop-shadow(0 6px 12px rgba(140, 74, 39, 0.16));`.
- **Mã nguồn CSS tinh chỉnh:**
```css
/* TỐI ƯU HIỂN THỊ ICON 3D CLAYMORPHISM */
.chotot-cat-icon-box {
    width: 52px;
    height: 52px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(236, 231, 222, 0.9);
    box-shadow: 0 4px 14px rgba(45, 24, 15, 0.04), inset 0 1px 1px #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 6px auto;
    transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
}
.chotot-cat-item:hover .chotot-cat-icon-box {
    background: #FFFFFF;
    border-color: #8C4A27;
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(140, 74, 39, 0.14), inset 0 1px 1px #FFFFFF;
}
.chotot-cat-3d-img {
    width: 42px;
    height: 42px;
    object-fit: contain;
    filter: drop-shadow(0 4px 8px rgba(140, 74, 39, 0.12));
    transition: transform 0.28s cubic-bezier(0.16, 1, 0.3, 1);
    image-rendering: -webkit-optimize-contrast;
}
.chotot-cat-item:hover .chotot-cat-3d-img {
    transform: scale(1.12) rotate(-3deg);
    filter: drop-shadow(0 6px 14px rgba(140, 74, 39, 0.22));
}
```

---

### Task 5: Đồng Bộ Hóa Asset & Hot-Sync Vào Target Classes
- **Mục tiêu:** Đồng bộ toàn bộ ảnh PNG và template vào `target/classes/` để máy chủ Spring Boot (Tomcat port 8080) phục vụ tức thì mà không cần khởi động lại server.
- **Lệnh thực thi:**
```bash
cp -R src/main/resources/static/images/categories/* target/classes/static/images/categories/
cp src/main/resources/templates/home.html target/classes/templates/home.html
cp src/main/resources/static/css/liquid-glass.css target/classes/static/css/liquid-glass.css
```
- **Kiểm tra phản hồi mạng:**
```bash
curl -s -I http://localhost:8080/images/categories/van-hoc.png | head -n 5
curl -s -I http://localhost:8080/images/categories/cntt.png | head -n 5
```
- **Kết quả kỳ vọng:** HTTP/1.1 200 OK.

---

### Task 6: Kiểm Thử Trực Quan Visual & Đối Soát Vision
- **Kịch bản kiểm thử Playwright:**
  - Điều hướng tới `http://localhost:8080/`.
  - Cuộn đến thanh danh mục `.chotot-category-section`.
  - Chụp ảnh full resolution `categories_3d_claymorphism_new.png`.
  - Di chuột hover vào mục "Văn Học" và "CNTT & Kỹ Thuật" để ghi lại trạng thái tương tác động.
- **Xác thực qua `vision_analyze`:**
  - Đối chiếu trực tiếp với `image_abd685.png`.
  - Đảm bảo 100% các icon đạt chuẩn: hình khối căng tròn không sắc nhọn, chất liệu đất sét nhám mờ, màu sắc rạng rỡ và sắc nét từng chi tiết.

---

### Task 7: Chạy Toàn Bộ Test Suite & Đóng Gói Git Commit
- **Lệnh thực thi:**
```bash
mvn -f "/Users/phucdeptrai/Library/CloudStorage/GoogleDrive-nguyensonghoangphuc0125@gmail.com/My Drive/Docu/Lap trinh web/doan_cuoiky/DoAn_QuanLyChuoiSachCu/pom.xml" test
git add .
git commit -m "feat(ui): nang cap toan bo he thong icon danh muc the loai sang chuan 3D Claymorphism"
git push origin feature/client-phuc
```
- **Kết quả kỳ vọng:**
  - 7/7 unit tests passed (VoucherServiceTest, ReportServiceBranchTest, OrderServiceShipperTest).
  - Git working tree sạch sẽ, commit đã được đẩy lên GitHub remote.

---

## 5. Tests & Validation Plan
1. **Kiểm tra toàn vẹn Asset (Asset Integrity Check):**
   - Đảm bảo 9 tệp ảnh có kích thước đúng $256 \times 256$, mode `RGBA`, không có pixel rác viền biên.
2. **Kiểm tra độ tương thích trình duyệt (Visual Playwright Check):**
   - Chụp viewport desktop ($1280 \times 800$) và mobile ($390 \times 844$) để đảm bảo các thẻ icon co giãn linh hoạt, không bị vỡ bố cục hay tràn dòng.
3. **Kiểm thử hồi quy tự động:**
   - Chạy lệnh `mvn test` xác thực không có lỗi biên dịch hay xung đột logic hệ thống.

---

## 6. Risks, Tradeoffs & Open Questions
- **Rủi ro dung lượng ảnh (Asset Payload):**
  - *Giải pháp:* Nén ảnh bằng bộ lọc Lanczos kết hợp nén PNG mức cao, giữ dung lượng mỗi tệp dưới 45KB để đảm bảo trang chủ tải nhanh dưới 200ms.
- **Tương thích phong cách hoài cổ (Style Cohesion):**
  - *Giải pháp:* Giữ độ bão hòa màu vừa phải (soft warm pastel), tránh các màu neon quá chói làm mất đi vẻ sang trọng, trang nhã của thương hiệu tiệm sách cũ ReBook.
