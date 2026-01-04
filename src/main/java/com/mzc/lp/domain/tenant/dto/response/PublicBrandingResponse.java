package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.Tenant;

/**
 * 공개 브랜딩 정보 응답 DTO
 * 인증 없이 접근 가능한 브랜딩 정보만 포함
 * 보안상 민감하지 않은 정보만 노출
 */
public record PublicBrandingResponse(
        String tenantName,
        String logoUrl,
        String darkLogoUrl,
        String faviconUrl,
        String primaryColor,
        String secondaryColor,
        String accentColor,
        String headingFont,
        String bodyFont
) {
    /**
     * TenantSettingsResponse로부터 PublicBrandingResponse 생성
     */
    public static PublicBrandingResponse from(TenantSettingsResponse settings, String tenantName) {
        return new PublicBrandingResponse(
                tenantName,
                settings.logoUrl(),
                settings.darkLogoUrl(),
                settings.faviconUrl(),
                settings.primaryColor(),
                settings.secondaryColor(),
                settings.accentColor(),
                settings.headingFont(),
                settings.bodyFont()
        );
    }

    /**
     * 기본 브랜딩 (테넌트를 찾을 수 없을 때)
     */
    public static PublicBrandingResponse defaultBranding() {
        return new PublicBrandingResponse(
                "MZC Learn",
                null,
                null,
                null,
                "#6778ff",
                "#a855f7",
                "#10B981",
                "Pretendard",
                "Pretendard"
        );
    }
}
