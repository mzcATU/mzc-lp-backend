package com.mzc.lp.domain.student.dto.response;

import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.constant.EnrollmentType;
import com.mzc.lp.domain.student.entity.Enrollment;

import java.time.Instant;

public record EnrollmentResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        Long courseTimeId,
        Instant enrolledAt,
        EnrollmentType type,
        EnrollmentStatus status,
        Integer progressPercent,
        Integer score,
        Instant completedAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                null,
                null,
                enrollment.getCourseTimeId(),
                enrollment.getEnrolledAt(),
                enrollment.getType(),
                enrollment.getStatus(),
                enrollment.getProgressPercent(),
                enrollment.getScore(),
                enrollment.getCompletedAt()
        );
    }

    public static EnrollmentResponse from(Enrollment enrollment, String userName, String userEmail) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                userName,
                userEmail,
                enrollment.getCourseTimeId(),
                enrollment.getEnrolledAt(),
                enrollment.getType(),
                enrollment.getStatus(),
                enrollment.getProgressPercent(),
                enrollment.getScore(),
                enrollment.getCompletedAt()
        );
    }
}
