package com.mzc.lp.common.security;

public record UserPrincipal(
        Long id,
        String email,
        String role
) {}
