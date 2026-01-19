package com.mzc.lp.domain.analytics.dto.response;

import com.mzc.lp.domain.analytics.constant.ExportFormat;
import com.mzc.lp.domain.analytics.constant.ReportPeriod;
import com.mzc.lp.domain.analytics.constant.ReportType;

import java.time.Instant;

/**
 * 내보내기 작업 응답 DTO
 */
public record ExportJobResponse(
        Long id,
        ReportType reportType,
        String reportTypeName,
        ExportFormat format,
        ReportPeriod period,
        String periodLabel,
        ExportStatus status,
        Instant createdAt,
        Instant completedAt,
        String fileSize,
        String downloadUrl
) {
    public enum ExportStatus {
        PROCESSING("처리중"),
        COMPLETED("완료"),
        FAILED("실패");

        private final String label;

        ExportStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static ExportJobResponse of(
            Long id,
            ReportType reportType,
            ExportFormat format,
            ReportPeriod period,
            ExportStatus status,
            Instant createdAt,
            Instant completedAt,
            String fileSize,
            String downloadUrl
    ) {
        return new ExportJobResponse(
                id,
                reportType,
                reportType.getName(),
                format,
                period,
                period.getLabel(),
                status,
                createdAt,
                completedAt,
                fileSize,
                downloadUrl
        );
    }
}
