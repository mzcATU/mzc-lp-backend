package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseOwnershipException extends BusinessException {

    public CourseOwnershipException() {
        super(ErrorCode.CM_UNAUTHORIZED_COURSE_ACCESS);
    }

    public CourseOwnershipException(String message) {
        super(ErrorCode.CM_UNAUTHORIZED_COURSE_ACCESS, message);
    }
}
