package com.mzc.lp.domain.tenant.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class DuplicateSubdomainException extends BusinessException {

    public DuplicateSubdomainException() {
        super(ErrorCode.DUPLICATE_SUBDOMAIN);
    }

    public DuplicateSubdomainException(String subdomain) {
        super(ErrorCode.DUPLICATE_SUBDOMAIN, "이미 존재하는 서브도메인입니다: " + subdomain);
    }
}
