package com.mzc.lp.domain.program.dto.request;

import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProgramRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
        String description,

        @Size(max = 500, message = "썸네일 URL은 500자 이하여야 합니다")
        String thumbnailUrl,

        ProgramLevel level,

        ProgramType type,

        Integer estimatedHours,

        Long snapshotId
) {
}
