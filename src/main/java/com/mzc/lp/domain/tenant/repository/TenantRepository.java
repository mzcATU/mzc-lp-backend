package com.mzc.lp.domain.tenant.repository;

import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
