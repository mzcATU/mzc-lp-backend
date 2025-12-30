package com.mzc.lp.domain.tenant.repository;

import com.mzc.lp.domain.tenant.entity.TenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantSettingsRepository extends JpaRepository<TenantSettings, Long> {

    Optional<TenantSettings> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);
}
