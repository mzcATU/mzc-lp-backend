package com.mzc.lp.domain.user.dto.response;

import java.util.List;

public record BulkCreateUsersResponse(
        int totalRequested,
        int successCount,
        int failedCount,
        int autoLinkedCount,
        List<CreatedUserInfo> createdUsers,
        List<FailedUserInfo> failedUsers,
        List<AutoLinkedUserInfo> autoLinkedUsers
) {
    public record CreatedUserInfo(
            Long id,
            String email,
            String name,
            boolean employeeLinked,
            Long employeeId
    ) {
        public CreatedUserInfo(Long id, String email, String name) {
            this(id, email, name, false, null);
        }
    }

    public record FailedUserInfo(
            String email,
            String reason,
            Integer rowNumber  // 파일에서 실패한 행 번호 (nullable)
    ) {
        public FailedUserInfo(String email, String reason) {
            this(email, reason, null);
        }
    }

    public record AutoLinkedUserInfo(
            Long userId,
            String email,
            Long employeeId,
            String employeeNumber,
            String employeeName,
            String department,
            String position,
            String jobTitle
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
                0,
                createdUsers,
                failedUsers,
                List.of()
        );
    }

    public static BulkCreateUsersResponse of(
            int totalRequested,
            List<CreatedUserInfo> createdUsers,
            List<FailedUserInfo> failedUsers,
            List<AutoLinkedUserInfo> autoLinkedUsers
    ) {
        return new BulkCreateUsersResponse(
                totalRequested,
                createdUsers.size(),
                failedUsers.size(),
                autoLinkedUsers.size(),
                createdUsers,
                failedUsers,
                autoLinkedUsers
        );
    }
}
