package com.mzc.lp.common.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final String PROFILE_IMAGES_SUBDIR = "profile-images";

    public String storeProfileImage(MultipartFile file) {
        validateFile(file);

        try {
            // 프로필 이미지 전용 서브디렉토리 생성
            Path uploadPath = Paths.get(uploadDir, PROFILE_IMAGES_SUBDIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Profile image stored: {}", filename);
            return "/uploads/" + PROFILE_IMAGES_SUBDIR + "/" + filename;

        } catch (IOException e) {
            log.error("Failed to store profile image", e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }
}
