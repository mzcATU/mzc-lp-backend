package com.mzc.lp.domain.analytics.dto.request;

import com.mzc.lp.domain.analytics.constant.ReportType;
import com.mzc.lp.domain.analytics.constant.ExportFormat;
import com.mzc.lp.domain.analytics.constant.ReportPeriod;
import jakarta.validation.constraints.NotNull;

/**
 * 리포트 내보내기 요청 DTO
 */
public record ExportReportRequest(
        @NotNull(message = "리포트 유형은 필수입니다")
        ReportType reportType,

        @NotNull(message = "파일 형식은 필수입니다")
        ExportFormat format,

        @NotNull(message = "기간은 필수입니다")
        ReportPeriod period
) {
}
