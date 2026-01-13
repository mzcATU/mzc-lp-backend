package com.mzc.lp.domain.tenant.dto.request;

/**
 * 테넌트 기능 On/Off 설정 업데이트 요청 DTO
 */
public record UpdateTenantFeaturesRequest(
        Boolean communityEnabled,
        Boolean userCourseCreationEnabled,
        Boolean cartEnabled,
        Boolean wishlistEnabled,
        Boolean instructorTabEnabled,
        Boolean paidModeEnabled // 유료 모드 (false면 무료 모드 - 가격 숨김)
) {
}
