package com.mzc.lp.domain.ts.dto.request;

import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCourseTimeRequest(
        Long cmCourseId,

        Long cmCourseVersionId,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotNull(message = "수업 유형은 필수입니다")
        DeliveryType deliveryType,

        @NotNull(message = "모집 시작일은 필수입니다")
        LocalDate enrollStartDate,

        @NotNull(message = "모집 종료일은 필수입니다")
        LocalDate enrollEndDate,

        @NotNull(message = "학습 시작일은 필수입니다")
        LocalDate classStartDate,

        @NotNull(message = "학습 종료일은 필수입니다")
        LocalDate classEndDate,

        @Min(value = 1, message = "정원은 1명 이상이어야 합니다")
        Integer capacity,

        @Min(value = 0, message = "대기자 수는 0 이상이어야 합니다")
        Integer maxWaitingCount,

        @NotNull(message = "모집 방식은 필수입니다")
        EnrollmentMethod enrollmentMethod,

        @NotNull(message = "수료 기준은 필수입니다")
        @Min(value = 0, message = "수료 기준은 0 이상이어야 합니다")
        @Max(value = 100, message = "수료 기준은 100 이하여야 합니다")
        Integer minProgressForCompletion,

        @NotNull(message = "가격은 필수입니다")
        @DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
        BigDecimal price,

        @NotNull(message = "무료 여부는 필수입니다")
        Boolean isFree,

        String locationInfo,

        Boolean allowLateEnrollment
) {
}
