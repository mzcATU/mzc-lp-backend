package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.response.TenantDomainSettingsResponse;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.exception.DuplicateCustomDomainException;
import com.mzc.lp.common.exception.TenantNotFoundException;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainSettingsServiceImpl implements DomainSettingsService {

    private final TenantRepository tenantRepository;

    @Value("${app.domain.base:mzc-lp.com}")
    private String baseDomain;

    @Override
    public TenantDomainSettingsResponse getDomainSettings(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));

        return TenantDomainSettingsResponse.from(tenant, baseDomain);
    }

    @Override
    @Transactional
    public TenantDomainSettingsResponse updateCustomDomain(Long tenantId, String customDomain) {
        log.info("Updating custom domain: tenantId={}, customDomain={}", tenantId, customDomain);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));

        // 다른 테넌트가 이미 사용 중인지 확인 (자기 자신 제외)
        if (!customDomain.equals(tenant.getCustomDomain()) &&
                tenantRepository.existsByCustomDomain(customDomain)) {
            throw new DuplicateCustomDomainException(customDomain);
        }

        tenant.update(tenant.getName(), customDomain, tenant.getPlan());

        log.info("Custom domain updated: tenantId={}, customDomain={}", tenantId, customDomain);
        return TenantDomainSettingsResponse.from(tenant, baseDomain);
    }

    @Override
    @Transactional
    public void deleteCustomDomain(Long tenantId) {
        log.info("Deleting custom domain: tenantId={}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));

        tenant.update(tenant.getName(), null, tenant.getPlan());

        log.info("Custom domain deleted: tenantId={}", tenantId);
    }
}
