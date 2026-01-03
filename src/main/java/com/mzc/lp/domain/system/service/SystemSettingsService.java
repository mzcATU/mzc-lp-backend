package com.mzc.lp.domain.system.service;

import com.mzc.lp.domain.system.dto.request.UpdateSystemSettingsRequest;
import com.mzc.lp.domain.system.dto.request.UpdateTenantDefaultsRequest;
import com.mzc.lp.domain.system.dto.response.SystemSettingsResponse;
import com.mzc.lp.domain.system.dto.response.TenantDefaultsResponse;

/**
 * SA 시스템 설정 서비스 인터페이스
 */
public interface SystemSettingsService {

    // ============================================
    // 시스템 설정
    // ============================================

    /** 시스템 설정 조회 */
    SystemSettingsResponse getSystemSettings();

    /** 시스템 설정 업데이트 */
    SystemSettingsResponse updateSystemSettings(UpdateSystemSettingsRequest request);

    // ============================================
    // 테넌트 기본값 설정
    // ============================================

    /** 테넌트 기본값 조회 */
    TenantDefaultsResponse getTenantDefaults();

    /** 테넌트 기본값 업데이트 */
    TenantDefaultsResponse updateTenantDefaults(UpdateTenantDefaultsRequest request);
}
