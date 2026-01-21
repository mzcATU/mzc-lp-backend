package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record UserListResponse(
        Long id,
        String email,
        String name,
        String profileImageUrl,
        String systemRole,
        Set<String> roles,  // 다중 역할 (1:N)
        String status,
        String organizationName,
        String department,
        String position,
        Instant lastLoginAt,
        Instant createdAt
) {
    public static UserListResponse from(User user) {
        // 사용자의 모든 역할 조회
        Set<String> roleNames = user.getRoles().stream()
                .map(TenantRole::name)
                .collect(Collectors.toSet());

        return new UserListResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                roleNames,
                user.getStatus().name(),
                user.getDepartment(),
                user.getDepartment(),
                user.getPosition(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
