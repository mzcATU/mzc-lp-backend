package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

/**
 * Course가 REGISTERED 상태가 아닐 때 발생하는 예외
 * 차수(CourseTime) 생성은 REGISTERED 상태의 Course에서만 가능
 */
public class CourseNotRegisteredException extends BusinessException {

    public CourseNotRegisteredException(Long courseId) {
        super(ErrorCode.CM_COURSE_NOT_REGISTERED,
                String.format("Course ID: %d", courseId));
    }

    public CourseNotRegisteredException(Long courseId, String currentStatus) {
        super(ErrorCode.CM_COURSE_NOT_REGISTERED,
                String.format("Course ID: %d, Current Status: %s", courseId, currentStatus));
    }
}
