package com.mzc.lp.domain.course.dto.response;

public record SetStartItemResponse(
        Long courseId,
        Long startItemId,
        String message
) {
    public static SetStartItemResponse of(Long courseId, Long startItemId) {
        return new SetStartItemResponse(courseId, startItemId, "시작점이 변경되었습니다.");
    }
}
