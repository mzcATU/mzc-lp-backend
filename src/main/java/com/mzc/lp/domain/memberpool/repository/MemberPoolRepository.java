package com.mzc.lp.domain.memberpool.repository;

import com.mzc.lp.domain.memberpool.entity.MemberPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberPoolRepository extends JpaRepository<MemberPool, Long> {

    List<MemberPool> findByTenantIdOrderBySortOrderAsc(Long tenantId);

    List<MemberPool> findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(Long tenantId);

    Optional<MemberPool> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    boolean existsByTenantIdAndNameAndIdNot(Long tenantId, String name, Long id);
}
