package com.mzc.lp.domain.ts.dto.request;

import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCourseTimeRequest(
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        String description,

        DeliveryType deliveryType,

        DurationType durationType,

        LocalDate enrollStartDate,

        LocalDate enrollEndDate,

        LocalDate classStartDate,

        LocalDate classEndDate,

        @Min(value = 1, message = "학습 일수는 1일 이상이어야 합니다")
        Integer durationDays,

        @Min(value = 1, message = "정원은 1명 이상이어야 합니다")
        Integer capacity,

        @Min(value = 0, message = "대기자 수는 0 이상이어야 합니다")
        Integer maxWaitingCount,

        EnrollmentMethod enrollmentMethod,

        @Min(value = 0, message = "수료 기준은 0 이상이어야 합니다")
        @Max(value = 100, message = "수료 기준은 100 이하여야 합니다")
        Integer minProgressForCompletion,

        @DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
        BigDecimal price,

        Boolean isFree,

        String locationInfo,

        Boolean allowLateEnrollment,

        /**
         * 정기 수업 일정 (null이면 변경 없음, 빈 객체면 삭제)
         */
        @Valid
        RecurringScheduleRequest recurringSchedule
) {
}
