package com.mzc.lp.domain.tenant.repository;

import com.mzc.lp.domain.tenant.entity.NavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 네비게이션 아이템 리포지토리
 */
@Repository
public interface NavigationItemRepository extends JpaRepository<NavigationItem, Long> {

    /**
     * 테넌트의 모든 네비게이션 항목 조회 (정렬)
     */
    List<NavigationItem> findByTenantIdOrderByDisplayOrderAsc(Long tenantId);

    /**
     * 테넌트의 활성화된 네비게이션 항목만 조회 (정렬)
     */
    List<NavigationItem> findByTenantIdAndEnabledTrueOrderByDisplayOrderAsc(Long tenantId);

    /**
     * 특정 테넌트의 특정 네비게이션 항목 조회
     */
    Optional<NavigationItem> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * 테넌트의 네비게이션 항목 개수
     */
    long countByTenantId(Long tenantId);

    /**
     * 테넌트의 최대 displayOrder 조회
     */
    @Query("SELECT COALESCE(MAX(n.displayOrder), 0) FROM NavigationItem n WHERE n.tenant.id = :tenantId")
    int findMaxDisplayOrderByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트의 모든 네비게이션 항목 삭제
     */
    @Modifying
    @Query("DELETE FROM NavigationItem n WHERE n.tenant.id = :tenantId")
    void deleteAllByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트에 네비게이션 항목이 있는지 확인
     */
    boolean existsByTenantId(Long tenantId);
}
