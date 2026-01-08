package com.mzc.lp.common.security;

import com.mzc.lp.common.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TenantContext 정리 필터
 * 요청 완료 후 ThreadLocal에 저장된 tenantId를 제거하여 메모리 릭 방지
 *
 * 필터 체인에서 가장 마지막에 실행되도록 Order를 최하위로 설정
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantContextCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 다음 필터 실행
            filterChain.doFilter(request, response);
        } finally {
            // 요청 완료 후 TenantContext 정리
            if (TenantContext.isSet()) {
                log.trace("Cleaning up TenantContext for request: {}", request.getRequestURI());
                TenantContext.clear();
            }
        }
    }
}
