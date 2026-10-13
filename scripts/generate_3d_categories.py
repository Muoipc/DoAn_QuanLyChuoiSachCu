import os
import numpy as np
from PIL import Image, ImageFilter

OUTPUT_DIR = '/Users/phucdeptrai/Library/CloudStorage/GoogleDrive-nguyensonghoangphuc0125@gmail.com/My Drive/Docu/Lap trinh web/doan_cuoiky/DoAn_QuanLyChuoiSachCu/src/main/resources/static/images/categories'
os.makedirs(OUTPUT_DIR, exist_ok=True)

SIZE = 1024
y_grid, x_grid = np.mgrid[:SIZE, :SIZE].astype(np.float32)

# Lighting vectors
light_dir = np.array([-0.45, -0.65, 0.60], dtype=np.float32)
light_dir /= np.linalg.norm(light_dir)

fill_dir = np.array([0.40, 0.45, 0.80], dtype=np.float32)
fill_dir /= np.linalg.norm(fill_dir)

def save_clay_icon(canvas_rgba, filename, shadow_y=840.0, shadow_rx=320.0, shadow_ry=80.0):
    img = Image.fromarray(canvas_rgba, mode='RGBA')
    
    # Soft contact shadow underneath
    s_dist = ((x_grid - 512.0) / shadow_rx)**2 + ((y_grid - shadow_y) / shadow_ry)**2
    shadow_mask = np.zeros((SIZE, SIZE), dtype=np.uint8)
    shadow_mask[s_dist <= 1.0] = 85
    s_img = Image.fromarray(shadow_mask, mode='L').filter(ImageFilter.GaussianBlur(36))
    
    shadow_layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    shadow_layer.putalpha(s_img)
    
    combined = Image.alpha_composite(shadow_layer, img)
    final_256 = combined.resize((256, 256), Image.Resampling.LANCZOS)
    out_path = os.path.join(OUTPUT_DIR, filename)
    final_256.save(out_path)
    print(f"Rendered: {filename}")

def shade_ellipsoid(cx, cy, rx, ry, rz, base_color, spec_power=10, spec_weight=0.30):
    d = ((x_grid - cx)/rx)**2 + ((y_grid - cy)/ry)**2
    mask = d <= 1.0
    z = np.zeros((SIZE, SIZE), dtype=np.float32)
    z[mask] = np.sqrt(np.maximum(0.0, 1.0 - d[mask])) * rz
    
    nx = np.zeros((SIZE, SIZE), dtype=np.float32)
    ny = np.zeros((SIZE, SIZE), dtype=np.float32)
    nz = np.ones((SIZE, SIZE), dtype=np.float32)
    nx[mask] = (x_grid[mask] - cx) / (rx * rx)
    ny[mask] = (y_grid[mask] - cy) / (ry * ry)
    nz[mask] = z[mask] / (rz * rz + 1e-4)
    nl = np.sqrt(nx**2 + ny**2 + nz**2 + 1e-5)
    nx, ny, nz = nx/nl, ny/nl, nz/nl
    
    diff = np.maximum(0.0, nx*light_dir[0] + ny*light_dir[1] + nz*light_dir[2])
    fill = np.maximum(0.0, nx*fill_dir[0] + ny*fill_dir[1] + nz*fill_dir[2])
    spec = np.maximum(0.0, nz)**spec_power * spec_weight
    
    col = np.zeros((SIZE, SIZE, 3), dtype=np.float32)
    for c in range(3):
        col[:, :, c] = base_color[c] * (0.42 + 0.62*diff + 0.16*fill) + 255.0*spec
    return mask, col

# ================= 1. VĂN HỌC (3 CUỐN SÁCH CÓ GÁY RÕ RÀNG & GIẤY TRẮNG) =================
def render_van_hoc():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    def render_single_book(cx, cy, color):
        mb, cb = shade_ellipsoid(cx, cy, 320, 85, 90, color)
        # Pages along right side
        mp = (((x_grid - (cx + 120))/160)**2 + ((y_grid - cy)/45)**2 <= 1.0) & mb
        cb[mp] = np.array([255, 252, 240])
        # Spine highlight along left
        ms = (((x_grid - (cx - 240))/55)**2 + ((y_grid - cy)/75)**2 <= 1.0) & mb
        cb[ms] = np.clip(color * 1.25, 0, 255)
        return mb, cb

    m3, c3 = render_single_book(512, 690, np.array([245, 96, 52])) # Bottom Coral
    m2, c2 = render_single_book(490, 540, np.array([16, 185, 129])) # Middle Mint
    m1, c1 = render_single_book(525, 390, np.array([244, 114, 182])) # Top Rose Pink

    for m, c in [(m3, c3), (m2, c2), (m1, c1)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'van-hoc.png')

# ================= 2. CNTT & KỸ THUẬT (LAPTOP RÕ BÀN PHÍM VÀ MÀN HÌNH CODE </>) =================
def render_cntt():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    # Base (Cyan laptop base)
    mb, cb = shade_ellipsoid(512, 690, 340, 90, 80, np.array([14, 165, 233]))
    # Keyboard cutout
    mk = (((x_grid - 512)/240)**2 + ((y_grid - 685)/40)**2 <= 1.0) & mb
    cb[mk] = np.array([3, 105, 161]) # Darker cyan
    
    # Screen Frame (Navy Blue)
    ms, cs = shade_ellipsoid(512, 420, 300, 210, 110, np.array([30, 41, 59]))
    # Glowing screen interior
    mi, ci = shade_ellipsoid(512, 415, 250, 160, 70, np.array([56, 189, 248]))
    
    # Clear White Code Marks: < / >
    c1 = (np.abs((x_grid - 430) + np.abs(y_grid - 415)*0.8) <= 14) & (y_grid > 355) & (y_grid < 475) & (x_grid < 450)
    c2 = (np.abs((x_grid - 512) + (y_grid - 415)*0.6) <= 14) & (y_grid > 350) & (y_grid < 480)
    c3 = (np.abs((x_grid - 594) - np.abs(y_grid - 415)*0.8) <= 14) & (y_grid > 355) & (y_grid < 475) & (x_grid > 574)
    
    code_mask = (c1 | c2 | c3) & mi
    ci[code_mask] = np.array([255, 255, 255])
    
    for m, c in [(mb, cb), (ms, cs), (mi, ci)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'cntt.png')

# ================= 3. KINH TẾ & QTKD (BIỂU ĐỒ TĂNG TRƯỞNG & ĐỒNG XU VÀNG) =================
def render_kinh_te():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    m1, c1 = shade_ellipsoid(320, 620, 95, 140, 90, np.array([168, 85, 247]))
    m2, c2 = shade_ellipsoid(480, 520, 95, 200, 90, np.array([59, 130, 246]))
    m3, c3 = shade_ellipsoid(640, 410, 95, 260, 90, np.array([16, 185, 129]))
    
    # Golden Coin
    m_coin, c_coin = shade_ellipsoid(700, 290, 130, 130, 110, np.array([245, 158, 11]), spec_weight=0.45)
    d_mark = (((x_grid - 700)/55)**2 + ((y_grid - 290)/55)**2 <= 1.0) & m_coin
    c_coin[d_mark] = np.array([254, 240, 138]) # Light gold
    
    for m, c in [(m1, c1), (m2, c2), (m3, c3), (m_coin, c_coin)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'kinh-te.png')

# ================= 4. KỸ NĂNG SỐNG (CHẬU ĐẤT NUNG & MẦM CÂY XANH) =================
def render_ky_nang():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    mp, cp = shade_ellipsoid(512, 680, 250, 140, 120, np.array([217, 119, 6]))
    me, ce = shade_ellipsoid(512, 580, 220, 65, 80, np.array([120, 53, 15]))
    mst, cst = shade_ellipsoid(512, 460, 38, 130, 50, np.array([34, 197, 94]))
    ml, cl = shade_ellipsoid(390, 370, 120, 85, 75, np.array([74, 222, 128]))
    mr, cr = shade_ellipsoid(634, 370, 120, 85, 75, np.array([34, 197, 94]))
    
    for m, c in [(mp, cp), (me, ce), (mst, cst), (ml, cl), (mr, cr)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'ky-nang.png')

# ================= 5. NGOẠI NGỮ (ĐỊA CẦU TO TRÒN & THOẠI CHỮ 'A') =================
def render_ngoai_ngu():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    mb, cb = shade_ellipsoid(512, 750, 200, 65, 60, np.array([217, 119, 6]))
    mp, cp = shade_ellipsoid(512, 660, 35, 90, 45, np.array([245, 158, 11]))
    mg, cg = shade_ellipsoid(512, 440, 260, 260, 240, np.array([96, 165, 250]))
    ml, cl = shade_ellipsoid(450, 420, 130, 140, 150, np.array([52, 211, 153]))
    
    ms, cs = shade_ellipsoid(690, 290, 130, 105, 90, np.array([255, 255, 255]))
    a_shape = (np.abs(np.abs(x_grid - 690)*1.4 + (y_grid - 290)) <= 20) & (y_grid > 240) & (y_grid < 340)
    a_bar = (np.abs(y_grid - 305) <= 10) & (np.abs(x_grid - 690) <= 35)
    cs[(a_shape | a_bar) & ms] = np.array([238, 77, 45])
    
    for m, c in [(mb, cb), (mp, cp), (mg, cg), (ml, cl), (ms, cs)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'ngoai-ngu.png')

# ================= 6. LỊCH SỬ & VĂN HÓA (CUỘN THƯ TỊCH LỚN & RUY BĂNG ĐỎ) =================
def render_lich_su():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    m1, c1 = shade_ellipsoid(512, 650, 310, 85, 100, np.array([254, 243, 199]))
    m2, c2 = shade_ellipsoid(512, 400, 290, 85, 100, np.array([254, 243, 199]))
    mb, cb = shade_ellipsoid(512, 525, 270, 140, 80, np.array([253, 230, 138]))
    mr, cr = shade_ellipsoid(512, 525, 75, 135, 95, np.array([239, 68, 68]))
    mq, cq = shade_ellipsoid(710, 340, 50, 180, 70, np.array([217, 119, 6]))
    
    for m, c in [(m1, c1), (m2, c2), (mb, cb), (mr, cr), (mq, cq)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'lich-su.png')

# ================= 7. GIÁO TRÌNH ĐH (SÁCH DÀY & MŨ CỬ NHÂN TỐT NGHIỆP) =================
def render_giao_trinh():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    mb, cb = shade_ellipsoid(512, 660, 310, 100, 100, np.array([30, 58, 138]))
    mp, cp = shade_ellipsoid(512, 580, 280, 70, 85, np.array([255, 252, 235]))
    mc, cc = shade_ellipsoid(512, 410, 290, 100, 95, np.array([15, 23, 42]))
    mt, ct = shade_ellipsoid(670, 440, 35, 90, 45, np.array([245, 158, 11]))
    
    for m, c in [(mb, cb), (mp, cp), (mc, cc), (mt, ct)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'giao-trinh.png')

# ================= 8. TRUYỆN TRANH (MANGA BUBBLE 3D VUI NHỘN) =================
def render_truyen_tranh():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    mb, cb = shade_ellipsoid(480, 500, 280, 240, 190, np.array([251, 191, 36]))
    ms, cs = shade_ellipsoid(700, 340, 105, 105, 90, np.array([249, 115, 22]))
    
    e1 = (((x_grid - 410)/28)**2 + ((y_grid - 490)/38)**2 <= 1.0)
    e2 = (((x_grid - 550)/28)**2 + ((y_grid - 490)/38)**2 <= 1.0)
    cb[e1 | e2] = np.array([30, 41, 59])
    
    canvas[mb, :3] = np.clip(cb[mb], 0, 255).astype(np.uint8)
    canvas[mb, 3] = 255
    canvas[ms, :3] = np.clip(cs[ms], 0, 255).astype(np.uint8)
    canvas[ms, 3] = 255
    save_clay_icon(canvas, 'truyen-tranh.png')

# ================= 9. TẤT CẢ THỂ LOẠI (RƯƠNG BÁU TRI THỨC TOẢNG ÁNH SÁNG) =================
def render_sach_moi():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)
    
    mc, cc = shade_ellipsoid(512, 640, 300, 160, 130, np.array([180, 83, 9]))
    mr, cr = shade_ellipsoid(512, 530, 280, 55, 65, np.array([245, 158, 11]))
    m1, c1 = shade_ellipsoid(420, 430, 120, 95, 80, np.array([244, 63, 94]))
    m2, c2 = shade_ellipsoid(600, 430, 120, 95, 80, np.array([16, 185, 129]))
    ms, cs = shade_ellipsoid(512, 320, 85, 85, 75, np.array([251, 191, 36]))
    
    for m, c in [(mc, cc), (mr, cr), (m1, c1), (m2, c2), (ms, cs)]:
        canvas[m, :3] = np.clip(c[m], 0, 255).astype(np.uint8)
        canvas[m, 3] = 255
    save_clay_icon(canvas, 'sach-moi.png')

if __name__ == '__main__':
    print("Re-rendering high-detail 3D Claymorphism icons...")
    render_van_hoc()
    render_cntt()
    render_kinh_te()
    render_ky_nang()
    render_ngoai_ngu()
    render_lich_su()
    render_giao_trinh()
    render_truyen_tranh()
    render_sach_moi()
    print("Done!")
