package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class UnauthorizedCourseTimeAccessException extends BusinessException {

    public UnauthorizedCourseTimeAccessException() {
        super(ErrorCode.UNAUTHORIZED_COURSE_TIME_ACCESS);
    }

    public UnauthorizedCourseTimeAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED_COURSE_TIME_ACCESS, message);
    }
}
