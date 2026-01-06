package com.mzc.lp.domain.certificate.dto.response;

import com.mzc.lp.domain.certificate.entity.CertificateTemplate;

import java.time.Instant;

public record CertificateTemplateResponse(
        Long id,
        String templateCode,
        String title,
        String description,
        String backgroundImageUrl,
        String signatureImageUrl,
        String certificateBodyHtml,
        Integer validityMonths,
        boolean isDefault,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
    public static CertificateTemplateResponse from(CertificateTemplate template) {
        return new CertificateTemplateResponse(
                template.getId(),
                template.getTemplateCode(),
                template.getTitle(),
                template.getDescription(),
                template.getBackgroundImageUrl(),
                template.getSignatureImageUrl(),
                template.getCertificateBodyHtml(),
                template.getValidityMonths(),
                template.isDefault(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
