package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ContentResponse(
        Long id,
        String originalFileName,
        String uploadedFileName,
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
        String customThumbnailPath,
        String description,
        String tags,
        Boolean downloadable,
        Long createdBy,
        Integer currentVersion,
        Boolean inCourse,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ContentResponse from(Content content) {
        return from(content, null);
    }

    public static ContentResponse from(Content content, Boolean inCourse) {
        return new ContentResponse(
                content.getId(),
                content.getOriginalFileName(),
                content.getUploadedFileName(),
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
                content.getCustomThumbnailPath(),
                content.getDescription(),
                content.getTags(),
                content.getDownloadable(),
                content.getCreatedBy(),
                content.getCurrentVersion(),
                inCourse,
                content.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(content.getCreatedAt(), ZoneId.systemDefault())
                        : null,
                content.getUpdatedAt() != null
                        ? LocalDateTime.ofInstant(content.getUpdatedAt(), ZoneId.systemDefault())
                        : null
        );
    }
}
