package com.mzc.lp.domain.assignment.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class SubmissionNotFoundException extends BusinessException {

    public SubmissionNotFoundException() {
        super(ErrorCode.SUBMISSION_NOT_FOUND);
    }

    public SubmissionNotFoundException(Long submissionId) {
        super(ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found with id: " + submissionId);
    }
}
