package com.mzc.lp.domain.learning.dto.response;

import com.mzc.lp.domain.learning.entity.ContentFolder;

import java.time.LocalDateTime;
import java.util.List;

public record ContentFolderResponse(
        Long folderId,
        String folderName,
        Long parentId,
        Integer depth,
        Integer childCount,
        Integer itemCount,
        List<ContentFolderResponse> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ContentFolderResponse from(ContentFolder folder) {
        return new ContentFolderResponse(
                folder.getId(),
                folder.getFolderName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getDepth(),
                folder.getChildCount(),
                folder.getItemCount(),
                null,
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }

    public static ContentFolderResponse fromWithChildren(ContentFolder folder) {
        return new ContentFolderResponse(
                folder.getId(),
                folder.getFolderName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getDepth(),
                folder.getChildCount(),
                folder.getItemCount(),
                folder.getChildren().stream()
                        .map(ContentFolderResponse::fromWithChildren)
                        .toList(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}
