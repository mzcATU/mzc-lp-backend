package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.TenantSettings;

import java.time.Instant;

/**
 * 테넌트 설정 응답 DTO
 */
public record TenantSettingsResponse(
        Long id,
        Long tenantId,

        // 브랜딩 설정
        String logoUrl,
        String faviconUrl,
        String primaryColor,
        String secondaryColor,
        String fontFamily,

        // 일반 설정
        String defaultLanguage,
        String timezone,

        // 사용자 관리 설정
        Boolean allowSelfRegistration,
        Boolean requireEmailVerification,
        Boolean requireApproval,
        String allowedEmailDomains,

        // 제한 설정
        Integer maxUsersCount,
        Integer maxStorageGB,
        Integer maxCourses,

        // 기능 활성화 설정
        Boolean allowCustomDomain,
        Boolean allowCustomBranding,
        Boolean ssoEnabled,
        Boolean apiAccessEnabled,

        Instant createdAt,
        Instant updatedAt
) {
    public static TenantSettingsResponse from(TenantSettings settings) {
        return new TenantSettingsResponse(
                settings.getId(),
                settings.getTenant().getId(),
                settings.getLogoUrl(),
                settings.getFaviconUrl(),
                settings.getPrimaryColor(),
                settings.getSecondaryColor(),
                settings.getFontFamily(),
                settings.getDefaultLanguage(),
                settings.getTimezone(),
                settings.getAllowSelfRegistration(),
                settings.getRequireEmailVerification(),
                settings.getRequireApproval(),
                settings.getAllowedEmailDomains(),
                settings.getMaxUsersCount(),
                settings.getMaxStorageGB(),
                settings.getMaxCourses(),
                settings.getAllowCustomDomain(),
                settings.getAllowCustomBranding(),
                settings.getSsoEnabled(),
                settings.getApiAccessEnabled(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}
