package com.mzc.lp.domain.assignment.dto.request;

import com.mzc.lp.domain.assignment.constant.SubmissionGrade;

public record GradeSubmissionRequest(
        Integer score,

        SubmissionGrade grade,

        String feedback,

        Boolean returnSubmission
) {
    public GradeSubmissionRequest {
        if (returnSubmission == null) {
            returnSubmission = false;
        }
    }
}
