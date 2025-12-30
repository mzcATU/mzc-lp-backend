package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.UpdateTenantSettingsRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantSettingsResponse;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.entity.TenantSettings;
import com.mzc.lp.domain.tenant.exception.TenantDomainNotFoundException;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.tenant.repository.TenantSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantSettingsServiceImpl implements TenantSettingsService {

    private final TenantSettingsRepository tenantSettingsRepository;
    private final TenantRepository tenantRepository;

    @Override
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

        // 브랜딩 설정 업데이트
        settings.updateBranding(
                request.logoUrl(),
                request.faviconUrl(),
                request.primaryColor(),
                request.secondaryColor(),
                request.fontFamily()
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
    public TenantSettingsResponse initializeSettings(Long tenantId) {
        if (tenantSettingsRepository.existsByTenantId(tenantId)) {
            return getSettings(tenantId);
        }

        TenantSettings settings = initializeAndGet(tenantId);
        return TenantSettingsResponse.from(settings);
    }

    private TenantSettings initializeAndGet(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException("Tenant not found: " + tenantId));

        TenantSettings settings = TenantSettings.createDefault(tenant);
        return tenantSettingsRepository.save(settings);
    }
}
