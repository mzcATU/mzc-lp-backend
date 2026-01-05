package com.mzc.lp.domain.cart.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartRemoveRequest(
        @NotEmpty(message = "삭제할 차수 ID 목록은 필수입니다")
        List<Long> courseTimeIds
) {
}
