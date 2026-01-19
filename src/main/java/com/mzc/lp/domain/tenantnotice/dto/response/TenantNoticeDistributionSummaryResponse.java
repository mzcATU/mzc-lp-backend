package com.mzc.lp.domain.tenantnotice.dto.response;

/**
 * 배포 통계 요약 응답
 */
public record TenantNoticeDistributionSummaryResponse(
        Long totalDistributions,   // 총 배포 건수 (발행된 공지 수)
        Long completedCount,       // 발행 완료 건수
        Long totalReadCount,       // 총 열람 수
        Long totalTargetUsers,     // 총 대상 사용자 수
        Double averageReadRate     // 평균 열람율 (%)
) {
    public static TenantNoticeDistributionSummaryResponse of(
            long totalDistributions,
            long completedCount,
            long totalReadCount,
            long totalTargetUsers
    ) {
        double averageReadRate = totalTargetUsers > 0
                ? (double) totalReadCount / totalTargetUsers * 100.0
                : 0.0;
        return new TenantNoticeDistributionSummaryResponse(
                totalDistributions,
                completedCount,
                totalReadCount,
                totalTargetUsers,
                averageReadRate
        );
    }
}
