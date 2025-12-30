package com.mzc.lp.domain.tenant.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantSettingsRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantSettingsResponse;
import com.mzc.lp.domain.tenant.service.TenantSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 테넌트 설정 컨트롤러
 * TA(Tenant Admin)가 자신의 테넌트 설정을 관리
 */
@Tag(name = "Tenant Settings", description = "테넌트 설정 관리 API")
@RestController
@RequestMapping("/api/tenant/settings")
@RequiredArgsConstructor
public class TenantSettingsController {

    private final TenantSettingsService tenantSettingsService;

    @Operation(summary = "테넌트 설정 조회", description = "현재 테넌트의 설정을 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> getSettings() {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantSettingsResponse response = tenantSettingsService.getSettings(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테넌트 설정 업데이트", description = "현재 테넌트의 설정을 업데이트합니다")
    @PutMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateTenantSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantSettingsResponse response = tenantSettingsService.updateSettings(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "브랜딩 설정 업데이트", description = "로고, 색상 등 브랜딩 설정만 업데이트합니다")
    @PatchMapping("/branding")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateBranding(
            @Valid @RequestBody UpdateTenantSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        // 브랜딩 관련 필드만 포함된 요청 처리
        UpdateTenantSettingsRequest brandingRequest = new UpdateTenantSettingsRequest(
                request.logoUrl(),
                request.faviconUrl(),
                request.primaryColor(),
                request.secondaryColor(),
                request.fontFamily(),
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
        TenantSettingsResponse response = tenantSettingsService.updateSettings(tenantId, brandingRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 관리 설정 업데이트", description = "사용자 가입, 인증 관련 설정만 업데이트합니다")
    @PatchMapping("/user-management")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateUserManagement(
            @Valid @RequestBody UpdateTenantSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        // 사용자 관리 관련 필드만 포함된 요청 처리
        UpdateTenantSettingsRequest userManagementRequest = new UpdateTenantSettingsRequest(
                null, null, null, null, null, null, null,
                request.allowSelfRegistration(),
                request.requireEmailVerification(),
                request.requireApproval(),
                request.allowedEmailDomains(),
                null, null, null, null, null, null, null
        );
        TenantSettingsResponse response = tenantSettingsService.updateSettings(tenantId, userManagementRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
