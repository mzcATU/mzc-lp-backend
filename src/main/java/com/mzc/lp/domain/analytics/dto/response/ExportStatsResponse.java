package com.mzc.lp.domain.analytics.dto.response;

/**
 * 내보내기 통계 응답 DTO
 */
public record ExportStatsResponse(
        int monthlyExportCount,
        String totalStorageUsed,
        String mostUsedReport
) {
    public static ExportStatsResponse of(int monthlyExportCount, String totalStorageUsed, String mostUsedReport) {
        return new ExportStatsResponse(monthlyExportCount, totalStorageUsed, mostUsedReport);
    }
}
