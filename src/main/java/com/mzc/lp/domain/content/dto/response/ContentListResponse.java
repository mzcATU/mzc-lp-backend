package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;

import java.time.LocalDateTime;

public record ContentListResponse(
        Long contentId,
        String originalFileName,
        ContentType contentType,
        Long fileSize,
        Integer duration,
        String resolution,
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
                content.getCreatedAt()
        );
    }
}
