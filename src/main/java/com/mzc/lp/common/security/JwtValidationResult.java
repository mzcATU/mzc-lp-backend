package com.mzc.lp.common.security;

/**
 * JWT 토큰 검증 결과
 */
public record JwtValidationResult(
        boolean isValid,
        boolean isExpired
) {
    public static JwtValidationResult validToken() {
        return new JwtValidationResult(true, false);
    }

    public static JwtValidationResult expiredToken() {
        return new JwtValidationResult(false, true);
    }

    public static JwtValidationResult invalidToken() {
        return new JwtValidationResult(false, false);
    }
}
