package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ContentListResponse(
        Long id,
        String originalFileName,
        ContentType contentType,
        ContentStatus status,
        Long fileSize,
        Integer duration,
        String resolution,
        String thumbnailPath,
        Integer currentVersion,
        LocalDateTime createdAt
) {
    public static ContentListResponse from(Content content) {
        return new ContentListResponse(
                content.getId(),
                content.getOriginalFileName(),
                content.getContentType(),
                content.getStatus(),
                content.getFileSize(),
                content.getDuration(),
                content.getResolution(),
                content.getThumbnailPath(),
                content.getCurrentVersion(),
                content.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(content.getCreatedAt(), ZoneId.systemDefault())
                        : null
        );
    }
}
