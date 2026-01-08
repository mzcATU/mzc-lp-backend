package com.mzc.lp.domain.banner.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.banner.constant.BannerPosition;
import com.mzc.lp.domain.banner.dto.request.CreateBannerRequest;
import com.mzc.lp.domain.banner.dto.request.UpdateBannerRequest;
import com.mzc.lp.domain.banner.dto.response.BannerResponse;
import com.mzc.lp.domain.banner.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Validated
public class BannerController {

    private final BannerService bannerService;

    // ========== 관리자 API (TENANT_ADMIN 권한) ==========

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<BannerResponse> response = bannerService.getAll(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/position/{position}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getByPosition(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable BannerPosition position
    ) {
        List<BannerResponse> response = bannerService.getByPosition(principal.tenantId(), position);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getActiveBanners(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<BannerResponse> response = bannerService.getActiveBanners(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{bannerId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long bannerId
    ) {
        BannerResponse response = bannerService.getById(principal.tenantId(), bannerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBannerRequest request
    ) {
        BannerResponse response = bannerService.create(principal.tenantId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PutMapping("/{bannerId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long bannerId,
            @Valid @RequestBody UpdateBannerRequest request
    ) {
        BannerResponse response = bannerService.update(principal.tenantId(), bannerId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{bannerId}/activate")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> activate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long bannerId
    ) {
        BannerResponse response = bannerService.activate(principal.tenantId(), bannerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{bannerId}/deactivate")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> deactivate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long bannerId
    ) {
        BannerResponse response = bannerService.deactivate(principal.tenantId(), bannerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{bannerId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long bannerId
    ) {
        bannerService.delete(principal.tenantId(), bannerId);
        return ResponseEntity.noContent().build();
    }

    // ========== 공개 API (인증 불필요) ==========

    @GetMapping("/public/displayable")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getDisplayableBanners(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = principal != null ? principal.tenantId() : TenantContext.getCurrentTenantId();
        List<BannerResponse> response = bannerService.getDisplayableBanners(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public/displayable/{position}")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getDisplayableBannersByPosition(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable BannerPosition position
    ) {
        Long tenantId = principal != null ? principal.tenantId() : TenantContext.getCurrentTenantId();
        List<BannerResponse> response = bannerService.getDisplayableBannersByPosition(tenantId, position);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
