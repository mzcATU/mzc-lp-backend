package com.mzc.lp.domain.student.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateItemProgressRequest(
        @Min(value = 0, message = "진도율은 0 이상이어야 합니다")
        @Max(value = 100, message = "진도율은 100 이하여야 합니다")
        Integer progressPercent,

        @Min(value = 0, message = "시청 시간은 0 이상이어야 합니다")
        Integer watchedSeconds,

        @Min(value = 0, message = "마지막 재생 위치는 0 이상이어야 합니다")
        Integer lastPositionSeconds
) {
}