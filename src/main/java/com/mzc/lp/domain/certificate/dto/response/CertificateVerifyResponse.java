package com.mzc.lp.domain.certificate.dto.response;

import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.entity.Certificate;

import java.time.Instant;

public record CertificateVerifyResponse(
        boolean valid,
        String certificateNumber,
        String userName,
        String programTitle,
        String courseTimeTitle,
        Instant completedAt,
        Instant issuedAt,
        CertificateStatus status,
        String message
) {
    public static CertificateVerifyResponse from(Certificate certificate) {
        boolean isValid = certificate.isValid();
        String message = isValid
                ? "유효한 수료증입니다."
                : "폐기된 수료증입니다. 사유: " + certificate.getRevokedReason();

        return new CertificateVerifyResponse(
                isValid,
                certificate.getCertificateNumber(),
                certificate.getUserName(),
                certificate.getProgramTitle(),
                certificate.getCourseTimeTitle(),
                certificate.getCompletedAt(),
                certificate.getIssuedAt(),
                certificate.getStatus(),
                message
        );
    }

    public static CertificateVerifyResponse notFound(String certificateNumber) {
        return new CertificateVerifyResponse(
                false,
                certificateNumber,
                null,
                null,
                null,
                null,
                null,
                null,
                "존재하지 않는 수료증 번호입니다."
        );
    }
}
