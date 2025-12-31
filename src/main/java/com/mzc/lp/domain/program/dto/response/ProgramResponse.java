package com.mzc.lp.domain.program.dto.response;

import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;

import java.time.Instant;

public record ProgramResponse(
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
        // OWNER 정보 (승인된 프로그램에만 존재)
        Long ownerId,
        String ownerName,
        String ownerEmail,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProgramResponse from(Program program) {
        return new ProgramResponse(
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
                null,
                null,
                null,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }

    public static ProgramResponse from(Program program, String creatorName, Long ownerId, String ownerName, String ownerEmail) {
        return new ProgramResponse(
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
                ownerId,
                ownerName,
                ownerEmail,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }
}
