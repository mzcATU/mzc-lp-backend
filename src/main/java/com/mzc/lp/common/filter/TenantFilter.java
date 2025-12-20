package com.mzc.lp.common.filter;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.security.JwtProvider;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final JwtProvider jwtProvider;
    private final Long defaultTenantId;

    public TenantFilter(
            JwtProvider jwtProvider,
            @Value("${tenant.default-id:1}") Long defaultTenantId) {
        this.jwtProvider = jwtProvider;
        this.defaultTenantId = defaultTenantId;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 기존 TenantContext가 설정되어 있는지 확인 (테스트 등에서 미리 설정한 경우)
        boolean wasAlreadySet = TenantContext.isSet();
        Long previousTenantId = TenantContext.getCurrentTenantIdOrNull();

        try {
            // 기존 컨텍스트가 없는 경우에만 설정
            if (!wasAlreadySet) {
                // 1. JWT에서 tenantId 추출
                Long tenantId = extractTenantIdFromRequest(httpRequest);

                // 2. TenantContext에 설정
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                    log.debug("TenantId set in context: {} for request: {} {}",
                            tenantId, httpRequest.getMethod(), httpRequest.getRequestURI());
                } else {
                    // Public API나 인증 불필요 경로는 기본 테넌트 사용
                    TenantContext.setTenantId(defaultTenantId);
                    log.debug("Default TenantId ({}) set for request: {} {}",
                            defaultTenantId, httpRequest.getMethod(), httpRequest.getRequestURI());
                }
            } else {
                log.debug("TenantContext already set (tenantId: {}), skipping for request: {} {}",
                        previousTenantId, httpRequest.getMethod(), httpRequest.getRequestURI());
            }

            // 3. 다음 필터 체인 실행
            chain.doFilter(request, response);

        } finally {
            // 4. 필터에서 설정한 경우에만 제거 (기존 컨텍스트는 유지)
            if (!wasAlreadySet) {
                TenantContext.clear();
                log.debug("TenantId cleared from context");
            }
        }
    }

    /**
     * 요청에서 tenantId 추출
     * JWT 토큰의 tenantId claim에서 추출
     *
     * @param request HTTP 요청
     * @return 테넌트 ID (토큰 없거나 유효하지 않으면 null)
     */
    private Long extractTenantIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());

            // 토큰 유효성 검증
            if (!jwtProvider.validateToken(token)) {
                return null;
            }

            // JWT에서 tenantId 추출
            return jwtProvider.getTenantId(token);

        } catch (Exception e) {
            log.warn("Failed to extract tenantId from JWT token: {}", e.getMessage());
            return null;
        }
    }
}
