package com.mzc.lp.domain.certificate.dto.response;

import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.entity.Certificate;

import java.time.Instant;

public record CertificateVerifyResponse(
        boolean valid,
        String certificateNumber,
        String userName,
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
                maskName(certificate.getUserName()),
                certificate.getCourseTimeTitle(),
                certificate.getCompletedAt(),
                certificate.getIssuedAt(),
                certificate.getStatus(),
                message
        );
    }

    /**
     * 이름 마스킹 (Public API용)
     * - 1자: * (예: 홍 → *)
     * - 2자: X* (예: 홍길 → 홍*)
     * - 3자: X*X (예: 홍길동 → 홍*동)
     * - 4자 이상: 첫글자 + ** + 마지막글자 (예: 홍길동수 → 홍**수)
     */
    private static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }

        int length = name.length();
        if (length == 1) {
            return "*";
        } else if (length == 2) {
            return name.charAt(0) + "*";
        } else if (length == 3) {
            return name.charAt(0) + "*" + name.charAt(2);
        } else {
            // 4자 이상: 첫글자와 마지막글자만 표시, 중간은 **
            return name.charAt(0) + "*".repeat(length - 2) + name.charAt(length - 1);
        }
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
                "존재하지 않는 수료증 번호입니다."
        );
    }
}
