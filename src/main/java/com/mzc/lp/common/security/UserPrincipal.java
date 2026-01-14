package com.mzc.lp.common.security;

import java.util.Set;

public record UserPrincipal(
        Long id,
        Long tenantId,
        String email,
        String role,           // 기본 역할 (하위 호환성)
        Set<String> roles,     // 다중 역할 (1:N)
        Set<String> courseRoles  // DESIGNER, INSTRUCTOR
) {
    // 하위 호환성을 위한 생성자 (tenantId 없이)
    public UserPrincipal(Long id, String email, String role) {
        this(id, null, email, role, Set.of(role), Set.of());
    }

    // 하위 호환성을 위한 생성자 (tenantId 없이, courseRoles 있음)
    public UserPrincipal(Long id, String email, String role, Set<String> courseRoles) {
        this(id, null, email, role, Set.of(role), courseRoles);
    }

    // 하위 호환성을 위한 생성자 (roles 없이)
    public UserPrincipal(Long id, Long tenantId, String email, String role, Set<String> courseRoles) {
        this(id, tenantId, email, role, Set.of(role), courseRoles);
    }

    // 특정 역할 보유 여부 확인
    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }
}
