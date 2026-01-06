package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateCreateRequest;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateUpdateRequest;
import com.mzc.lp.domain.certificate.dto.response.CertificateTemplateResponse;
import com.mzc.lp.domain.certificate.entity.CertificateTemplate;
import com.mzc.lp.domain.certificate.exception.CertificateTemplateCodeDuplicateException;
import com.mzc.lp.domain.certificate.exception.CertificateTemplateNotFoundException;
import com.mzc.lp.domain.certificate.repository.CertificateTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateTemplateServiceImpl implements CertificateTemplateService {

    private final CertificateTemplateRepository certificateTemplateRepository;

    @Override
    @Transactional
    public CertificateTemplateResponse createTemplate(CertificateTemplateCreateRequest request, Long createdBy) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Creating certificate template: code={}, tenantId={}", request.templateCode(), tenantId);

        // 템플릿 코드 중복 체크
        if (certificateTemplateRepository.existsByTemplateCodeAndTenantId(request.templateCode(), tenantId)) {
            throw new CertificateTemplateCodeDuplicateException(request.templateCode());
        }

        CertificateTemplate template = CertificateTemplate.create(
                request.templateCode(),
                request.title(),
                request.description(),
                createdBy
        );

        // 추가 필드 설정
        template.update(
                request.title(),
                request.description(),
                request.backgroundImageUrl(),
                request.signatureImageUrl(),
                request.certificateBodyHtml(),
                request.validityMonths()
        );

        // 기본 템플릿으로 설정 요청 시
        if (Boolean.TRUE.equals(request.isDefault())) {
            // 기존 기본 템플릿 해제
            certificateTemplateRepository.findByTenantIdAndIsDefaultTrue(tenantId)
                    .ifPresent(CertificateTemplate::unsetDefault);
            template.setAsDefault();
        }

        CertificateTemplate saved = certificateTemplateRepository.save(template);
        log.info("Certificate template created: id={}, code={}", saved.getId(), saved.getTemplateCode());

        return CertificateTemplateResponse.from(saved);
    }

    @Override
    public Page<CertificateTemplateResponse> getTemplates(Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return certificateTemplateRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                .map(CertificateTemplateResponse::from);
    }

    @Override
    public List<CertificateTemplateResponse> getActiveTemplates() {
        Long tenantId = TenantContext.getCurrentTenantId();
        return certificateTemplateRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .map(CertificateTemplateResponse::from)
                .toList();
    }

    @Override
    public CertificateTemplateResponse getTemplate(Long templateId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        CertificateTemplate template = certificateTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new CertificateTemplateNotFoundException(templateId));
        return CertificateTemplateResponse.from(template);
    }

    @Override
    @Transactional
    public CertificateTemplateResponse updateTemplate(Long templateId, CertificateTemplateUpdateRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Updating certificate template: id={}, tenantId={}", templateId, tenantId);

        CertificateTemplate template = certificateTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new CertificateTemplateNotFoundException(templateId));

        template.update(
                request.title(),
                request.description(),
                request.backgroundImageUrl(),
                request.signatureImageUrl(),
                request.certificateBodyHtml(),
                request.validityMonths()
        );

        log.info("Certificate template updated: id={}", templateId);
        return CertificateTemplateResponse.from(template);
    }

    @Override
    @Transactional
    public void setAsDefault(Long templateId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Setting template as default: id={}, tenantId={}", templateId, tenantId);

        CertificateTemplate template = certificateTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new CertificateTemplateNotFoundException(templateId));

        // 기존 기본 템플릿 해제
        certificateTemplateRepository.findByTenantIdAndIsDefaultTrue(tenantId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(templateId)) {
                        existing.unsetDefault();
                    }
                });

        template.setAsDefault();
        log.info("Template set as default: id={}", templateId);
    }

    @Override
    @Transactional
    public void activateTemplate(Long templateId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Activating template: id={}, tenantId={}", templateId, tenantId);

        CertificateTemplate template = certificateTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new CertificateTemplateNotFoundException(templateId));

        template.activate();
        log.info("Template activated: id={}", templateId);
    }

    @Override
    @Transactional
    public void deactivateTemplate(Long templateId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Deactivating template: id={}, tenantId={}", templateId, tenantId);

        CertificateTemplate template = certificateTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new CertificateTemplateNotFoundException(templateId));

        template.deactivate();
        log.info("Template deactivated: id={}", templateId);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Deleting template: id={}, tenantId={}", templateId, tenantId);

        CertificateTemplate template = certificateTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new CertificateTemplateNotFoundException(templateId));

        certificateTemplateRepository.delete(template);
        log.info("Template deleted: id={}", templateId);
    }
}
