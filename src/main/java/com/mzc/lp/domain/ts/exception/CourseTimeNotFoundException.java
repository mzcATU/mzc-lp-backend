package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseTimeNotFoundException extends BusinessException {

    public CourseTimeNotFoundException() {
        super(ErrorCode.COURSE_TIME_NOT_FOUND);
    }

    public CourseTimeNotFoundException(Long courseTimeId) {
        super(ErrorCode.COURSE_TIME_NOT_FOUND, "CourseTime not found with id: " + courseTimeId);
    }
}
