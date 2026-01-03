package com.mzc.lp.domain.system.dto.response;

import com.mzc.lp.domain.system.entity.TenantDefaults;

import java.time.Instant;

/**
 * 테넌트 기본값 응답 DTO
 */
public record TenantDefaultsResponse(
        Long id,

        // 리소스 제한 기본값
        LimitsDefaults limits,

        // 기능 활성화 기본값
        FeaturesDefaults features,

        // 브랜딩 권한 기본값
        BrandingDefaults branding,

        // 알림 설정 기본값
        NotificationsDefaults notifications,

        Instant createdAt,
        Instant updatedAt
) {
    public record LimitsDefaults(
            Integer maxUsers,
            Integer maxCourses,
            Integer maxStorage,
            Integer maxAdmins
    ) {}

    public record FeaturesDefaults(
            Boolean customDomain,
            Boolean ssoIntegration,
            Boolean apiAccess,
            Boolean whiteLabeling,
            Boolean advancedAnalytics
    ) {}

    public record BrandingDefaults(
            Boolean allowCustomLogo,
            Boolean allowCustomColors,
            Boolean allowCustomFonts
    ) {}

    public record NotificationsDefaults(
            Boolean emailNotifications,
            Boolean pushNotifications,
            Boolean smsNotifications
    ) {}

    public static TenantDefaultsResponse from(TenantDefaults defaults) {
        return new TenantDefaultsResponse(
                defaults.getId(),
                new LimitsDefaults(
                        defaults.getDefaultMaxUsers(),
                        defaults.getDefaultMaxCourses(),
                        defaults.getDefaultMaxStorageGB(),
                        defaults.getDefaultMaxAdmins()
                ),
                new FeaturesDefaults(
                        defaults.getDefaultCustomDomain(),
                        defaults.getDefaultSsoIntegration(),
                        defaults.getDefaultApiAccess(),
                        defaults.getDefaultWhiteLabeling(),
                        defaults.getDefaultAdvancedAnalytics()
                ),
                new BrandingDefaults(
                        defaults.getDefaultAllowCustomLogo(),
                        defaults.getDefaultAllowCustomColors(),
                        defaults.getDefaultAllowCustomFonts()
                ),
                new NotificationsDefaults(
                        defaults.getDefaultEmailNotifications(),
                        defaults.getDefaultPushNotifications(),
                        defaults.getDefaultSmsNotifications()
                ),
                defaults.getCreatedAt(),
                defaults.getUpdatedAt()
        );
    }
}
