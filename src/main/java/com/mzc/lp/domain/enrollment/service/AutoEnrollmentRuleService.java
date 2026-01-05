package com.mzc.lp.domain.enrollment.service;

import com.mzc.lp.domain.enrollment.constant.AutoEnrollmentTrigger;
import com.mzc.lp.domain.enrollment.dto.request.CreateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.request.UpdateAutoEnrollmentRuleRequest;
import com.mzc.lp.domain.enrollment.dto.response.AutoEnrollmentRuleResponse;

import java.util.List;

public interface AutoEnrollmentRuleService {

    AutoEnrollmentRuleResponse create(Long tenantId, CreateAutoEnrollmentRuleRequest request);

    AutoEnrollmentRuleResponse update(Long tenantId, Long ruleId, UpdateAutoEnrollmentRuleRequest request);

    void delete(Long tenantId, Long ruleId);

    AutoEnrollmentRuleResponse getById(Long tenantId, Long ruleId);

    List<AutoEnrollmentRuleResponse> getAll(Long tenantId);

    List<AutoEnrollmentRuleResponse> getActiveRules(Long tenantId);

    List<AutoEnrollmentRuleResponse> getByTrigger(Long tenantId, AutoEnrollmentTrigger trigger);

    AutoEnrollmentRuleResponse activate(Long tenantId, Long ruleId);

    AutoEnrollmentRuleResponse deactivate(Long tenantId, Long ruleId);
}
