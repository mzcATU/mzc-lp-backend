package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CertificateService {

    /**
     * 수료 시 수료증 자동 발급
     */
    CertificateDetailResponse issueCertificate(Long enrollmentId);

    /**
     * 내 수료증 목록 조회
     */
    Page<CertificateResponse> getMyCertificates(Long userId, Pageable pageable);

    /**
     * 수료증 상세 조회
     */
    CertificateDetailResponse getCertificate(Long certificateId, Long userId);

    /**
     * 수료증 PDF 다운로드
     */
    byte[] downloadCertificatePdf(Long certificateId, Long userId);

    /**
     * 수료증 진위 확인
     */
    CertificateVerifyResponse verifyCertificate(String certificateNumber);

    /**
     * 수료증 폐기 (관리자)
     */
    void revokeCertificate(Long certificateId, String reason);
}
