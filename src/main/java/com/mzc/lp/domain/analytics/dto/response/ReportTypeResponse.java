package com.mzc.lp.domain.analytics.dto.response;

import com.mzc.lp.domain.analytics.constant.ReportType;

/**
 * 리포트 유형 응답 DTO
 */
public record ReportTypeResponse(
        String id,
        String name,
        String description
) {
    public static ReportTypeResponse from(ReportType type) {
        return new ReportTypeResponse(
                type.name().toLowerCase(),
                type.getName(),
                type.getDescription()
        );
    }
}
