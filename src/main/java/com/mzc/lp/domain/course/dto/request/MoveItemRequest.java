package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveItemRequest(
        @NotNull(message = "이동할 항목 ID는 필수입니다")
        Long itemId,

        Long targetParentId,

        @PositiveOrZero(message = "순서는 0 이상이어야 합니다")
        Integer targetIndex
) {
}
