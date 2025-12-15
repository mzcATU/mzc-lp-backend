package com.mzc.lp.domain.iis.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class InstructorAlreadyAssignedException extends BusinessException {

    public InstructorAlreadyAssignedException() {
        super(ErrorCode.INSTRUCTOR_ALREADY_ASSIGNED);
    }

    public InstructorAlreadyAssignedException(Long userId, Long timeId) {
        super(ErrorCode.INSTRUCTOR_ALREADY_ASSIGNED,
                "Instructor already assigned: userId=" + userId + ", timeId=" + timeId);
    }
}
