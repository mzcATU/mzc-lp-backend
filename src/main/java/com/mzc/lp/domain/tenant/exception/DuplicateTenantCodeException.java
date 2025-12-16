package com.mzc.lp.domain.tenant.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class DuplicateTenantCodeException extends BusinessException {

    public DuplicateTenantCodeException() {
        super(ErrorCode.DUPLICATE_TENANT_CODE);
    }

    public DuplicateTenantCodeException(String code) {
        super(ErrorCode.DUPLICATE_TENANT_CODE, "이미 존재하는 테넌트 코드입니다: " + code);
    }
}
