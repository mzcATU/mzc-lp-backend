package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.NavigationItemRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateDesignSettingsRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateLayoutSettingsRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantSettingsRequest;
import com.mzc.lp.domain.tenant.dto.response.NavigationItemResponse;
import com.mzc.lp.domain.tenant.dto.response.PublicBrandingResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantSettingsResponse;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.entity.NavigationItem;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.entity.TenantSettings;
import com.mzc.lp.domain.tenant.exception.TenantDomainNotFoundException;
import com.mzc.lp.domain.tenant.repository.NavigationItemRepository;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.tenant.repository.TenantSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantSettingsServiceImpl implements TenantSettingsService {

    private final TenantSettingsRepository tenantSettingsRepository;
    private final TenantRepository tenantRepository;
    private final NavigationItemRepository navigationItemRepository;

    @Override
    @Transactional
    public TenantSettingsResponse getSettings(Long tenantId) {
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> initializeAndGet(tenantId));
        return TenantSettingsResponse.from(settings);
    }

    @Override
    @Transactional
    public TenantSettingsResponse updateSettings(Long tenantId, UpdateTenantSettingsRequest request) {
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> initializeAndGet(tenantId));

        // 브랜딩 설정 업데이트 (기존 호환성 유지)
        settings.updateBranding(
                request.logoUrl(),
                null, // darkLogoUrl
                request.faviconUrl(),
                request.primaryColor(),
                request.secondaryColor(),
                null, // accentColor
                request.fontFamily(),
                null, // headingFont
                null  // bodyFont
        );

        // 일반 설정 업데이트
        settings.updateGeneralSettings(
                request.defaultLanguage(),
                request.timezone()
        );

        // 사용자 관리 설정 업데이트
        settings.updateUserManagementSettings(
                request.allowSelfRegistration(),
                request.requireEmailVerification(),
                request.requireApproval(),
                request.allowedEmailDomains()
        );

        // 제한 설정 업데이트
        settings.updateLimits(
                request.maxUsersCount(),
                request.maxStorageGB(),
                request.maxCourses()
        );

        // 기능 활성화 설정 업데이트
        settings.updateFeatures(
                request.allowCustomDomain(),
                request.allowCustomBranding(),
                request.ssoEnabled(),
                request.apiAccessEnabled()
        );

        return TenantSettingsResponse.from(settings);
    }

    @Override
    @Transactional
    public TenantSettingsResponse updateDesignSettings(Long tenantId, UpdateDesignSettingsRequest request) {
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> initializeAndGet(tenantId));

        settings.updateBranding(
                request.logoUrl(),
                request.darkLogoUrl(),
                request.faviconUrl(),
                request.primaryColor(),
                request.secondaryColor(),
                request.accentColor(),
                null, // fontFamily (legacy)
                request.headingFont(),
                request.bodyFont()
        );

        return TenantSettingsResponse.from(settings);
    }

    @Override
    @Transactional
    public TenantSettingsResponse updateLayoutSettings(Long tenantId, UpdateLayoutSettingsRequest request) {
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> initializeAndGet(tenantId));

        settings.updateLayoutSettings(
                request.headerSettings(),
                request.sidebarSettings(),
                request.footerSettings(),
                request.contentSettings()
        );

        return TenantSettingsResponse.from(settings);
    }

    @Override
    @Transactional
    public TenantSettingsResponse initializeSettings(Long tenantId) {
        if (tenantSettingsRepository.existsByTenantId(tenantId)) {
            return getSettings(tenantId);
        }

        TenantSettings settings = initializeAndGet(tenantId);
        return TenantSettingsResponse.from(settings);
    }

    // ============================================
    // 네비게이션 관리
    // ============================================

    @Override
    @Transactional
    public List<NavigationItemResponse> getNavigationItems(Long tenantId) {
        // 네비게이션 항목이 없으면 기본 항목 초기화
        if (!navigationItemRepository.existsByTenantId(tenantId)) {
            return initializeDefaultNavigationItems(tenantId);
        }

        return navigationItemRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId)
                .stream()
                .map(NavigationItemResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public NavigationItemResponse createNavigationItem(Long tenantId, NavigationItemRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Tenant not found: " + tenantId));

        int maxOrder = navigationItemRepository.findMaxDisplayOrderByTenantId(tenantId);
        int newOrder = request.displayOrder() != null ? request.displayOrder() : maxOrder + 1;

        NavigationItem item = NavigationItem.create(
                tenant,
                request.label(),
                request.icon(),
                request.path(),
                newOrder
        );

        if (request.enabled() != null) {
            item.update(null, null, null, request.enabled(), null, request.target());
        }

        NavigationItem saved = navigationItemRepository.save(item);
        return NavigationItemResponse.from(saved);
    }

    @Override
    @Transactional
    public NavigationItemResponse updateNavigationItem(Long tenantId, Long itemId, NavigationItemRequest request) {
        NavigationItem item = navigationItemRepository.findByIdAndTenantId(itemId, tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Navigation item not found: " + itemId));

        item.update(
                request.label(),
                request.icon(),
                request.path(),
                request.enabled(),
                request.displayOrder(),
                request.target()
        );

        return NavigationItemResponse.from(item);
    }

    @Override
    @Transactional
    public void deleteNavigationItem(Long tenantId, Long itemId) {
        NavigationItem item = navigationItemRepository.findByIdAndTenantId(itemId, tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Navigation item not found: " + itemId));

        navigationItemRepository.delete(item);
    }

    @Override
    @Transactional
    public List<NavigationItemResponse> reorderNavigationItems(Long tenantId, List<Long> itemIds) {
        List<NavigationItem> items = navigationItemRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId);

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> item.updateOrder(itemIds.indexOf(item.getId()) + 1));
        }

        // 순서 재설정
        for (int i = 0; i < itemIds.size(); i++) {
            final int order = i + 1;
            final Long id = itemIds.get(i);
            items.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .ifPresent(item -> item.updateOrder(order));
        }

        return items.stream()
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(NavigationItemResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public List<NavigationItemResponse> initializeDefaultNavigationItems(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Tenant not found: " + tenantId));

        // 기존 항목 삭제
        navigationItemRepository.deleteAllByTenantId(tenantId);

        // 기본 네비게이션 항목 생성
        List<NavigationItem> defaultItems = new ArrayList<>();
        defaultItems.add(NavigationItem.createDefault(tenant, "홈", "Home", "/", 1));
        defaultItems.add(NavigationItem.createDefault(tenant, "강좌", "BookOpen", "/courses", 2));
        defaultItems.add(NavigationItem.createDefault(tenant, "내 학습", "Award", "/my-learning", 3));
        defaultItems.add(NavigationItem.createDefault(tenant, "도움말", "HelpCircle", "/help", 4));

        List<NavigationItem> saved = navigationItemRepository.saveAll(defaultItems);

        return saved.stream()
                .map(NavigationItemResponse::from)
                .toList();
    }

    @Override
    public PublicBrandingResponse getPublicBranding(String identifier, String type) {
        final Tenant tenant;

        if ("subdomain".equals(type)) {
            tenant = tenantRepository.findBySubdomain(identifier)
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        } else if ("customDomain".equals(type)) {
            tenant = tenantRepository.findByCustomDomain(identifier)
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        } else {
            tenant = null;
        }

        if (tenant == null) {
            return PublicBrandingResponse.defaultBranding();
        }

        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                .orElseGet(() -> TenantSettings.createDefault(tenant));

        TenantSettingsResponse settingsResponse = TenantSettingsResponse.from(settings);
        return PublicBrandingResponse.from(settingsResponse, tenant.getName());
    }

    @Override
    public PublicBrandingResponse getBrandingByTenantId(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElse(null);

        if (tenant == null) {
            return PublicBrandingResponse.defaultBranding();
        }

        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> TenantSettings.createDefault(tenant));

        TenantSettingsResponse settingsResponse = TenantSettingsResponse.from(settings);
        return PublicBrandingResponse.from(settingsResponse, tenant.getName());
    }

    private TenantSettings initializeAndGet(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Tenant not found: " + tenantId));

        TenantSettings settings = TenantSettings.createDefault(tenant);
        return tenantSettingsRepository.save(settings);
    }
}
