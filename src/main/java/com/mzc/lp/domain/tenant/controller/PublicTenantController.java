package com.mzc.lp.domain.tenant.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.tenant.dto.response.PublicBrandingResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantCategoryResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantFeaturesResponse;
import com.mzc.lp.domain.tenant.service.TenantCategoryService;
import com.mzc.lp.domain.tenant.service.TenantSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공개 테넌트 API 컨트롤러
 * 인증 없이 접근 가능한 테넌트 정보 제공
 */
@Tag(name = "Public Tenant", description = "공개 테넌트 정보 API (인증 불필요)")
@RestController
@RequestMapping("/api/public/tenants")
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantSettingsService tenantSettingsService;
    private final TenantCategoryService tenantCategoryService;

    @Operation(
        summary = "테넌트 브랜딩 정보 조회 (공개)",
        description = "subdomain 또는 customDomain으로 테넌트의 브랜딩 정보를 조회합니다. " +
                     "인증이 필요하지 않으며, 민감하지 않은 정보만 반환합니다. " +
                     "테넌트를 찾을 수 없거나 ACTIVE 상태가 아니면 기본 브랜딩을 반환합니다."
    )
    @GetMapping("/branding")
    public ResponseEntity<ApiResponse<PublicBrandingResponse>> getPublicBranding(
            @Parameter(description = "테넌트 식별자 (subdomain 또는 customDomain)", required = true)
            @RequestParam String identifier,

            @Parameter(description = "식별자 타입 (subdomain 또는 customDomain)", example = "subdomain")
            @RequestParam(defaultValue = "subdomain") String type
    ) {
        PublicBrandingResponse response = tenantSettingsService.getPublicBranding(identifier, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "테넌트 기능 설정 조회 (공개)",
        description = "subdomain 또는 customDomain으로 테넌트의 기능 On/Off 설정을 조회합니다. " +
                     "인증이 필요하지 않으며, 테넌트를 찾을 수 없으면 기본 설정을 반환합니다."
    )
    @GetMapping("/features")
    public ResponseEntity<ApiResponse<TenantFeaturesResponse>> getPublicFeatures(
            @Parameter(description = "테넌트 식별자 (subdomain 또는 customDomain)", required = true)
            @RequestParam String identifier,

            @Parameter(description = "식별자 타입 (subdomain 또는 customDomain)", example = "subdomain")
            @RequestParam(defaultValue = "subdomain") String type
    ) {
        TenantFeaturesResponse response = tenantSettingsService.getPublicTenantFeatures(identifier, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "테넌트 카테고리 조회 (공개)",
        description = "subdomain 또는 customDomain으로 테넌트의 커스텀 카테고리를 조회합니다. " +
                     "인증이 필요하지 않으며, 활성화된 카테고리만 반환합니다."
    )
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<TenantCategoryResponse>>> getPublicCategories(
            @Parameter(description = "테넌트 식별자 (subdomain 또는 customDomain)", required = true)
            @RequestParam String identifier,

            @Parameter(description = "식별자 타입 (subdomain 또는 customDomain)", example = "subdomain")
            @RequestParam(defaultValue = "subdomain") String type
    ) {
        List<TenantCategoryResponse> response = tenantCategoryService.getPublicCategories(identifier, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
