package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeStatusRequest(
        @NotNull(message = "상태는 필수입니다")
        UserStatus status,

        @Size(max = 500, message = "사유는 500자 이하여야 합니다")
        String reason
) {}
