package com.mzc.lp.domain.tenant.dto.request;

import java.util.Map;

/**
 * 레이아웃/UI 설정 업데이트 요청 DTO
 */
public record UpdateLayoutSettingsRequest(
        // 헤더 설정 (style, showLogo, showSearch, showNotifications)
        Map<String, Object> headerSettings,

        // 사이드바 설정 (style, defaultCollapsed, showIcons)
        Map<String, Object> sidebarSettings,

        // 푸터 설정 (enabled, showLinks, showCopyright)
        Map<String, Object> footerSettings,

        // 콘텐츠 영역 설정 (maxWidth, padding)
        Map<String, Object> contentSettings
) {
}
