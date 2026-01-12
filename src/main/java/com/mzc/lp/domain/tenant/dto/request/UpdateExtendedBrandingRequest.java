package com.mzc.lp.domain.tenant.dto.request;

import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 확장 브랜딩 설정 업데이트 요청 DTO
 * 배너, 랜딩페이지, 사이드바 설정 등
 */
public record UpdateExtendedBrandingRequest(
        @Size(max = 200)
        String companyName,

        Map<String, Object> bannerSettings,

        Map<String, Object> landingPageSettings,

        Map<String, Object> sidebarTUSettings,

        Map<String, Object> sidebarCOSettings
) {
}
