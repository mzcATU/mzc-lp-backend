package com.mzc.lp.domain.tenant.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.tenant.dto.request.TenantCategoryRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantCategoryResponse;
import com.mzc.lp.domain.tenant.service.TenantCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 테넌트 카테고리 컨트롤러
 * TA(Tenant Admin)가 자신의 테넌트의 커스텀 카테고리를 관리
 */
@Tag(name = "Tenant Categories", description = "테넌트 커스텀 카테고리 관리 API")
@RestController
@RequestMapping("/api/tenant/categories")
@RequiredArgsConstructor
public class TenantCategoryController {

    private final TenantCategoryService tenantCategoryService;

    @Operation(summary = "카테고리 목록 조회", description = "현재 테넌트의 모든 커스텀 카테고리를 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<List<TenantCategoryResponse>>> getCategories() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<TenantCategoryResponse> response = tenantCategoryService.getCategories(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "활성화된 카테고리 목록 조회 (TU용)", description = "활성화된 커스텀 카테고리만 조회합니다")
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<TenantCategoryResponse>>> getPublicCategories() {
        Long tenantId = TenantContext.getCurrentTenantIdOrNull();
        if (tenantId == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        List<TenantCategoryResponse> response = tenantCategoryService.getEnabledCategories(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 생성", description = "새로운 커스텀 카테고리를 추가합니다")
    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantCategoryResponse>> createCategory(
            @Valid @RequestBody TenantCategoryRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantCategoryResponse response = tenantCategoryService.createCategory(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 수정", description = "기존 커스텀 카테고리를 수정합니다")
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantCategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody TenantCategoryRequest request
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantCategoryResponse response = tenantCategoryService.updateCategory(tenantId, categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 삭제", description = "커스텀 카테고리를 삭제합니다")
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        tenantCategoryService.deleteCategory(tenantId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "카테고리 순서 변경", description = "커스텀 카테고리의 순서를 변경합니다")
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<TenantCategoryResponse>>> reorderCategories(
            @RequestBody List<Long> categoryIds
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<TenantCategoryResponse> response = tenantCategoryService.reorderCategories(tenantId, categoryIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
