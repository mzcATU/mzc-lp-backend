package com.mzc.lp.domain.student.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProgressRequest(
        @NotNull(message = "아이템 ID는 필수입니다")
        Long itemId,

        @NotNull(message = "진도율은 필수입니다")
        @Min(value = 0, message = "진도율은 0 이상이어야 합니다")
        @Max(value = 100, message = "진도율은 100 이하여야 합니다")
        Integer progressPercent,

        Integer watchedSeconds
) {
}
