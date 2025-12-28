package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.dto.stats.BooleanCountProjection;
import com.mzc.lp.common.dto.stats.DailyCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.dashboard.dto.response.OperatorTasksResponse;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OperatorDashboardServiceTest extends TenantTestSupport {

    @InjectMocks
    private OperatorDashboardServiceImpl operatorDashboardService;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private static final Long TENANT_ID = 1L;

    @Nested
    @DisplayName("getOperatorTasks - 운영 대시보드 조회")
    class GetOperatorTasks {

        @Test
        @DisplayName("성공 - 전체 통계 조회")
        void getOperatorTasks_success() {
            // given
            given(programRepository.countPendingPrograms(TENANT_ID))
                    .willReturn(5L);
            given(courseTimeRepository.countCourseTimesNeedingInstructor(
                    eq(TENANT_ID), anyList(), any(InstructorRole.class), any(AssignmentStatus.class)))
                    .willReturn(3L);

            given(courseTimeRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("DRAFT", 5L),
                            createStatusCountProjection("RECRUITING", 10L),
                            createStatusCountProjection("ONGOING", 15L),
                            createStatusCountProjection("CLOSED", 20L),
                            createStatusCountProjection("ARCHIVED", 5L)
                    ));
            given(courseTimeRepository.countByTenantIdGroupByDeliveryType(TENANT_ID))
                    .willReturn(List.of(
                            createTypeCountProjection("ONLINE", 20L),
                            createTypeCountProjection("OFFLINE", 15L),
                            createTypeCountProjection("BLENDED", 10L),
                            createTypeCountProjection("LIVE", 10L)
                    ));
            given(courseTimeRepository.countByTenantIdGroupByFree(TENANT_ID))
                    .willReturn(List.of(
                            createBooleanCountProjection(true, 15L),
                            createBooleanCountProjection(false, 40L)
                    ));
            given(courseTimeRepository.countByTenantId(TENANT_ID))
                    .willReturn(55L);

            given(enrollmentRepository.countByTenantId(TENANT_ID))
                    .willReturn(1000L);
            given(enrollmentRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ENROLLED", 400L),
                            createStatusCountProjection("COMPLETED", 500L),
                            createStatusCountProjection("DROPPED", 50L),
                            createStatusCountProjection("FAILED", 50L)
                    ));
            given(enrollmentRepository.countByTenantIdGroupByType(TENANT_ID))
                    .willReturn(List.of(
                            createTypeCountProjection("VOLUNTARY", 800L),
                            createTypeCountProjection("MANDATORY", 200L)
                    ));
            given(enrollmentRepository.getCompletionRateByTenantId(TENANT_ID))
                    .willReturn(50.0);
            given(courseTimeRepository.getAverageCapacityUtilization(TENANT_ID))
                    .willReturn(75.5);
            given(enrollmentRepository.countDailyEnrollments(eq(TENANT_ID), any(Instant.class), any(Instant.class)))
                    .willReturn(List.of(
                            createDailyCountProjection(LocalDate.now().minusDays(1), 12L),
                            createDailyCountProjection(LocalDate.now(), 15L)
                    ));

            // when
            OperatorTasksResponse response = operatorDashboardService.getOperatorTasks();

            // then
            assertThat(response).isNotNull();

            // PendingTasks 검증
            assertThat(response.pendingTasks().programsPendingApproval()).isEqualTo(5L);
            assertThat(response.pendingTasks().courseTimesNeedingInstructor()).isEqualTo(3L);

            // CourseTimeStats 검증
            assertThat(response.courseTimeStats().total()).isEqualTo(55L);
            assertThat(response.courseTimeStats().byStatus().draft()).isEqualTo(5L);
            assertThat(response.courseTimeStats().byStatus().recruiting()).isEqualTo(10L);
            assertThat(response.courseTimeStats().byStatus().ongoing()).isEqualTo(15L);
            assertThat(response.courseTimeStats().byStatus().closed()).isEqualTo(20L);
            assertThat(response.courseTimeStats().byStatus().archived()).isEqualTo(5L);
            assertThat(response.courseTimeStats().byDeliveryType().online()).isEqualTo(20L);
            assertThat(response.courseTimeStats().byDeliveryType().offline()).isEqualTo(15L);
            assertThat(response.courseTimeStats().byDeliveryType().blended()).isEqualTo(10L);
            assertThat(response.courseTimeStats().byDeliveryType().live()).isEqualTo(10L);
            assertThat(response.courseTimeStats().freeVsPaid().free()).isEqualTo(15L);
            assertThat(response.courseTimeStats().freeVsPaid().paid()).isEqualTo(40L);

            // EnrollmentStats 검증
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(1000L);
            assertThat(response.enrollmentStats().byStatus().enrolled()).isEqualTo(400L);
            assertThat(response.enrollmentStats().byStatus().completed()).isEqualTo(500L);
            assertThat(response.enrollmentStats().byStatus().dropped()).isEqualTo(50L);
            assertThat(response.enrollmentStats().byStatus().failed()).isEqualTo(50L);
            assertThat(response.enrollmentStats().byType().voluntary()).isEqualTo(800L);
            assertThat(response.enrollmentStats().byType().mandatory()).isEqualTo(200L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(new BigDecimal("50.0"));
            assertThat(response.enrollmentStats().averageCapacityUtilization()).isEqualTo(new BigDecimal("75.5"));

            // DailyTrend 검증
            assertThat(response.dailyTrend()).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 데이터 없는 경우")
        void getOperatorTasks_success_noData() {
            // given
            given(programRepository.countPendingPrograms(TENANT_ID))
                    .willReturn(0L);
            given(courseTimeRepository.countCourseTimesNeedingInstructor(
                    eq(TENANT_ID), anyList(), any(InstructorRole.class), any(AssignmentStatus.class)))
                    .willReturn(0L);
            given(courseTimeRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(courseTimeRepository.countByTenantIdGroupByDeliveryType(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(courseTimeRepository.countByTenantIdGroupByFree(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(courseTimeRepository.countByTenantId(TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByTenantId(TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(enrollmentRepository.countByTenantIdGroupByType(TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(enrollmentRepository.getCompletionRateByTenantId(TENANT_ID))
                    .willReturn(null);
            given(courseTimeRepository.getAverageCapacityUtilization(TENANT_ID))
                    .willReturn(null);
            given(enrollmentRepository.countDailyEnrollments(eq(TENANT_ID), any(Instant.class), any(Instant.class)))
                    .willReturn(Collections.emptyList());

            // when
            OperatorTasksResponse response = operatorDashboardService.getOperatorTasks();

            // then
            assertThat(response).isNotNull();

            // PendingTasks 검증
            assertThat(response.pendingTasks().programsPendingApproval()).isEqualTo(0L);
            assertThat(response.pendingTasks().courseTimesNeedingInstructor()).isEqualTo(0L);

            // CourseTimeStats 검증
            assertThat(response.courseTimeStats().total()).isEqualTo(0L);
            assertThat(response.courseTimeStats().byStatus().draft()).isEqualTo(0L);
            assertThat(response.courseTimeStats().byDeliveryType().online()).isEqualTo(0L);
            assertThat(response.courseTimeStats().freeVsPaid().free()).isEqualTo(0L);

            // EnrollmentStats 검증
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(0L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(response.enrollmentStats().averageCapacityUtilization()).isEqualTo(BigDecimal.ZERO);

            // DailyTrend 검증
            assertThat(response.dailyTrend()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 일부 상태만 존재하는 경우")
        void getOperatorTasks_success_partialData() {
            // given
            given(programRepository.countPendingPrograms(TENANT_ID))
                    .willReturn(2L);
            given(courseTimeRepository.countCourseTimesNeedingInstructor(
                    eq(TENANT_ID), anyList(), any(InstructorRole.class), any(AssignmentStatus.class)))
                    .willReturn(1L);
            given(courseTimeRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("RECRUITING", 5L),
                            createStatusCountProjection("ONGOING", 3L)
                    ));
            given(courseTimeRepository.countByTenantIdGroupByDeliveryType(TENANT_ID))
                    .willReturn(List.of(
                            createTypeCountProjection("ONLINE", 8L)
                    ));
            given(courseTimeRepository.countByTenantIdGroupByFree(TENANT_ID))
                    .willReturn(List.of(
                            createBooleanCountProjection(true, 8L)
                    ));
            given(courseTimeRepository.countByTenantId(TENANT_ID))
                    .willReturn(8L);
            given(enrollmentRepository.countByTenantId(TENANT_ID))
                    .willReturn(100L);
            given(enrollmentRepository.countByTenantIdGroupByStatus(TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ENROLLED", 80L),
                            createStatusCountProjection("COMPLETED", 20L)
                    ));
            given(enrollmentRepository.countByTenantIdGroupByType(TENANT_ID))
                    .willReturn(List.of(
                            createTypeCountProjection("VOLUNTARY", 100L)
                    ));
            given(enrollmentRepository.getCompletionRateByTenantId(TENANT_ID))
                    .willReturn(20.0);
            given(courseTimeRepository.getAverageCapacityUtilization(TENANT_ID))
                    .willReturn(60.0);
            given(enrollmentRepository.countDailyEnrollments(eq(TENANT_ID), any(Instant.class), any(Instant.class)))
                    .willReturn(List.of(
                            createDailyCountProjection(LocalDate.now(), 5L)
                    ));

            // when
            OperatorTasksResponse response = operatorDashboardService.getOperatorTasks();

            // then
            assertThat(response).isNotNull();

            // CourseTimeStats - 없는 상태는 0
            assertThat(response.courseTimeStats().byStatus().draft()).isEqualTo(0L);
            assertThat(response.courseTimeStats().byStatus().recruiting()).isEqualTo(5L);
            assertThat(response.courseTimeStats().byStatus().ongoing()).isEqualTo(3L);
            assertThat(response.courseTimeStats().byStatus().closed()).isEqualTo(0L);
            assertThat(response.courseTimeStats().byStatus().archived()).isEqualTo(0L);

            // ByDeliveryType - 없는 타입은 0
            assertThat(response.courseTimeStats().byDeliveryType().online()).isEqualTo(8L);
            assertThat(response.courseTimeStats().byDeliveryType().offline()).isEqualTo(0L);
            assertThat(response.courseTimeStats().byDeliveryType().blended()).isEqualTo(0L);
            assertThat(response.courseTimeStats().byDeliveryType().live()).isEqualTo(0L);

            // FreeVsPaid - 유료 없음
            assertThat(response.courseTimeStats().freeVsPaid().free()).isEqualTo(8L);
            assertThat(response.courseTimeStats().freeVsPaid().paid()).isEqualTo(0L);

            // EnrollmentStats - 없는 상태는 0
            assertThat(response.enrollmentStats().byStatus().dropped()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().failed()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byType().mandatory()).isEqualTo(0L);
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

    private TypeCountProjection createTypeCountProjection(String type, Long count) {
        return new TypeCountProjection() {
            @Override
            public String getType() {
                return type;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }

    private BooleanCountProjection createBooleanCountProjection(Boolean value, Long count) {
        return new BooleanCountProjection() {
            @Override
            public Boolean getValue() {
                return value;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }

    private DailyCountProjection createDailyCountProjection(LocalDate date, Long count) {
        return new DailyCountProjection() {
            @Override
            public LocalDate getDate() {
                return date;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }
}
