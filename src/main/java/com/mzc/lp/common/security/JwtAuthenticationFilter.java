package com.mzc.lp.common.security;

import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
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
