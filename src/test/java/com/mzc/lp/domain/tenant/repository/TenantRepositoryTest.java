package com.mzc.lp.domain.tenant.repository;

import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.constant.TenantType;
import com.mzc.lp.domain.tenant.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        tenantRepository.deleteAll();

        Tenant tenant = Tenant.create(
                "SAMSUNG",
                "삼성전자",
                TenantType.B2B,
                "samsung",
                PlanType.ENTERPRISE,
                "learn.samsung.com"
        );
        tenant.activate();
        tenantRepository.save(tenant);
    }

    @Nested
    @DisplayName("findByCode 메서드는")
    class Describe_findByCode {

        @Test
        @DisplayName("코드로 테넌트를 조회한다")
        void it_finds_tenant_by_code() {
            // when
            Optional<Tenant> result = tenantRepository.findByCode("SAMSUNG");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("삼성전자");
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회하면 빈 Optional을 반환한다")
        void it_returns_empty_for_nonexistent_code() {
            // when
            Optional<Tenant> result = tenantRepository.findByCode("NONEXISTENT");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySubdomain 메서드는")
    class Describe_findBySubdomain {

        @Test
        @DisplayName("서브도메인으로 테넌트를 조회한다")
        void it_finds_tenant_by_subdomain() {
            // when
            Optional<Tenant> result = tenantRepository.findBySubdomain("samsung");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("SAMSUNG");
        }
    }

    @Nested
    @DisplayName("findBySubdomainAndStatus 메서드는")
    class Describe_findBySubdomainAndStatus {

        @Test
        @DisplayName("서브도메인과 상태로 테넌트를 조회한다")
        void it_finds_tenant_by_subdomain_and_status() {
            // when
            Optional<Tenant> result = tenantRepository.findBySubdomainAndStatus("samsung", TenantStatus.ACTIVE);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("상태가 일치하지 않으면 빈 Optional을 반환한다")
        void it_returns_empty_for_mismatched_status() {
            // when
            Optional<Tenant> result = tenantRepository.findBySubdomainAndStatus("samsung", TenantStatus.PENDING);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCustomDomain 메서드는")
    class Describe_findByCustomDomain {

        @Test
        @DisplayName("커스텀 도메인으로 테넌트를 조회한다")
        void it_finds_tenant_by_custom_domain() {
            // when
            Optional<Tenant> result = tenantRepository.findByCustomDomain("learn.samsung.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("SAMSUNG");
        }
    }

    @Nested
    @DisplayName("exists 메서드는")
    class Describe_exists {

        @Test
        @DisplayName("existsByCode는 존재 여부를 반환한다")
        void it_checks_code_exists() {
            assertThat(tenantRepository.existsByCode("SAMSUNG")).isTrue();
            assertThat(tenantRepository.existsByCode("NONEXISTENT")).isFalse();
        }

        @Test
        @DisplayName("existsBySubdomain은 존재 여부를 반환한다")
        void it_checks_subdomain_exists() {
            assertThat(tenantRepository.existsBySubdomain("samsung")).isTrue();
            assertThat(tenantRepository.existsBySubdomain("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("existsByCustomDomain은 존재 여부를 반환한다")
        void it_checks_custom_domain_exists() {
            assertThat(tenantRepository.existsByCustomDomain("learn.samsung.com")).isTrue();
            assertThat(tenantRepository.existsByCustomDomain("nonexistent.com")).isFalse();
        }
    }
}
