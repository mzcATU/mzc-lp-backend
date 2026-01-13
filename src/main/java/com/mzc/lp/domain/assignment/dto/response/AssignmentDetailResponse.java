package com.mzc.lp.domain.assignment.dto.response;

import com.mzc.lp.domain.assignment.constant.AssignmentStatus;
import com.mzc.lp.domain.assignment.constant.GradingType;
import com.mzc.lp.domain.assignment.entity.Assignment;

import java.time.Instant;
import java.time.LocalDateTime;

public record AssignmentDetailResponse(
        Long id,
        Long courseTimeId,
        String title,
        String description,
        GradingType gradingType,
        Integer maxScore,
        Integer passingScore,
        LocalDateTime dueDate,
        AssignmentStatus status,
        Long createdBy,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        Boolean isOverdue,
        Boolean isSubmittable,
        // 통계 정보 (관리자용)
        Long totalSubmissions,
        Long gradedSubmissions,
        Double averageScore
) {
    public static AssignmentDetailResponse from(Assignment assignment) {
        return from(assignment, null, null, null);
    }

    public static AssignmentDetailResponse from(
            Assignment assignment,
            Long totalSubmissions,
            Long gradedSubmissions,
            Double averageScore
    ) {
        return new AssignmentDetailResponse(
                assignment.getId(),
                assignment.getCourseTimeId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getGradingType(),
                assignment.getMaxScore(),
                assignment.getPassingScore(),
                assignment.getDueDate(),
                assignment.getStatus(),
                assignment.getCreatedBy(),
                assignment.getPublishedAt(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt(),
                assignment.isOverdue(),
                assignment.isSubmittable(),
                totalSubmissions,
                gradedSubmissions,
                averageScore
        );
    }
}
