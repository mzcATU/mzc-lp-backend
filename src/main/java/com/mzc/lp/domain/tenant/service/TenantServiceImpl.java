package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.domain.tenant.dto.request.CreateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantStatusRequest;
import com.mzc.lp.domain.tenant.dto.response.TenantResponse;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.exception.DuplicateCustomDomainException;
import com.mzc.lp.domain.tenant.exception.DuplicateSubdomainException;
import com.mzc.lp.domain.tenant.exception.DuplicateTenantCodeException;
import com.mzc.lp.domain.tenant.exception.TenantDomainNotFoundException;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("Creating tenant: code={}, name={}", request.code(), request.name());

        // 중복 검증
        validateDuplicateCode(request.code());
        validateDuplicateSubdomain(request.subdomain());
        if (request.customDomain() != null && !request.customDomain().isBlank()) {
            validateDuplicateCustomDomain(request.customDomain());
        }

        Tenant tenant = Tenant.create(
                request.code(),
                request.name(),
                request.type(),
                request.subdomain(),
                request.plan(),
                request.customDomain()
        );

        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant created: tenantId={}, code={}", saved.getId(), saved.getCode());

        return TenantResponse.from(saved);
    }

    @Override
    public Page<TenantResponse> getTenants(String keyword, Pageable pageable) {
        log.debug("Getting tenants: keyword={}", keyword);

        Page<Tenant> tenants;
        if (keyword != null && !keyword.isBlank()) {
            tenants = tenantRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else {
            tenants = tenantRepository.findAll(pageable);
        }

        return tenants.map(TenantResponse::from);
    }

    @Override
    public TenantResponse getTenant(Long tenantId) {
        log.debug("Getting tenant: tenantId={}", tenantId);

        Tenant tenant = findTenantById(tenantId);
        return TenantResponse.from(tenant);
    }

    @Override
    public TenantResponse getTenantByCode(String code) {
        log.debug("Getting tenant by code: code={}", code);

        Tenant tenant = tenantRepository.findByCode(code)
                .orElseThrow(() -> new TenantDomainNotFoundException("테넌트를 찾을 수 없습니다: " + code));
        return TenantResponse.from(tenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(Long tenantId, UpdateTenantRequest request) {
        log.info("Updating tenant: tenantId={}", tenantId);

        Tenant tenant = findTenantById(tenantId);

        // 커스텀 도메인 중복 검증 (변경된 경우에만)
        if (request.customDomain() != null && !request.customDomain().isBlank()) {
            if (!request.customDomain().equals(tenant.getCustomDomain())) {
                validateDuplicateCustomDomain(request.customDomain());
            }
        }

        tenant.update(request.name(), request.customDomain(), request.plan());
        log.info("Tenant updated: tenantId={}", tenantId);

        return TenantResponse.from(tenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenantStatus(Long tenantId, UpdateTenantStatusRequest request) {
        log.info("Updating tenant status: tenantId={}, status={}", tenantId, request.status());

        Tenant tenant = findTenantById(tenantId);
        tenant.changeStatus(request.status());
        log.info("Tenant status updated: tenantId={}, newStatus={}", tenantId, request.status());

        return TenantResponse.from(tenant);
    }

    @Override
    @Transactional
    public void deleteTenant(Long tenantId) {
        log.info("Deleting tenant: tenantId={}", tenantId);

        Tenant tenant = findTenantById(tenantId);
        tenantRepository.delete(tenant);
        log.info("Tenant deleted: tenantId={}", tenantId);
    }

    // === Private Helper Methods ===

    private Tenant findTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantDomainNotFoundException(tenantId));
    }

    private void validateDuplicateCode(String code) {
        if (tenantRepository.existsByCode(code)) {
            throw new DuplicateTenantCodeException(code);
        }
    }

    private void validateDuplicateSubdomain(String subdomain) {
        if (tenantRepository.existsBySubdomain(subdomain)) {
            throw new DuplicateSubdomainException(subdomain);
        }
    }

    private void validateDuplicateCustomDomain(String customDomain) {
        if (tenantRepository.existsByCustomDomain(customDomain)) {
            throw new DuplicateCustomDomainException(customDomain);
        }
    }
}
