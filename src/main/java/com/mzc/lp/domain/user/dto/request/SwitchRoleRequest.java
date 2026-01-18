package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;
import jakarta.validation.constraints.NotNull;

/**
 * 역할 전환 요청 DTO
 * 사용자가 보유한 여러 역할 중 하나를 선택하여 전환할 때 사용
 */
public record SwitchRoleRequest(
        @NotNull(message = "전환할 역할은 필수입니다")
        TenantRole targetRole
) {}
