package com.mzc.lp.domain.enrollment.repository;

import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.entity.AutoEnrollmentRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 페이지네이션 + 필터링 (keyword, isActive, trigger)
    @Query("SELECT r FROM AutoEnrollmentRule r " +
            "WHERE r.tenantId = :tenantId " +
            "AND (:keyword IS NULL OR r.name LIKE %:keyword%) " +
            "AND (:isActive IS NULL OR r.isActive = :isActive) " +
            "AND (:trigger IS NULL OR r.trigger = :trigger) " +
            "ORDER BY r.sortOrder ASC")
    Page<AutoEnrollmentRule> findByFilters(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            @Param("trigger") AutoEnrollmentTrigger trigger,
            Pageable pageable
    );
}
