package com.mzc.lp.domain.ts.dto.response;

import java.util.Map;

/**
 * 다국어 지원 메시지 DTO
 */
public record I18nMessage(
        String messageCode,
        Map<String, Object> params
) {
    public static I18nMessage of(String messageCode) {
        return new I18nMessage(messageCode, Map.of());
    }

    public static I18nMessage of(String messageCode, Map<String, Object> params) {
        return new I18nMessage(messageCode, params);
    }
}
