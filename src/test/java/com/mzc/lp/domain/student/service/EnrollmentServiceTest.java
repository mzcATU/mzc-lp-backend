package com.mzc.lp.domain.student.service;

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
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.ts.service.CourseTimeService;
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

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private CourseTimeService courseTimeService;

    private static final Long TENANT_ID = 1L;

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

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
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

            verify(courseTimeService).occupySeat(courseTimeId);
            verify(enrollmentRepository).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("실패 - 중복 수강 신청")
        void enroll_fail_alreadyEnrolled() {
            // given
            Long userId = 1L;
            Long courseTimeId = 1L;
            CourseTime courseTime = createTestCourseTime(true);

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));
            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> enrollmentService.enroll(courseTimeId, userId))
                    .isInstanceOf(AlreadyEnrolledException.class);

            verify(courseTimeService, never()).occupySeat(any());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 수강 신청 기간 외")
        void enroll_fail_periodClosed() {
            // given
            Long userId = 1L;
            Long courseTimeId = 1L;
            CourseTime courseTime = createTestCourseTime(false);

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));

            // when & then
            assertThatThrownBy(() -> enrollmentService.enroll(courseTimeId, userId))
                    .isInstanceOf(EnrollmentPeriodClosedException.class);

            verify(courseTimeService, never()).occupySeat(any());
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

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
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

            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
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
            UpdateProgressRequest request = new UpdateProgressRequest(50);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));

            // when
            EnrollmentResponse response = enrollmentService.updateProgress(enrollmentId, request, userId);

            // then
            assertThat(response.progressPercent()).isEqualTo(50);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수강")
        void updateProgress_fail_notFound() {
            // given
            Long enrollmentId = 999L;
            UpdateProgressRequest request = new UpdateProgressRequest(50);

            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> enrollmentService.updateProgress(enrollmentId, request, 1L))
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

            // when
            enrollmentService.cancelEnrollment(enrollmentId, userId);

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
            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(enrollmentId, userId))
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
}
