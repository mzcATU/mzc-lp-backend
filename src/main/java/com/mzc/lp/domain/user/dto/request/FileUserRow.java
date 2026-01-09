package com.mzc.lp.domain.user.dto.request;

public record FileUserRow(
        String email,
        String name,
        String password,
        String phone,
        String department,
        String position
) {}
