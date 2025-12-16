package com.mzc.lp.common.config;

import com.mzc.lp.common.context.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Hibernate Tenant Filter 활성화 Aspect
 * Repository 메서드 호출 전 tenantFilter를 활성화하여
 * 모든 조회 쿼리에 자동으로 tenant_id 조건을 추가
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterConfig {

    private final EntityManager entityManager;

    /**
     * JpaRepository 메서드 실행 전 tenantFilter 활성화
     */
    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void enableTenantFilter() {
        if (TenantContext.isSet()) {
            Long tenantId = TenantContext.getCurrentTenantId();
            Session session = entityManager.unwrap(Session.class);

            // 필터가 이미 활성화되어 있지 않은 경우에만 활성화
            if (session.getEnabledFilter("tenantFilter") == null) {
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
                log.debug("Tenant filter enabled for tenantId: {}", tenantId);
            }
        }
    }
}
