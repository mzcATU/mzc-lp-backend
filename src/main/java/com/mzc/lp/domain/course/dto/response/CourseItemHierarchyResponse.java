package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.entity.CourseItem;

import java.util.List;

public record CourseItemHierarchyResponse(
        Long itemId,
        String itemName,
        Integer depth,
        Long learningObjectId,
        boolean isFolder,
        String displayName,
        String description,
        List<CourseItemHierarchyResponse> children
) {
    public static CourseItemHierarchyResponse from(CourseItem item) {
        return new CourseItemHierarchyResponse(
                item.getId(),
                item.getItemName(),
                item.getDepth(),
                item.getLearningObjectId(),
                item.isFolder(),
                item.getDisplayName(),
                item.getDescription(),
                item.getChildren().stream()
                        .map(CourseItemHierarchyResponse::from)
                        .toList()
        );
    }

    public static List<CourseItemHierarchyResponse> fromList(List<CourseItem> rootItems) {
        return rootItems.stream()
                .map(CourseItemHierarchyResponse::from)
                .toList();
    }
}
