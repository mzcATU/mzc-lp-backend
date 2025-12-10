package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record UserResponse(
        Long userId,
        String email,
        String name,
        TenantRole role,
        Instant createdAt
) {
    public static UserResponse from(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }
}
