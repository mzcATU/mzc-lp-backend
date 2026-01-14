package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * 사용자 역할 업데이트 요청 (1:N)
 * 여러 역할을 한 번에 설정
 */
public record UpdateUserRolesRequest(
        @NotEmpty(message = "최소 하나의 역할이 필요합니다")
        Set<TenantRole> roles
) {}
