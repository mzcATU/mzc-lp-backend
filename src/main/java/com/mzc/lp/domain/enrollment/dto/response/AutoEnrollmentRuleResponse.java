package com.mzc.lp.domain.enrollment.dto.response;

import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.entity.AutoEnrollmentRule;

import java.time.Instant;

public record AutoEnrollmentRuleResponse(
        Long id,
        String name,
        String description,
        AutoEnrollmentTrigger trigger,
        Long departmentId,
        String departmentName,
        Long courseTimeId,
        String courseTimeTitle,
        Boolean isActive,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static AutoEnrollmentRuleResponse from(AutoEnrollmentRule rule) {
        return from(rule, null, null);
    }

    public static AutoEnrollmentRuleResponse from(AutoEnrollmentRule rule,
                                                   String departmentName,
                                                   String courseTimeTitle) {
        return new AutoEnrollmentRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getTrigger(),
                rule.getDepartmentId(),
                departmentName,
                rule.getCourseTimeId(),
                courseTimeTitle,
                rule.getIsActive(),
                rule.getSortOrder(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
