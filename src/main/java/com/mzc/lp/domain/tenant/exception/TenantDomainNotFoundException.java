package com.mzc.lp.domain.tenant.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class TenantDomainNotFoundException extends BusinessException {

    public TenantDomainNotFoundException() {
        super(ErrorCode.TENANT_NOT_FOUND);
    }

    public TenantDomainNotFoundException(Long tenantId) {
        super(ErrorCode.TENANT_NOT_FOUND, "테넌트를 찾을 수 없습니다: " + tenantId);
    }

    public TenantDomainNotFoundException(String message) {
        super(ErrorCode.TENANT_NOT_FOUND, message);
    }
}
