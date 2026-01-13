package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.TenantSettings;

import java.util.List;
import java.util.Map;

/**
 * TU용 공개 레이아웃 설정 응답 DTO
 * 인증 없이 접근 가능한 레이아웃 정보
 */
public record PublicLayoutResponse(
        Map<String, Object> headerSettings,
        Map<String, Object> footerSettings,
        Map<String, Object> contentSettings,
        List<NavigationItemResponse> navigationItems,
        // 확장 브랜딩 설정
        String companyName,
        Map<String, Object> bannerSettings,
        Map<String, Object> landingPageSettings,
        Map<String, Object> sidebarTUSettings,
        Map<String, Object> sidebarCOSettings
) {
    public static PublicLayoutResponse from(
            TenantSettings settings,
            List<NavigationItemResponse> navigationItems
    ) {
        return new PublicLayoutResponse(
                settings.getHeaderSettings(),
                settings.getFooterSettings(),
                settings.getContentSettings(),
                navigationItems,
                settings.getCompanyName(),
                settings.getBannerSettings(),
                settings.getLandingPageSettings(),
                settings.getSidebarTUSettings(),
                settings.getSidebarCOSettings()
        );
    }

    /**
     * 기본 레이아웃 설정 반환
     */
    public static PublicLayoutResponse defaultLayout() {
        return new PublicLayoutResponse(
                Map.of(
                        "style", "fixed",
                        "height", "default",
                        "showLogo", true,
                        "showSearch", true,
                        "showNotifications", true
                ),
                Map.of(
                        "enabled", true,
                        "showLinks", true,
                        "showCopyright", true,
                        "showSocialLinks", true
                ),
                Map.of(
                        "maxWidth", "xl",
                        "padding", "normal"
                ),
                List.of(),
                null,
                null,
                null,
                null,
                null
        );
    }
}
