package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.snapshot.entity.SnapshotItem;

import java.util.List;

/**
 * 커리큘럼 아이템 응답 DTO (트리 구조)
 */
public record CurriculumItemResponse(
        Long id,
        String itemName,
        String itemType,
        boolean isFolder,
        Integer duration,
        List<CurriculumItemResponse> children
) {
    /**
     * SnapshotItem을 CurriculumItemResponse로 변환 (children 포함)
     */
    public static CurriculumItemResponse fromWithChildren(SnapshotItem item) {
        if (item == null) {
            return null;
        }

        List<CurriculumItemResponse> childResponses = item.getChildren().stream()
                .map(CurriculumItemResponse::fromWithChildren)
                .toList();

        Integer duration = null;
        if (item.getSnapshotLearningObject() != null) {
            duration = item.getSnapshotLearningObject().getDuration();
        }

        return new CurriculumItemResponse(
                item.getId(),
                item.getItemName(),
                item.getItemType(),
                item.isFolder(),
                duration,
                childResponses
        );
    }
}
