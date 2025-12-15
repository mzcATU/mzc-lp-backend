package com.mzc.lp.domain.student.dto.response;

import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.constant.EnrollmentType;
import com.mzc.lp.domain.student.entity.Enrollment;

import java.time.Instant;

public record EnrollmentDetailResponse(
        Long id,
        Long userId,
        Long courseTimeId,
        Instant enrolledAt,
        Long enrolledBy,
        EnrollmentType type,
        EnrollmentStatus status,
        Integer progressPercent,
        Integer score,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static EnrollmentDetailResponse from(Enrollment enrollment) {
        return new EnrollmentDetailResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseTimeId(),
                enrollment.getEnrolledAt(),
                enrollment.getEnrolledBy(),
                enrollment.getType(),
                enrollment.getStatus(),
                enrollment.getProgressPercent(),
                enrollment.getScore(),
                enrollment.getCompletedAt(),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt()
        );
    }
}
