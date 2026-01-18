package com.mzc.lp.domain.ts.validator;

import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.constant.QualityRating;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.CourseTimeValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        LocalDate now = LocalDate.now();
        return new CreateCourseTimeRequest(
                1L,
                "테스트 차수",
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
                deliveryType == DeliveryType.OFFLINE ? "{\"address\": \"서울\"}" : null,
                false
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
                    1L, "테스트", DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false
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
                    1L, "테스트", DeliveryType.ONLINE, DurationType.RELATIVE,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false
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
                    1L, "테스트", DeliveryType.ONLINE, DurationType.UNLIMITED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false
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
                    1L, "테스트", DeliveryType.OFFLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false
            );
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R10"));
        }

        @Test
        @DisplayName("R14 - LIVE에서 FIXED 이외의 DurationType 사용 시 오류")
        void R14_liveType_requiresFixedDuration() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", DeliveryType.LIVE, DurationType.RELATIVE,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), null, 30,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.ruleCode().equals("R14"));
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
                    1L, "테스트", DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.APPROVAL, 80,
                    BigDecimal.valueOf(10000), false, null, false
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
                    1L, "테스트", DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 0, EnrollmentMethod.APPROVAL, 80,
                    BigDecimal.valueOf(10000), false, null, false
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
                    1L, "테스트", DeliveryType.OFFLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 5, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false
            );
            Course course = createMockCourse(CourseType.OFFLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.BEST);
        }

        @Test
        @DisplayName("ONLINE + FIXED = COMMON (비권장)")
        void onlineFixed_common() {
            // given
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L, "테스트", DeliveryType.ONLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 0, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, null, false
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.qualityRating()).isEqualTo(QualityRating.COMMON);
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
                    1L, "테스트", DeliveryType.OFFLINE, DurationType.FIXED,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(40), null,
                    30, 0, EnrollmentMethod.FIRST_COME, 80,
                    BigDecimal.valueOf(10000), false, "{\"address\": \"서울\"}", false
            );
            Course course = createMockCourse(CourseType.ONLINE);

            // when
            CourseTimeValidationResult result = validator.validate(request, course);

            // then
            assertThat(result.valid()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.ruleCode().equals("R70"));
        }
    }
}
