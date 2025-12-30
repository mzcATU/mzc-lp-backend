package com.mzc.lp.domain.tenant.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 테넌트 설정 업데이트 요청 DTO
 */
public record UpdateTenantSettingsRequest(
        // 브랜딩 설정
        @Size(max = 500)
        String logoUrl,

        @Size(max = 500)
        String faviconUrl,

        @Size(max = 7)
        String primaryColor,

        @Size(max = 7)
        String secondaryColor,

        @Size(max = 100)
        String fontFamily,

        // 일반 설정
        @Size(max = 10)
        String defaultLanguage,

        @Size(max = 50)
        String timezone,

        // 사용자 관리 설정
        Boolean allowSelfRegistration,

        Boolean requireEmailVerification,

        Boolean requireApproval,

        @Size(max = 500)
        String allowedEmailDomains,

        // 제한 설정
        Integer maxUsersCount,

        Integer maxStorageGB,

        Integer maxCourses,

        // 기능 활성화 설정
        Boolean allowCustomDomain,

        Boolean allowCustomBranding,

        Boolean ssoEnabled,

        Boolean apiAccessEnabled
) {
}
