package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record UserListResponse(
        Long userId,
        String email,
        String name,
        String role,
        String status,
        Instant createdAt
) {
    public static UserListResponse from(User user) {
        return new UserListResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
