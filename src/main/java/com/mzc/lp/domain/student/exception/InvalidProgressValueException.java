package com.mzc.lp.domain.student.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class InvalidProgressValueException extends BusinessException {

    public InvalidProgressValueException() {
        super(ErrorCode.INVALID_PROGRESS_VALUE);
    }

    public InvalidProgressValueException(Integer value) {
        super(ErrorCode.INVALID_PROGRESS_VALUE,
              "Progress value must be between 0 and 100, but was: " + value);
    }
}
