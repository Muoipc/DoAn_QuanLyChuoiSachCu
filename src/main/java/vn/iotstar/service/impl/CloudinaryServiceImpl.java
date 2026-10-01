package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.ICloudinaryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements ICloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud_name:demo}")
    private String cloudName;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String folder) {
        Map<String, String> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            return result;
        }

        // Kiểm tra xem đã cấu hình key thật hay đang dùng default 'demo'
        boolean isRealCloudinary = cloudName != null && !cloudName.isBlank() && !"demo".equalsIgnoreCase(cloudName);

        if (isRealCloudinary) {
            try {
                Map<?, ?> uploadParams = ObjectUtils.asMap(
                        "folder", folder != null ? folder : "old_book_store/books",
                        "resource_type", "auto"
                );
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                String secureUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                result.put("url", secureUrl);
                result.put("public_id", publicId);
                log.info("Uploaded to Cloudinary successfully: {}", secureUrl);
                return result;
            } catch (Exception e) {
                log.warn("Cloudinary upload failed ({}). Fallback to local storage.", e.getMessage());
            }
        }

        // FALLBACK: Lưu trữ cục bộ trong trường hợp mạng offline hoặc chưa nạp key Cloudinary thật
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueName = UUID.randomUUID().toString() + extension;

            // Thư mục uploads
            String uploadDir = "src/main/resources/static/uploads/books";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Copy vào target/classes nếu có để hot reload
            try {
                Path targetPath = Paths.get("target/classes/static/uploads/books");
                if (Files.exists(targetPath.getParent())) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                    Files.copy(file.getInputStream(), targetPath.resolve(uniqueName), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ignored) {}

            String localUrl = "/uploads/books/" + uniqueName;
            result.put("url", localUrl);
            result.put("public_id", "local_" + uniqueName);
            log.info("Saved image to local storage: {}", localUrl);
            return result;
        } catch (IOException e) {
            log.error("Failed to save image locally", e);
            result.put("url", "/images/books/book-1.jpg");
            result.put("public_id", "fallback_default");
            return result;
        }
    }

    @Override
    public boolean deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }
        if (publicId.startsWith("local_")) {
            try {
                String fileName = publicId.replace("local_", "");
                Path path = Paths.get("src/main/resources/static/uploads/books", fileName);
                Files.deleteIfExists(path);
                return true;
            } catch (Exception e) {
                log.warn("Failed to delete local file {}", publicId);
                return false;
            }
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return true;
        } catch (Exception e) {
            log.error("Error deleting from Cloudinary: {}", publicId, e);
            return false;
        }
    }
}
