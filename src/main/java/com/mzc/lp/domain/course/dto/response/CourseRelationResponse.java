package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.entity.CourseRelation;

import java.util.List;

public record CourseRelationResponse(
        Long courseId,
        List<OrderedItem> orderedItems,
        List<RelationItem> relations
) {
    public record OrderedItem(
            Long itemId,
            String itemName,
            int order
    ) {
    }

    public record RelationItem(
            Long relationId,
            Long fromItemId,
            Long toItemId
    ) {
        public static RelationItem from(CourseRelation relation) {
            return new RelationItem(
                    relation.getId(),
                    relation.getFromItem() != null ? relation.getFromItem().getId() : null,
                    relation.getToItem().getId()
            );
        }
    }

    public static CourseRelationResponse from(Long courseId, List<OrderedItem> orderedItems, List<CourseRelation> relations) {
        return new CourseRelationResponse(
                courseId,
                orderedItems,
                relations.stream()
                        .map(RelationItem::from)
                        .toList()
        );
    }
}
