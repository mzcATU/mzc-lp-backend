package com.mzc.lp.domain.snapshot.dto.response;

import com.mzc.lp.domain.snapshot.entity.SnapshotItem;

import java.time.Instant;
import java.util.List;

public record SnapshotItemResponse(
        Long itemId,
        Long snapshotId,
        String itemName,
        Long parentId,
        Integer depth,
        Boolean isFolder,
        String itemType,
        SnapshotLearningObjectResponse snapshotLearningObject,
        List<SnapshotItemResponse> children,
        Instant createdAt,
        Instant updatedAt
) {
    public static SnapshotItemResponse from(SnapshotItem item) {
        return new SnapshotItemResponse(
                item.getId(),
                item.getSnapshot().getId(),
                item.getItemName(),
                item.getParent() != null ? item.getParent().getId() : null,
                item.getDepth(),
                item.isFolder(),
                item.getItemType(),
                SnapshotLearningObjectResponse.from(item.getSnapshotLearningObject()),
                null,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public static SnapshotItemResponse fromWithChildren(SnapshotItem item) {
        List<SnapshotItemResponse> childResponses = item.getChildren().stream()
                .map(SnapshotItemResponse::fromWithChildren)
                .toList();

        return new SnapshotItemResponse(
                item.getId(),
                item.getSnapshot().getId(),
                item.getItemName(),
                item.getParent() != null ? item.getParent().getId() : null,
                item.getDepth(),
                item.isFolder(),
                item.getItemType(),
                SnapshotLearningObjectResponse.from(item.getSnapshotLearningObject()),
                childResponses,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
