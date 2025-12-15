package com.mzc.lp.domain.tenant.entity;

import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.constant.TenantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantTest {

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("기본값으로 테넌트를 생성한다")
        void it_creates_tenant_with_defaults() {
            // given
            String code = "SAMSUNG";
            String name = "삼성전자";
            TenantType type = TenantType.B2B;
            String subdomain = "samsung";

            // when
            Tenant tenant = Tenant.create(code, name, type, subdomain, null);

            // then
            assertThat(tenant.getCode()).isEqualTo(code);
            assertThat(tenant.getName()).isEqualTo(name);
            assertThat(tenant.getType()).isEqualTo(type);
            assertThat(tenant.getSubdomain()).isEqualTo(subdomain);
            assertThat(tenant.getPlan()).isEqualTo(PlanType.FREE);
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING);
            assertThat(tenant.getCustomDomain()).isNull();
        }

        @Test
        @DisplayName("요금제와 커스텀 도메인을 포함하여 테넌트를 생성한다")
        void it_creates_tenant_with_plan_and_custom_domain() {
            // given
            String code = "SAMSUNG";
            String name = "삼성전자";
            TenantType type = TenantType.B2B;
            String subdomain = "samsung";
            PlanType plan = PlanType.ENTERPRISE;
            String customDomain = "learn.samsung.com";

            // when
            Tenant tenant = Tenant.create(code, name, type, subdomain, plan, customDomain);

            // then
            assertThat(tenant.getPlan()).isEqualTo(PlanType.ENTERPRISE);
            assertThat(tenant.getCustomDomain()).isEqualTo(customDomain);
        }
    }

    @Nested
    @DisplayName("상태 변경 메서드는")
    class Describe_status_change {

        @Test
        @DisplayName("activate로 ACTIVE 상태로 변경한다")
        void it_activates_tenant() {
            // given
            Tenant tenant = Tenant.create("TEST", "테스트", TenantType.B2C, "test", null);
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING);

            // when
            tenant.activate();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("suspend로 SUSPENDED 상태로 변경한다")
        void it_suspends_tenant() {
            // given
            Tenant tenant = Tenant.create("TEST", "테스트", TenantType.B2C, "test", null);
            tenant.activate();

            // when
            tenant.suspend();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("terminate로 TERMINATED 상태로 변경한다")
        void it_terminates_tenant() {
            // given
            Tenant tenant = Tenant.create("TEST", "테스트", TenantType.B2C, "test", null);

            // when
            tenant.terminate();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.TERMINATED);
            assertThat(tenant.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("update 메서드는")
    class Describe_update {

        @Test
        @DisplayName("이름, 커스텀 도메인, 요금제를 수정한다")
        void it_updates_tenant() {
            // given
            Tenant tenant = Tenant.create("SAMSUNG", "삼성전자", TenantType.B2B, "samsung", PlanType.BASIC);

            // when
            tenant.update("삼성전자 러닝센터", "learn.samsung.com", PlanType.ENTERPRISE);

            // then
            assertThat(tenant.getName()).isEqualTo("삼성전자 러닝센터");
            assertThat(tenant.getCustomDomain()).isEqualTo("learn.samsung.com");
            assertThat(tenant.getPlan()).isEqualTo(PlanType.ENTERPRISE);
        }

        @Test
        @DisplayName("null 이름은 무시하고 기존 값을 유지한다")
        void it_ignores_null_name() {
            // given
            Tenant tenant = Tenant.create("SAMSUNG", "삼성전자", TenantType.B2B, "samsung", PlanType.BASIC);

            // when
            tenant.update(null, "learn.samsung.com", PlanType.ENTERPRISE);

            // then
            assertThat(tenant.getName()).isEqualTo("삼성전자");
        }

        @Test
        @DisplayName("빈 이름은 무시하고 기존 값을 유지한다")
        void it_ignores_blank_name() {
            // given
            Tenant tenant = Tenant.create("SAMSUNG", "삼성전자", TenantType.B2B, "samsung", PlanType.BASIC);

            // when
            tenant.update("   ", "learn.samsung.com", PlanType.ENTERPRISE);

            // then
            assertThat(tenant.getName()).isEqualTo("삼성전자");
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드는")
    class Describe_status_check {

        @Test
        @DisplayName("isPending은 PENDING 상태일 때 true를 반환한다")
        void it_returns_true_for_pending() {
            Tenant tenant = Tenant.create("TEST", "테스트", TenantType.B2C, "test", null);

            assertThat(tenant.isPending()).isTrue();
            assertThat(tenant.isActive()).isFalse();
            assertThat(tenant.isSuspended()).isFalse();
            assertThat(tenant.isTerminated()).isFalse();
        }
    }
}
