package com.mzc.lp.domain.assignment.dto.response;

import com.mzc.lp.domain.assignment.constant.AssignmentStatus;
import com.mzc.lp.domain.assignment.constant.GradingType;
import com.mzc.lp.domain.assignment.entity.Assignment;

import java.time.Instant;
import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long courseTimeId,
        String title,
        GradingType gradingType,
        Integer maxScore,
        Integer passingScore,
        LocalDateTime dueDate,
        AssignmentStatus status,
        Long createdBy,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        Boolean isOverdue
) {
    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getCourseTimeId(),
                assignment.getTitle(),
                assignment.getGradingType(),
                assignment.getMaxScore(),
                assignment.getPassingScore(),
                assignment.getDueDate(),
                assignment.getStatus(),
                assignment.getCreatedBy(),
                assignment.getPublishedAt(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt(),
                assignment.isOverdue()
        );
    }
}
