package com.mzc.lp.domain.tenant.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.tenant.dto.request.UpdateCustomDomainRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantDomainSettingsResponse;
import com.mzc.lp.domain.tenant.service.DomainSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ta/domain-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class TaDomainSettingsController {

    private final DomainSettingsService domainSettingsService;

    /**
     * 도메인 설정 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<TenantDomainSettingsResponse>> getDomainSettings(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TenantDomainSettingsResponse response = domainSettingsService.getDomainSettings(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 커스텀 도메인 설정/수정
     */
    @PutMapping("/custom")
    public ResponseEntity<ApiResponse<TenantDomainSettingsResponse>> updateCustomDomain(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCustomDomainRequest request
    ) {
        TenantDomainSettingsResponse response = domainSettingsService.updateCustomDomain(
                principal.tenantId(),
                request.customDomain()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 커스텀 도메인 삭제
     */
    @DeleteMapping("/custom")
    public ResponseEntity<Void> deleteCustomDomain(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        domainSettingsService.deleteCustomDomain(principal.tenantId());
        return ResponseEntity.noContent().build();
    }
}
