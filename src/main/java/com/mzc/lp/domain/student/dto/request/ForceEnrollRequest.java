package com.mzc.lp.domain.student.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ForceEnrollRequest(
        @NotEmpty(message = "사용자 ID 목록은 필수입니다")
        @Size(max = 100, message = "한 번에 최대 100명까지 배정 가능합니다")
        List<Long> userIds,

        @Size(max = 500, message = "사유는 500자 이내로 입력해주세요")
        String reason
) {
}
