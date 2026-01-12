package com.mzc.lp.domain.notice.dto.response;

/**
 * 공지사항 배포 요약 통계 응답 DTO
 */
public record NoticeDistributionSummaryResponse(
        Integer totalDistributions,
        Integer completedCount,
        Integer inProgressCount,
        Integer totalTenants,
        Integer totalReadCount,
        Double averageReadRate
) {
    public static NoticeDistributionSummaryResponse of(
            Integer totalDistributions,
            Integer completedCount,
            Integer inProgressCount,
            Integer totalTenants,
            Integer totalReadCount,
            Double averageReadRate
    ) {
        return new NoticeDistributionSummaryResponse(
                totalDistributions,
                completedCount,
                inProgressCount,
                totalTenants,
                totalReadCount,
                averageReadRate
        );
    }
}
