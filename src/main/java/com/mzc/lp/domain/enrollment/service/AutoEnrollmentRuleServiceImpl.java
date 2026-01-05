package com.mzc.lp.domain.enrollment.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.dto.request.CreateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.request.UpdateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.response.AutoEnrollmentRuleResponse;
import com.mzc.lp.domain.enrollment.entity.AutoEnrollmentRule;
import com.mzc.lp.domain.enrollment.exception.AutoEnrollmentRuleNotFoundException;
import com.mzc.lp.domain.enrollment.repository.AutoEnrollmentRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoEnrollmentRuleServiceImpl implements AutoEnrollmentRuleService {

    private final AutoEnrollmentRuleRepository ruleRepository;

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
            return AutoEnrollmentRuleResponse.from(saved);
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

        return AutoEnrollmentRuleResponse.from(rule);
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

        return AutoEnrollmentRuleResponse.from(rule);
    }

    @Override
    public List<AutoEnrollmentRuleResponse> getAll(Long tenantId) {
        return ruleRepository.findByTenantIdOrderBySortOrderAsc(tenantId)
                .stream()
                .map(AutoEnrollmentRuleResponse::from)
                .toList();
    }

    @Override
    public List<AutoEnrollmentRuleResponse> getActiveRules(Long tenantId) {
        return ruleRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId)
                .stream()
                .map(AutoEnrollmentRuleResponse::from)
                .toList();
    }

    @Override
    public List<AutoEnrollmentRuleResponse> getByTrigger(Long tenantId, AutoEnrollmentTrigger trigger) {
        return ruleRepository.findByTenantIdAndTriggerOrderBySortOrderAsc(tenantId, trigger)
                .stream()
                .map(AutoEnrollmentRuleResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public AutoEnrollmentRuleResponse activate(Long tenantId, Long ruleId) {
        log.info("Activating auto enrollment rule: tenantId={}, ruleId={}", tenantId, ruleId);

        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        rule.activate();
        return AutoEnrollmentRuleResponse.from(rule);
    }

    @Override
    @Transactional
    public AutoEnrollmentRuleResponse deactivate(Long tenantId, Long ruleId) {
        log.info("Deactivating auto enrollment rule: tenantId={}, ruleId={}", tenantId, ruleId);

        AutoEnrollmentRule rule = ruleRepository.findByIdAndTenantId(ruleId, tenantId)
                .orElseThrow(() -> new AutoEnrollmentRuleNotFoundException(ruleId));

        rule.deactivate();
        return AutoEnrollmentRuleResponse.from(rule);
    }
}
