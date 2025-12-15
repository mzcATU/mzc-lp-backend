package com.mzc.lp.domain.student.dto.response;

import java.util.List;

public record ForceEnrollResultResponse(
        int successCount,
        int failCount,
        List<EnrollmentResponse> enrollments,
        List<FailureDetail> failures
) {
    public record FailureDetail(
            Long userId,
            String reason
    ) {
    }

    public static ForceEnrollResultResponse of(
            List<EnrollmentResponse> enrollments,
            List<FailureDetail> failures
    ) {
        return new ForceEnrollResultResponse(
                enrollments.size(),
                failures.size(),
                enrollments,
                failures
        );
    }
}
