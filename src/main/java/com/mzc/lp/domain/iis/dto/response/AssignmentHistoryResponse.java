package com.mzc.lp.domain.iis.dto.response;

import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.AssignmentHistory;

import java.time.Instant;

public record AssignmentHistoryResponse(
        Long id,
        Long assignmentId,
        AssignmentAction action,
        AssignmentStatus oldStatus,
        AssignmentStatus newStatus,
        InstructorRole oldRole,
        InstructorRole newRole,
        String reason,
        Long changedBy,
        Instant changedAt
) {
    public static AssignmentHistoryResponse from(AssignmentHistory entity) {
        return new AssignmentHistoryResponse(
                entity.getId(),
                entity.getAssignmentId(),
                entity.getAction(),
                entity.getOldStatus(),
                entity.getNewStatus(),
                entity.getOldRole(),
                entity.getNewRole(),
                entity.getReason(),
                entity.getChangedBy(),
                entity.getChangedAt()
        );
    }
}
