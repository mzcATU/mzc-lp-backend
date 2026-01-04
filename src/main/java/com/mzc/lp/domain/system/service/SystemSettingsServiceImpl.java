package com.mzc.lp.domain.system.service;

import com.mzc.lp.domain.system.dto.request.UpdateSystemSettingsRequest;
import com.mzc.lp.domain.system.dto.request.UpdateTenantDefaultsRequest;
import com.mzc.lp.domain.system.dto.response.SystemSettingsResponse;
import com.mzc.lp.domain.system.dto.response.TenantDefaultsResponse;
import com.mzc.lp.domain.system.entity.SystemSettings;
import com.mzc.lp.domain.system.entity.TenantDefaults;
import com.mzc.lp.domain.system.repository.SystemSettingsRepository;
import com.mzc.lp.domain.system.repository.TenantDefaultsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final TenantDefaultsRepository tenantDefaultsRepository;

    // ============================================
    // 시스템 설정
    // ============================================

    @Override
    public SystemSettingsResponse getSystemSettings() {
        SystemSettings settings = systemSettingsRepository.findGlobalSettings()
                .orElseGet(this::initializeSystemSettings);
        return SystemSettingsResponse.from(settings);
    }

    @Override
    @Transactional
    public SystemSettingsResponse updateSystemSettings(UpdateSystemSettingsRequest request) {
        SystemSettings settings = systemSettingsRepository.findGlobalSettings()
                .orElseGet(this::initializeSystemSettings);

        // 일반 설정 업데이트
        settings.updateGeneralSettings(
                request.platformName(),
                request.timezone(),
                request.language(),
                request.maintenanceMode(),
                request.maintenanceMessage()
        );

        // 보안 설정 업데이트
        settings.updateSecuritySettings(
                request.sessionTimeout(),
                request.maxLoginAttempts(),
                request.passwordExpiry(),
                request.mfaRequired(),
                request.ipWhitelist(),
                request.loginLockoutMinutes()
        );

        // 스토리지 설정 업데이트
        settings.updateStorageSettings(
                request.maxUploadSize(),
                request.allowedFormats(),
                request.autoCleanup(),
                request.cleanupDays(),
                request.totalStorageLimitGB()
        );

        // 이메일 설정 업데이트
        settings.updateEmailSettings(
                request.smtpHost(),
                request.smtpPort(),
                request.smtpUser(),
                request.smtpPassword(),
                request.useTls(),
                request.senderEmail(),
                request.senderName()
        );

        return SystemSettingsResponse.from(settings);
    }

    @Transactional
    private SystemSettings initializeSystemSettings() {
        SystemSettings settings = SystemSettings.createDefault();
        return systemSettingsRepository.save(settings);
    }

    // ============================================
    // 테넌트 기본값 설정
    // ============================================

    @Override
    public TenantDefaultsResponse getTenantDefaults() {
        TenantDefaults defaults = tenantDefaultsRepository.findDefaultSettings()
                .orElseGet(this::initializeTenantDefaults);
        return TenantDefaultsResponse.from(defaults);
    }

    @Override
    @Transactional
    public TenantDefaultsResponse updateTenantDefaults(UpdateTenantDefaultsRequest request) {
        TenantDefaults defaults = tenantDefaultsRepository.findDefaultSettings()
                .orElseGet(this::initializeTenantDefaults);

        // 리소스 제한 업데이트
        defaults.updateLimits(
                request.maxUsers(),
                request.maxCourses(),
                request.maxStorage(),
                request.maxAdmins()
        );

        // 기능 활성화 업데이트
        defaults.updateFeatures(
                request.customDomain(),
                request.ssoIntegration(),
                request.apiAccess(),
                request.whiteLabeling(),
                request.advancedAnalytics()
        );

        // 브랜딩 권한 업데이트
        defaults.updateBrandingPermissions(
                request.allowCustomLogo(),
                request.allowCustomColors(),
                request.allowCustomFonts()
        );

        // 알림 설정 업데이트
        defaults.updateNotifications(
                request.emailNotifications(),
                request.pushNotifications(),
                request.smsNotifications()
        );

        return TenantDefaultsResponse.from(defaults);
    }

    @Transactional
    private TenantDefaults initializeTenantDefaults() {
        TenantDefaults defaults = TenantDefaults.createDefault();
        return tenantDefaultsRepository.save(defaults);
    }
}
