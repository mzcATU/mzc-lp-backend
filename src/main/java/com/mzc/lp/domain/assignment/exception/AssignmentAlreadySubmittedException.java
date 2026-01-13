package com.mzc.lp.domain.assignment.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AssignmentAlreadySubmittedException extends BusinessException {

    public AssignmentAlreadySubmittedException() {
        super(ErrorCode.SUBMISSION_ALREADY_EXISTS);
    }

    public AssignmentAlreadySubmittedException(Long assignmentId, Long studentId) {
        super(ErrorCode.SUBMISSION_ALREADY_EXISTS,
                "Student " + studentId + " has already submitted assignment " + assignmentId);
    }
}
