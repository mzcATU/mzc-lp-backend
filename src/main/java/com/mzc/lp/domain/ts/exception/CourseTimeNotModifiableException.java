package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;

public class CourseTimeNotModifiableException extends BusinessException {

    public CourseTimeNotModifiableException(Long courseTimeId, CourseTimeStatus status) {
        super(ErrorCode.COURSE_TIME_NOT_MODIFIABLE,
                String.format("CourseTime (id=%d) is not modifiable in %s status", courseTimeId, status));
    }
}
