package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.TenantSettings;

/**
 * 테넌트 기능 On/Off 설정 응답 DTO
 */
public record TenantFeaturesResponse(
        Boolean communityEnabled,
        Boolean userCourseCreationEnabled,
        Boolean cartEnabled,
        Boolean wishlistEnabled,
        Boolean instructorTabEnabled
) {
    public static TenantFeaturesResponse from(TenantSettings settings) {
        return new TenantFeaturesResponse(
                settings.getCommunityEnabled(),
                settings.getUserCourseCreationEnabled(),
                settings.getCartEnabled(),
                settings.getWishlistEnabled(),
                settings.getInstructorTabEnabled()
        );
    }

    /**
     * 기본값 (모든 기능 활성화)
     */
    public static TenantFeaturesResponse defaultFeatures() {
        return new TenantFeaturesResponse(
                true,   // communityEnabled
                false,  // userCourseCreationEnabled
                true,   // cartEnabled
                true,   // wishlistEnabled
                true    // instructorTabEnabled
        );
    }
}
