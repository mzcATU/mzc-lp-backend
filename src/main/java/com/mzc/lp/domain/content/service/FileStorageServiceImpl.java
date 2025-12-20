package com.mzc.lp.domain.content.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.domain.content.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadPath;

    public FileStorageServiceImpl(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.uploadPath);
            log.info("File storage initialized at: {}", this.uploadPath);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory: " + e.getMessage());
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = generateStoredFileName(originalFileName);

        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Invalid file path: " + originalFileName);
            }

            // 날짜별 디렉토리 경로 생성: /uploads/2025/01/
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d", now.getYear(), now.getMonthValue());
            Path targetDirectory = this.uploadPath.resolve(datePath);
            Files.createDirectories(targetDirectory);

            // 파일 저장
            Path targetLocation = targetDirectory.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 상대 경로 반환: /uploads/2025/01/uuid.ext
            String relativePath = "/uploads/" + datePath + "/" + storedFileName;
            log.info("File stored successfully: {} -> {}", originalFileName, relativePath);

            return relativePath;
        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFileName, e);
            throw new FileStorageException("Failed to store file: " + originalFileName);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            // /uploads/... 형태의 경로에서 실제 파일 경로로 변환
            String relativePath = filePath.startsWith("/uploads/")
                    ? filePath.substring("/uploads/".length())
                    : filePath;

            Path file = this.uploadPath.resolve(relativePath).normalize();

            // Path Traversal 방지: 파일이 uploadPath 내에 있는지 확인
            if (!file.startsWith(this.uploadPath)) {
                log.error("Path traversal attempt detected: {}", filePath);
                throw new FileStorageException(ErrorCode.FILE_NOT_FOUND, "Invalid file path: " + filePath);
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.error("File not found or not readable: {}", filePath);
                throw new FileStorageException(ErrorCode.FILE_NOT_FOUND, "File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            log.error("Invalid file path: {}", filePath, e);
            throw new FileStorageException(ErrorCode.FILE_NOT_FOUND, "Invalid file path: " + filePath);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            String relativePath = filePath.startsWith("/uploads/")
                    ? filePath.substring("/uploads/".length())
                    : filePath;

            Path file = this.uploadPath.resolve(relativePath).normalize();

            // Path Traversal 방지: 파일이 uploadPath 내에 있는지 확인
            if (!file.startsWith(this.uploadPath)) {
                log.error("Path traversal attempt detected on delete: {}", filePath);
                throw new FileStorageException("Invalid file path: " + filePath);
            }

            Files.deleteIfExists(file);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }

    @Override
    public String generateStoredFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
    }

    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}
