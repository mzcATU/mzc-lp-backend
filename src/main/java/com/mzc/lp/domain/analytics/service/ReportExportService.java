package com.mzc.lp.domain.analytics.service;

import com.mzc.lp.domain.analytics.constant.ExportFormat;
import com.mzc.lp.domain.analytics.constant.ReportPeriod;
import com.mzc.lp.domain.analytics.constant.ReportType;
import com.mzc.lp.domain.analytics.dto.response.ExportJobResponse;
import com.mzc.lp.domain.analytics.dto.response.ExportStatsResponse;

import java.io.OutputStream;
import java.util.List;

/**
 * 리포트 내보내기 서비스 인터페이스
 */
public interface ReportExportService {

    /**
     * 리포트 생성 및 내보내기
     */
    void exportReport(Long tenantId, ReportType reportType, ExportFormat format,
                      ReportPeriod period, OutputStream outputStream);

    /**
     * 내보내기 이력 조회
     */
    List<ExportJobResponse> getExportHistory(Long tenantId, int limit);

    /**
     * 내보내기 통계 조회
     */
    ExportStatsResponse getExportStats(Long tenantId);
}
