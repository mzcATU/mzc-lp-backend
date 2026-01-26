package com.mzc.lp.domain.enrollment.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.dto.request.CreateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.request.UpdateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.response.AutoEnrollmentRuleResponse;
import com.mzc.lp.domain.enrollment.entity.AutoEnrollmentRule;
import com.mzc.lp.domain.enrollment.exception.AutoEnrollmentRuleNotFoundException;
import com.mzc.lp.domain.enrollment.repository.AutoEnrollmentRuleRepository;
import com.mzc.lp.domain.department.repository.DepartmentRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoEnrollmentRuleServiceImpl implements AutoEnrollmentRuleService {

    private final AutoEnrollmentRuleRepository ruleRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseTimeRepository courseTimeRepository;

    @Override
    @Transactional
    public AutoEnrollmentRuleResponse create(Long tenantId, CreateAutoEnrollmentRuleRequest request) {
        log.info("Creating auto enrollment rule: tenantId={}, name={}, trigger={}",
                tenantId, request.name(), request.trigger());

        TenantContext.setTenantId(tenantId);

        try {
            AutoEnrollmentRule rule;
            if (request.trigger() == AutoEnrollmentTrigger.DEPARTMENT_ASSIGN && request.departmentId() != null) {
                rule = AutoEnrollmentRule.createForDepartment(
                        request.name(),
                        request.departmentId(),
                        request.courseTimeId()
                );
            } else {
                rule = AutoEnrollmentRule.create(
                        request.name(),
                        request.trigger(),
                        request.courseTimeId()
                );
            }

            rule.update(null, request.description(), null, request.departmentId(), null);

            if (request.sortOrder() != null) {
                rule.setSortOrder(request.sortOrder());
            }

            AutoEnrollmentRule saved = ruleRepository.save(rule);
            return toResponseWithRelations(saved, tenantId);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public AutoEnrollmentRuleResponse update(Long tenantId, Long ruleId, UpdateAutoEnrollmentRuleRequest request) {
        log.info("Updating auto enrollment rule: tenantId={}, ruleId={}", tenantId, ruleId);

        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        rule.update(
                request.name(),
                request.description(),
                request.trigger(),
                request.departmentId(),
                request.courseTimeId()
        );

        if (request.sortOrder() != null) {
            rule.setSortOrder(request.sortOrder());
        }

        return toResponseWithRelations(rule, tenantId);
    }

    @Override
    @Transactional
    public void delete(Long tenantId, Long ruleId) {
        log.info("Deleting auto enrollment rule: tenantId={}, ruleId={}", tenantId, ruleId);

        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        ruleRepository.delete(rule);
    }

    @Override
    public AutoEnrollmentRuleResponse getById(Long tenantId, Long ruleId) {
        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        return toResponseWithRelations(rule, tenantId);
    }

    @Override
    public List<AutoEnrollmentRuleResponse> getAll(Long tenantId) {
        return ruleRepository.findByTenantIdOrderBySortOrderAsc(tenantId)
                .stream()
                .map(rule -> toResponseWithRelations(rule, tenantId))
                .toList();
    }

    @Override
    public Page<AutoEnrollmentRuleResponse> getAllWithFilters(
            Long tenantId,
            String keyword,
            Boolean isActive,
            AutoEnrollmentTrigger trigger,
            Pageable pageable
    ) {
        log.debug("Getting auto enrollment rules with filters: tenantId={}, keyword={}, isActive={}, trigger={}",
                tenantId, keyword, isActive, trigger);

        Page<AutoEnrollmentRule> rulePage = ruleRepository.findByFilters(
                tenantId, keyword, isActive, trigger, pageable);

        // N+1 방지: 관련 엔티티 일괄 조회
        List<AutoEnrollmentRule> rules = rulePage.getContent();

        // Department 이름 일괄 조회
        List<Long> departmentIds = rules.stream()
                .map(AutoEnrollmentRule::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> departmentNameMap = departmentIds.isEmpty() ? Map.of() :
                departmentRepository.findAllById(departmentIds).stream()
                        .collect(Collectors.toMap(
                                dept -> dept.getId(),
                                dept -> dept.getName()
                        ));

        // CourseTime 제목 일괄 조회
        List<Long> courseTimeIds = rules.stream()
                .map(AutoEnrollmentRule::getCourseTimeId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> courseTimeTitleMap = courseTimeIds.isEmpty() ? Map.of() :
                courseTimeRepository.findAllById(courseTimeIds).stream()
                        .collect(Collectors.toMap(
                                ct -> ct.getId(),
                                ct -> ct.getTitle()
                        ));

        return rulePage.map(rule -> {
            String departmentName = rule.getDepartmentId() != null ?
                    departmentNameMap.get(rule.getDepartmentId()) : null;
            String courseTimeTitle = rule.getCourseTimeId() != null ?
                    courseTimeTitleMap.get(rule.getCourseTimeId()) : null;
            return AutoEnrollmentRuleResponse.from(rule, departmentName, courseTimeTitle);
        });
    }

    @Override
    public List<AutoEnrollmentRuleResponse> getActiveRules(Long tenantId) {
        return ruleRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId)
                .stream()
                .map(rule -> toResponseWithRelations(rule, tenantId))
                .toList();
    }

    @Override
    public List<AutoEnrollmentRuleResponse> getByTrigger(Long tenantId, AutoEnrollmentTrigger trigger) {
        return ruleRepository.findByTenantIdAndTriggerOrderBySortOrderAsc(tenantId, trigger)
                .stream()
                .map(rule -> toResponseWithRelations(rule, tenantId))
                .toList();
    }

    @Override
    @Transactional
    public AutoEnrollmentRuleResponse activate(Long tenantId, Long ruleId) {
        log.info("Activating auto enrollment rule: tenantId={}, ruleId={}", tenantId, ruleId);

        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        rule.activate();
        return toResponseWithRelations(rule, tenantId);
    }

    @Override
    @Transactional
    public AutoEnrollmentRuleResponse deactivate(Long tenantId, Long ruleId) {
        log.info("Deactivating auto enrollment rule: tenantId={}, ruleId={}", tenantId, ruleId);

        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        rule.deactivate();
        return toResponseWithRelations(rule, tenantId);
    }

    private AutoEnrollmentRuleResponse toResponseWithRelations(AutoEnrollmentRule rule, Long tenantId) {
        String departmentName = null;
        String courseTimeTitle = null;

        if (rule.getDepartmentId() != null) {
            departmentName = departmentRepository.findByIdAndTenantId(rule.getDepartmentId(), tenantId)
                    .map(dept -> dept.getName())
                    .orElse(null);
        }

        if (rule.getCourseTimeId() != null) {
            courseTimeTitle = courseTimeRepository.findByIdAndTenantId(rule.getCourseTimeId(), tenantId)
                    .map(ct -> ct.getTitle())
                    .orElse(null);
        }

        return AutoEnrollmentRuleResponse.from(rule, departmentName, courseTimeTitle);
    }
}
