package com.mzc.lp.domain.certificate.dto.response;

import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.entity.Certificate;

import java.time.Instant;

public record CertificateDetailResponse(
        Long id,
        String certificateNumber,
        Long userId,
        String userName,
        Long enrollmentId,
        Long courseTimeId,
        String courseTimeTitle,
        Long programId,
        String programTitle,
        Instant completedAt,
        Instant issuedAt,
        CertificateStatus status,
        Instant revokedAt,
        String revokedReason
) {
    public static CertificateDetailResponse from(Certificate certificate) {
        return new CertificateDetailResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getUserId(),
                certificate.getUserName(),
                certificate.getEnrollmentId(),
                certificate.getCourseTimeId(),
                certificate.getCourseTimeTitle(),
                certificate.getProgramId(),
                certificate.getProgramTitle(),
                certificate.getCompletedAt(),
                certificate.getIssuedAt(),
                certificate.getStatus(),
                certificate.getRevokedAt(),
                certificate.getRevokedReason()
        );
    }
}
