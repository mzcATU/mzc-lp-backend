package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;

public record FileBulkCreateUsersRequest(
        String defaultPassword,
        TenantRole role,
        Boolean autoLinkEmployees,
        Boolean sendWelcomeEmail
) {
    public FileBulkCreateUsersRequest {
        if (role == null) {
            role = TenantRole.USER;
        }
        if (autoLinkEmployees == null) {
            autoLinkEmployees = true;
        }
        if (sendWelcomeEmail == null) {
            sendWelcomeEmail = false;
        }
    }
}
