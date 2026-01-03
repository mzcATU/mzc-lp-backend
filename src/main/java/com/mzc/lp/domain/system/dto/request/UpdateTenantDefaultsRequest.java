package com.mzc.lp.domain.system.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 테넌트 기본값 업데이트 요청 DTO
 */
public record UpdateTenantDefaultsRequest(
        // 리소스 제한 기본값
        @Min(1) @Max(100000) Integer maxUsers,
        @Min(1) @Max(10000) Integer maxCourses,
        @Min(1) @Max(10000) Integer maxStorage,
        @Min(1) @Max(100) Integer maxAdmins,

        // 기능 활성화 기본값
        Boolean customDomain,
        Boolean ssoIntegration,
        Boolean apiAccess,
        Boolean whiteLabeling,
        Boolean advancedAnalytics,

        // 브랜딩 권한 기본값
        Boolean allowCustomLogo,
        Boolean allowCustomColors,
        Boolean allowCustomFonts,

        // 알림 설정 기본값
        Boolean emailNotifications,
        Boolean pushNotifications,
        Boolean smsNotifications
) {}
