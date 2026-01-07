package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseIncompleteException extends BusinessException {

    public CourseIncompleteException() {
        super(ErrorCode.CM_COURSE_INCOMPLETE);
    }

    public CourseIncompleteException(Long courseId) {
        super(ErrorCode.CM_COURSE_INCOMPLETE, "완성되지 않은 강의는 발행할 수 없습니다. ID: " + courseId);
    }
}
