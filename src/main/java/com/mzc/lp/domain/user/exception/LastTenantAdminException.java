package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class LastTenantAdminException extends BusinessException {

    public LastTenantAdminException() {
        super(ErrorCode.LAST_TENANT_ADMIN_CANNOT_BE_REMOVED);
    }
}
