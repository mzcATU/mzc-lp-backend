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
        String department,
        String position,
        String role,
        String status,
        Long tenantId,
        String tenantSubdomain,
        String tenantCustomDomain,
        Instant createdAt,
        Instant updatedAt,
        List<CourseRoleResponse> courseRoles,
        Boolean profileCompleted
) {
    /**
     * 프로필 완성 여부 판단
     * - 이름이 null이거나 빈 문자열이면 미완성
     * - 이름이 이메일 로컬파트와 동일하면 단체 생성된 계정으로 간주
     *   단, 부서와 직급이 모두 있으면 완성으로 간주 (파일 업로드 시 부서/직급 포함 케이스)
     */
    private static Boolean isProfileCompleted(User user) {
        String name = user.getName();
        String email = user.getEmail();

        if (name == null || name.isBlank()) {
            return false;
        }

        // 이메일 로컬파트(@ 앞부분) 추출
        String emailLocalPart = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;

        // 이름이 이메일 로컬파트와 동일하면 단체 생성된 계정
        // 하지만 부서와 직급이 모두 있으면 완성으로 간주 (파일에 부서/직급 포함된 경우)
        if (name.equals(emailLocalPart)) {
            return user.getDepartment() != null && !user.getDepartment().isBlank() &&
                   user.getPosition() != null && !user.getPosition().isBlank();
        }

        // 이름이 다르면 프로필 완성
        return true;
    }

    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getDepartment(),
                user.getPosition(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getTenantId(),
                null,
                null,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                Collections.emptyList(),
                isProfileCompleted(user)
        );
    }

    public static UserDetailResponse from(User user, List<CourseRoleResponse> courseRoles) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getDepartment(),
                user.getPosition(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getTenantId(),
                null,
                null,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                courseRoles != null ? courseRoles : Collections.emptyList(),
                isProfileCompleted(user)
        );
    }

    public static UserDetailResponse from(User user, List<CourseRoleResponse> courseRoles, String tenantSubdomain, String tenantCustomDomain) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getDepartment(),
                user.getPosition(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getTenantId(),
                tenantSubdomain,
                tenantCustomDomain,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                courseRoles != null ? courseRoles : Collections.emptyList(),
                isProfileCompleted(user)
        );
    }
}
