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
        Long snapshotId,
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
                program.getSnapshot() != null ? program.getSnapshot().getId() : null,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }
}
