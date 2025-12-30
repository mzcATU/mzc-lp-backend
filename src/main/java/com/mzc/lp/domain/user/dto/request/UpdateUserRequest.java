package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import jakarta.validation.constraints.Size;

/**
 * 관리자가 사용자 정보를 수정할 때 사용하는 요청 DTO
 */
public record UpdateUserRequest(
        @Size(max = 50, message = "이름은 50자 이하여야 합니다")
        String name,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
        String phone,

        @Size(max = 100, message = "부서는 100자 이하여야 합니다")
        String department,

        @Size(max = 100, message = "직책은 100자 이하여야 합니다")
        String position,

        UserStatus status,

        TenantRole role
) {}
