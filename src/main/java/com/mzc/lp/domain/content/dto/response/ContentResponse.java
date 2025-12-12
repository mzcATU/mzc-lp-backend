package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;

import java.time.Instant;

public record ContentResponse(
        Long id,
        String originalFileName,
        String storedFileName,
        ContentType contentType,
        Long fileSize,
        Integer duration,
        String resolution,
        Integer pageCount,
        String externalUrl,
        String filePath,
        Instant createdAt,
        Instant updatedAt
) {
    public static ContentResponse from(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getOriginalFileName(),
                content.getStoredFileName(),
                content.getContentType(),
                content.getFileSize(),
                content.getDuration(),
                content.getResolution(),
                content.getPageCount(),
                content.getExternalUrl(),
                content.getFilePath(),
                content.getCreatedAt(),
                content.getUpdatedAt()
        );
    }
}
