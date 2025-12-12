package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CircularReferenceException extends BusinessException {

    public CircularReferenceException() {
        super(ErrorCode.CM_CIRCULAR_REFERENCE);
    }

    public CircularReferenceException(String message) {
        super(ErrorCode.CM_CIRCULAR_REFERENCE, message);
    }
}
