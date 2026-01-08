package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.CreateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantStatusRequest;
import com.mzc.lp.domain.tenant.dto.response.CreateTenantResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantUserStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService {

    /**
     * 테넌트 생성 (TENANT_ADMIN 계정도 함께 생성)
     */
    CreateTenantResponse createTenant(CreateTenantRequest request);

    /**
     * 테넌트 목록 조회 (페이징)
     */
    Page<TenantResponse> getTenants(String keyword, Pageable pageable);

    /**
     * 테넌트 상세 조회
     */
    TenantResponse getTenant(Long tenantId);

    /**
     * 테넌트 코드로 조회
     */
    TenantResponse getTenantByCode(String code);

    /**
     * 테넌트 수정
     */
    TenantResponse updateTenant(Long tenantId, UpdateTenantRequest request);

    /**
     * 테넌트 상태 변경
     */
    TenantResponse updateTenantStatus(Long tenantId, UpdateTenantStatusRequest request);

    /**
     * 테넌트 삭제
     */
    void deleteTenant(Long tenantId);

    /**
     * 테넌트별 사용자 수 통계
     */
    TenantUserStatsResponse getTenantUserStats();
}
