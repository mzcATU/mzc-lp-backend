package com.mzc.lp.domain.ts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CloneCourseTimeRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotNull(message = "모집 시작일은 필수입니다")
        LocalDate enrollStartDate,

        @NotNull(message = "모집 종료일은 필수입니다")
        LocalDate enrollEndDate,

        @NotNull(message = "학습 시작일은 필수입니다")
        LocalDate classStartDate,

        @NotNull(message = "학습 종료일은 필수입니다")
        LocalDate classEndDate
) {
}
