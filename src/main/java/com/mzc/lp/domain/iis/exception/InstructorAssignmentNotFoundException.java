package com.mzc.lp.domain.iis.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class InstructorAssignmentNotFoundException extends BusinessException {

    public InstructorAssignmentNotFoundException() {
        super(ErrorCode.INSTRUCTOR_ASSIGNMENT_NOT_FOUND);
    }

    public InstructorAssignmentNotFoundException(Long assignmentId) {
        super(ErrorCode.INSTRUCTOR_ASSIGNMENT_NOT_FOUND,
                "Instructor assignment not found with id: " + assignmentId);
    }
}
