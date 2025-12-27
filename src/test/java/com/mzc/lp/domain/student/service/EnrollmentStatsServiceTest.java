package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.dto.response.MyLearningStatsResponse;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
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
class EnrollmentStatsServiceTest extends TenantTestSupport {

    @InjectMocks
    private EnrollmentStatsServiceImpl enrollmentStatsService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private UserRepository userRepository;

    private static final Long TENANT_ID = 1L;

    @Nested
    @DisplayName("getMyLearningStats - 내 학습 통계 조회")
    class GetMyLearningStats {

        @Test
        @DisplayName("성공 - 학습 통계 조회")
        void getMyLearningStats_success() {
            // given
            Long userId = 1L;

            given(enrollmentRepository.countByUserIdAndTenantId(userId, TENANT_ID))
                    .willReturn(10L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.COMPLETED, TENANT_ID))
                    .willReturn(6L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.ENROLLED, TENANT_ID))
                    .willReturn(3L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.DROPPED, TENANT_ID))
                    .willReturn(1L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.FAILED, TENANT_ID))
                    .willReturn(0L);

            List<TypeCountProjection> typeCounts = List.of(
                    createTypeCountProjection("VOLUNTARY", 8L),
                    createTypeCountProjection("MANDATORY", 2L)
            );
            given(enrollmentRepository.countByUserIdGroupByType(userId, TENANT_ID))
                    .willReturn(typeCounts);

            given(enrollmentRepository.findAverageProgressByUserId(userId, TENANT_ID))
                    .willReturn(75.5);
            given(enrollmentRepository.findAverageScoreByUserId(userId, TENANT_ID))
                    .willReturn(85.2);

            // when
            MyLearningStatsResponse response = enrollmentStatsService.getMyLearningStats(userId);

            // then
            assertThat(response).isNotNull();

            // Overview 검증
            assertThat(response.overview().totalCourses()).isEqualTo(10L);
            assertThat(response.overview().inProgress()).isEqualTo(3L);
            assertThat(response.overview().completed()).isEqualTo(6L);
            assertThat(response.overview().dropped()).isEqualTo(1L);
            assertThat(response.overview().failed()).isEqualTo(0L);
            assertThat(response.overview().completionRate()).isEqualTo(new BigDecimal("60.0"));

            // ByType 검증
            assertThat(response.overview().byType().voluntary()).isEqualTo(8L);
            assertThat(response.overview().byType().mandatory()).isEqualTo(2L);

            // Progress 검증
            assertThat(response.progress().averageProgress()).isEqualTo(new BigDecimal("75.5"));
            assertThat(response.progress().averageScore()).isEqualTo(new BigDecimal("85.2"));
        }

        @Test
        @DisplayName("성공 - 수강 이력 없는 경우")
        void getMyLearningStats_success_noEnrollments() {
            // given
            Long userId = 1L;

            given(enrollmentRepository.countByUserIdAndTenantId(userId, TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.COMPLETED, TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.ENROLLED, TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.DROPPED, TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.FAILED, TENANT_ID))
                    .willReturn(0L);

            given(enrollmentRepository.countByUserIdGroupByType(userId, TENANT_ID))
                    .willReturn(Collections.emptyList());

            given(enrollmentRepository.findAverageProgressByUserId(userId, TENANT_ID))
                    .willReturn(null);
            given(enrollmentRepository.findAverageScoreByUserId(userId, TENANT_ID))
                    .willReturn(null);

            // when
            MyLearningStatsResponse response = enrollmentStatsService.getMyLearningStats(userId);

            // then
            assertThat(response).isNotNull();

            // Overview 검증
            assertThat(response.overview().totalCourses()).isEqualTo(0L);
            assertThat(response.overview().inProgress()).isEqualTo(0L);
            assertThat(response.overview().completed()).isEqualTo(0L);
            assertThat(response.overview().dropped()).isEqualTo(0L);
            assertThat(response.overview().failed()).isEqualTo(0L);
            assertThat(response.overview().completionRate()).isEqualTo(BigDecimal.ZERO);

            // ByType 검증
            assertThat(response.overview().byType().voluntary()).isEqualTo(0L);
            assertThat(response.overview().byType().mandatory()).isEqualTo(0L);

            // Progress 검증
            assertThat(response.progress().averageProgress()).isEqualTo(BigDecimal.ZERO);
            assertThat(response.progress().averageScore()).isNull();
        }

        @Test
        @DisplayName("성공 - 자발적 수강만 있는 경우")
        void getMyLearningStats_success_onlyVoluntary() {
            // given
            Long userId = 1L;

            given(enrollmentRepository.countByUserIdAndTenantId(userId, TENANT_ID))
                    .willReturn(5L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.COMPLETED, TENANT_ID))
                    .willReturn(3L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.ENROLLED, TENANT_ID))
                    .willReturn(2L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.DROPPED, TENANT_ID))
                    .willReturn(0L);
            given(enrollmentRepository.countByUserIdAndStatusAndTenantId(userId, EnrollmentStatus.FAILED, TENANT_ID))
                    .willReturn(0L);

            List<TypeCountProjection> typeCounts = List.of(
                    createTypeCountProjection("VOLUNTARY", 5L)
            );
            given(enrollmentRepository.countByUserIdGroupByType(userId, TENANT_ID))
                    .willReturn(typeCounts);

            given(enrollmentRepository.findAverageProgressByUserId(userId, TENANT_ID))
                    .willReturn(60.0);
            given(enrollmentRepository.findAverageScoreByUserId(userId, TENANT_ID))
                    .willReturn(90.0);

            // when
            MyLearningStatsResponse response = enrollmentStatsService.getMyLearningStats(userId);

            // then
            assertThat(response.overview().byType().voluntary()).isEqualTo(5L);
            assertThat(response.overview().byType().mandatory()).isEqualTo(0L);
        }
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
}
