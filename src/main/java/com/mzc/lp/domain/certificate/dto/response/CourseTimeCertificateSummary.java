package com.mzc.lp.domain.certificate.dto.response;

public record CourseTimeCertificateSummary(
        long total,
        long issued,
        long pending
) {
    public static CourseTimeCertificateSummary of(long total, long issued) {
        return new CourseTimeCertificateSummary(total, issued, total - issued);
    }
}
