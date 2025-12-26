package com.mzc.lp.domain.category.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.category.dto.request.CreateCategoryRequest;
import com.mzc.lp.domain.category.dto.request.UpdateCategoryRequest;
import com.mzc.lp.domain.category.dto.response.CategoryResponse;
import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.category.exception.CategoryNotFoundException;
import com.mzc.lp.domain.category.exception.DuplicateCategoryCodeException;
import com.mzc.lp.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category: name={}, code={}", request.name(), request.code());

        Long tenantId = TenantContext.getCurrentTenantId();

        // 중복 코드 체크
        if (categoryRepository.existsByCodeAndTenantId(request.code(), tenantId)) {
            throw new DuplicateCategoryCodeException(request.code());
        }

        Category category = Category.create(
                request.name(),
                request.code(),
                request.sortOrder()
        );

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: id={}, name={}", savedCategory.getId(), savedCategory.getName());

        return CategoryResponse.from(savedCategory);
    }

    @Override
    public List<CategoryResponse> getCategories(Boolean activeOnly) {
        log.debug("Getting categories: activeOnly={}", activeOnly);

        Long tenantId = TenantContext.getCurrentTenantId();

        List<Category> categories;
        if (Boolean.TRUE.equals(activeOnly)) {
            categories = categoryRepository.findByTenantIdAndActiveOrderBySortOrderAsc(tenantId, true);
        } else {
            categories = categoryRepository.findByTenantIdOrderBySortOrderAsc(tenantId);
        }

        return categories.stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public CategoryResponse getCategory(Long categoryId) {
        log.debug("Getting category: categoryId={}", categoryId);

        Category category = categoryRepository.findByIdAndTenantId(categoryId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return CategoryResponse.from(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        log.info("Updating category: categoryId={}", categoryId);

        Long tenantId = TenantContext.getCurrentTenantId();

        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // 코드 변경 시 중복 체크
        if (request.code() != null && !request.code().equals(category.getCode())) {
            if (categoryRepository.existsByCodeAndTenantIdAndIdNot(request.code(), tenantId, categoryId)) {
                throw new DuplicateCategoryCodeException(request.code());
            }
        }

        category.update(
                request.name(),
                request.code(),
                request.sortOrder(),
                request.active()
        );

        log.info("Category updated: id={}", categoryId);
        return CategoryResponse.from(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category: categoryId={}", categoryId);

        Category category = categoryRepository.findByIdAndTenantId(categoryId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        categoryRepository.delete(category);
        log.info("Category deleted: id={}", categoryId);
    }
}
