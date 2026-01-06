package com.mzc.lp.domain.tenant.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.tenant.dto.request.NavigationItemRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateDesignSettingsRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateLayoutSettingsRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantSettingsRequest;
import com.mzc.lp.domain.tenant.dto.response.NavigationItemResponse;
import com.mzc.lp.domain.tenant.dto.response.PublicBrandingResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantSettingsResponse;
import com.mzc.lp.domain.tenant.service.TenantSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ============================================
    // 기본 설정 API
    // ============================================

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

    // ============================================
    // 디자인/브랜딩 설정 API
    // ============================================

    @Operation(summary = "현재 테넌트 브랜딩 조회", description = "로그인한 사용자의 테넌트 브랜딩 정보를 조회합니다")
    @GetMapping("/branding")
    public ResponseEntity<ApiResponse<PublicBrandingResponse>> getBranding() {
        Long tenantId = TenantContext.getCurrentTenantId();
        PublicBrandingResponse response = tenantSettingsService.getBrandingByTenantId(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "디자인 설정 업데이트", description = "로고, 색상, 폰트 등 디자인 설정을 업데이트합니다")
    @PutMapping("/design")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateDesignSettings(
            @Valid @RequestBody UpdateDesignSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantSettingsResponse response = tenantSettingsService.updateDesignSettings(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "브랜딩 설정 업데이트 (레거시)", description = "로고, 색상 등 브랜딩 설정만 업데이트합니다")
    @PatchMapping("/branding")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateBranding(
            @Valid @RequestBody UpdateTenantSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
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

    // ============================================
    // 레이아웃 설정 API
    // ============================================

    @Operation(summary = "레이아웃 설정 업데이트", description = "헤더, 사이드바, 푸터, 콘텐츠 영역 설정을 업데이트합니다")
    @PutMapping("/layout")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateLayoutSettings(
            @Valid @RequestBody UpdateLayoutSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantSettingsResponse response = tenantSettingsService.updateLayoutSettings(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ============================================
    // 사용자 관리 설정 API
    // ============================================

    @Operation(summary = "사용자 관리 설정 업데이트", description = "사용자 가입, 인증 관련 설정만 업데이트합니다")
    @PatchMapping("/user-management")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantSettingsResponse>> updateUserManagement(
            @Valid @RequestBody UpdateTenantSettingsRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
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

    // ============================================
    // 네비게이션 관리 API
    // ============================================

    @Operation(summary = "네비게이션 항목 목록 조회", description = "현재 테넌트의 네비게이션 메뉴 항목을 조회합니다")
    @GetMapping("/navigation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<List<NavigationItemResponse>>> getNavigationItems() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<NavigationItemResponse> response = tenantSettingsService.getNavigationItems(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "네비게이션 항목 생성", description = "새로운 네비게이션 메뉴 항목을 추가합니다")
    @PostMapping("/navigation")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<NavigationItemResponse>> createNavigationItem(
            @Valid @RequestBody NavigationItemRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        NavigationItemResponse response = tenantSettingsService.createNavigationItem(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "네비게이션 항목 수정", description = "기존 네비게이션 메뉴 항목을 수정합니다")
    @PutMapping("/navigation/{itemId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<NavigationItemResponse>> updateNavigationItem(
            @PathVariable Long itemId,
            @Valid @RequestBody NavigationItemRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        NavigationItemResponse response = tenantSettingsService.updateNavigationItem(tenantId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "네비게이션 항목 삭제", description = "네비게이션 메뉴 항목을 삭제합니다")
    @DeleteMapping("/navigation/{itemId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNavigationItem(@PathVariable Long itemId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        tenantSettingsService.deleteNavigationItem(tenantId, itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "네비게이션 항목 순서 변경", description = "네비게이션 메뉴 항목의 순서를 변경합니다")
    @PutMapping("/navigation/reorder")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<NavigationItemResponse>>> reorderNavigationItems(
            @RequestBody List<Long> itemIds
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<NavigationItemResponse> response = tenantSettingsService.reorderNavigationItems(tenantId, itemIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "네비게이션 초기화", description = "네비게이션 메뉴를 기본값으로 초기화합니다")
    @PostMapping("/navigation/reset")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<NavigationItemResponse>>> resetNavigationItems() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<NavigationItemResponse> response = tenantSettingsService.initializeDefaultNavigationItems(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
