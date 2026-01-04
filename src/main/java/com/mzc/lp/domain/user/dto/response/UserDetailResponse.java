package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record UserDetailResponse(
        Long userId,
        String email,
        String name,
        String phone,
        String profileImageUrl,
        String role,
        String status,
        Long tenantId,
        Instant createdAt,
        Instant updatedAt,
        List<CourseRoleResponse> courseRoles
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getTenantId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                Collections.emptyList()
        );
    }

    public static UserDetailResponse from(User user, List<CourseRoleResponse> courseRoles) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getTenantId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                courseRoles != null ? courseRoles : Collections.emptyList()
        );
    }
}
