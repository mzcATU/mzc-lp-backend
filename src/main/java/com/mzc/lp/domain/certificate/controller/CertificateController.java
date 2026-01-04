package com.mzc.lp.domain.certificate.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import com.mzc.lp.domain.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * 내 수료증 목록 조회
     * GET /api/users/me/certificates
     */
    @GetMapping("/api/users/me/certificates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CertificateResponse>>> getMyCertificates(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<CertificateResponse> response = certificateService.getMyCertificates(
                principal.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 수료증 상세 조회
     * GET /api/certificates/{id}
     */
    @GetMapping("/api/certificates/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificateDetailResponse>> getCertificate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateDetailResponse response = certificateService.getCertificate(id, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 수료증 PDF 다운로드
     * GET /api/certificates/{id}/download
     */
    @GetMapping("/api/certificates/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        byte[] pdfBytes = certificateService.downloadCertificatePdf(id, principal.id());

        String filename = "certificate_" + id + ".pdf";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .body(pdfBytes);
    }

    /**
     * 수료증 진위 확인
     * GET /api/certificates/verify/{certificateNumber}
     */
    @GetMapping("/api/certificates/verify/{certificateNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificateVerifyResponse>> verifyCertificate(
            @PathVariable String certificateNumber
    ) {
        CertificateVerifyResponse response = certificateService.verifyCertificate(certificateNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 수료증 폐기 (관리자)
     * DELETE /api/certificates/{id}
     */
    @DeleteMapping("/api/certificates/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> revokeCertificate(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        certificateService.revokeCertificate(id, reason);
        return ResponseEntity.noContent().build();
    }
}
