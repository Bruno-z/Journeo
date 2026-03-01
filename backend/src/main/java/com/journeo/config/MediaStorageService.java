package com.journeo.config;

import com.journeo.model.GuideMedia;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class MediaStorageService {

    @Value("${media.upload-dir:./uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            // Assainir l'extension : conserver uniquement le point + caractères alphanumériques
            String raw = originalFilename.substring(originalFilename.lastIndexOf("."));
            extension = raw.replaceAll("[^a-zA-Z0-9.]", "");
        }
        String uniqueFileName = UUID.randomUUID() + extension;
        Path targetPath = uploadPath.resolve(uniqueFileName).normalize();
        // Protection path traversal : le chemin résolu doit rester dans uploadPath
        if (!targetPath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Chemin de fichier invalide : " + uniqueFileName);
        }
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + uniqueFileName, e);
        }
        return uniqueFileName;
    }

    public Resource load(String fileName) {
        try {
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Chemin de fichier invalide : " + fileName);
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found or not readable: " + fileName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }

    public void delete(String fileName) {
        Path filePath = uploadPath.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Chemin de fichier invalide : " + fileName);
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }

    public GuideMedia.FileType detectFileType(String contentType) {
        if (contentType == null) return GuideMedia.FileType.IMAGE;
        if (contentType.startsWith("video/")) return GuideMedia.FileType.VIDEO;
        return GuideMedia.FileType.IMAGE;
    }
}
