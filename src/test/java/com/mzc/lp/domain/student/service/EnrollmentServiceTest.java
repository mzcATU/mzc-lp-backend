package com.mzc.lp.domain.student.service;
import com.mzc.lp.common.support.TenantTestSupport;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.constant.EnrollmentType;
import com.mzc.lp.domain.student.dto.request.CompleteEnrollmentRequest;
import com.mzc.lp.domain.student.dto.request.ForceEnrollRequest;
import com.mzc.lp.domain.student.dto.request.UpdateEnrollmentStatusRequest;
import com.mzc.lp.domain.student.dto.request.UpdateProgressRequest;
import com.mzc.lp.domain.student.dto.response.EnrollmentDetailResponse;
import com.mzc.lp.domain.student.dto.response.EnrollmentResponse;
import com.mzc.lp.domain.student.dto.response.ForceEnrollResultResponse;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.exception.AlreadyEnrolledException;
import com.mzc.lp.domain.student.exception.CannotCancelCompletedException;
import com.mzc.lp.domain.student.exception.EnrollmentNotFoundException;
import com.mzc.lp.domain.student.exception.EnrollmentPeriodClosedException;
import com.mzc.lp.domain.student.exception.UnauthorizedEnrollmentAccessException;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.ts.service.CourseTimeService;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest extends TenantTestSupport {

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private CourseTimeService courseTimeService;

    @Mock
    private InstructorAssignmentRepository instructorAssignmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    private static final Long TENANT_ID = 1L;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // userRepository.findAllById() 기본 mock 설정 (빈 리스트 반환)
        org.mockito.Mockito.lenient().when(userRepository.findAllById(any())).thenReturn(java.util.Collections.emptyList());
    }

    private CourseTime createTestCourseTime(boolean canEnroll) {
        LocalDate enrollStart = LocalDate.now().minusDays(1);
        LocalDate enrollEnd = LocalDate.now().plusDays(7);

        CourseTime courseTime = CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                enrollStart,
                enrollEnd,
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                1L
        );

        // 테스트용 tenantId 설정 (리플렉션 사용 - @PrePersist가 실행되지 않으므로)
        try {
            var tenantIdField = com.mzc.lp.common.entity.TenantEntity.class.getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(courseTime, TENANT_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // canEnroll이 true면 RECRUITING 상태로 변경
        if (canEnroll) {
            courseTime.open();  // DRAFT -> RECRUITING
        }

        return courseTime;
    }

    private Enrollment createTestEnrollment(Long userId, Long courseTimeId, EnrollmentStatus status) {
        Enrollment enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        if (status == EnrollmentStatus.COMPLETED) {
            enrollment.complete(100);
        } else if (status == EnrollmentStatus.DROPPED) {
            enrollment.drop();
        }
        return enrollment;
    }

    // ==================== 수강 신청 테스트 ====================

    @Nested
    @DisplayName("enroll - 수강 신청")
    class Enroll {

        @Test
        @DisplayName("성공 - 자발적 수강 신청")
        void enroll_success() {
            // given
            Long userId = 1L;
            Long courseTimeId = 1L;
            CourseTime courseTime = createTestCourseTime(true);

            // 비관적 락 사용으로 변경됨
            given(courseTimeRepository.findByIdWithLock(courseTimeId))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(false);
            given(enrollmentRepository.save(any(Enrollment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            EnrollmentDetailResponse response = enrollmentService.enroll(courseTimeId, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.courseTimeId()).isEqualTo(courseTimeId);
            assertThat(response.type()).isEqualTo(EnrollmentType.VOLUNTARY);
            assertThat(response.status()).isEqualTo(EnrollmentStatus.ENROLLED);
            assertThat(response.progressPercent()).isEqualTo(0);

            // 비관적 락 사용 시 courseTime.incrementEnrollment() 직접 호출
            verify(enrollmentRepository).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("실패 - 중복 수강 신청")
        void enroll_fail_alreadyEnrolled() {
            // given
            Long userId = 1L;
            Long courseTimeId = 1L;
            CourseTime courseTime = createTestCourseTime(true);

            // 비관적 락 사용으로 변경됨
            given(courseTimeRepository.findByIdWithLock(courseTimeId))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> enrollmentService.enroll(courseTimeId, userId))
                    .isInstanceOf(AlreadyEnrolledException.class);

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 수강 신청 기간 외")
        void enroll_fail_periodClosed() {
            // given
            Long userId = 1L;
            Long courseTimeId = 1L;
            CourseTime courseTime = createTestCourseTime(false);

            // 비관적 락 사용으로 변경됨
            given(courseTimeRepository.findByIdWithLock(courseTimeId))
                    .willReturn(Optional.of(courseTime));

            // when & then
            assertThatThrownBy(() -> enrollmentService.enroll(courseTimeId, userId))
                    .isInstanceOf(EnrollmentPeriodClosedException.class);

            verify(enrollmentRepository, never()).save(any());
        }
    }

    // ==================== 강제 배정 테스트 ====================

    @Nested
    @DisplayName("forceEnroll - 강제 배정")
    class ForceEnroll {

        @Test
        @DisplayName("성공 - 다수 사용자 강제 배정")
        void forceEnroll_success() {
            // given
            Long courseTimeId = 1L;
            Long operatorId = 99L;
            List<Long> userIds = List.of(1L, 2L, 3L);
            ForceEnrollRequest request = new ForceEnrollRequest(userIds, null);
            CourseTime courseTime = createTestCourseTime(true);

            // 비관적 락 사용으로 변경됨
            given(courseTimeRepository.findByIdWithLock(courseTimeId))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(any(), eq(courseTimeId), eq(TENANT_ID)))
                    .willReturn(false);
            given(enrollmentRepository.save(any(Enrollment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ForceEnrollResultResponse response = enrollmentService.forceEnroll(courseTimeId, request, operatorId);

            // then
            assertThat(response.enrollments()).hasSize(3);
            assertThat(response.failures()).isEmpty();
            assertThat(response.successCount()).isEqualTo(3);
            assertThat(response.failCount()).isEqualTo(0);

            verify(enrollmentRepository, times(3)).save(any(Enrollment.class));
            // 비관적 락 사용 시 courseTime.forceIncrementEnrollment() 직접 호출
        }

        @Test
        @DisplayName("부분 성공 - 일부 사용자 이미 수강 중")
        void forceEnroll_partialSuccess() {
            // given
            Long courseTimeId = 1L;
            Long operatorId = 99L;
            List<Long> userIds = List.of(1L, 2L, 3L);
            ForceEnrollRequest request = new ForceEnrollRequest(userIds, null);
            CourseTime courseTime = createTestCourseTime(true);

            // 비관적 락 사용으로 변경됨
            given(courseTimeRepository.findByIdWithLock(courseTimeId))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(1L, courseTimeId, TENANT_ID))
                    .willReturn(true);  // 이미 등록됨
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(2L, courseTimeId, TENANT_ID))
                    .willReturn(false);
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(3L, courseTimeId, TENANT_ID))
                    .willReturn(false);
            given(enrollmentRepository.save(any(Enrollment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ForceEnrollResultResponse response = enrollmentService.forceEnroll(courseTimeId, request, operatorId);

            // then
            assertThat(response.enrollments()).hasSize(2);
            assertThat(response.failures()).hasSize(1);
            assertThat(response.failures().get(0).userId()).isEqualTo(1L);

            verify(enrollmentRepository, times(2)).save(any(Enrollment.class));
            // 비관적 락 사용 시 courseTime.forceIncrementEnrollment() 직접 호출
        }
    }

    // ==================== 진도율 업데이트 테스트 ====================

    @Nested
    @DisplayName("updateProgress - 진도율 업데이트")
    class UpdateProgress {

        @Test
        @DisplayName("성공 - 진도율 업데이트")
        void updateProgress_success() {
            // given
            Long enrollmentId = 1L;
            Long userId = 1L;
            Enrollment enrollment = createTestEnrollment(userId, 1L, EnrollmentStatus.ENROLLED);
            UpdateProgressRequest request = new UpdateProgressRequest(1L, 50, 0);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));

            // when (본인의 수강이므로 isAdmin=false)
            EnrollmentResponse response = enrollmentService.updateProgress(enrollmentId, request, userId, false);

            // then
            assertThat(response.progressPercent()).isEqualTo(50);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수강")
        void updateProgress_fail_notFound() {
            // given
            Long enrollmentId = 999L;
            UpdateProgressRequest request = new UpdateProgressRequest(1L, 50, 0);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> enrollmentService.updateProgress(enrollmentId, request, 1L, false))
                    .isInstanceOf(EnrollmentNotFoundException.class);
        }
    }

    // ==================== 수료 처리 테스트 ====================

    @Nested
    @DisplayName("completeEnrollment - 수료 처리")
    class CompleteEnrollment {

        @Test
        @DisplayName("성공 - 수료 처리")
        void completeEnrollment_success() {
            // given
            Long enrollmentId = 1L;
            Enrollment enrollment = createTestEnrollment(1L, 1L, EnrollmentStatus.ENROLLED);
            CompleteEnrollmentRequest request = new CompleteEnrollmentRequest(95);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));

            // when
            EnrollmentDetailResponse response = enrollmentService.completeEnrollment(enrollmentId, request);

            // then
            assertThat(response.status()).isEqualTo(EnrollmentStatus.COMPLETED);
            assertThat(response.score()).isEqualTo(95);
            assertThat(response.progressPercent()).isEqualTo(100);
            assertThat(response.completedAt()).isNotNull();
        }
    }

    // ==================== 수강 취소 테스트 ====================

    @Nested
    @DisplayName("cancelEnrollment - 수강 취소")
    class CancelEnrollment {

        @Test
        @DisplayName("성공 - 수강 취소")
        void cancelEnrollment_success() {
            // given
            Long enrollmentId = 1L;
            Long userId = 1L;
            Long courseTimeId = 1L;
            Enrollment enrollment = createTestEnrollment(userId, courseTimeId, EnrollmentStatus.ENROLLED);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));

            // when (본인의 수강 취소이므로 isAdmin=false)
            enrollmentService.cancelEnrollment(enrollmentId, userId, false);

            // then
            assertThat(enrollment.isDropped()).isTrue();
            verify(courseTimeService).releaseSeat(courseTimeId);
        }

        @Test
        @DisplayName("실패 - 수료 상태에서 취소 불가")
        void cancelEnrollment_fail_completed() {
            // given
            Long enrollmentId = 1L;
            Long userId = 1L;
            Enrollment enrollment = createTestEnrollment(userId, 1L, EnrollmentStatus.COMPLETED);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));

            // when & then
            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(enrollmentId, userId, false))
                    .isInstanceOf(CannotCancelCompletedException.class);

            verify(courseTimeService, never()).releaseSeat(any());
        }
    }

    // ==================== 상태 변경 테스트 ====================

    @Nested
    @DisplayName("updateStatus - 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("성공 - 상태를 FAILED로 변경")
        void updateStatus_success() {
            // given
            Long enrollmentId = 1L;
            Enrollment enrollment = createTestEnrollment(1L, 1L, EnrollmentStatus.ENROLLED);
            UpdateEnrollmentStatusRequest request = new UpdateEnrollmentStatusRequest(EnrollmentStatus.FAILED, null);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));

            // when
            EnrollmentDetailResponse response = enrollmentService.updateStatus(enrollmentId, request);

            // then
            assertThat(response.status()).isEqualTo(EnrollmentStatus.FAILED);
        }
    }

    // ==================== 조회 테스트 ====================

    @Nested
    @DisplayName("getMyEnrollments - 내 수강 목록 조회")
    class GetMyEnrollments {

        @Test
        @DisplayName("성공 - 내 수강 목록 조회")
        void getMyEnrollments_success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20);
            List<Enrollment> enrollments = List.of(
                    createTestEnrollment(userId, 1L, EnrollmentStatus.ENROLLED),
                    createTestEnrollment(userId, 2L, EnrollmentStatus.COMPLETED)
            );
            Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

            given(enrollmentRepository.findByUserIdAndTenantId(userId, TENANT_ID, pageable))
                    .willReturn(page);

            // when
            Page<EnrollmentResponse> response = enrollmentService.getMyEnrollments(userId, null, pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 상태별 필터링")
        void getMyEnrollments_success_withStatusFilter() {
            // given
            Long userId = 1L;
            EnrollmentStatus status = EnrollmentStatus.ENROLLED;
            Pageable pageable = PageRequest.of(0, 20);
            List<Enrollment> enrollments = List.of(
                    createTestEnrollment(userId, 1L, EnrollmentStatus.ENROLLED)
            );
            Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

            given(enrollmentRepository.findByUserIdAndStatusAndTenantId(userId, status, TENANT_ID, pageable))
                    .willReturn(page);

            // when
            Page<EnrollmentResponse> response = enrollmentService.getMyEnrollments(userId, status, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).status()).isEqualTo(EnrollmentStatus.ENROLLED);
        }
    }

    // ==================== 차수별 수강생 목록 조회 (소유권 검증) 테스트 ====================

    @Nested
    @DisplayName("getEnrollmentsByCourseTime - 차수별 수강생 목록 조회 (소유권 검증)")
    class GetEnrollmentsByCourseTime {

        private CourseTime createCourseTimeWithProgram(Long createdBy, Long programOwner) {
            CourseTime courseTime = createTestCourseTime(true);

            try {
                // CourseTime.createdBy 설정
                var createdByField = CourseTime.class.getDeclaredField("createdBy");
                createdByField.setAccessible(true);
                createdByField.set(courseTime, createdBy);

                // Program 설정 (programOwner가 null이 아닌 경우)
                if (programOwner != null) {
                    Program program = mock(Program.class, withSettings().lenient());
                    given(program.getCreatedBy()).willReturn(programOwner);
                    given(program.getId()).willReturn(1L);

                    var programField = CourseTime.class.getDeclaredField("program");
                    programField.setAccessible(true);
                    programField.set(courseTime, program);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return courseTime;
        }

        @Test
        @DisplayName("성공 - 관리자(OPERATOR/TENANT_ADMIN)는 검증 없이 조회 가능")
        void getEnrollmentsByCourseTime_success_admin() {
            // given
            Long courseTimeId = 1L;
            Long adminUserId = 99L;
            Pageable pageable = PageRequest.of(0, 20);
            List<Enrollment> enrollments = List.of(
                    createTestEnrollment(1L, courseTimeId, EnrollmentStatus.ENROLLED)
            );
            Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

            given(enrollmentRepository.findByCourseTimeIdAndTenantId(courseTimeId, TENANT_ID, pageable))
                    .willReturn(page);

            // when - isAdmin = true
            Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByCourseTime(
                    courseTimeId, null, pageable, adminUserId, true);

            // then
            assertThat(response.getContent()).hasSize(1);
            // 관리자는 validateCourseTimeAccess가 호출되지 않음
            verify(courseTimeRepository, never()).findByIdAndTenantId(any(), any());
        }

        @Test
        @DisplayName("성공 - 프로그램 소유자(createdBy)가 조회")
        void getEnrollmentsByCourseTime_success_programOwner() {
            // given
            Long courseTimeId = 1L;
            Long programOwnerId = 10L;
            CourseTime courseTime = createCourseTimeWithProgram(99L, programOwnerId);
            Pageable pageable = PageRequest.of(0, 20);
            List<Enrollment> enrollments = List.of(
                    createTestEnrollment(1L, courseTimeId, EnrollmentStatus.ENROLLED)
            );
            Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.findByCourseTimeIdAndTenantId(courseTimeId, TENANT_ID, pageable))
                    .willReturn(page);

            // when - 프로그램 소유자가 조회 (isAdmin = false)
            Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByCourseTime(
                    courseTimeId, null, pageable, programOwnerId, false);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 차수 생성자(createdBy)가 조회")
        void getEnrollmentsByCourseTime_success_courseTimeCreator() {
            // given
            Long courseTimeId = 1L;
            Long courseTimeCreatorId = 20L;
            CourseTime courseTime = createCourseTimeWithProgram(courseTimeCreatorId, null);
            Pageable pageable = PageRequest.of(0, 20);
            List<Enrollment> enrollments = List.of(
                    createTestEnrollment(1L, courseTimeId, EnrollmentStatus.ENROLLED)
            );
            Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.findByCourseTimeIdAndTenantId(courseTimeId, TENANT_ID, pageable))
                    .willReturn(page);

            // when - 차수 생성자가 조회 (isAdmin = false)
            Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByCourseTime(
                    courseTimeId, null, pageable, courseTimeCreatorId, false);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 배정된 강사가 조회")
        void getEnrollmentsByCourseTime_success_assignedInstructor() {
            // given
            Long courseTimeId = 1L;
            Long instructorId = 30L;
            Long otherCreatorId = 99L;
            Long otherProgramOwnerId = 88L;
            CourseTime courseTime = createCourseTimeWithProgram(otherCreatorId, otherProgramOwnerId); // 다른 사람이 소유자
            Pageable pageable = PageRequest.of(0, 20);
            List<Enrollment> enrollments = List.of(
                    createTestEnrollment(1L, courseTimeId, EnrollmentStatus.ENROLLED)
            );
            Page<Enrollment> page = new PageImpl<>(enrollments, pageable, enrollments.size());

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));
            // 강사 배정 확인 - 프로그램/차수 소유자가 아니므로 이 체크가 호출됨
            given(instructorAssignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    courseTimeId, instructorId, TENANT_ID, AssignmentStatus.ACTIVE))
                    .willReturn(true);
            given(enrollmentRepository.findByCourseTimeIdAndTenantId(courseTimeId, TENANT_ID, pageable))
                    .willReturn(page);

            // when - 배정된 강사가 조회 (isAdmin = false)
            Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByCourseTime(
                    courseTimeId, null, pageable, instructorId, false);

            // then
            assertThat(response.getContent()).hasSize(1);
            verify(instructorAssignmentRepository).existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    courseTimeId, instructorId, TENANT_ID, AssignmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("실패 - 권한 없는 사용자가 조회 시도")
        void getEnrollmentsByCourseTime_fail_unauthorized() {
            // given
            Long courseTimeId = 1L;
            Long unauthorizedUserId = 999L;
            CourseTime courseTime = createCourseTimeWithProgram(10L, 20L); // 다른 사람이 소유자
            Pageable pageable = PageRequest.of(0, 20);

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));
            given(instructorAssignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    courseTimeId, unauthorizedUserId, TENANT_ID, AssignmentStatus.ACTIVE))
                    .willReturn(false);

            // when & then - 권한 없는 사용자 (isAdmin = false)
            assertThatThrownBy(() -> enrollmentService.getEnrollmentsByCourseTime(
                    courseTimeId, null, pageable, unauthorizedUserId, false))
                    .isInstanceOf(UnauthorizedEnrollmentAccessException.class);
        }
    }
}
