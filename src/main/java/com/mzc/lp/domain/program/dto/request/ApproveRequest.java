package com.mzc.lp.domain.program.dto.request;

import jakarta.validation.constraints.Size;

public record ApproveRequest(
        @Size(max = 500, message = "승인 코멘트는 500자 이하여야 합니다")
        String comment
) {
}
