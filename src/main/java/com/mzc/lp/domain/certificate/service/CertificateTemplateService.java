package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateCreateRequest;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateUpdateRequest;
import com.mzc.lp.domain.certificate.dto.response.CertificateTemplateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CertificateTemplateService {

    /**
     * 템플릿 생성
     */
    CertificateTemplateResponse createTemplate(CertificateTemplateCreateRequest request, Long createdBy);

    /**
     * 템플릿 목록 조회
     */
    Page<CertificateTemplateResponse> getTemplates(Pageable pageable);

    /**
     * 활성 템플릿 목록 조회
     */
    List<CertificateTemplateResponse> getActiveTemplates();

    /**
     * 템플릿 상세 조회
     */
    CertificateTemplateResponse getTemplate(Long templateId);

    /**
     * 템플릿 수정
     */
    CertificateTemplateResponse updateTemplate(Long templateId, CertificateTemplateUpdateRequest request);

    /**
     * 기본 템플릿으로 설정
     */
    void setAsDefault(Long templateId);

    /**
     * 템플릿 활성화
     */
    void activateTemplate(Long templateId);

    /**
     * 템플릿 비활성화
     */
    void deactivateTemplate(Long templateId);

    /**
     * 템플릿 삭제
     */
    void deleteTemplate(Long templateId);
}
