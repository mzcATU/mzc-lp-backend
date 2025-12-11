package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record UserRoleResponse(
        Long userId,
        String email,
        String name,
        String role,
        Instant updatedAt
) {
    public static UserRoleResponse from(User user) {
        return new UserRoleResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getUpdatedAt()
        );
    }
}
