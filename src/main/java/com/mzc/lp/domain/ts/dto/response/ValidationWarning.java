package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.ts.constant.QualityRating;

/**
 * 검증 경고 DTO
 */
public record ValidationWarning(
        String ruleCode,
        String field,
        I18nMessage message,
        QualityRating qualityRating
) {
    public static ValidationWarning of(String ruleCode, String field, I18nMessage message, QualityRating qualityRating) {
        return new ValidationWarning(ruleCode, field, message, qualityRating);
    }

    public static ValidationWarning caution(String ruleCode, String field, I18nMessage message) {
        return new ValidationWarning(ruleCode, field, message, QualityRating.CAUTION);
    }

    public static ValidationWarning common(String ruleCode, String field, I18nMessage message) {
        return new ValidationWarning(ruleCode, field, message, QualityRating.COMMON);
    }
}
