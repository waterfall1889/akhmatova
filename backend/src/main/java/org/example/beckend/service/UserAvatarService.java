package org.example.beckend.service;

import java.util.Locale;

import org.example.beckend.dto.LoginResponse;
import org.example.beckend.mongo.UserAvatarDocument;
import org.example.beckend.mongo.UserAvatarMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserAvatarService {

    private static final int MAX_BYTES = 2 * 1024 * 1024;

    private final UserAvatarMongoRepository userAvatarMongoRepository;

    public UserAvatarService(UserAvatarMongoRepository userAvatarMongoRepository) {
        this.userAvatarMongoRepository = userAvatarMongoRepository;
    }

    public LoginResponse saveAvatar(long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new LoginResponse(false, "请选择图片文件");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            return new LoginResponse(false, "读取文件失败");
        }
        if (bytes.length == 0) {
            return new LoginResponse(false, "文件为空");
        }
        if (bytes.length > MAX_BYTES) {
            return new LoginResponse(false, "图片不能超过 2MB");
        }
        String contentType = resolveContentType(file.getOriginalFilename(), file.getContentType());
        String key = Long.toString(userId);
        UserAvatarDocument doc = userAvatarMongoRepository.findById(key).orElseGet(UserAvatarDocument::new);
        doc.setUserId(key);
        doc.setImage(bytes);
        doc.setContentType(contentType);
        userAvatarMongoRepository.save(doc);
        return new LoginResponse(true, "头像已更新");
    }

    private static String resolveContentType(String filename, String declared) {
        if (declared != null && !declared.isBlank() && !MediaType.APPLICATION_OCTET_STREAM_VALUE.equalsIgnoreCase(declared)) {
            return declared.split(";")[0].trim();
        }
        if (filename == null) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        return MediaType.IMAGE_JPEG_VALUE;
    }
}
