package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.util.Set;

/**
 * 사용자 역할 응답 (1:N)
 */
public record UserRolesResponse(
        Long userId,
        String email,
        String name,
        Set<TenantRole> roles,
        TenantRole primaryRole,
        Instant updatedAt
) {
    public static UserRolesResponse from(User user) {
        return new UserRolesResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles(),
                user.getRole(),
                user.getUpdatedAt()
        );
    }
}
