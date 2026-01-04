package com.mzc.lp.domain.system.dto.response;

import com.mzc.lp.domain.system.entity.SystemSettings;

import java.time.Instant;

/**
 * 시스템 설정 응답 DTO
 */
public record SystemSettingsResponse(
        Long id,

        // 일반 설정
        GeneralSettings general,

        // 보안 설정
        SecuritySettings security,

        // 스토리지 설정
        StorageSettings storage,

        // 이메일 설정
        EmailSettings email,

        Instant createdAt,
        Instant updatedAt
) {
    public record GeneralSettings(
            String platformName,
            String timezone,
            String language,
            Boolean maintenanceMode,
            String maintenanceMessage
    ) {}

    public record SecuritySettings(
            Integer sessionTimeout,
            Integer maxLoginAttempts,
            Integer passwordExpiry,
            Boolean mfaRequired,
            String ipWhitelist,
            Integer loginLockoutMinutes
    ) {}

    public record StorageSettings(
            Integer maxUploadSize,
            String allowedFormats,
            Boolean autoCleanup,
            Integer cleanupDays,
            Long totalStorageLimitGB
    ) {}

    public record EmailSettings(
            String smtpHost,
            Integer smtpPort,
            String smtpUser,
            Boolean useTls,
            String senderEmail,
            String senderName
    ) {}

    public static SystemSettingsResponse from(SystemSettings settings) {
        return new SystemSettingsResponse(
                settings.getId(),
                new GeneralSettings(
                        settings.getPlatformName(),
                        settings.getTimezone(),
                        settings.getLanguage(),
                        settings.getMaintenanceMode(),
                        settings.getMaintenanceMessage()
                ),
                new SecuritySettings(
                        settings.getSessionTimeoutMinutes(),
                        settings.getMaxLoginAttempts(),
                        settings.getPasswordExpiryDays(),
                        settings.getMfaRequired(),
                        settings.getIpWhitelist(),
                        settings.getLoginLockoutMinutes()
                ),
                new StorageSettings(
                        settings.getMaxUploadSizeMB(),
                        settings.getAllowedFileFormats(),
                        settings.getAutoCleanupEnabled(),
                        settings.getCleanupDays(),
                        settings.getTotalStorageLimitGB()
                ),
                new EmailSettings(
                        settings.getSmtpHost(),
                        settings.getSmtpPort(),
                        settings.getSmtpUser(),
                        settings.getSmtpUseTls(),
                        settings.getSenderEmail(),
                        settings.getSenderName()
                ),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}
