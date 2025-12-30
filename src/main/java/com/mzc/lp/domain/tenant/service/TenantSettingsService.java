package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.UpdateTenantSettingsRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantSettingsResponse;

/**
 * 테넌트 설정 서비스 인터페이스
 */
public interface TenantSettingsService {

    /**
     * 테넌트 설정 조회
     * @param tenantId 테넌트 ID
     * @return 테넌트 설정
     */
    TenantSettingsResponse getSettings(Long tenantId);

    /**
     * 테넌트 설정 업데이트
     * @param tenantId 테넌트 ID
     * @param request 업데이트 요청
     * @return 업데이트된 설정
     */
    TenantSettingsResponse updateSettings(Long tenantId, UpdateTenantSettingsRequest request);

    /**
     * 테넌트 설정 초기화 (기본값으로 생성)
     * @param tenantId 테넌트 ID
     * @return 생성된 설정
     */
    TenantSettingsResponse initializeSettings(Long tenantId);
}
