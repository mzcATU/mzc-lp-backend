package com.mzc.lp.domain.enrollment.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class AutoEnrollmentRuleNotFoundException extends BusinessException {

    public AutoEnrollmentRuleNotFoundException() {
        super(ErrorCode.AUTO_ENROLLMENT_RULE_NOT_FOUND);
    }

    public AutoEnrollmentRuleNotFoundException(Long ruleId) {
        super(ErrorCode.AUTO_ENROLLMENT_RULE_NOT_FOUND, "자동 수강신청 규칙을 찾을 수 없습니다. ID: " + ruleId);
    }
}
