package com.mzc.lp.domain.tenant.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.tenant.dto.request.CreateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantStatusRequest;
import com.mzc.lp.domain.tenant.dto.response.CreateTenantResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantResponse;
import com.mzc.lp.domain.tenant.dto.response.TenantUserStatsResponse;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.exception.DuplicateCustomDomainException;
import com.mzc.lp.domain.tenant.exception.DuplicateSubdomainException;
import com.mzc.lp.domain.tenant.exception.DuplicateTenantCodeException;
import com.mzc.lp.domain.tenant.exception.TenantDomainNotFoundException;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_TENANT_ADMIN_PASSWORD = "1q2w3e4r!";

    @Override
    @Transactional
    public CreateTenantResponse createTenant(CreateTenantRequest request) {
        log.info("Creating tenant: code={}, name={}, adminEmail={}",
                request.code(), request.name(), request.adminEmail());

        // 중복 검증
        validateDuplicateCode(request.code());
        validateDuplicateSubdomain(request.subdomain());
        if (request.customDomain() != null && !request.customDomain().isBlank()) {
            validateDuplicateCustomDomain(request.customDomain());
        }

        // 1. 테넌트 생성
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

        // 2. TENANT_ADMIN 사용자 생성
        User tenantAdmin = createTenantAdmin(saved.getId(), request.adminEmail(), request.adminName(), DEFAULT_TENANT_ADMIN_PASSWORD);
        log.info("Tenant admin created: userId={}, email={}, tenantId={}",
                tenantAdmin.getId(), request.adminEmail(), saved.getId());

        return CreateTenantResponse.from(saved, tenantAdmin, DEFAULT_TENANT_ADMIN_PASSWORD);
    }

    /**
     * 테넌트 관리자 계정 생성
     */
    private User createTenantAdmin(Long tenantId, String email, String name, String rawPassword) {
        // TenantContext 설정 (User 엔티티의 tenantId 자동 주입을 위해)
        Long originalTenantId = null;
        try {
            originalTenantId = TenantContext.getCurrentTenantId();
        } catch (Exception ignored) {
            // TenantContext가 설정되지 않은 경우
        }

        try {
            TenantContext.setTenantId(tenantId);

            User admin = User.create(email, name, passwordEncoder.encode(rawPassword));
            admin.updateRole(TenantRole.TENANT_ADMIN);
            return userRepository.save(admin);
        } finally {
            // 원래 TenantContext 복원
            if (originalTenantId != null) {
                TenantContext.setTenantId(originalTenantId);
            } else {
                TenantContext.clear();
            }
        }
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

        return tenants.map(tenant -> {
            Long userCount = userRepository.countByTenantId(tenant.getId());
            Long courseCount = courseRepository.countByTenantId(tenant.getId());
            return TenantResponse.from(tenant, userCount, courseCount);
        });
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

        // 상태 변경 처리
        if (request.status() != null) {
            tenant.changeStatus(request.status());
            log.info("Tenant status changed: tenantId={}, newStatus={}", tenantId, request.status());
        }

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

        // 논리적 삭제 (Soft Delete) - TERMINATED 상태로 변경
        tenant.terminate();
        tenantRepository.save(tenant);

        log.info("Tenant marked as TERMINATED: tenantId={}", tenantId);
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

    @Override
    public TenantUserStatsResponse getTenantUserStats() {
        log.debug("Getting tenant user stats");

        List<Tenant> allTenants = tenantRepository.findAll();

        List<TenantUserStatsResponse.TenantUserCount> tenantUserCounts = allTenants.stream()
                .map(tenant -> new TenantUserStatsResponse.TenantUserCount(
                        tenant.getId(),
                        tenant.getCode(),
                        tenant.getName(),
                        userRepository.countByTenantId(tenant.getId())
                ))
                .toList();

        long totalUsers = tenantUserCounts.stream()
                .mapToLong(TenantUserStatsResponse.TenantUserCount::userCount)
                .sum();

        return TenantUserStatsResponse.of(tenantUserCounts, totalUsers);
    }
}
