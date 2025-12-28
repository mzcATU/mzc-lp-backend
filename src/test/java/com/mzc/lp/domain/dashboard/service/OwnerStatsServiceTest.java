package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.dto.stats.ProgramStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.dashboard.dto.response.OwnerStatsResponse;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OwnerStatsServiceTest extends TenantTestSupport {

    @InjectMocks
    private OwnerStatsServiceImpl ownerStatsService;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;

    @Nested
    @DisplayName("getMyStats - OWNER 내 강의 통계 조회")
    class GetMyStats {

        @Test
        @DisplayName("성공 - 전체 통계 조회")
        void getMyStats_success() {
            // given
            List<Long> programIds = List.of(1L, 2L);
            List<Long> courseTimeIds = List.of(10L, 11L, 12L);

            given(programRepository.findIdsByCreatedByAndTenantId(USER_ID, TENANT_ID))
                    .willReturn(programIds);
            given(courseTimeRepository.findIdsByProgramIdInAndTenantId(programIds, TENANT_ID))
                    .willReturn(courseTimeIds);
            given(enrollmentRepository.countByCourseTimeIdInAndTenantId(courseTimeIds, TENANT_ID))
                    .willReturn(50L);
            given(enrollmentRepository.countByCourseTimeIdInGroupByStatus(courseTimeIds, TENANT_ID))
                    .willReturn(List.of(
                            createStatusCountProjection("ENROLLED", 30L),
                            createStatusCountProjection("COMPLETED", 15L),
                            createStatusCountProjection("DROPPED", 3L),
                            createStatusCountProjection("FAILED", 2L)
                    ));
            given(enrollmentRepository.getCompletionRateByCourseTimeIds(courseTimeIds, TENANT_ID))
                    .willReturn(30.0);
            given(programRepository.findProgramStatsByOwner(USER_ID, TENANT_ID))
                    .willReturn(List.of(
                            createProgramStatsProjection(1L, "프로그램 A", 2L, 30L, 25.0),
                            createProgramStatsProjection(2L, "프로그램 B", 1L, 20L, 40.0)
                    ));

            // when
            OwnerStatsResponse response = ownerStatsService.getMyStats(USER_ID);

            // then
            assertThat(response).isNotNull();

            // Overview 검증
            assertThat(response.overview().totalPrograms()).isEqualTo(2L);
            assertThat(response.overview().totalCourseTimes()).isEqualTo(3L);
            assertThat(response.overview().totalStudents()).isEqualTo(50L);

            // EnrollmentStats 검증
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(50L);
            assertThat(response.enrollmentStats().byStatus().enrolled()).isEqualTo(30L);
            assertThat(response.enrollmentStats().byStatus().completed()).isEqualTo(15L);
            assertThat(response.enrollmentStats().byStatus().dropped()).isEqualTo(3L);
            assertThat(response.enrollmentStats().byStatus().failed()).isEqualTo(2L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(new BigDecimal("30.0"));

            // ProgramStats 검증
            assertThat(response.programStats()).hasSize(2);
            assertThat(response.programStats().get(0).programId()).isEqualTo(1L);
            assertThat(response.programStats().get(0).title()).isEqualTo("프로그램 A");
            assertThat(response.programStats().get(0).courseTimeCount()).isEqualTo(2L);
            assertThat(response.programStats().get(0).totalStudents()).isEqualTo(30L);
            assertThat(response.programStats().get(0).completionRate()).isEqualTo(new BigDecimal("25.0"));
        }

        @Test
        @DisplayName("성공 - 소유 프로그램 없는 경우")
        void getMyStats_success_noPrograms() {
            // given
            given(programRepository.findIdsByCreatedByAndTenantId(USER_ID, TENANT_ID))
                    .willReturn(Collections.emptyList());

            // when
            OwnerStatsResponse response = ownerStatsService.getMyStats(USER_ID);

            // then
            assertThat(response).isNotNull();

            // Overview 검증 - 모두 0
            assertThat(response.overview().totalPrograms()).isEqualTo(0L);
            assertThat(response.overview().totalCourseTimes()).isEqualTo(0L);
            assertThat(response.overview().totalStudents()).isEqualTo(0L);

            // EnrollmentStats 검증 - 모두 0
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().enrolled()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().completed()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().dropped()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().failed()).isEqualTo(0L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(BigDecimal.ZERO);

            // ProgramStats 검증 - 빈 리스트
            assertThat(response.programStats()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 프로그램은 있지만 차수 없는 경우")
        void getMyStats_success_noCourseTime() {
            // given
            List<Long> programIds = List.of(1L);

            given(programRepository.findIdsByCreatedByAndTenantId(USER_ID, TENANT_ID))
                    .willReturn(programIds);
            given(courseTimeRepository.findIdsByProgramIdInAndTenantId(programIds, TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(programRepository.findProgramStatsByOwner(USER_ID, TENANT_ID))
                    .willReturn(List.of(
                            createProgramStatsProjection(1L, "프로그램 A", 0L, 0L, null)
                    ));

            // when
            OwnerStatsResponse response = ownerStatsService.getMyStats(USER_ID);

            // then
            assertThat(response).isNotNull();

            // Overview 검증
            assertThat(response.overview().totalPrograms()).isEqualTo(1L);
            assertThat(response.overview().totalCourseTimes()).isEqualTo(0L);
            assertThat(response.overview().totalStudents()).isEqualTo(0L);

            // EnrollmentStats 검증 - 차수가 없으므로 수강도 없음
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(0L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(BigDecimal.ZERO);

            // ProgramStats 검증
            assertThat(response.programStats()).hasSize(1);
            assertThat(response.programStats().get(0).courseTimeCount()).isEqualTo(0L);
            assertThat(response.programStats().get(0).totalStudents()).isEqualTo(0L);
            assertThat(response.programStats().get(0).completionRate()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("성공 - 차수는 있지만 수강생 없는 경우")
        void getMyStats_success_noStudents() {
            // given
            List<Long> programIds = List.of(1L);
            List<Long> courseTimeIds = List.of(10L);

            given(programRepository.findIdsByCreatedByAndTenantId(USER_ID, TENANT_ID))
                    .willReturn(programIds);
            given(courseTimeRepository.findIdsByProgramIdInAndTenantId(programIds, TENANT_ID))
                    .willReturn(courseTimeIds);
            given(enrollmentRepository.countByCourseTimeIdInAndTenantId(courseTimeIds, TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByCourseTimeIdInGroupByStatus(courseTimeIds, TENANT_ID))
                    .willReturn(Collections.emptyList());
            given(enrollmentRepository.getCompletionRateByCourseTimeIds(courseTimeIds, TENANT_ID))
                    .willReturn(null);
            given(programRepository.findProgramStatsByOwner(USER_ID, TENANT_ID))
                    .willReturn(List.of(
                            createProgramStatsProjection(1L, "프로그램 A", 1L, 0L, null)
                    ));

            // when
            OwnerStatsResponse response = ownerStatsService.getMyStats(USER_ID);

            // then
            assertThat(response).isNotNull();

            // Overview 검증
            assertThat(response.overview().totalPrograms()).isEqualTo(1L);
            assertThat(response.overview().totalCourseTimes()).isEqualTo(1L);
            assertThat(response.overview().totalStudents()).isEqualTo(0L);

            // EnrollmentStats 검증 - 수강생 없음
            assertThat(response.enrollmentStats().totalEnrollments()).isEqualTo(0L);
            assertThat(response.enrollmentStats().byStatus().enrolled()).isEqualTo(0L);
            assertThat(response.enrollmentStats().completionRate()).isEqualTo(BigDecimal.ZERO);
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

    private ProgramStatsProjection createProgramStatsProjection(
            Long programId, String title, Long courseTimeCount, Long totalStudents, Double completionRate) {
        return new ProgramStatsProjection() {
            @Override
            public Long getProgramId() {
                return programId;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public Long getCourseTimeCount() {
                return courseTimeCount;
            }

            @Override
            public Long getTotalStudents() {
                return totalStudents;
            }

            @Override
            public Double getCompletionRate() {
                return completionRate;
            }
        };
    }
}
