package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

public record UserStatusResponse(
        Long userId,
        String status,
        Instant updatedAt
) {
    public static UserStatusResponse from(User user) {
        return new UserStatusResponse(
                user.getId(),
                user.getStatus().name(),
                user.getUpdatedAt()
        );
    }
}
