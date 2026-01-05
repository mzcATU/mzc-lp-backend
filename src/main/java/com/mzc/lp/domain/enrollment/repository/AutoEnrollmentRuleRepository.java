package com.mzc.lp.domain.enrollment.repository;

import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.entity.AutoEnrollmentRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutoEnrollmentRuleRepository extends JpaRepository<AutoEnrollmentRule, Long> {

    List<AutoEnrollmentRule> findByTenantIdOrderBySortOrderAsc(Long tenantId);

    List<AutoEnrollmentRule> findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(Long tenantId);

    List<AutoEnrollmentRule> findByTenantIdAndTriggerOrderBySortOrderAsc(Long tenantId, AutoEnrollmentTrigger trigger);

    List<AutoEnrollmentRule> findByTenantIdAndTriggerAndIsActiveTrueOrderBySortOrderAsc(
            Long tenantId, AutoEnrollmentTrigger trigger);

    List<AutoEnrollmentRule> findByTenantIdAndDepartmentIdAndIsActiveTrueOrderBySortOrderAsc(
            Long tenantId, Long departmentId);

    Optional<AutoEnrollmentRule> findByIdAndTenantId(Long id, Long tenantId);
}
