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
}
