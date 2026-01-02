package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;

public record ProgramSummaryResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        ProgramLevel level,
        ProgramType type,
        Integer estimatedHours
) {
    public static ProgramSummaryResponse from(Program program) {
        if (program == null) {
            return null;
        }
        return new ProgramSummaryResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours()
        );
    }

    /**
     * 목록용 요약 응답 (description 제외)
     */
    public static ProgramSummaryResponse forList(Program program) {
        if (program == null) {
            return null;
        }
        return new ProgramSummaryResponse(
                program.getId(),
                program.getTitle(),
                null,
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours()
        );
    }
}
