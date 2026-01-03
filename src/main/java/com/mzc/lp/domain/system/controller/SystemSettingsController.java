package com.mzc.lp.domain.system.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.system.dto.request.UpdateSystemSettingsRequest;
import com.mzc.lp.domain.system.dto.request.UpdateTenantDefaultsRequest;
import com.mzc.lp.domain.system.dto.response.SystemSettingsResponse;
import com.mzc.lp.domain.system.dto.response.TenantDefaultsResponse;
import com.mzc.lp.domain.system.service.SystemSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * SA 시스템 설정 컨트롤러
 * System Admin(SA)가 플랫폼 전역 설정을 관리
 */
@Tag(name = "System Settings", description = "SA 시스템 설정 관리 API")
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    // ============================================
    // 시스템 설정 API
    // ============================================

    @Operation(summary = "시스템 설정 조회", description = "플랫폼 전역 시스템 설정을 조회합니다")
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<SystemSettingsResponse>> getSystemSettings() {
        SystemSettingsResponse response = systemSettingsService.getSystemSettings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "시스템 설정 업데이트", description = "플랫폼 전역 시스템 설정을 업데이트합니다")
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<SystemSettingsResponse>> updateSystemSettings(
            @Valid @RequestBody UpdateSystemSettingsRequest request
    ) {
        SystemSettingsResponse response = systemSettingsService.updateSystemSettings(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ============================================
    // 테넌트 기본값 API
    // ============================================

    @Operation(summary = "테넌트 기본값 조회", description = "새 테넌트 생성 시 적용되는 기본값을 조회합니다")
    @GetMapping("/tenant-defaults")
    public ResponseEntity<ApiResponse<TenantDefaultsResponse>> getTenantDefaults() {
        TenantDefaultsResponse response = systemSettingsService.getTenantDefaults();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테넌트 기본값 업데이트", description = "새 테넌트 생성 시 적용되는 기본값을 업데이트합니다")
    @PutMapping("/tenant-defaults")
    public ResponseEntity<ApiResponse<TenantDefaultsResponse>> updateTenantDefaults(
            @Valid @RequestBody UpdateTenantDefaultsRequest request
    ) {
        TenantDefaultsResponse response = systemSettingsService.updateTenantDefaults(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
