package com.mzc.lp.domain.tenant.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.tenant.constant.TenantStatus;

public class InvalidTenantStatusException extends BusinessException {

    public InvalidTenantStatusException() {
        super(ErrorCode.INVALID_TENANT_STATUS);
    }

    public InvalidTenantStatusException(TenantStatus from, TenantStatus to) {
        super(ErrorCode.INVALID_TENANT_STATUS,
                String.format("잘못된 상태 전환입니다: %s -> %s", from, to));
    }

    public InvalidTenantStatusException(String message) {
        super(ErrorCode.INVALID_TENANT_STATUS, message);
    }
}
