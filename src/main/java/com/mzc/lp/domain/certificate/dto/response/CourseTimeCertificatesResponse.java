package com.mzc.lp.domain.certificate.dto.response;

import org.springframework.data.domain.Page;

public record CourseTimeCertificatesResponse(
        CourseTimeCertificateSummary summary,
        Page<CertificateResponse> certificates
) {
    public static CourseTimeCertificatesResponse of(
            CourseTimeCertificateSummary summary,
            Page<CertificateResponse> certificates
    ) {
        return new CourseTimeCertificatesResponse(summary, certificates);
    }
}
