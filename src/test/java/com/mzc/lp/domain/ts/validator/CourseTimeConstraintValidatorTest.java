package com.mzc.lp.domain.ts.validator;

import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.constant.QualityRating;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.RecurringScheduleRequest;
import com.mzc.lp.domain.ts.dto.response.CourseTimeValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CourseTimeConstraintValidator 테스트")
class CourseTimeConstraintValidatorTest {

    private CourseTimeConstraintValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CourseTimeConstraintValidator();
    }

    private CreateCourseTimeRequest createBaseRequest(
            DeliveryType deliveryType,
            DurationType durationType,
            EnrollmentMethod enrollmentMethod
    ) {
        return createBaseRequest(deliveryType, durationType, enrollmentMethod, null);
    }

    private CreateCourseTimeRequest createBaseRequest(
            DeliveryType deliveryType,
            DurationType durationType,
            EnrollmentMethod enrollmentMethod,
            RecurringScheduleRequest recurringSchedule
    ) {
        LocalDate now = LocalDate.now();
        return new CreateCourseTimeRequest(
                1L,
                "테스트 차수",
                null,  // description
                deliveryType,
                durationType,
                now.plusDays(1),
                now.plusDays(10),
                now.plusDays(11),
                durationType == DurationType.FIXED ? now.plusDays(40) : null,
                durationType == DurationType.RELATIVE ? 30 : null,
                30,
                5,
                enrollmentMethod,
                80,
                new BigDecimal("100000"),
                false,
                deliveryType == DeliveryType.OFFLINE || deliveryType == DeliveryType.BLENDED
                        ? "{\"address\": \"서울\"}" : null,
                false,
                recurringSchedule
        );
    }

    private Course createMockCourse(CourseType type) {
        Course course = mock(Course.class);
        when(course.getType()).thenReturn(type);
        return course;
    }

    @Nested
    @DisplayName("일관성 제약 검증 (R61-R65)")
    class ConsistencyConstraints {

        @Test
        @DisplayName("R61 - FIXED 타입에서 classEndDate 누락 시 오류")
        void R61_fixedType_requiresClassEndDate() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R61"));
        }

        @Test
        @DisplayName("R62 - RELATIVE 타입에서 durationDays 누락 시 오류")
        void R62_relativeType_requiresDurationDays() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.RELATIVE,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R62"));
        }

        @Test
        @DisplayName("R63 - UNLIMITED 타입에서 classEndDate 존재 시 오류")
        void R63_unlimitedType_noClassEndDate() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.UNLIMITED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R63"));
        }
    }

    @Nested
    @DisplayName("DeliveryType 제약 검증 (R10-R15)")
    class DeliveryTypeConstraints {

        @Test
        @DisplayName("R10 - OFFLINE에서 locationInfo 누락 시 오류")
        void R10_offlineType_requiresLocationInfo() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.OFFLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R10"));
        }

        @Test
        @DisplayName("LIVE + RELATIVE 조합 허용 (B2B 유연성)")
        void liveType_allowsRelativeDuration() {
            // given - LIVE + RELATIVE: 반복되는 라이브 세션이 있는 장기 교육 프로그램
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "정기 멘토링", null, DeliveryType.LIVE, DurationType.RELATIVE,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, 30,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then - R14 제거로 LIVE + RELATIVE 허용
            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("LIVE + UNLIMITED 조합 허용 (B2B 유연성)")
        void liveType_allowsUnlimitedDuration() {
            // given - LIVE + UNLIMITED: 정기적인 Q&A 세션, 멘토링 프로그램
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "주간 Q&A", null, DeliveryType.LIVE, DurationType.UNLIMITED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then - R14 제거로 LIVE + UNLIMITED 허용
            // QualityRating은 COMMON (LIVE에는 대응하는 CourseType이 없어 R70 경고 발생)
            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
            assertThat(result.warnings()).anyMatch(w -> w.ruleCode().equals("R70"));
            assertThat(result.qualityRating()).isEqualTo(QualityRating.COMMON);
        }
    }

    @Nested
    @DisplayName("EnrollmentMethod 제약 검증 (R50-R53)")
    class EnrollmentMethodConstraints {

        @Test
        @DisplayName("R53 - APPROVAL + 대기자 > 0 조합 시 오류")
        void R53_approvalWithWaitingList_error() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.APPROVAL, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R53"));
        }

        @Test
        @DisplayName("APPROVAL + 대기자 0 조합은 허용")
        void approval_withoutWaitingList_allowed() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 0, EnrollmentMethod.APPROVAL, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("QualityRating 평가")
    class QualityRatingEvaluation {

        @Test
        @DisplayName("ONLINE + RELATIVE = BEST")
        void onlineRelative_best() {
            // given
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.ONLINE, DurationType.RELATIVE, EnrollmentMethod.FIRST_COME);
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.BEST);
        }

        @Test
        @DisplayName("OFFLINE + FIXED = BEST (대기자 설정 포함)")
        void offlineFixed_best() {
            // given - FIRST_COME + 정원 있으면 대기자 설정 권장 (R52)되므로 대기자 설정
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.OFFLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false, null
            );
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.BEST);
        }

        @Test
        @DisplayName("ONLINE + FIXED = BEST (B2C 단체 수업)")
        void onlineFixed_best() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.BEST);
        }

        @Test
        @DisplayName("OFFLINE + RELATIVE = BEST (B2B 신입사원 OJT)")
        void offlineRelative_best() {
            // given - B2B 기업 교육: 입사일로부터 90일 OJT
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "신입사원 OJT", null, DeliveryType.OFFLINE, DurationType.RELATIVE,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, 90,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false, null
            );
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.BEST);
        }

        @Test
        @DisplayName("BLENDED + UNLIMITED = BEST (B2B 자율 학습)")
        void blendedUnlimited_best() {
            // given - B2B 기업 교육: 팀별 자율 진행 리더십 과정
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "리더십 과정", null, DeliveryType.BLENDED, DurationType.UNLIMITED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false, null
            );
            Course course = createMockCourse(CourseType.BLENDED);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.BEST);
        }

        @Test
        @DisplayName("ONLINE + UNLIMITED = GOOD (무제한은 약간 낮은 등급)")
        void onlineUnlimited_good() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.ONLINE, DurationType.UNLIMITED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.GOOD);
        }
    }

    @Nested
    @DisplayName("Course 연동 제약 검증 (R70)")
    class CourseConstraints {

        @Test
        @DisplayName("R70 - Course.type과 DeliveryType 불일치 시 경고")
        void R70_courseTypeMismatch_warning() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", null, DeliveryType.OFFLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 0, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false, null
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.ruleCode().equals("R70"));
        }
    }

    @Nested
    @DisplayName("정기 일정 제약 검증 (R80-R83)")
    class RecurringScheduleConstraints {

        @Test
        @DisplayName("R80 - 요일 범위 벗어남 (7 이상) 시 오류")
        void R80_invalidDayOfWeek_error() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(1, 3, 7), // 7은 유효하지 않음 (0-6만 허용)
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0)
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R80"));
        }

        @Test
        @DisplayName("R80 - 음수 요일 값 시 오류")
        void R80_negativeDayOfWeek_error() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(-1, 1, 3),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0)
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R80"));
        }

        @Test
        @DisplayName("R81 - 종료 시간이 시작 시간보다 이전일 때 오류")
        void R81_endTimeBeforeStartTime_error() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(1, 3, 5),
                    LocalTime.of(21, 0), // 시작 시간
                    LocalTime.of(19, 0)  // 종료 시간 (시작보다 이전)
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R81"));
        }

        @Test
        @DisplayName("R81 - 시작 시간과 종료 시간이 같을 때 오류")
        void R81_sameStartEndTime_error() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(1, 3, 5),
                    LocalTime.of(19, 0),
                    LocalTime.of(19, 0) // 동일한 시간
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R81"));
        }

        @Test
        @DisplayName("R82 - RELATIVE 타입에서 정기 일정 설정 시 오류")
        void R82_relativeWithSchedule_error() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(1, 3, 5),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0)
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.RELATIVE, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R82"));
        }

        @Test
        @DisplayName("R83 - ONLINE에서 정기 일정 설정 시 오류")
        void R83_onlineWithSchedule_error() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(1, 3, 5),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0)
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.ONLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R83"));
        }

        @Test
        @DisplayName("유효한 정기 일정 (OFFLINE + FIXED)")
        void validRecurringSchedule_offlineFixed() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(1, 3, 5), // 월, 수, 금
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0)
            );
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, schedule);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("유효한 정기 일정 (LIVE + FIXED)")
        void validRecurringSchedule_liveFixed() {
            // given
            RecurringScheduleRequest schedule = new RecurringScheduleRequest(
                    List.of(2, 4), // 화, 목
                    LocalTime.of(20, 0),
                    LocalTime.of(22, 0)
            );
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "실시간 강의", null, DeliveryType.LIVE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false, schedule
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("정기 일정 없이 생성 가능")
        void noRecurringSchedule_allowed() {
            // given
            CreateCourseTimeRequest request = createBaseRequest(
                    DeliveryType.OFFLINE, DurationType.FIXED, EnrollmentMethod.FIRST_COME, null);
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
        }
    }
}
