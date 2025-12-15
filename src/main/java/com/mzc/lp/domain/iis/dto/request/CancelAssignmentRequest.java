package com.mzc.lp.domain.iis.dto.request;

import jakarta.validation.constraints.Size;

public record CancelAssignmentRequest(
        @Size(max = 500, message = "사유는 500자 이하여야 합니다")
        String reason
) {
}
