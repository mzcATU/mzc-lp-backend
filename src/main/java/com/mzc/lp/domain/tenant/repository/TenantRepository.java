package com.mzc.lp.domain.tenant.repository;

import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.entity.Tenant;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByCode(String code);

    Optional<Tenant> findBySubdomain(String subdomain);

    Optional<Tenant> findByCustomDomain(String customDomain);

    Optional<Tenant> findBySubdomainAndStatus(String subdomain, TenantStatus status);

    Optional<Tenant> findByCustomDomainAndStatus(String customDomain, TenantStatus status);

    boolean existsByCode(String code);

    boolean existsBySubdomain(String subdomain);

    boolean existsByCustomDomain(String customDomain);

    Page<Tenant> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);

    /**
     * 비관적 락으로 테넌트 조회 (동시성 제어용)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tenant t WHERE t.id = :tenantId")
    Optional<Tenant> findByIdWithLock(@Param("tenantId") Long tenantId);

    // ===== 기간 필터 통계 쿼리 (SA 대시보드) =====

    /**
     * 기간 내 생성된 테넌트 조회 (createdAt 기준)
     */
    @Query("SELECT t FROM Tenant t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    List<Tenant> findAllWithPeriod(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
