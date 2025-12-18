package com.mzc.lp.domain.snapshot.dto.response;

import com.mzc.lp.domain.snapshot.entity.SnapshotRelation;

import java.time.Instant;
import java.util.List;

public record SnapshotRelationResponse(
        Long relationId,
        Long snapshotId,
        Long fromItemId,
        String fromItemName,
        Long toItemId,
        String toItemName,
        Boolean isStartPoint,
        Instant createdAt
) {
    public static SnapshotRelationResponse from(SnapshotRelation relation) {
        return new SnapshotRelationResponse(
                relation.getId(),
                relation.getSnapshot().getId(),
                relation.getFromItem() != null ? relation.getFromItem().getId() : null,
                relation.getFromItem() != null ? relation.getFromItem().getItemName() : null,
                relation.getToItem().getId(),
                relation.getToItem().getItemName(),
                relation.isStartPoint(),
                relation.getCreatedAt()
        );
    }

    public record OrderedItem(
            Long itemId,
            String itemName,
            Integer seq
    ) {
    }

    public record SnapshotRelationsResponse(
            Long snapshotId,
            List<OrderedItem> orderedItems,
            List<SnapshotRelationResponse> relations
    ) {
        public static SnapshotRelationsResponse from(
                Long snapshotId,
                List<OrderedItem> orderedItems,
                List<SnapshotRelation> relations
        ) {
            List<SnapshotRelationResponse> relationResponses = relations.stream()
                    .map(SnapshotRelationResponse::from)
                    .toList();

            return new SnapshotRelationsResponse(snapshotId, orderedItems, relationResponses);
        }
    }
}
