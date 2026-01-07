package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.TenantCategoryRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantCategoryResponse;

import java.util.List;

/**
 * 테넌트 카테고리 서비스 인터페이스
 */
public interface TenantCategoryService {

    /**
     * 테넌트의 모든 카테고리 조회
     */
    List<TenantCategoryResponse> getCategories(Long tenantId);

    /**
     * 테넌트의 활성화된 카테고리만 조회
     */
    List<TenantCategoryResponse> getEnabledCategories(Long tenantId);

    /**
     * 카테고리 생성
     */
    TenantCategoryResponse createCategory(Long tenantId, TenantCategoryRequest request);

    /**
     * 카테고리 수정
     */
    TenantCategoryResponse updateCategory(Long tenantId, Long categoryId, TenantCategoryRequest request);

    /**
     * 카테고리 삭제
     */
    void deleteCategory(Long tenantId, Long categoryId);

    /**
     * 카테고리 순서 변경
     */
    List<TenantCategoryResponse> reorderCategories(Long tenantId, List<Long> categoryIds);

    /**
     * 공개 API: 서브도메인/커스텀도메인으로 카테고리 조회
     */
    List<TenantCategoryResponse> getPublicCategories(String identifier, String type);
}
