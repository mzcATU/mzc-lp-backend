package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateLearningObjectRequest(
        @NotNull(message = "학습 객체 ID는 필수입니다")
        Long learningObjectId
) {
}
