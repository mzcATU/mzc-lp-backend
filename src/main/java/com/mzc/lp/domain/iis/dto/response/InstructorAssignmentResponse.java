package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record InstructorAssignmentResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
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
                null,
                null,
                entity.getTimeKey(),
                entity.getRole(),
                entity.getStatus(),
                entity.getAssignedAt(),
                entity.getReplacedAt(),
                entity.getAssignedBy(),
                entity.getCreatedAt()
        );
    }

    public static InstructorAssignmentResponse from(InstructorAssignment entity, User user) {
        return new InstructorAssignmentResponse(
                entity.getId(),
                entity.getUserKey(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
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
