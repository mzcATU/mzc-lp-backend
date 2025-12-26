package com.mzc.lp.domain.program.dto.response;

import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;

import java.time.Instant;

public record PendingProgramResponse(
        Long id,
        String title,
        String thumbnailUrl,
        ProgramLevel level,
        ProgramType type,
        Long createdBy,
        Instant submittedAt,
        Long snapshotId
) {
    public static PendingProgramResponse from(Program program) {
        return new PendingProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getCreatedBy(),
                program.getSubmittedAt(),
                program.getSnapshot() != null ? program.getSnapshot().getId() : null
        );
    }
}
