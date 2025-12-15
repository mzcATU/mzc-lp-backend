package com.mzc.lp.domain.student.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CompleteEnrollmentRequest(
        @Min(value = 0, message = "점수는 0 이상이어야 합니다")
        @Max(value = 100, message = "점수는 100 이하여야 합니다")
        Integer score
) {
}
