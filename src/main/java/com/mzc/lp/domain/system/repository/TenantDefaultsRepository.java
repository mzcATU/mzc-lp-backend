package com.mzc.lp.domain.system.repository;

import com.mzc.lp.domain.system.entity.TenantDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantDefaultsRepository extends JpaRepository<TenantDefaults, Long> {

    Optional<TenantDefaults> findBySettingsKey(String settingsKey);

    default Optional<TenantDefaults> findDefaultSettings() {
        return findBySettingsKey("DEFAULT");
    }
}
