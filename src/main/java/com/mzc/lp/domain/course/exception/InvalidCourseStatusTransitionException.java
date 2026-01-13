package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.course.constant.CourseStatus;

public class InvalidCourseStatusTransitionException extends BusinessException {

    public InvalidCourseStatusTransitionException() {
        super(ErrorCode.CM_INVALID_COURSE_STATUS_TRANSITION);
    }

    public InvalidCourseStatusTransitionException(CourseStatus from, CourseStatus to) {
        super(ErrorCode.CM_INVALID_COURSE_STATUS_TRANSITION,
                String.format("강의 상태를 %s에서 %s(으)로 변경할 수 없습니다.", from.getDescription(), to.getDescription()));
    }
}
