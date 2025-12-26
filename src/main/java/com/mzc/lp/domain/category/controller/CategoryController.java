package com.mzc.lp.domain.category.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.category.dto.request.CreateCategoryRequest;
import com.mzc.lp.domain.category.dto.request.UpdateCategoryRequest;
import com.mzc.lp.domain.category.dto.response.CategoryResponse;
import com.mzc.lp.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 생성
     * POST /api/categories
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 카테고리 목록 조회
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CategoryResponse> response = categoryService.getCategories(activeOnly);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 상세 조회
     * GET /api/categories/{categoryId}
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable @Positive Long categoryId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 수정
     * PUT /api/categories/{categoryId}
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 삭제
     * DELETE /api/categories/{categoryId}
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @Positive Long categoryId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
