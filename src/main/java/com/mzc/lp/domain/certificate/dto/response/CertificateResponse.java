package com.mzc.lp.domain.certificate.dto.response;

import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.entity.Certificate;

import java.time.Instant;

public record CertificateResponse(
        Long id,
        String certificateNumber,
        String programTitle,
        String courseTimeTitle,
        String userName,
        Instant completedAt,
        Instant issuedAt,
        CertificateStatus status
) {
    public static CertificateResponse from(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getProgramTitle(),
                certificate.getCourseTimeTitle(),
                certificate.getUserName(),
                certificate.getCompletedAt(),
                certificate.getIssuedAt(),
                certificate.getStatus()
        );
    }
}
