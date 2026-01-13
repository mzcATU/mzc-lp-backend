package com.mzc.lp.domain.assignment.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AssignmentNotFoundException extends BusinessException {

    public AssignmentNotFoundException() {
        super(ErrorCode.ASSIGNMENT_NOT_FOUND);
    }

    public AssignmentNotFoundException(Long assignmentId) {
        super(ErrorCode.ASSIGNMENT_NOT_FOUND, "Assignment not found with id: " + assignmentId);
    }
}
