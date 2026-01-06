package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record UserListResponse(
        Long id,
        String email,
        String name,
        String profileImageUrl,
        String systemRole,
        String status,
        String organizationName,
        Instant lastLoginAt,
        Instant createdAt
) {
    public static UserListResponse from(User user) {
        return new UserListResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getStatus().name(),
                null,
                null,
                user.getCreatedAt()
        );
    }
}
