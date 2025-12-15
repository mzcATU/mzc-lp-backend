package com.mzc.lp.common.filter;

import com.mzc.lp.common.context.TenantContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tenant Context 설정 Filter
 * 요청의 JWT 토큰에서 tenantId를 추출하여 TenantContext에 설정
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // SecurityFilter 다음에 실행
@Slf4j
public class TenantFilter implements Filter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 1. JWT에서 tenantId 추출
            Long tenantId = extractTenantIdFromRequest(httpRequest);

            // 2. TenantContext에 설정
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
                log.debug("TenantId set in context: {} for request: {} {}",
                        tenantId, httpRequest.getMethod(), httpRequest.getRequestURI());
            } else {
                // Public API나 인증 불필요 경로는 기본 테넌트 (임시)
                TenantContext.setTenantId(1L);
                log.debug("Default TenantId (1L) set for request: {} {}",
                        httpRequest.getMethod(), httpRequest.getRequestURI());
            }

            // 3. 다음 필터 체인 실행
            chain.doFilter(request, response);

        } finally {
            // 4. 요청 완료 후 반드시 제거 (메모리 릭 방지)
            TenantContext.clear();
            log.debug("TenantId cleared from context");
        }
    }

    /**
     * 요청에서 tenantId 추출
     * 현재는 JWT 연동 전이므로 임시로 null 반환
     *
     * @param request HTTP 요청
     * @return 테넌트 ID (현재는 null)
     */
    private Long extractTenantIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        try {
            // JWT에서 tenantId 추출 (TODO: JwtTokenProvider 연동)
            // String token = authHeader.substring(BEARER_PREFIX.length());
            // return jwtTokenProvider.getTenantIdFromToken(token);

            // 임시: JWT 연동 전까지 null 반환
            return null;

        } catch (Exception e) {
            log.warn("Failed to extract tenantId from JWT token: {}", e.getMessage());
            return null;
        }
    }
}
