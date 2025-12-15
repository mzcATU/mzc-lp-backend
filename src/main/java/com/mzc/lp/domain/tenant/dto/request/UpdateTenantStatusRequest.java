package com.mzc.lp.domain.tenant.dto.request;

import com.mzc.lp.domain.tenant.constant.TenantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTenantStatusRequest(
        @NotNull(message = "변경할 상태는 필수입니다")
        TenantStatus status
) {
}
