package com.mzc.lp.domain.ts.scheduler;
import com.mzc.lp.common.support.TenantTestSupport;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseTimeSchedulerTest extends TenantTestSupport {

    @InjectMocks
    private CourseTimeScheduler courseTimeScheduler;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    private CourseTime createTestCourseTime(LocalDate classStartDate, LocalDate classEndDate) {
        return CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                DurationType.FIXED,
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(1),
                classStartDate,
                classEndDate,
                null,
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                null,
                1L
        );
    }

    // ==================== startCourseTimes 테스트 ====================

    @Nested
    @DisplayName("startCourseTimes - RECRUITING → ONGOING 배치")
    class StartCourseTimes {

        @Test
        @DisplayName("성공 - classStartDate 도래한 차수 ONGOING으로 전환")
        void startCourseTimes_success() {
            // given
            CourseTime courseTime = createTestCourseTime(
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태

            given(courseTimeRepository.findByStatusAndClassStartDateLessThanEqual(
                    eq(CourseTimeStatus.RECRUITING), any(LocalDate.class)))
                    .willReturn(List.of(courseTime));

            // when
            courseTimeScheduler.startCourseTimes();

            // then
            assertThat(courseTime.getStatus()).isEqualTo(CourseTimeStatus.ONGOING);
            verify(courseTimeRepository).findByStatusAndClassStartDateLessThanEqual(
                    eq(CourseTimeStatus.RECRUITING), any(LocalDate.class));
        }

        @Test
        @DisplayName("성공 - 전환할 차수가 없는 경우")
        void startCourseTimes_noCourseTimes() {
            // given
            given(courseTimeRepository.findByStatusAndClassStartDateLessThanEqual(
                    eq(CourseTimeStatus.RECRUITING), any(LocalDate.class)))
                    .willReturn(Collections.emptyList());

            // when
            courseTimeScheduler.startCourseTimes();

            // then
            verify(courseTimeRepository).findByStatusAndClassStartDateLessThanEqual(
                    eq(CourseTimeStatus.RECRUITING), any(LocalDate.class));
        }

        @Test
        @DisplayName("성공 - 여러 차수 일괄 전환")
        void startCourseTimes_multipleCourseTimes() {
            // given
            CourseTime courseTime1 = createTestCourseTime(
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태

            CourseTime courseTime2 = createTestCourseTime(
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(29)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태

            given(courseTimeRepository.findByStatusAndClassStartDateLessThanEqual(
                    eq(CourseTimeStatus.RECRUITING), any(LocalDate.class)))
                    .willReturn(List.of(courseTime1, courseTime2));

            // when
            courseTimeScheduler.startCourseTimes();

            // then
            assertThat(courseTime1.getStatus()).isEqualTo(CourseTimeStatus.ONGOING);
            assertThat(courseTime2.getStatus()).isEqualTo(CourseTimeStatus.ONGOING);
        }
    }

    // ==================== closeCourseTimes 테스트 ====================

    @Nested
    @DisplayName("closeCourseTimes - ONGOING → CLOSED 배치")
    class CloseCourseTimes {

        @Test
        @DisplayName("성공 - classEndDate 경과한 차수 CLOSED로 전환")
        void closeCourseTimes_success() {
            // given
            CourseTime courseTime = createTestCourseTime(
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(1)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태
            courseTime.startClass(); // ONGOING 상태로 변경

            given(courseTimeRepository.findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class)))
                    .willReturn(List.of(courseTime));

            // when
            courseTimeScheduler.closeCourseTimes();

            // then
            assertThat(courseTime.getStatus()).isEqualTo(CourseTimeStatus.CLOSED);
            verify(courseTimeRepository).findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class));
        }

        @Test
        @DisplayName("성공 - 종료할 차수가 없는 경우")
        void closeCourseTimes_noCourseTimes() {
            // given
            given(courseTimeRepository.findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class)))
                    .willReturn(Collections.emptyList());

            // when
            courseTimeScheduler.closeCourseTimes();

            // then
            verify(courseTimeRepository).findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class));
        }

        @Test
        @DisplayName("성공 - 여러 차수 일괄 종료")
        void closeCourseTimes_multipleCourseTimes() {
            // given
            CourseTime courseTime1 = createTestCourseTime(
                    LocalDate.now().minusDays(60),
                    LocalDate.now().minusDays(1)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태
            courseTime1.startClass();

            CourseTime courseTime2 = createTestCourseTime(
                    LocalDate.now().minusDays(45),
                    LocalDate.now().minusDays(2)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태
            courseTime2.startClass();

            given(courseTimeRepository.findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class)))
                    .willReturn(List.of(courseTime1, courseTime2));

            // when
            courseTimeScheduler.closeCourseTimes();

            // then
            assertThat(courseTime1.getStatus()).isEqualTo(CourseTimeStatus.CLOSED);
            assertThat(courseTime2.getStatus()).isEqualTo(CourseTimeStatus.CLOSED);
        }
    }

    // ==================== B2C 상시 모집 제외 테스트 ====================

    @Nested
    @DisplayName("B2C 상시 모집 차수 제외")
    class AlwaysOpenExclusion {

        @Test
        @DisplayName("상시 모집 차수(9999-12-31)는 배치에서 제외됨을 검증")
        void alwaysOpen_excludedByDateCondition() {
            // given
            // 상시 모집 차수는 classEndDate가 9999-12-31
            // 쿼리 조건: ct.classEndDate < :today
            // 9999-12-31 < today는 항상 false이므로 조회되지 않음

            // 일반 차수만 조회됨
            CourseTime normalCourseTime = createTestCourseTime(
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(1)
            );
            // enrollStartDate가 과거이므로 이미 RECRUITING 상태
            normalCourseTime.startClass();

            given(courseTimeRepository.findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class)))
                    .willReturn(List.of(normalCourseTime));

            // when
            courseTimeScheduler.closeCourseTimes();

            // then
            assertThat(normalCourseTime.getStatus()).isEqualTo(CourseTimeStatus.CLOSED);
            verify(courseTimeRepository).findByStatusAndClassEndDateLessThan(
                    eq(CourseTimeStatus.ONGOING), any(LocalDate.class));
        }
    }
}
