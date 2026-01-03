package com.mzc.lp.domain.tenant.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 디자인/브랜딩 설정 업데이트 요청 DTO
 */
public record UpdateDesignSettingsRequest(
        // 로고
        @Size(max = 500)
        String logoUrl,

        @Size(max = 500)
        String darkLogoUrl,

        @Size(max = 500)
        String faviconUrl,

        // 색상
        @Size(max = 7)
        String primaryColor,

        @Size(max = 7)
        String secondaryColor,

        @Size(max = 7)
        String accentColor,

        // 폰트
        @Size(max = 100)
        String headingFont,

        @Size(max = 100)
        String bodyFont
) {
}
