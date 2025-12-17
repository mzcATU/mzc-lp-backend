package com.mzc.lp.domain.program.dto.response;

import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;

import java.time.Instant;

public record ProgramDetailResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        ProgramLevel level,
        ProgramType type,
        Integer estimatedHours,
        ProgramStatus status,
        Long creatorId,
        Long snapshotId,
        String snapshotName,
        // 승인 정보
        Long approvedBy,
        Instant approvedAt,
        String approvalComment,
        // 반려 정보
        String rejectionReason,
        Instant rejectedAt,
        // 제출 정보
        Instant submittedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProgramDetailResponse from(Program program) {
        return new ProgramDetailResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                program.getStatus(),
                program.getCreatorId(),
                program.getSnapshot() != null ? program.getSnapshot().getId() : null,
                program.getSnapshot() != null ? program.getSnapshot().getSnapshotName() : null,
                program.getApprovedBy(),
                program.getApprovedAt(),
                program.getApprovalComment(),
                program.getRejectionReason(),
                program.getRejectedAt(),
                program.getSubmittedAt(),
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }
}
