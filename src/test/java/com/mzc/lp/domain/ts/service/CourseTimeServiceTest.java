package com.mzc.lp.domain.ts.service;
import com.mzc.lp.common.support.TenantTestSupport;

import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.ts.dto.response.CapacityResponse;
import com.mzc.lp.domain.ts.dto.response.PriceResponse;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.exception.CapacityExceededException;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.exception.InvalidDateRangeException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseTimeServiceTest extends TenantTestSupport {

    @InjectMocks
    private CourseTimeServiceImpl courseTimeService;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private InstructorAssignmentService instructorAssignmentService;

    @Mock
    private UserCourseRoleRepository userCourseRoleRepository;

    private CourseTime createTestCourseTime(Integer capacity, int currentEnrollment) {
        CourseTime courseTime = CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                capacity,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                1L
        );
        // 현재 등록 인원 설정
        for (int i = 0; i < currentEnrollment; i++) {
            courseTime.incrementEnrollment();
        }
        return courseTime;
    }

    private Program createApprovedProgram() {
        Program program = Program.create("테스트 프로그램", 1L);
        ReflectionTestUtils.setField(program, "id", 1L);
        ReflectionTestUtils.setField(program, "status", ProgramStatus.APPROVED);
        return program;
    }

    // ==================== 좌석 점유 테스트 ====================

    @Nested
    @DisplayName("occupySeat - 좌석 점유")
    class OccupySeat {

        @Test
        @DisplayName("성공 - 좌석 점유")
        void occupySeat_success() {
            // given
            CourseTime courseTime = createTestCourseTime(30, 10);
            given(courseTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(courseTime));

            // when
            courseTimeService.occupySeat(1L);

            // then
            assertThat(courseTime.getCurrentEnrollment()).isEqualTo(11);
            verify(courseTimeRepository).findByIdWithLock(1L);
        }

        @Test
        @DisplayName("성공 - 무제한 정원에서 좌석 점유")
        void occupySeat_success_unlimited() {
            // given
            CourseTime courseTime = createTestCourseTime(null, 100);  // capacity = null
            given(courseTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(courseTime));

            // when
            courseTimeService.occupySeat(1L);

            // then
            assertThat(courseTime.getCurrentEnrollment()).isEqualTo(101);
        }

        @Test
        @DisplayName("실패 - 정원 초과")
        void occupySeat_fail_capacityExceeded() {
            // given
            CourseTime courseTime = createTestCourseTime(30, 30);  // 만석
            given(courseTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(courseTime));

            // when & then
            assertThatThrownBy(() -> courseTimeService.occupySeat(1L))
                    .isInstanceOf(CapacityExceededException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void occupySeat_fail_notFound() {
            // given
            given(courseTimeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> courseTimeService.occupySeat(999L))
                    .isInstanceOf(CourseTimeNotFoundException.class);
        }
    }

    // ==================== 좌석 해제 테스트 ====================

    @Nested
    @DisplayName("releaseSeat - 좌석 해제")
    class ReleaseSeat {

        @Test
        @DisplayName("성공 - 좌석 해제")
        void releaseSeat_success() {
            // given
            CourseTime courseTime = createTestCourseTime(30, 10);
            given(courseTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(courseTime));

            // when
            courseTimeService.releaseSeat(1L);

            // then
            assertThat(courseTime.getCurrentEnrollment()).isEqualTo(9);
            verify(courseTimeRepository).findByIdWithLock(1L);
        }

        @Test
        @DisplayName("성공 - 등록 인원 0일 때 해제 (0 이하로 내려가지 않음)")
        void releaseSeat_success_zeroEnrollment() {
            // given
            CourseTime courseTime = createTestCourseTime(30, 0);
            given(courseTimeRepository.findByIdWithLock(1L)).willReturn(Optional.of(courseTime));

            // when
            courseTimeService.releaseSeat(1L);

            // then
            assertThat(courseTime.getCurrentEnrollment()).isEqualTo(0);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void releaseSeat_fail_notFound() {
            // given
            given(courseTimeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> courseTimeService.releaseSeat(999L))
                    .isInstanceOf(CourseTimeNotFoundException.class);
        }
    }

    // ==================== Public API 테스트 ====================

    @Nested
    @DisplayName("getCapacity - 정원 조회")
    class GetCapacity {

        @Test
        @DisplayName("성공 - 정원 조회 (제한 있음)")
        void getCapacity_success_limited() {
            // given
            CourseTime courseTime = createTestCourseTime(30, 10);
            given(courseTimeRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(courseTime));

            // when
            CapacityResponse response = courseTimeService.getCapacity(1L);

            // then
            assertThat(response.capacity()).isEqualTo(30);
            assertThat(response.currentEnrollment()).isEqualTo(10);
            assertThat(response.availableSeats()).isEqualTo(20);
            assertThat(response.unlimited()).isFalse();
        }

        @Test
        @DisplayName("성공 - 정원 조회 (무제한)")
        void getCapacity_success_unlimited() {
            // given
            CourseTime courseTime = createTestCourseTime(null, 100);
            given(courseTimeRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(courseTime));

            // when
            CapacityResponse response = courseTimeService.getCapacity(1L);

            // then
            assertThat(response.capacity()).isNull();
            assertThat(response.currentEnrollment()).isEqualTo(100);
            assertThat(response.availableSeats()).isNull();
            assertThat(response.unlimited()).isTrue();
        }
    }

    @Nested
    @DisplayName("getPrice - 가격 조회")
    class GetPrice {

        @Test
        @DisplayName("성공 - 가격 조회 (유료)")
        void getPrice_success_paid() {
            // given
            CourseTime courseTime = createTestCourseTime(30, 0);
            given(courseTimeRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(courseTime));

            // when
            PriceResponse response = courseTimeService.getPrice(1L);

            // then
            assertThat(response.price()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(response.free()).isFalse();
        }

        @Test
        @DisplayName("성공 - 가격 조회 (무료)")
        void getPrice_success_free() {
            // given
            CourseTime courseTime = CourseTime.create(
                    "무료 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    30,
                    null,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    BigDecimal.ZERO,
                    true,
                    null,
                    true,
                    1L
            );
            given(courseTimeRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(courseTime));

            // when
            PriceResponse response = courseTimeService.getPrice(1L);

            // then
            assertThat(response.price()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.free()).isTrue();
        }
    }

    // ==================== 차수 생성 - 날짜 검증 테스트 ====================

    @Nested
    @DisplayName("createCourseTime - 날짜 검증")
    class CreateCourseTimeDateValidation {

        @Test
        @DisplayName("실패 - [R-DATE-03] 모집 종료일이 학습 시작일보다 늦은 경우")
        void createCourseTime_fail_enrollEndDateAfterClassStartDate() {
            // given
            LocalDate today = LocalDate.now();
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L,  // programId
                    null,
                    null,
                    "테스트 차수",
                    DeliveryType.ONLINE,
                    today,                      // enrollStartDate
                    today.plusDays(10),         // enrollEndDate - 학습 시작일보다 늦음
                    today.plusDays(7),          // classStartDate - 모집 종료일보다 빠름
                    today.plusDays(30),         // classEndDate
                    30,
                    5,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    null,
                    true
            );

            // Program Mock 설정
            Program program = createApprovedProgram();
            given(programRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(program));

            // when & then
            assertThatThrownBy(() -> courseTimeService.createCourseTime(request, 1L))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("모집 종료일은 학습 시작일 이전이어야 합니다");
        }

        @Test
        @DisplayName("실패 - [R-DATE-04] 과거 날짜로 모집 시작일 설정 시")
        void createCourseTime_fail_enrollStartDateInPast() {
            // given
            LocalDate today = LocalDate.now();
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L,  // programId
                    null,
                    null,
                    "테스트 차수",
                    DeliveryType.ONLINE,
                    today.minusDays(1),         // enrollStartDate - 과거 날짜
                    today.plusDays(7),          // enrollEndDate
                    today.plusDays(7),          // classStartDate
                    today.plusDays(30),         // classEndDate
                    30,
                    5,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    null,
                    true
            );

            // Program Mock 설정
            Program program = createApprovedProgram();
            given(programRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(program));

            // when & then
            assertThatThrownBy(() -> courseTimeService.createCourseTime(request, 1L))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("모집 시작일은 오늘 이후여야 합니다");
        }

        @Test
        @DisplayName("성공 - 오늘 날짜로 모집 시작일 설정 가능")
        void createCourseTime_success_enrollStartDateToday() {
            // given
            LocalDate today = LocalDate.now();
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    1L,  // programId
                    null,
                    null,
                    "테스트 차수",
                    DeliveryType.ONLINE,
                    today,                      // enrollStartDate - 오늘
                    today.plusDays(7),          // enrollEndDate
                    today.plusDays(7),          // classStartDate
                    today.plusDays(30),         // classEndDate
                    30,
                    5,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    null,
                    true
            );

            // Program Mock 설정
            Program program = createApprovedProgram();
            given(programRepository.findByIdAndTenantId(1L, DEFAULT_TENANT_ID)).willReturn(Optional.of(program));

            CourseTime savedCourseTime = createTestCourseTime(30, 0);
            given(courseTimeRepository.save(org.mockito.ArgumentMatchers.any(CourseTime.class)))
                    .willReturn(savedCourseTime);

            // when & then - 날짜 검증 통과, repository.save 호출됨
            courseTimeService.createCourseTime(request, 1L);

            verify(courseTimeRepository).save(org.mockito.ArgumentMatchers.any(CourseTime.class));
        }
    }
}
