package com.mzc.lp.domain.community.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.content.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/images")
@RequiredArgsConstructor
public class CommunityImageController {

    private final FileStorageService fileStorageService;

    // 허용되는 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    // 최대 파일 크기: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        // 파일 검증
        validateImageFile(file);

        // 파일 저장
        String imageUrl = fileStorageService.storeFile(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("url", imageUrl)));
    }

    @PostMapping("/upload/multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 images allowed per upload");
        }

        List<String> urls = files.stream()
                .peek(this::validateImageFile)
                .map(fileStorageService::storeFile)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("urls", urls)));
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        String extension = fileStorageService.getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }
}
