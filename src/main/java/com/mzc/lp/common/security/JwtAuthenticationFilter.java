package com.mzc.lp.common.security;

import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
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

    private final JwtProvider jwtProvider;
    private final UserCourseRoleRepository userCourseRoleRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            // 토큰 유효성 검사 (만료 여부 포함)
            JwtValidationResult validationResult = jwtProvider.validateTokenWithResult(token);

            if (validationResult.isExpired()) {
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

                // DB에서 CourseRole 조회 (DESIGNER, OWNER, INSTRUCTOR)
                Set<String> courseRoles = userCourseRoleRepository.findByUserId(userId)
                        .stream()
                        .map(ucr -> ucr.getRole().name())
                        .collect(Collectors.toSet());

                UserPrincipal principal = new UserPrincipal(userId, email, role, courseRoles);

                // 시스템 역할 + CourseRole 모두 authorities에 추가
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                courseRoles.forEach(courseRole ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + courseRole))
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {}, courseRoles: {}", email, courseRoles);
            } else {
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
