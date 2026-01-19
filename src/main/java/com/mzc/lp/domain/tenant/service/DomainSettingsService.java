package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.response.TenantDomainSettingsResponse;

public interface DomainSettingsService {

    /**
     * 테넌트의 도메인 설정 조회
     */
    TenantDomainSettingsResponse getDomainSettings(Long tenantId);

    /**
     * 커스텀 도메인 설정/수정
     */
    TenantDomainSettingsResponse updateCustomDomain(Long tenantId, String customDomain);

    /**
     * 커스텀 도메인 삭제
     */
    void deleteCustomDomain(Long tenantId);
}
