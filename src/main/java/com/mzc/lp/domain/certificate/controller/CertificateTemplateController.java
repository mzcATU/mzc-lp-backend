package com.mzc.lp.domain.certificate.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateCreateRequest;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateUpdateRequest;
import com.mzc.lp.domain.certificate.dto.response.CertificateTemplateResponse;
import com.mzc.lp.domain.certificate.service.CertificateTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificate-templates")
@RequiredArgsConstructor
public class CertificateTemplateController {

    private final CertificateTemplateService certificateTemplateService;

    /**
     * 템플릿 생성
     * POST /api/certificate-templates
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CertificateTemplateResponse>> createTemplate(
            @Valid @RequestBody CertificateTemplateCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateTemplateResponse response = certificateTemplateService.createTemplate(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 템플릿 목록 조회
     * GET /api/certificate-templates
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CertificateTemplateResponse>>> getTemplates(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CertificateTemplateResponse> response = certificateTemplateService.getTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 활성 템플릿 목록 조회
     * GET /api/certificate-templates/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<CertificateTemplateResponse>>> getActiveTemplates() {
        List<CertificateTemplateResponse> response = certificateTemplateService.getActiveTemplates();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 템플릿 상세 조회
     * GET /api/certificate-templates/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CertificateTemplateResponse>> getTemplate(
            @PathVariable Long id
    ) {
        CertificateTemplateResponse response = certificateTemplateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 템플릿 수정
     * PUT /api/certificate-templates/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CertificateTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CertificateTemplateUpdateRequest request
    ) {
        CertificateTemplateResponse response = certificateTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 기본 템플릿으로 설정
     * POST /api/certificate-templates/{id}/set-default
     */
    @PostMapping("/{id}/set-default")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> setAsDefault(@PathVariable Long id) {
        certificateTemplateService.setAsDefault(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 템플릿 활성화
     * POST /api/certificate-templates/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> activateTemplate(@PathVariable Long id) {
        certificateTemplateService.activateTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 템플릿 비활성화
     * POST /api/certificate-templates/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long id) {
        certificateTemplateService.deactivateTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 템플릿 삭제
     * DELETE /api/certificate-templates/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        certificateTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
