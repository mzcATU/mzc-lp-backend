package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ContentResponse(
        Long id,
        String originalFileName,
        String storedFileName,
        ContentType contentType,
        ContentStatus status,
        Long fileSize,
        Integer duration,
        String resolution,
        Integer pageCount,
        String externalUrl,
        String filePath,
        String thumbnailPath,
        Long createdBy,
        Integer currentVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ContentResponse from(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getOriginalFileName(),
                content.getStoredFileName(),
                content.getContentType(),
                content.getStatus(),
                content.getFileSize(),
                content.getDuration(),
                content.getResolution(),
                content.getPageCount(),
                content.getExternalUrl(),
                content.getFilePath(),
                content.getThumbnailPath(),
                content.getCreatedBy(),
                content.getCurrentVersion(),
                content.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(content.getCreatedAt(), ZoneId.systemDefault())
                        : null,
                content.getUpdatedAt() != null
                        ? LocalDateTime.ofInstant(content.getUpdatedAt(), ZoneId.systemDefault())
                        : null
        );
    }
}
