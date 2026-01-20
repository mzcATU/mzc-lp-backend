package com.mzc.lp.common.security;

import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // permitAll() 엔드포인트 목록 - 토큰 검증을 건너뜀
    private static final Set<String> PERMIT_ALL_PATHS = Set.of(
            "/api/auth/",
            "/uploads/",
            "/actuator/health",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final JwtProvider jwtProvider;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final UserRepository userRepository;

    /**
     * permitAll() 엔드포인트인지 확인
     */
    private boolean isPermitAllPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // /api/auth/switch-role은 인증 필요
        if (path.equals("/api/auth/switch-role")) {
            return false;
        }

        // 정적 경로 체크
        for (String permitPath : PERMIT_ALL_PATHS) {
            if (path.startsWith(permitPath)) {
                return true;
            }
        }

        // GET 메서드로만 허용된 공개 API
        // 주의: /api/courses/my는 인증이 필요하므로 제외
        if ("GET".equalsIgnoreCase(method)) {
            if (path.equals("/api/tenant/settings/branding") ||
                path.equals("/api/tenant/settings/features/public") ||
                path.equals("/api/tenant/settings/layout/public") ||
                path.equals("/api/tenant/settings/navigation/public") ||
                (path.startsWith("/api/courses") && !path.equals("/api/courses/my")) ||
                (path.startsWith("/api/community/posts") && !path.equals("/api/community/posts/my") && !path.equals("/api/community/posts/commented")) ||
                path.equals("/api/community/categories") ||
                path.startsWith("/api/public/course-times") ||
                path.startsWith("/api/banners/public")) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        boolean isPermitAllPath = isPermitAllPath(request);
        String token = resolveToken(request);

        // permitAll() 경로에서 토큰이 없으면 바로 통과
        if (isPermitAllPath && !StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (StringUtils.hasText(token)) {
            // 토큰 유효성 검사 (만료 여부 포함)
            JwtValidationResult validationResult = jwtProvider.validateTokenWithResult(token);

            if (validationResult.isExpired()) {
                // permitAll() 경로에서는 만료된 토큰이어도 에러 반환하지 않고 통과
                if (isPermitAllPath) {
                    log.debug("Token expired on permitAll path, proceeding without authentication");
                    filterChain.doFilter(request, response);
                    return;
                }
                // 토큰 만료 시 401 반환 (프론트엔드에서 refresh 토큰으로 갱신 시도)
                log.debug("Token expired, returning 401 for refresh");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"TOKEN_EXPIRED\",\"message\":\"Access token has expired\"}}");
                return;
            }

            if (validationResult.isValid()) {
                Long userId = jwtProvider.getUserId(token);
                String email = jwtProvider.getEmail(token);
                String role = jwtProvider.getRole(token);
                Set<String> roles = jwtProvider.getRoles(token);  // 다중 역할

                // tenantId는 토큰에서 가져오거나, 없으면 DB에서 조회
                Long tenantId = jwtProvider.getTenantId(token);
                if (tenantId == null) {
                    // DB에서 조회
                    tenantId = userRepository.findById(userId)
                            .map(User::getTenantId)
                            .orElse(null);
                }

                // DB에서 CourseRole 조회 (DESIGNER, INSTRUCTOR)
                Set<String> courseRoles = userCourseRoleRepository.findByUserId(userId)
                        .stream()
                        .map(ucr -> ucr.getRole().name())
                        .collect(Collectors.toSet());

                UserPrincipal principal = new UserPrincipal(userId, tenantId, email, role, roles, courseRoles);

                // 다중 시스템 역할 + CourseRole 모두 authorities에 추가
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                roles.forEach(r ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + r))
                );
                courseRoles.forEach(courseRole ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + courseRole))
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // TenantContext에 tenantId 설정 (서비스 레이어에서 사용)
                if (tenantId != null) {
                    com.mzc.lp.common.context.TenantContext.setTenantId(tenantId);
                }

                log.debug("Authenticated user: {}, tenantId: {}, roles: {}, courseRoles: {}", email, tenantId, roles, courseRoles);
            } else {
                // permitAll() 경로에서는 유효하지 않은 토큰이어도 에러 반환하지 않고 통과
                if (isPermitAllPath) {
                    log.debug("Invalid token on permitAll path, proceeding without authentication");
                    filterChain.doFilter(request, response);
                    return;
                }
                // 유효하지 않은 토큰 (만료 제외) - 401 반환
                log.debug("Invalid token, returning 401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"INVALID_TOKEN\",\"message\":\"Invalid access token\"}}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
