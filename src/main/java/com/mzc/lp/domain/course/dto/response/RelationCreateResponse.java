package com.mzc.lp.domain.course.dto.response;

public record RelationCreateResponse(
        Long courseId,
        int relationCount,
        Long startItemId
) {
    public static RelationCreateResponse of(Long courseId, int relationCount, Long startItemId) {
        return new RelationCreateResponse(courseId, relationCount, startItemId);
    }
}
