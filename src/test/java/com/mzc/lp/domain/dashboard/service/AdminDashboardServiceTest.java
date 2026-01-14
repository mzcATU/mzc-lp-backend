package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.dto.stats.DailyEnrollmentStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.dashboard.dto.response.AdminKpiResponse;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest extends TenantTestSupport {

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private static final Long TENANT_ID = 1L;

    @Nested
    @DisplayName("getKpiStats - 관리자 KPI 대시보드 조회")
    class GetKpiStats {

        @Test
        @DisplayName("성공 - 전체 통계 조회")
        void getKpiStats_success() {
            // given
            // 사용자 통계 Mock
            given(userRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ACTIVE", 100L),
                            createStatusCountProjection("INACTIVE", 20L),
                            createStatusCountProjection("SUSPENDED", 5L),
                            createStatusCountProjection("WITHDRAWN", 10L)
                    ));
            given(userRepository.countByTenantId(TENANT_ID))
                    .willReturn(135L);
            given(userRepository.countNewUsersSince(eq(TENANT_ID), any(Instant.class)))
                    .willReturn(15L);

            // 강의 통계 Mock (Program → Course)
            given(courseRepository.countByTenantId(TENANT_ID))
                    .willReturn(21L);

            // 수강 통계 Mock
            given(enrollmentRepository.countByTenantId(TENANT_ID))
                    .willReturn(500L);
            given(enrollmentRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ENROLLED", 200L),
                            createStatusCountProjection("COMPLETED", 250L),
                            createStatusCountProjection("DROPPED", 30L),
                            createStatusCountProjection("FAILED", 20L)
                    ));
            given(enrollmentRepository.getCompletionRateByTenantId(TENANT_ID))
                    .willReturn(50.0);

            // 일별 추이 Mock
            given(enrollmentRepository.countDailyEnrollmentStats(eq(TENANT_ID), any(Instant.class), any(Instant.class)))
                    .willReturn(List.of(
                            createDailyEnrollmentStatsProjection(LocalDate.now().minusDays(1), 45L, 30L),
                            createDailyEnrollmentStatsProjection(LocalDate.now(), 40L, 35L)
                    ));

            // when
            AdminKpiResponse response = adminDashboardService.getKpiStats(null);

            // then
            assertThat(response).isNotNull();

            // UserStats 검증
            assertThat(response.userStats().active()).isEqualTo(100L);
            assertThat(response.userStats().inactive()).isEqualTo(20L);
            assertThat(response.userStats().suspended()).isEqualTo(5L);
            assertThat(response.userStats().withdrawn()).isEqualTo(10L);
            assertThat(response.userStats().total()).isEqualTo(135L);
            assertThat(response.userStats().newInPeriod()).isEqualTo(15L);

            // CourseStats 검증 (Program → Course)
            assertThat(response.courseStats().total()).isEqualTo(21L);

            // EnrollmentStats 검증
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(500L);
            assertThat(response.enrollmentStats().byStatus().enrolled()).isEqualTo(200L);
            assertThat(response.enrollmentStats().byStatus().completed()).isEqualTo(250L);
            assertThat(response.enrollmentStats().byStatus().dropped()).isEqualTo(30L);
            assertThat(response.enrollmentStats().byStatus().failed()).isEqualTo(20L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(new BigDecimal("50.0"));

            // DailyTrend 검증
            assertThat(response.dailyTrend()).hasSize(2);
            assertThat(response.dailyTrend().get(0).enrollments()).isEqualTo(45L);
            assertThat(response.dailyTrend().get(0).completions()).isEqualTo(30L);
        }

        @Test
        @DisplayName("성공 - 데이터 없는 경우")
        void getKpiStats_success_noData() {
            // given
            given(userRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(userRepository.countByTenantId(TENANT_ID))
                    .willReturn(0L);
            given(userRepository.countNewUsersSince(eq(TENANT_ID), any(Instant.class)))
                    .willReturn(0L);
            given(courseRepository.countByTenantId(TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByTenantId(TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(enrollmentRepository.getCompletionRateByTenantId(TENANT_ID))
                    .willReturn(null);
            given(enrollmentRepository.countDailyEnrollmentStats(eq(TENANT_ID), any(Instant.class), any(Instant.class)))
                    .willReturn(Collections.emptyList());

            // when
            AdminKpiResponse response = adminDashboardService.getKpiStats(null);

            // then
            assertThat(response).isNotNull();

            // UserStats 검증 - 모두 0
            assertThat(response.userStats().active()).isEqualTo(0L);
            assertThat(response.userStats().inactive()).isEqualTo(0L);
            assertThat(response.userStats().suspended()).isEqualTo(0L);
            assertThat(response.userStats().withdrawn()).isEqualTo(0L);
            assertThat(response.userStats().total()).isEqualTo(0L);
            assertThat(response.userStats().newInPeriod()).isEqualTo(0L);

            // CourseStats 검증 - 0
            assertThat(response.courseStats().total()).isEqualTo(0L);

            // EnrollmentStats 검증 - 모두 0
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().enrolled()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().completed()).isEqualTo(0L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(BigDecimal.ZERO);

            // DailyTrend 검증 - 빈 리스트
            assertThat(response.dailyTrend()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 일부 상태만 존재하는 경우")
        void getKpiStats_success_partialData() {
            // given
            given(userRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ACTIVE", 50L)
                    ));
            given(userRepository.countByTenantId(TENANT_ID))
                    .willReturn(50L);
            given(userRepository.countNewUsersSince(eq(TENANT_ID), any(Instant.class)))
                    .willReturn(5L);
            given(courseRepository.countByTenantId(TENANT_ID))
                    .willReturn(10L);
            given(enrollmentRepository.countByTenantId(TENANT_ID))
                    .willReturn(100L);
            given(enrollmentRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ENROLLED", 80L),
                            createStatusCountProjection("COMPLETED", 20L)
                    ));
            given(enrollmentRepository.getCompletionRateByTenantId(TENANT_ID))
                    .willReturn(20.0);
            given(enrollmentRepository.countDailyEnrollmentStats(eq(TENANT_ID), any(Instant.class), any(Instant.class)))
                    .willReturn(List.of(
                            createDailyEnrollmentStatsProjection(LocalDate.now(), 100L, 20L)
                    ));

            // when
            AdminKpiResponse response = adminDashboardService.getKpiStats(null);

            // then
            assertThat(response).isNotNull();

            // UserStats - 없는 상태는 0
            assertThat(response.userStats().active()).isEqualTo(50L);
            assertThat(response.userStats().inactive()).isEqualTo(0L);
            assertThat(response.userStats().suspended()).isEqualTo(0L);
            assertThat(response.userStats().withdrawn()).isEqualTo(0L);

            // CourseStats
            assertThat(response.courseStats().total()).isEqualTo(10L);

            // EnrollmentStats - 없는 상태는 0
            assertThat(response.enrollmentStats().byStatus().dropped()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().failed()).isEqualTo(0L);
        }
    }

    private StatusCountProjection createStatusCountProjection(String status, Long count) {
        return new StatusCountProjection() {
            @Override
            public String getStatus() {
                return status;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }

    private DailyEnrollmentStatsProjection createDailyEnrollmentStatsProjection(
            LocalDate date, Long enrollments, Long completions) {
        return new DailyEnrollmentStatsProjection() {
            @Override
            public LocalDate getDate() {
                return date;
            }

            @Override
            public Long getEnrollments() {
                return enrollments;
            }

            @Override
            public Long getCompletions() {
                return completions;
            }
        };
    }
}
