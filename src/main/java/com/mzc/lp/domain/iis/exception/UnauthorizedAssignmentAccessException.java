package com.mzc.lp.domain.iis.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class UnauthorizedAssignmentAccessException extends BusinessException {

    public UnauthorizedAssignmentAccessException() {
        super(ErrorCode.UNAUTHORIZED_ASSIGNMENT_ACCESS);
    }

    public UnauthorizedAssignmentAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED_ASSIGNMENT_ACCESS, message);
    }
}
