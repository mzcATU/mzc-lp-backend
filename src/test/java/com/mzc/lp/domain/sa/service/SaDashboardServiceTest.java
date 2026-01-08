package com.mzc.lp.domain.sa.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.sa.dto.response.SaDashboardResponse;
import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantType;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SaDashboardServiceTest extends TenantTestSupport {

    @Autowired
    private SaDashboardService saDashboardService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userCourseRoleRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    @DisplayName("대시보드 통계를 조회한다")
    void getDashboard_success() {
        // given
        Tenant tenant1 = Tenant.create("TENANT1", "테넌트1", TenantType.B2B, "tenant1", PlanType.BASIC);
        tenant1.activate();
        tenantRepository.save(tenant1);

        Tenant tenant2 = Tenant.create("TENANT2", "테넌트2", TenantType.B2C, "tenant2", PlanType.PRO);
        tenantRepository.save(tenant2);

        // TenantContext를 통해 자동으로 tenantId가 설정됨
        User user1 = User.create("user1@example.com", "사용자1", passwordEncoder.encode("Password123!"));
        userRepository.save(user1);

        User user2 = User.create("user2@example.com", "사용자2", passwordEncoder.encode("Password123!"));
        userRepository.save(user2);

        // when
        SaDashboardResponse response = saDashboardService.getDashboard(null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTenantStats().getTotal()).isEqualTo(2);
        assertThat(response.getTenantStats().getActive()).isEqualTo(1);
        assertThat(response.getTenantStats().getPending()).isEqualTo(1);
        assertThat(response.getUserStats().getTotal()).isEqualTo(2);
        assertThat(response.getUserStats().getActive()).isEqualTo(2);
        assertThat(response.getRecentTenants()).isNotEmpty();
    }

    @Test
    @DisplayName("데이터가 없을 때 대시보드 통계를 조회한다")
    void getDashboard_empty() {
        // when
        SaDashboardResponse response = saDashboardService.getDashboard(null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTenantStats().getTotal()).isZero();
        assertThat(response.getUserStats().getTotal()).isZero();
        assertThat(response.getRecentTenants()).isEmpty();
    }
}
