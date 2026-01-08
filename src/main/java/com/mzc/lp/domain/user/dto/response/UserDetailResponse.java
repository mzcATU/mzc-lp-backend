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
     * - 이름이 이메일 로컬파트와 동일하면 미완성 (단체 계정 생성 시 이메일 prefix를 이름으로 사용)
     * - 이름이 null이거나 빈 문자열이면 미완성
     * - USER 역할: 부서(department)와 직급(position)이 모두 있어야 완성
     * - 관리자 역할(SA, TA, TO, DESIGNER): 부서/직급 선택사항
     */
    private static Boolean isProfileCompleted(User user) {
        String name = user.getName();
        String email = user.getEmail();

        if (name == null || name.isBlank()) {
            return false;
        }

        // 이메일 로컬파트(@ 앞부분) 추출
        String emailLocalPart = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;

        // 이름이 이메일 로컬파트와 동일하면 단체 생성된 계정으로 간주
        if (name.equals(emailLocalPart)) {
            return false;
        }

        // USER 역할은 부서와 직급이 필수
        if (user.getRole() != com.mzc.lp.domain.user.constant.TenantRole.USER) {
            return true;
        }
        return user.getDepartment() != null && !user.getDepartment().isBlank() &&
               user.getPosition() != null && !user.getPosition().isBlank();
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
