package com.mzc.lp.domain.ts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CloneCourseTimeRequest(
        // 기본 정보 (필수)
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        String description,

        // 기간 설정 (필수)
        @NotNull(message = "모집 시작일은 필수입니다")
        LocalDate enrollStartDate,

        @NotNull(message = "모집 종료일은 필수입니다")
        LocalDate enrollEndDate,

        @NotNull(message = "학습 시작일은 필수입니다")
        LocalDate classStartDate,

        // FIXED: 필수 (서비스에서 검증), RELATIVE/UNLIMITED: null
        LocalDate classEndDate,

        // 운영 설정 (선택 - null이면 원본 복사)
        Integer capacity,
        BigDecimal price,
        Boolean isFree,
        String locationInfo,

        // 정기 일정 (true: 원본 일정 복사, false/null: 일정 없이)
        Boolean copyRecurringSchedule
) {
}
