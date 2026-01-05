package com.mzc.lp.domain.enrollment.dto.request;

import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAutoEnrollmentRuleRequest(
        @NotBlank(message = "규칙 이름은 필수입니다")
        @Size(max = 200, message = "규칙 이름은 200자 이하여야 합니다")
        String name,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다")
        String description,

        @NotNull(message = "트리거 타입은 필수입니다")
        AutoEnrollmentTrigger trigger,

        Long departmentId,

        @NotNull(message = "과정 회차 ID는 필수입니다")
        Long courseTimeId,

        Integer sortOrder
) {}
