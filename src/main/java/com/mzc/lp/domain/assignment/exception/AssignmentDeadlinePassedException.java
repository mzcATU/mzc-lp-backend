package com.mzc.lp.domain.assignment.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class AssignmentDeadlinePassedException extends BusinessException {

    public AssignmentDeadlinePassedException() {
        super(ErrorCode.ASSIGNMENT_DEADLINE_PASSED);
    }

    public AssignmentDeadlinePassedException(Long assignmentId) {
        super(ErrorCode.ASSIGNMENT_DEADLINE_PASSED,
                "Assignment " + assignmentId + " deadline has passed");
    }
}
