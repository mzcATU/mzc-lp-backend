package com.mzc.lp.domain.system.repository;

import com.mzc.lp.domain.system.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    Optional<SystemSettings> findBySettingsKey(String settingsKey);

    default Optional<SystemSettings> findGlobalSettings() {
        return findBySettingsKey("GLOBAL");
    }
}
