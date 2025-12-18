package com.mzc.lp.domain.snapshot.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSnapshotRelationRequest(
        Long fromItemId,

        @NotNull(message = "대상 항목 ID는 필수입니다")
        @Positive(message = "대상 항목 ID는 양수여야 합니다")
        Long toItemId
) {
}
