package com.mzc.lp.domain.tenant.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class DuplicateCustomDomainException extends BusinessException {

    public DuplicateCustomDomainException() {
        super(ErrorCode.DUPLICATE_CUSTOM_DOMAIN);
    }

    public DuplicateCustomDomainException(String customDomain) {
        super(ErrorCode.DUPLICATE_CUSTOM_DOMAIN, "이미 존재하는 커스텀 도메인입니다: " + customDomain);
    }
}
