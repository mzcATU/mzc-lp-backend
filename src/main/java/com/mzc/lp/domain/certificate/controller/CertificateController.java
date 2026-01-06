package com.mzc.lp.domain.certificate.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.certificate.dto.request.ReissueCertificateRequest;
import com.mzc.lp.domain.certificate.dto.request.RevokeCertificateRequest;
import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import com.mzc.lp.domain.certificate.dto.response.CourseTimeCertificatesResponse;
import com.mzc.lp.domain.certificate.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
     * 수료증 발급
     * POST /api/enrollments/{enrollmentId}/certificate
     */
    @PostMapping("/api/enrollments/{enrollmentId}/certificate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificateDetailResponse>> issueCertificate(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateDetailResponse response = certificateService.issueCertificateByUser(
                enrollmentId, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 수강별 수료증 조회
     * GET /api/enrollments/{enrollmentId}/certificate
     */
    @GetMapping("/api/enrollments/{enrollmentId}/certificate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificateDetailResponse>> getCertificateByEnrollment(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateDetailResponse response = certificateService.getCertificateByEnrollment(
                enrollmentId, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 수료증 재발급
     * POST /api/certificates/{id}/reissue
     */
    @PostMapping("/api/certificates/{id}/reissue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificateDetailResponse>> reissueCertificate(
            @PathVariable Long id,
            @Valid @RequestBody ReissueCertificateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateDetailResponse response = certificateService.reissueCertificate(
                id, request.reason(), principal.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

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
     * 수료증 진위 확인 (Public API - 인증 불필요)
     * GET /api/certificates/verify/{certificateNumber}
     */
    @GetMapping("/api/certificates/verify/{certificateNumber}")
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
    public ResponseEntity<ApiResponse<CertificateDetailResponse>> revokeCertificate(
            @PathVariable Long id,
            @Valid @RequestBody RevokeCertificateRequest request
    ) {
        CertificateDetailResponse response = certificateService.revokeCertificate(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 차수별 수료증 현황 조회 (관리자)
     * GET /api/times/{timeId}/certificates
     */
    @GetMapping("/api/times/{timeId}/certificates")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeCertificatesResponse>> getCertificatesByCourseTime(
            @PathVariable Long timeId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        CourseTimeCertificatesResponse response = certificateService.getCertificatesByCourseTime(timeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
