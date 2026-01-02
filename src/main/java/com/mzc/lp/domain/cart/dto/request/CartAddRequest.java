package com.mzc.lp.domain.cart.dto.request;

import jakarta.validation.constraints.NotNull;

public record CartAddRequest(
        @NotNull(message = "강의 ID는 필수입니다")
        Long courseId
) {
}
