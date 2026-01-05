package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;

public record FileBulkCreateUsersRequest(
        String defaultPassword,
        TenantRole role
) {
    public FileBulkCreateUsersRequest {
        if (role == null) {
            role = TenantRole.USER;
        }
    }
}
