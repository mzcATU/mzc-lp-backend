package com.mzc.lp.domain.certificate.constant;

/**
 * 수료증 상태
 */
public enum CertificateStatus {
    ISSUED,     // 발급됨 (초기 상태, VALID와 동일)
    VALID,      // 유효함
    EXPIRED,    // 만료됨 (유효 기간 초과)
    REVOKED     // 폐기됨 (무효)
}
