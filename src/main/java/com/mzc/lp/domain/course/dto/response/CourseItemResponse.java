package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.entity.CourseItem;

import java.time.Instant;

public record CourseItemResponse(
        Long itemId,
        String itemName,
        Integer depth,
        Long parentId,
        Long learningObjectId,
        boolean isFolder,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static CourseItemResponse from(CourseItem item) {
        return new CourseItemResponse(
                item.getId(),
                item.getItemName(),
                item.getDepth(),
                item.getParent() != null ? item.getParent().getId() : null,
                item.getLearningObjectId(),
                item.isFolder(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
