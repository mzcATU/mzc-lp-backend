package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseNotFoundException extends BusinessException {

    public CourseNotFoundException() {
        super(ErrorCode.CM_COURSE_NOT_FOUND);
    }

    public CourseNotFoundException(Long courseId) {
        super(ErrorCode.CM_COURSE_NOT_FOUND, "강의를 찾을 수 없습니다. ID: " + courseId);
    }
}
