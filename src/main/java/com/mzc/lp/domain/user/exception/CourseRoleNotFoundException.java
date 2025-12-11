package com.mzc.lp.domain.user.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CourseRoleNotFoundException extends BusinessException {

    public CourseRoleNotFoundException() {
        super(ErrorCode.COURSE_ROLE_NOT_FOUND);
    }

    public CourseRoleNotFoundException(Long courseRoleId) {
        super(ErrorCode.COURSE_ROLE_NOT_FOUND, "Course role not found: " + courseRoleId);
    }
}
