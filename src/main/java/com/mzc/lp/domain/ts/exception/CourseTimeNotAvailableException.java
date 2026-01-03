package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseTimeNotAvailableException extends BusinessException {

    public CourseTimeNotAvailableException() {
        super(ErrorCode.COURSE_TIME_NOT_AVAILABLE);
    }

    public CourseTimeNotAvailableException(Long courseTimeId) {
        super(ErrorCode.COURSE_TIME_NOT_AVAILABLE, "CourseTime is not available for public access: " + courseTimeId);
    }
}
