package org.example.shopping._core.utils;

import org.example.shopping._core.errors.exception.Exception400;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class FileUtil {

    // 이 확장자로만 저장한다: /images/ 로 서빙될 때 확장자로 Content-Type 이 정해지므로 html 등은 절대 저장하지 않음
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private static String imagesDir;

    @Value("${file.upload.path}")
    public void setImagesDir(String value) {
        imagesDir = value;
    }

    public static String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (!isImageFile(file)) {
            throw new Exception400("이미지 파일(jpg, png, gif, webp)만 업로드 가능합니다.");
        }

        Path uploadPath = Paths.get(imagesDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // 원래 파일명은 쓰지 않는다: "../" 같은 경로 조작을 막기 위해 UUID + 검증된 확장자만 사용
        String savedFileName = UUID.randomUUID() + "." + extensionOf(file.getOriginalFilename());
        Path filePath = resolveInside(uploadPath, savedFileName);

        Files.copy(file.getInputStream(), filePath);
        return savedFileName;
    }

    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/")
                && IMAGE_EXTENSIONS.contains(extensionOf(file.getOriginalFilename()));
    }

    public static void deleteFile(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        Path uploadPath = Paths.get(imagesDir).toAbsolutePath().normalize();
        Files.deleteIfExists(resolveInside(uploadPath, filename));
    }

    static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    // 업로드 폴더 밖을 가리키는 경로는 거부
    static Path resolveInside(Path dir, String filename) {
        Path path = dir.resolve(filename).normalize();
        if (!path.getParent().equals(dir)) {
            throw new Exception400("잘못된 파일 경로입니다.");
        }
        return path;
    }
}
