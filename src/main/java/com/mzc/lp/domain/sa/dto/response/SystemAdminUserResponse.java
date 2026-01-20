package com.mzc.lp.domain.sa.dto.response;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record SystemAdminUserResponse(
        Long userId,
        String email,
        String name,
        String phone,
        String profileImageUrl,
        String department,
        String position,
        TenantRole role,
        UserStatus status,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static SystemAdminUserResponse from(User user) {
        return new SystemAdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getDepartment(),
                user.getPosition(),
                user.getRole(),
                user.getStatus(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
