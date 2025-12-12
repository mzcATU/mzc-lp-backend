package com.mzc.lp.domain.learning.dto.response;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.learning.entity.LearningObject;

import java.time.LocalDateTime;

public record LearningObjectResponse(
        Long learningObjectId,
        String name,
        Long contentId,
        ContentType contentType,
        Long fileSize,
        Integer duration,
        String resolution,
        Long folderId,
        String folderName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LearningObjectResponse from(LearningObject lo) {
        return new LearningObjectResponse(
                lo.getId(),
                lo.getName(),
                lo.getContent() != null ? lo.getContent().getId() : null,
                lo.getContent() != null ? lo.getContent().getContentType() : null,
                lo.getContent() != null ? lo.getContent().getFileSize() : null,
                lo.getContent() != null ? lo.getContent().getDuration() : null,
                lo.getContent() != null ? lo.getContent().getResolution() : null,
                lo.getFolder() != null ? lo.getFolder().getId() : null,
                lo.getFolder() != null ? lo.getFolder().getFolderName() : null,
                lo.getCreatedAt(),
                lo.getUpdatedAt()
        );
    }
}
