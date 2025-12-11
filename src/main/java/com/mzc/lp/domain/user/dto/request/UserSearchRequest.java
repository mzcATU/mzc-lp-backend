package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;

public record UserSearchRequest(
        String keyword,
        TenantRole role,
        UserStatus status
) {}
