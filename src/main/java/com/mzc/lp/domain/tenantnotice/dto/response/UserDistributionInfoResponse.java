package com.mzc.lp.domain.tenantnotice.dto.response;

import java.time.Instant;

/**
 * 사용자별 배포 정보 응답
 */
public record UserDistributionInfoResponse(
        Long userId,
        String userName,
        String userEmail,
        String userRole,
        Boolean isRead,
        Instant distributedAt,
        Instant readAt
) {
    public static UserDistributionInfoResponse of(
            Long userId,
            String userName,
            String userEmail,
            String userRole,
            Boolean isRead,
            Instant distributedAt,
            Instant readAt
    ) {
        return new UserDistributionInfoResponse(
                userId,
                userName,
                userEmail,
                userRole,
                isRead,
                distributedAt,
                readAt
        );
    }
}
