package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.domain.certificate.entity.Certificate;

public interface CertificatePdfService {

    /**
     * 수료증 PDF 생성
     * @param certificate 수료증 엔티티
     * @return PDF 바이트 배열
     */
    byte[] generatePdf(Certificate certificate);
}
