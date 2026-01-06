package com.mzc.lp.domain.tenant.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.tenant.dto.request.CreateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantStatusRequest;
import com.mzc.lp.domain.tenant.dto.response.CreateTenantResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantResponse;
import com.mzc.lp.domain.tenant.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Validated
public class TenantController {

    private final TenantService tenantService;

    /**
     * 테넌트 생성 (TENANT_ADMIN 계정도 함께 생성)
     * POST /api/tenants
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<CreateTenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        CreateTenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 테넌트 목록 조회 (페이징, 키워드 검색)
     * GET /api/tenants
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TenantResponse>>> getTenants(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TenantResponse> response = tenantService.getTenants(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 테넌트 상세 조회
     * GET /api/tenants/{tenantId}
     */
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenant(
            @PathVariable @Positive Long tenantId
    ) {
        TenantResponse response = tenantService.getTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 테넌트 코드로 조회
     * GET /api/tenants/code/{code}
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantByCode(
            @PathVariable String code
    ) {
        TenantResponse response = tenantService.getTenantByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 테넌트 수정
     * PUT /api/tenants/{tenantId}
     */
    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable @Positive Long tenantId,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        TenantResponse response = tenantService.updateTenant(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 테넌트 상태 변경
     * PATCH /api/tenants/{tenantId}/status
     */
    @PatchMapping("/{tenantId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenantStatus(
            @PathVariable @Positive Long tenantId,
            @Valid @RequestBody UpdateTenantStatusRequest request
    ) {
        TenantResponse response = tenantService.updateTenantStatus(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 테넌트 삭제
     * DELETE /api/tenants/{tenantId}
     */
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable @Positive Long tenantId
    ) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
