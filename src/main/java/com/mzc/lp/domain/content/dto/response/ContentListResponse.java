package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ContentListResponse(
        Long id,
        String originalFileName,
        ContentType contentType,
        Long fileSize,
        Integer duration,
        String resolution,
        String thumbnailPath,
        LocalDateTime createdAt
) {
    public static ContentListResponse from(Content content) {
        return new ContentListResponse(
                content.getId(),
                content.getOriginalFileName(),
                content.getContentType(),
                content.getFileSize(),
                content.getDuration(),
                content.getResolution(),
                content.getThumbnailPath(),
                content.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(content.getCreatedAt(), ZoneId.systemDefault())
                        : null
        );
    }
}
