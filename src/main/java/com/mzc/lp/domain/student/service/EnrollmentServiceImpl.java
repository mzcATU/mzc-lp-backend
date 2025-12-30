package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
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
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.ts.service.CourseTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final CourseTimeService courseTimeService;
    private final InstructorAssignmentRepository instructorAssignmentRepository;

    @Override
    @Transactional
    public EnrollmentDetailResponse enroll(Long courseTimeId, Long userId) {
        log.info("Enrolling user: userId={}, courseTimeId={}", userId, courseTimeId);

        Long tenantId = TenantContext.getCurrentTenantId();

        // [Race Condition 방지] 비관적 락으로 차수 조회 - 모든 검증을 락 상태에서 수행
        CourseTime courseTime = courseTimeRepository.findByIdWithLock(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 테넌트 검증
        if (!courseTime.getTenantId().equals(tenantId)) {
            throw new CourseTimeNotFoundException(courseTimeId);
        }

        // 수강 신청 가능 여부 체크 (락 상태에서)
        if (!courseTime.canEnroll()) {
            throw new EnrollmentPeriodClosedException(courseTimeId);
        }

        // 중복 수강 체크 (락 상태에서)
        if (enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, tenantId)) {
            throw new AlreadyEnrolledException(userId, courseTimeId);
        }

        // 정원 체크 및 증가 (락 상태에서 직접 수행)
        if (!courseTime.hasUnlimitedCapacity() && !courseTime.hasAvailableSeats()) {
            throw new EnrollmentPeriodClosedException(courseTimeId);
        }
        courseTime.incrementEnrollment();

        // 수강 생성
        Enrollment enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        log.info("Enrollment created: id={}, userId={}, courseTimeId={}",
                savedEnrollment.getId(), userId, courseTimeId);

        return EnrollmentDetailResponse.from(savedEnrollment);
    }

    @Override
    @Transactional
    public ForceEnrollResultResponse forceEnroll(Long courseTimeId, ForceEnrollRequest request, Long operatorId) {
        log.info("Force enrolling users: courseTimeId={}, userCount={}, operatorId={}",
                courseTimeId, request.userIds().size(), operatorId);

        Long tenantId = TenantContext.getCurrentTenantId();

        // [Race Condition 방지] 비관적 락으로 차수 조회
        CourseTime courseTime = courseTimeRepository.findByIdWithLock(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 테넌트 검증
        if (!courseTime.getTenantId().equals(tenantId)) {
            throw new CourseTimeNotFoundException(courseTimeId);
        }

        List<EnrollmentResponse> enrollments = new ArrayList<>();
        List<ForceEnrollResultResponse.FailureDetail> failures = new ArrayList<>();

        for (Long userId : request.userIds()) {
            try {
                // 중복 수강 체크 (락 상태에서)
                if (enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, tenantId)) {
                    failures.add(new ForceEnrollResultResponse.FailureDetail(userId, "이미 수강 중입니다"));
                    continue;
                }

                // 정원 증가 (강제 배정은 정원 초과 무시, 락 상태에서 직접 증가)
                courseTime.incrementEnrollment();

                // 수강 생성
                Enrollment enrollment = Enrollment.createMandatory(userId, courseTimeId, operatorId);
                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

                enrollments.add(EnrollmentResponse.from(savedEnrollment));
            } catch (Exception e) {
                log.warn("Failed to force enroll user: userId={}, error={}", userId, e.getMessage());
                failures.add(new ForceEnrollResultResponse.FailureDetail(userId, e.getMessage()));
            }
        }

        log.info("Force enrollment completed: successCount={}, failCount={}",
                enrollments.size(), failures.size());

        return ForceEnrollResultResponse.of(enrollments, failures);
    }

    @Override
    public EnrollmentDetailResponse getEnrollment(Long enrollmentId) {
        log.debug("Getting enrollment: id={}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        return EnrollmentDetailResponse.from(enrollment);
    }

    @Override
    public Page<EnrollmentResponse> getEnrollmentsByCourseTime(Long courseTimeId, EnrollmentStatus status, Pageable pageable, Long userId, boolean isAdmin) {
        log.debug("Getting enrollments by course time: courseTimeId={}, status={}, userId={}, isAdmin={}",
                courseTimeId, status, userId, isAdmin);

        Long tenantId = TenantContext.getCurrentTenantId();

        // 관리자(OPERATOR/TENANT_ADMIN)가 아닌 경우 소유권/강사 검증
        if (!isAdmin) {
            validateCourseTimeAccess(courseTimeId, userId, tenantId);
        }

        if (status != null) {
            return enrollmentRepository.findByCourseTimeIdAndStatusAndTenantId(courseTimeId, status, tenantId, pageable)
                    .map(EnrollmentResponse::from);
        }

        return enrollmentRepository.findByCourseTimeIdAndTenantId(courseTimeId, tenantId, pageable)
                .map(EnrollmentResponse::from);
    }

    /**
     * 차수 접근 권한 검증
     * - 프로그램 소유자(createdBy)인 경우 허용
     * - 해당 차수에 배정된 강사인 경우 허용
     */
    private void validateCourseTimeAccess(Long courseTimeId, Long userId, Long tenantId) {
        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 1. 프로그램 소유자 확인 (Program.createdBy == userId)
        Program program = courseTime.getProgram();
        if (program != null && program.getCreatedBy().equals(userId)) {
            log.debug("User {} is the owner of program {}", userId, program.getId());
            return;
        }

        // 2. 차수 생성자 확인 (CourseTime.createdBy == userId)
        if (courseTime.getCreatedBy() != null && courseTime.getCreatedBy().equals(userId)) {
            log.debug("User {} is the creator of course time {}", userId, courseTimeId);
            return;
        }

        // 3. 배정된 강사인지 확인
        boolean isAssignedInstructor = instructorAssignmentRepository
                .existsByTimeKeyAndUserKeyAndTenantIdAndStatus(courseTimeId, userId, tenantId, AssignmentStatus.ACTIVE);
        if (isAssignedInstructor) {
            log.debug("User {} is an assigned instructor for course time {}", userId, courseTimeId);
            return;
        }

        // 권한 없음
        log.warn("User {} has no access to course time {} enrollments", userId, courseTimeId);
        throw new UnauthorizedEnrollmentAccessException(courseTimeId, userId);
    }

    @Override
    public Page<EnrollmentResponse> getMyEnrollments(Long userId, EnrollmentStatus status, Pageable pageable) {
        log.debug("Getting my enrollments: userId={}, status={}", userId, status);

        Long tenantId = TenantContext.getCurrentTenantId();

        if (status != null) {
            return enrollmentRepository.findByUserIdAndStatusAndTenantId(userId, status, tenantId, pageable)
                    .map(EnrollmentResponse::from);
        }

        return enrollmentRepository.findByUserIdAndTenantId(userId, tenantId, pageable)
                .map(EnrollmentResponse::from);
    }

    @Override
    public Page<EnrollmentResponse> getEnrollmentsByUser(Long userId, EnrollmentStatus status, Pageable pageable) {
        log.debug("Getting enrollments by user: userId={}, status={}", userId, status);

        // 관리자용 - 동일 로직
        return getMyEnrollments(userId, status, pageable);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateProgress(Long enrollmentId, UpdateProgressRequest request, Long userId, boolean isAdmin) {
        log.info("Updating progress: enrollmentId={}, progressPercent={}, userId={}, isAdmin={}",
                enrollmentId, request.progressPercent(), userId, isAdmin);

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 확인 (관리자가 아닌 경우)
        if (!isAdmin && !enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(enrollmentId, userId);
        }

        enrollment.updateProgress(request.progressPercent());

        log.info("Progress updated: enrollmentId={}, progressPercent={}",
                enrollmentId, request.progressPercent());

        return EnrollmentResponse.from(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse completeEnrollment(Long enrollmentId, CompleteEnrollmentRequest request) {
        log.info("Completing enrollment: enrollmentId={}, score={}", enrollmentId, request.score());

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        enrollment.complete(request.score());

        log.info("Enrollment completed: enrollmentId={}", enrollmentId);

        return EnrollmentDetailResponse.from(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse updateStatus(Long enrollmentId, UpdateEnrollmentStatusRequest request) {
        log.info("Updating enrollment status: enrollmentId={}, status={}", enrollmentId, request.status());

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        enrollment.updateStatus(request.status());

        log.info("Enrollment status updated: enrollmentId={}, status={}", enrollmentId, request.status());

        return EnrollmentDetailResponse.from(enrollment);
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long enrollmentId, Long userId, boolean isAdmin) {
        log.info("Cancelling enrollment: enrollmentId={}, userId={}, isAdmin={}", enrollmentId, userId, isAdmin);

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 확인 (관리자가 아닌 경우)
        if (!isAdmin && !enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(enrollmentId, userId);
        }

        // 수료 상태에서는 취소 불가
        if (!enrollment.canCancel()) {
            throw new CannotCancelCompletedException(enrollmentId);
        }

        // 정원 반환
        courseTimeService.releaseSeat(enrollment.getCourseTimeId());

        // 상태 변경
        enrollment.drop();

        log.info("Enrollment cancelled: enrollmentId={}", enrollmentId);
    }
}
