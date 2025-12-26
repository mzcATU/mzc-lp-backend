package com.mzc.lp.domain.iis.dto.request;

import com.mzc.lp.domain.iis.constant.InstructorRole;
import jakarta.validation.constraints.NotNull;

public record AssignInstructorRequest(
        @NotNull(message = "사용자 ID는 필수입니다")
        Long userId,

        @NotNull(message = "역할은 필수입니다")
        InstructorRole role,

        Boolean forceAssign
) {
    public AssignInstructorRequest {
        if (forceAssign == null) {
            forceAssign = false;
        }
    }
}
