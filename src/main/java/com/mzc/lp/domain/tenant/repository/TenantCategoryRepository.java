package com.mzc.lp.domain.tenant.repository;

import com.mzc.lp.domain.tenant.entity.TenantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 테넌트 카테고리 리포지토리
 */
@Repository
public interface TenantCategoryRepository extends JpaRepository<TenantCategory, Long> {

    /**
     * 테넌트의 모든 카테고리 조회 (정렬)
     */
    List<TenantCategory> findByTenantIdOrderByDisplayOrderAsc(Long tenantId);

    /**
     * 테넌트의 활성화된 카테고리만 조회 (정렬)
     */
    List<TenantCategory> findByTenantIdAndEnabledTrueOrderByDisplayOrderAsc(Long tenantId);

    /**
     * 특정 테넌트의 특정 카테고리 조회
     */
    Optional<TenantCategory> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * 테넌트에 해당 슬러그가 존재하는지 확인
     */
    boolean existsByTenantIdAndSlug(Long tenantId, String slug);

    /**
     * 테넌트에 해당 슬러그가 존재하는지 확인 (자기 자신 제외)
     */
    @Query("SELECT COUNT(c) > 0 FROM TenantCategory c WHERE c.tenant.id = :tenantId AND c.slug = :slug AND c.id != :excludeId")
    boolean existsByTenantIdAndSlugAndIdNot(@Param("tenantId") Long tenantId,
                                             @Param("slug") String slug,
                                             @Param("excludeId") Long excludeId);

    /**
     * 테넌트의 최대 displayOrder 조회
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM TenantCategory c WHERE c.tenant.id = :tenantId")
    int findMaxDisplayOrderByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 테넌트의 카테고리 개수
     */
    long countByTenantId(Long tenantId);
}
