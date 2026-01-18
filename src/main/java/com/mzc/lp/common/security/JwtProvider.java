package com.mzc.lp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessTokenExpiry,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String createAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiry);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * tenantId를 포함한 AccessToken 생성 (CMS/LO 모듈용)
     */
    public String createAccessToken(Long userId, String email, String role, Long tenantId) {
        return createAccessToken(userId, email, role, Set.of(role), tenantId);
    }

    /**
     * 다중 역할을 포함한 AccessToken 생성
     */
    public String createAccessToken(Long userId, String email, String role, Set<String> roles, Long tenantId) {
        return createAccessToken(userId, email, role, roles, role, tenantId);
    }

    /**
     * 다중 역할 + 현재 선택된 역할을 포함한 AccessToken 생성
     */
    public String createAccessToken(Long userId, String email, String role, Set<String> roles, String currentRole, Long tenantId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiry);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .claim("roles", new ArrayList<>(roles))  // 다중 역할
                .claim("currentRole", currentRole)       // 현재 선택된 역할
                .claim("tenantId", tenantId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiry);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰 검증 결과를 상세하게 반환 (만료 여부 구분)
     */
    public JwtValidationResult validateTokenWithResult(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return JwtValidationResult.validToken();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            return JwtValidationResult.expiredToken();
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return JwtValidationResult.invalidToken();
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        try {
            return Long.parseLong(getClaims(token).getSubject());
        } catch (NumberFormatException e) {
            log.error("Invalid userId format in token subject");
            throw new IllegalArgumentException("Invalid userId format in token");
        }
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * 다중 역할 조회
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRoles(String token) {
        Claims claims = getClaims(token);
        List<String> rolesList = claims.get("roles", List.class);
        if (rolesList != null) {
            return new HashSet<>(rolesList);
        }
        // 하위 호환성: roles가 없으면 role 하나만 반환
        String role = claims.get("role", String.class);
        return role != null ? Set.of(role) : Set.of();
    }

    /**
     * 현재 선택된 역할 조회
     */
    public String getCurrentRole(String token) {
        Claims claims = getClaims(token);
        String currentRole = claims.get("currentRole", String.class);
        // 하위 호환성: currentRole이 없으면 role 반환
        return currentRole != null ? currentRole : claims.get("role", String.class);
    }

    /**
     * tenantId claim 추출 (CMS/LO 모듈용)
     */
    public Long getTenantId(String token) {
        return getClaims(token).get("tenantId", Long.class);
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }
}
