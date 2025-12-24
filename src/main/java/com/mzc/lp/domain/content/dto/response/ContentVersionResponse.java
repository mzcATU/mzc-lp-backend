package com.mzc.lp.domain.content.dto.response;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.constant.VersionChangeType;
import com.mzc.lp.domain.content.entity.ContentVersion;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ContentVersionResponse(
        Long id,
        Long contentId,
        Integer versionNumber,
        VersionChangeType changeType,
        String originalFileName,
        String uploadedFileName,
        String storedFileName,
        ContentType contentType,
        Long fileSize,
        Integer duration,
        String resolution,
        String changeSummary,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static ContentVersionResponse from(ContentVersion version) {
        return new ContentVersionResponse(
                version.getId(),
                version.getContent().getId(),
                version.getVersionNumber(),
                version.getChangeType(),
                version.getOriginalFileName(),
                version.getUploadedFileName(),
                version.getStoredFileName(),
                version.getContentType(),
                version.getFileSize(),
                version.getDuration(),
                version.getResolution(),
                version.getChangeSummary(),
                version.getCreatedBy(),
                version.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(version.getCreatedAt(), ZoneId.systemDefault())
                        : null
        );
    }
}
