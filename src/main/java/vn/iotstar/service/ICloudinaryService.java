package vn.iotstar.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface ICloudinaryService {
    /**
     * Tải tệp lên Cloudinary (hoặc fallback lưu cục bộ nếu không có mạng / dùng key demo).
     * @param file Tệp ảnh
     * @param folder Thư mục lưu trữ
     * @return Map chứa "url" và "public_id"
     */
    Map<String, String> uploadFile(MultipartFile file, String folder);

    /**
     * Xóa tệp trên Cloudinary theo public_id.
     * @param publicId Mã định danh ảnh trên Cloudinary
     * @return true nếu thành công
     */
    boolean deleteFile(String publicId);
}
