package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;

import java.time.Instant;

public record InstructorAssignmentResponse(
        Long id,
        Long userId,
        Long timeId,
        InstructorRole role,
        AssignmentStatus status,
        Instant assignedAt,
        Instant replacedAt,
        Long assignedBy,
        Instant createdAt
) {
    public static InstructorAssignmentResponse from(InstructorAssignment entity) {
        return new InstructorAssignmentResponse(
                entity.getId(),
                entity.getUserKey(),
                entity.getTimeKey(),
                entity.getRole(),
                entity.getStatus(),
                entity.getAssignedAt(),
                entity.getReplacedAt(),
                entity.getAssignedBy(),
                entity.getCreatedAt()
        );
    }
}
