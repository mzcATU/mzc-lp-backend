package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.NavigationItemRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateDesignSettingsRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateLayoutSettingsRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantSettingsRequest;
import com.mzc.lp.domain.tenant.dto.response.NavigationItemResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantSettingsResponse;

import java.util.List;

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
     * 디자인/브랜딩 설정 업데이트
     * @param tenantId 테넌트 ID
     * @param request 디자인 설정 요청
     * @return 업데이트된 설정
     */
    TenantSettingsResponse updateDesignSettings(Long tenantId, UpdateDesignSettingsRequest request);

    /**
     * 레이아웃 설정 업데이트
     * @param tenantId 테넌트 ID
     * @param request 레이아웃 설정 요청
     * @return 업데이트된 설정
     */
    TenantSettingsResponse updateLayoutSettings(Long tenantId, UpdateLayoutSettingsRequest request);

    /**
     * 테넌트 설정 초기화 (기본값으로 생성)
     * @param tenantId 테넌트 ID
     * @return 생성된 설정
     */
    TenantSettingsResponse initializeSettings(Long tenantId);

    // ============================================
    // 네비게이션 관리
    // ============================================

    /**
     * 네비게이션 항목 목록 조회
     * @param tenantId 테넌트 ID
     * @return 네비게이션 항목 목록
     */
    List<NavigationItemResponse> getNavigationItems(Long tenantId);

    /**
     * 네비게이션 항목 생성
     * @param tenantId 테넌트 ID
     * @param request 생성 요청
     * @return 생성된 항목
     */
    NavigationItemResponse createNavigationItem(Long tenantId, NavigationItemRequest request);

    /**
     * 네비게이션 항목 수정
     * @param tenantId 테넌트 ID
     * @param itemId 항목 ID
     * @param request 수정 요청
     * @return 수정된 항목
     */
    NavigationItemResponse updateNavigationItem(Long tenantId, Long itemId, NavigationItemRequest request);

    /**
     * 네비게이션 항목 삭제
     * @param tenantId 테넌트 ID
     * @param itemId 항목 ID
     */
    void deleteNavigationItem(Long tenantId, Long itemId);

    /**
     * 네비게이션 항목 순서 변경
     * @param tenantId 테넌트 ID
     * @param itemIds 순서대로 정렬된 항목 ID 목록
     * @return 재정렬된 항목 목록
     */
    List<NavigationItemResponse> reorderNavigationItems(Long tenantId, List<Long> itemIds);

    /**
     * 기본 네비게이션 항목 초기화
     * @param tenantId 테넌트 ID
     * @return 초기화된 항목 목록
     */
    List<NavigationItemResponse> initializeDefaultNavigationItems(Long tenantId);
}
