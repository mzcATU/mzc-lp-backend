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
        Long createdBy,
        String creatorName,
        Long snapshotId,
        String snapshotName,
        // 승인 정보
        Long approvedBy,
        String approvedByName,
        Instant approvedAt,
        String approvalComment,
        // 반려 정보
        String rejectionReason,
        Instant rejectedAt,
        // 제출 정보
        Instant submittedAt,
        // OWNER 정보 (승인된 프로그램에만 존재)
        Long ownerId,
        String ownerName,
        String ownerEmail,
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
                program.getCreatedBy(),
                null,
                program.getSnapshot() != null ? program.getSnapshot().getId() : null,
                program.getSnapshot() != null ? program.getSnapshot().getSnapshotName() : null,
                program.getApprovedBy(),
                null,
                program.getApprovedAt(),
                program.getApprovalComment(),
                program.getRejectionReason(),
                program.getRejectedAt(),
                program.getSubmittedAt(),
                null,
                null,
                null,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }

    public static ProgramDetailResponse from(Program program, String creatorName, String approvedByName,
                                              Long ownerId, String ownerName, String ownerEmail) {
        return new ProgramDetailResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                program.getStatus(),
                program.getCreatedBy(),
                creatorName,
                program.getSnapshot() != null ? program.getSnapshot().getId() : null,
                program.getSnapshot() != null ? program.getSnapshot().getSnapshotName() : null,
                program.getApprovedBy(),
                approvedByName,
                program.getApprovedAt(),
                program.getApprovalComment(),
                program.getRejectionReason(),
                program.getRejectedAt(),
                program.getSubmittedAt(),
                ownerId,
                ownerName,
                ownerEmail,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }
}
