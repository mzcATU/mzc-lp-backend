package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.dto.request.TenantCategoryRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantCategoryResponse;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.entity.TenantCategory;
import com.mzc.lp.domain.tenant.exception.TenantDomainNotFoundException;
import com.mzc.lp.domain.tenant.repository.TenantCategoryRepository;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 테넌트 카테고리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantCategoryServiceImpl implements TenantCategoryService {

    private final TenantCategoryRepository tenantCategoryRepository;
    private final TenantRepository tenantRepository;

    @Override
    public List<TenantCategoryResponse> getCategories(Long tenantId) {
        return tenantCategoryRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId)
                .stream()
                .map(TenantCategoryResponse::from)
                .toList();
    }

    @Override
    public List<TenantCategoryResponse> getEnabledCategories(Long tenantId) {
        return tenantCategoryRepository.findByTenantIdAndEnabledTrueOrderByDisplayOrderAsc(tenantId)
                .stream()
                .map(TenantCategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TenantCategoryResponse createCategory(Long tenantId, TenantCategoryRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Tenant not found: " + tenantId));

        // 슬러그 중복 검사
        if (tenantCategoryRepository.existsByTenantIdAndSlug(tenantId, request.slug())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 슬러그입니다: " + request.slug());
        }

        // displayOrder 설정
        int maxOrder = tenantCategoryRepository.findMaxDisplayOrderByTenantId(tenantId);
        int newOrder = request.displayOrder() != null ? request.displayOrder() : maxOrder + 1;

        TenantCategory category = TenantCategory.create(
                tenant,
                request.name(),
                request.slug(),
                request.description(),
                request.icon(),
                newOrder
        );

        TenantCategory saved = tenantCategoryRepository.save(category);
        return TenantCategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public TenantCategoryResponse updateCategory(Long tenantId, Long categoryId, TenantCategoryRequest request) {
        TenantCategory category = tenantCategoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Category not found: " + categoryId));

        // 슬러그 변경 시 중복 검사
        if (request.slug() != null && !request.slug().equals(category.getSlug())) {
            if (tenantCategoryRepository.existsByTenantIdAndSlugAndIdNot(tenantId, request.slug(), categoryId)) {
                throw new IllegalArgumentException("이미 존재하는 카테고리 슬러그입니다: " + request.slug());
            }
        }

        category.update(
                request.name(),
                request.slug(),
                request.description(),
                request.icon(),
                request.enabled()
        );

        if (request.displayOrder() != null) {
            category.updateOrder(request.displayOrder());
        }

        return TenantCategoryResponse.from(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long tenantId, Long categoryId) {
        TenantCategory category = tenantCategoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Category not found: " + categoryId));

        tenantCategoryRepository.delete(category);
    }

    @Override
    @Transactional
    public List<TenantCategoryResponse> reorderCategories(Long tenantId, List<Long> categoryIds) {
        List<TenantCategory> categories = tenantCategoryRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId);

        for (int i = 0; i < categoryIds.size(); i++) {
            final int order = i + 1;
            final Long id = categoryIds.get(i);
            categories.stream()
                    .filter(cat -> cat.getId().equals(id))
                    .findFirst()
                    .ifPresent(cat -> cat.updateOrder(order));
        }

        return categories.stream()
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(TenantCategoryResponse::from)
                .toList();
    }

    @Override
    public List<TenantCategoryResponse> getPublicCategories(String identifier, String type) {
        Tenant tenant = findTenantByIdentifier(identifier, type);
        if (tenant == null) {
            return Collections.emptyList();
        }

        return getEnabledCategories(tenant.getId());
    }

    private Tenant findTenantByIdentifier(String identifier, String type) {
        if ("subdomain".equals(type)) {
            return tenantRepository.findBySubdomain(identifier)
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        } else if ("customDomain".equals(type)) {
            return tenantRepository.findByCustomDomain(identifier)
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        }
        return null;
    }
}
