package com.mzc.lp.domain.banner.repository;

import com.mzc.lp.domain.banner.constant.BannerPosition;
import com.mzc.lp.domain.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    // 테넌트별 전체 조회
    List<Banner> findByTenantIdOrderBySortOrderAsc(Long tenantId);

    // 테넌트별 위치별 조회
    List<Banner> findByTenantIdAndPositionOrderBySortOrderAsc(Long tenantId, BannerPosition position);

    // 테넌트별 활성 배너 조회
    List<Banner> findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(Long tenantId);

    // 단건 조회
    Optional<Banner> findByIdAndTenantId(Long id, Long tenantId);

    // 현재 표시 가능한 배너 조회 (활성 + 기간 내)
    @Query("SELECT b FROM Banner b WHERE b.tenantId = :tenantId " +
           "AND b.isActive = true " +
           "AND (b.startDate IS NULL OR b.startDate <= :today) " +
           "AND (b.endDate IS NULL OR b.endDate >= :today) " +
           "ORDER BY b.sortOrder ASC")
    List<Banner> findDisplayableBanners(@Param("tenantId") Long tenantId,
                                        @Param("today") LocalDate today);

    // 위치별 현재 표시 가능한 배너 조회
    @Query("SELECT b FROM Banner b WHERE b.tenantId = :tenantId " +
           "AND b.position = :position " +
           "AND b.isActive = true " +
           "AND (b.startDate IS NULL OR b.startDate <= :today) " +
           "AND (b.endDate IS NULL OR b.endDate >= :today) " +
           "ORDER BY b.sortOrder ASC")
    List<Banner> findDisplayableBannersByPosition(@Param("tenantId") Long tenantId,
                                                   @Param("position") BannerPosition position,
                                                   @Param("today") LocalDate today);
}
