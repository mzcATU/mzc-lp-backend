package com.mzc.lp.domain.category.service;

import com.mzc.lp.domain.category.dto.request.CreateCategoryRequest;
import com.mzc.lp.domain.category.dto.request.UpdateCategoryRequest;
import com.mzc.lp.domain.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    /**
     * 카테고리 생성
     * @param request 생성 요청 DTO
     * @return 생성된 카테고리 정보
     */
    CategoryResponse createCategory(CreateCategoryRequest request);

    /**
     * 카테고리 목록 조회 (정렬순)
     * @param activeOnly 활성 카테고리만 조회할지 여부
     * @return 카테고리 목록
     */
    List<CategoryResponse> getCategories(Boolean activeOnly);

    /**
     * 카테고리 상세 조회
     * @param categoryId 카테고리 ID
     * @return 카테고리 정보
     */
    CategoryResponse getCategory(Long categoryId);

    /**
     * 카테고리 수정
     * @param categoryId 카테고리 ID
     * @param request 수정 요청 DTO
     * @return 수정된 카테고리 정보
     */
    CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request);

    /**
     * 카테고리 삭제
     * @param categoryId 카테고리 ID
     */
    void deleteCategory(Long categoryId);
}
