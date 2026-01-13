package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.course.constant.CourseStatus;

public class CourseNotModifiableException extends BusinessException {

    public CourseNotModifiableException() {
        super(ErrorCode.CM_COURSE_NOT_MODIFIABLE);
    }

    public CourseNotModifiableException(Long courseId, CourseStatus status) {
        super(ErrorCode.CM_COURSE_NOT_MODIFIABLE,
                String.format("강의(ID: %d)는 현재 상태(%s)에서 수정할 수 없습니다.", courseId, status.getDescription()));
    }
}
