package com.mzc.lp.domain.student.dto.request;

import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEnrollmentStatusRequest(
        @NotNull(message = "상태는 필수입니다")
        EnrollmentStatus status,

        @Size(max = 500, message = "사유는 500자 이내로 입력해주세요")
        String reason
) {
}
