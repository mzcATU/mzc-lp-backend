package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CertificateService {

    /**
     * 수료증 발급 (내부/이벤트 호출용)
     */
    CertificateDetailResponse issueCertificate(Long enrollmentId);

    /**
     * 수료증 발급 (사용자 API 요청)
     */
    CertificateDetailResponse issueCertificateByUser(Long enrollmentId, Long userId);

    /**
     * 수료증 재발급
     * - 기존 수료증 자동 무효화
     * - 새로운 수료증 번호 발급
     */
    CertificateDetailResponse reissueCertificate(Long certificateId, String reason, Long userId);

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
