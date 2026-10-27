import os
import numpy as np
from PIL import Image

OUTPUT_DIR = '/Users/phucdeptrai/Library/CloudStorage/GoogleDrive-nguyensonghoangphuc0125@gmail.com/My Drive/Docu/Lap trinh web/doan_cuoiky/DoAn_QuanLyChuoiSachCu/src/main/resources/static/images/categories'
os.makedirs(OUTPUT_DIR, exist_ok=True)

SIZE = 1024
y_grid, x_grid = np.mgrid[:SIZE, :SIZE].astype(np.float32)

# Top-Right-Front studio light
light_dir = np.array([0.45, -0.65, 0.60], dtype=np.float32)
light_dir /= np.linalg.norm(light_dir)

# Bottom-Left soft ambient fill
fill_dir = np.array([-0.40, 0.50, 0.75], dtype=np.float32)
fill_dir /= np.linalg.norm(fill_dir)

def render_clay_chat():
    canvas = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)

    # ================= 1. BACK BUBBLE (Cyan / Soft Indigo #6366F1) =================
    # Position: top-right, center=(620, 390), rx=250, ry=195, rz=160
    cx2, cy2 = 610.0, 390.0
    rx2, ry2, rz2 = 250.0, 195.0, 160.0

    d2 = ((x_grid - cx2)/rx2)**2 + ((y_grid - cy2)/ry2)**2

    # Smooth tail 2 pointing right-down
    t2_dist = ((x_grid - 780.0)/95.0)**2 + ((y_grid - 530.0)/95.0)**2
    in_tail2 = (t2_dist <= 1.0) & (x_grid > 640.0) & (y_grid > 400.0)

    mask2 = (d2 <= 1.0) | in_tail2

    z2 = np.zeros((SIZE, SIZE), dtype=np.float32)
    z2[d2 <= 1.0] = np.sqrt(np.maximum(0.0, 1.0 - d2[d2 <= 1.0])) * rz2
    z2[in_tail2] = np.maximum(z2[in_tail2], np.sqrt(np.maximum(0.0, 1.0 - t2_dist[in_tail2])) * 95.0)

    # Normals 2
    nx2 = np.zeros((SIZE, SIZE), dtype=np.float32)
    ny2 = np.zeros((SIZE, SIZE), dtype=np.float32)
    nz2 = np.ones((SIZE, SIZE), dtype=np.float32)
    nx2[mask2] = (x_grid[mask2] - cx2) / (rx2 * rx2)
    ny2[mask2] = (y_grid[mask2] - cy2) / (ry2 * ry2)
    nz2[mask2] = z2[mask2] / (rz2 * rz2 + 1e-4)
    nl2 = np.sqrt(nx2**2 + ny2**2 + nz2**2 + 1e-5)
    nx2, ny2, nz2 = nx2/nl2, ny2/nl2, nz2/nl2

    # Lighting 2: Soft matte diffuse + subtle rim
    diff2 = np.maximum(0.0, nx2*light_dir[0] + ny2*light_dir[1] + nz2*light_dir[2])
    fill2 = np.maximum(0.0, nx2*fill_dir[0] + ny2*fill_dir[1] + nz2*fill_dir[2])
    spec2 = np.maximum(0.0, nz2)**8 * 0.25

    base2 = np.array([99, 102, 241], dtype=np.float32) # Soft Indigo/Cyan
    col2 = np.zeros((SIZE, SIZE, 3), dtype=np.float32)
    for c in range(3):
        col2[:, :, c] = base2[c] * (0.42 + 0.65*diff2 + 0.16*fill2) + 255.0*spec2

    # ================= 2. FRONT BUBBLE (Warm Coral Orange #FF5A36) =================
    # Position: bottom-left, center=(410, 560), rx=285, ry=225, rz=180
    cx1, cy1 = 410.0, 560.0
    rx1, ry1, rz1 = 285.0, 225.0, 180.0

    d1 = ((x_grid - cx1)/rx1)**2 + ((y_grid - cy1)/ry1)**2

    # Smooth tail 1 pointing left-down
    t1_dist = ((x_grid - 250.0)/105.0)**2 + ((y_grid - 740.0)/105.0)**2
    in_tail1 = (t1_dist <= 1.0) & (x_grid < 360.0) & (y_grid > 610.0)

    mask1 = (d1 <= 1.0) | in_tail1

    z1 = np.zeros((SIZE, SIZE), dtype=np.float32)
    z1[d1 <= 1.0] = np.sqrt(np.maximum(0.0, 1.0 - d1[d1 <= 1.0])) * rz1
    z1[in_tail1] = np.maximum(z1[in_tail1], np.sqrt(np.maximum(0.0, 1.0 - t1_dist[in_tail1])) * 115.0)

    # Normals 1
    nx1 = np.zeros((SIZE, SIZE), dtype=np.float32)
    ny1 = np.zeros((SIZE, SIZE), dtype=np.float32)
    nz1 = np.ones((SIZE, SIZE), dtype=np.float32)
    nx1[mask1] = (x_grid[mask1] - cx1) / (rx1 * rx1)
    ny1[mask1] = (y_grid[mask1] - cy1) / (ry1 * ry1)
    nz1[mask1] = z1[mask1] / (rz1 * rz1 + 1e-4)
    nl1 = np.sqrt(nx1**2 + ny1**2 + nz1**2 + 1e-5)
    nx1, ny1, nz1 = nx1/nl1, ny1/nl1, nz1/nl1

    # Lighting 1
    diff1 = np.maximum(0.0, nx1*light_dir[0] + ny1*light_dir[1] + nz1*light_dir[2])
    fill1 = np.maximum(0.0, nx1*fill_dir[0] + ny1*fill_dir[1] + nz1*fill_dir[2])
    spec1 = np.maximum(0.0, nz1)**8 * 0.30

    base1 = np.array([255, 90, 54], dtype=np.float32) # Vibrant Coral ReBook
    col1 = np.zeros((SIZE, SIZE, 3), dtype=np.float32)
    for c in range(3):
        col1[:, :, c] = base1[c] * (0.44 + 0.64*diff1 + 0.16*fill1) + 255.0*spec1

    # 3 Plump rounded white clay dots
    dots_mask = np.zeros((SIZE, SIZE), dtype=bool)
    for dot_x in [325.0, 410.0, 495.0]:
        dot_d = ((x_grid - dot_x)/26.0)**2 + ((y_grid - 560.0)/26.0)**2
        dots_mask |= (dot_d <= 1.0)

    # White clay dots with soft 3D highlight
    col1[dots_mask & mask1] = np.array([255.0, 252.0, 245.0])

    # Ambient Occlusion: Front bubble casts soft shadow onto Back bubble
    shadow_d = ((x_grid - (cx1 - 20.0))/(rx1 * 1.06))**2 + ((y_grid - (cy1 - 25.0))/(ry1 * 1.06))**2
    in_shadow = (shadow_d <= 1.0) & mask2
    col2[in_shadow] *= 0.65

    # Compose (NO floor shadow as requested - pure floating object like van-hoc.png)
    canvas[mask2, :3] = np.clip(col2[mask2], 0, 255).astype(np.uint8)
    canvas[mask2, 3] = 255

    canvas[mask1, :3] = np.clip(col1[mask1], 0, 255).astype(np.uint8)
    canvas[mask1, 3] = 255

    img = Image.fromarray(canvas, mode='RGBA')
    # 4x Lanczos downsampling to 256x256
    icon_256 = img.resize((256, 256), Image.Resampling.LANCZOS)
    
    out_file = os.path.join(OUTPUT_DIR, 'chat-3d.png')
    icon_256.save(out_file)
    print("Rendered pure Claymorphism chat-3d.png to", out_file)

if __name__ == '__main__':
    render_clay_chat()
