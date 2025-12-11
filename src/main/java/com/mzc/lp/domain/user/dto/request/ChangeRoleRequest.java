package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "역할은 필수입니다")
        TenantRole role
) {}
