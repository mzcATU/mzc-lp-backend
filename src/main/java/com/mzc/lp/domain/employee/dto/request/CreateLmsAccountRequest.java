package com.mzc.lp.domain.employee.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;

public record CreateLmsAccountRequest(
        String password,
        Boolean generatePassword,
        Boolean sendWelcomeEmail,
        TenantRole role
) {
    public CreateLmsAccountRequest {
        if (generatePassword == null) {
            generatePassword = true;
        }
        if (sendWelcomeEmail == null) {
            sendWelcomeEmail = false;
        }
        if (role == null) {
            role = TenantRole.USER;
        }
    }
}
