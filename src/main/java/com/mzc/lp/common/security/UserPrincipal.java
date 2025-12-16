package com.mzc.lp.common.security;

import java.util.Set;

public record UserPrincipal(
        Long id,
        String email,
        String role,
        Set<String> courseRoles  // DESIGNER, OWNER, INSTRUCTOR
) {
    // 하위 호환성을 위한 생성자
    public UserPrincipal(Long id, String email, String role) {
        this(id, email, role, Set.of());
    }
}
