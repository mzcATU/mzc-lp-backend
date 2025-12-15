package com.mzc.lp.domain.course.dto.response;

public record AutoRelationResponse(
        Long courseId,
        int relationCount,
        String message
) {
    public static AutoRelationResponse of(Long courseId, int relationCount) {
        return new AutoRelationResponse(courseId, relationCount, "자동 순서 생성 완료");
    }
}
