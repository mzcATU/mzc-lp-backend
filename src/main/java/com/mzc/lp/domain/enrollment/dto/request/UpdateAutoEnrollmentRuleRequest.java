package com.mzc.lp.domain.enrollment.dto.request;

import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import jakarta.validation.constraints.Size;

public record UpdateAutoEnrollmentRuleRequest(
        @Size(max = 200, message = "규칙 이름은 200자 이하여야 합니다")
        String name,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다")
        String description,

        AutoEnrollmentTrigger trigger,

        Long departmentId,

        Long courseTimeId,

        Integer sortOrder
) {}
