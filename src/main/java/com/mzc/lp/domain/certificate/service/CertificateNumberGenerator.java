package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.domain.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CertificateNumberGenerator {

    private final CertificateRepository certificateRepository;

    /**
     * 테넌트별/연도별 Sequence 기반 수료증 번호 생성
     * 형식: CERT-{tenantId(3자리)}-{year}-{sequence(6자리)}
     * 예시: CERT-001-2026-000001
     */
    public String generate(Long tenantId) {
        int currentYear = LocalDate.now().getYear();
        String prefix = String.format("CERT-%03d-%d-", tenantId, currentYear);

        Long currentSequence = certificateRepository
                .countByTenantIdAndCertificateNumberStartingWith(tenantId, prefix);

        long nextSequence = currentSequence + 1;

        return String.format("CERT-%03d-%d-%06d", tenantId, currentYear, nextSequence);
    }
}
