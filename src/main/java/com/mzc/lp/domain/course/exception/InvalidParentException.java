package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class InvalidParentException extends BusinessException {

    public InvalidParentException() {
        super(ErrorCode.CM_INVALID_PARENT);
    }

    public InvalidParentException(String message) {
        super(ErrorCode.CM_INVALID_PARENT, message);
    }
}
