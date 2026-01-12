package com.mzc.lp.common.security;

import java.util.Set;

public record UserPrincipal(
        Long id,
        Long tenantId,
        String email,
        String role,
        Set<String> courseRoles  // DESIGNER, INSTRUCTOR
) {
    // 하위 호환성을 위한 생성자 (tenantId 없이)
    public UserPrincipal(Long id, String email, String role) {
        this(id, null, email, role, Set.of());
    }

    // 하위 호환성을 위한 생성자 (tenantId 없이, courseRoles 있음)
    public UserPrincipal(Long id, String email, String role, Set<String> courseRoles) {
        this(id, null, email, role, courseRoles);
    }
}
