package com.mzc.lp.domain.user.dto.response;

import java.util.List;

public record BulkCreateUsersResponse(
        int totalRequested,
        int successCount,
        int failedCount,
        List<CreatedUserInfo> createdUsers,
        List<FailedUserInfo> failedUsers
) {
    public record CreatedUserInfo(
            Long id,
            String email,
            String name
    ) {}

    public record FailedUserInfo(
            String email,
            String reason
    ) {}

    public static BulkCreateUsersResponse of(
            int totalRequested,
            List<CreatedUserInfo> createdUsers,
            List<FailedUserInfo> failedUsers
    ) {
        return new BulkCreateUsersResponse(
                totalRequested,
                createdUsers.size(),
                failedUsers.size(),
                createdUsers,
                failedUsers
        );
    }
}
