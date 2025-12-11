package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.CourseRole;
import jakarta.validation.constraints.NotNull;

public record AssignCourseRoleRequest(
        Long courseId,  // null 가능 (DESIGNER 역할의 경우)

        @NotNull(message = "Role is required")
        CourseRole role,

        Integer revenueSharePercent  // OWNER인 경우 수익 분배 비율
) {
}
