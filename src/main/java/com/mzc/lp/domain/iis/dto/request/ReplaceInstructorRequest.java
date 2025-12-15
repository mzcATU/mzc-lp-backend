package com.mzc.lp.domain.iis.dto.request;

import com.mzc.lp.domain.iis.constant.InstructorRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReplaceInstructorRequest(
        @NotNull(message = "새 강사 ID는 필수입니다")
        Long newUserId,

        @NotNull(message = "역할은 필수입니다")
        InstructorRole role,

        @Size(max = 500, message = "사유는 500자 이하여야 합니다")
        String reason
) {
}
