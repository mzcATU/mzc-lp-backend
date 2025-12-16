package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotNull;

public record SetStartItemRequest(
        @NotNull(message = "시작점 항목 ID는 필수입니다")
        Long startItemId
) {
}
