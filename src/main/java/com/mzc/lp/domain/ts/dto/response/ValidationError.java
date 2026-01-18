package com.mzc.lp.domain.ts.dto.response;

/**
 * 검증 오류 DTO
 */
public record ValidationError(
        String ruleCode,
        String field,
        I18nMessage message,
        boolean clientValidatable
) {
    public static ValidationError of(String ruleCode, String field, I18nMessage message, boolean clientValidatable) {
        return new ValidationError(ruleCode, field, message, clientValidatable);
    }

    public static ValidationError serverOnly(String ruleCode, String field, I18nMessage message) {
        return new ValidationError(ruleCode, field, message, false);
    }

    public static ValidationError clientValidatable(String ruleCode, String field, I18nMessage message) {
        return new ValidationError(ruleCode, field, message, true);
    }
}
