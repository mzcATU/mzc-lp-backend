package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.service.ActivityLogService;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.dto.request.BulkEnrollmentRequest;
import com.mzc.lp.domain.student.dto.request.CompleteEnrollmentRequest;
import com.mzc.lp.domain.student.dto.request.ForceEnrollRequest;
import com.mzc.lp.domain.student.dto.request.UpdateEnrollmentStatusRequest;
import com.mzc.lp.domain.student.dto.request.UpdateProgressRequest;
import com.mzc.lp.domain.student.dto.response.BulkEnrollmentResponse;
import com.mzc.lp.domain.student.dto.response.EnrollmentDetailResponse;
import com.mzc.lp.domain.student.dto.response.EnrollmentResponse;
import com.mzc.lp.domain.student.dto.response.ForceEnrollResultResponse;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.exception.AlreadyEnrolledException;
import com.mzc.lp.domain.student.exception.CannotCancelCompletedException;
import com.mzc.lp.domain.student.exception.EnrollmentNotFoundException;
import com.mzc.lp.domain.student.exception.EnrollmentPeriodClosedException;
import com.mzc.lp.domain.student.exception.InviteOnlyEnrollmentException;
import com.mzc.lp.domain.student.exception.UnauthorizedEnrollmentAccessException;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.ts.service.CourseTimeService;
import com.mzc.lp.domain.certificate.event.EnrollmentCompletedEvent;
import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.event.NotificationEventPublisher;
import com.mzc.lp.domain.notification.service.NotificationService;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final CourseTimeService courseTimeService;
    private final InstructorAssignmentRepository instructorAssignmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final ActivityLogService activityLogService;

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

        // INVITE_ONLY 방식은 자발적 수강신청 불가
        if (courseTime.getEnrollmentMethod() == EnrollmentMethod.INVITE_ONLY) {
            throw new InviteOnlyEnrollmentException(courseTimeId);
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

        // EnrollmentMethod에 따른 수강 생성
        Enrollment enrollment;
        if (courseTime.getEnrollmentMethod() == EnrollmentMethod.APPROVAL) {
            // 승인제: PENDING 상태로 생성
            enrollment = Enrollment.createPending(userId, courseTimeId);
            log.info("Enrollment created with PENDING status (approval required): userId={}, courseTimeId={}",
                    userId, courseTimeId);
        } else {
            // 선착순(FIRST_COME): 즉시 ENROLLED 상태
            enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        }
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        log.info("Enrollment created: id={}, userId={}, courseTimeId={}, status={}",
                savedEnrollment.getId(), userId, courseTimeId, savedEnrollment.getStatus());

        // 활동 로그 기록
        activityLogService.log(
                ActivityType.ENROLLMENT_CREATE,
                String.format("수강신청: %s", courseTime.getTitle()),
                "CourseTime",
                courseTimeId,
                courseTime.getTitle()
        );

        // 수강신청 완료 알림 발송 (템플릿 기반) - ENROLLED 상태인 경우만
        if (savedEnrollment.isEnrolled()) {
            sendEnrollmentCompleteNotification(tenantId, savedEnrollment, courseTime);
        }

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

                // 수강신청 완료 알림 발송 (템플릿 기반)
                sendEnrollmentCompleteNotification(tenantId, savedEnrollment, courseTime);
            } catch (Exception e) {
                log.warn("Failed to force enroll user: userId={}, error={}", userId, e.getMessage());
                failures.add(new ForceEnrollResultResponse.FailureDetail(userId, e.getMessage()));
            }
        }

        log.info("Force enrollment completed: successCount={}, failCount={}",
                enrollments.size(), failures.size());

        return ForceEnrollResultResponse.of(enrollments, failures);
    }

    /**
     * 수강신청 완료 알림 발송 (템플릿 기반)
     */
    private void sendEnrollmentCompleteNotification(Long tenantId, Enrollment enrollment, CourseTime courseTime) {
        try {
            User user = userRepository.findById(enrollment.getUserId()).orElse(null);
            String userName = user != null ? user.getName() : "회원";
            String courseName = courseTime.getTitle();

            notificationEventPublisher.publishEnrollmentComplete(
                    tenantId,
                    enrollment.getUserId(),
                    userName,
                    courseName,
                    enrollment.getId()
            );
            log.debug("Enrollment complete notification event published for user: {}", enrollment.getUserId());
        } catch (Exception e) {
            log.warn("Failed to publish enrollment notification for user {}: {}", enrollment.getUserId(), e.getMessage());
        }
    }

    /**
     * 과정 완료 축하 알림 발송 (템플릿 기반)
     */
    private void sendCourseCompleteNotification(Enrollment enrollment) {
        try {
            Long tenantId = TenantContext.getCurrentTenantId();
            Long userId = enrollment.getUserId();

            User user = userRepository.findById(userId).orElse(null);
            String userName = user != null ? user.getName() : "회원";

            CourseTime courseTime = courseTimeRepository.findById(enrollment.getCourseTimeId()).orElse(null);
            String courseName = courseTime != null ? courseTime.getTitle() : "과정";

            notificationEventPublisher.publishCourseComplete(
                    tenantId,
                    userId,
                    userName,
                    courseName,
                    enrollment.getId()
            );
            log.debug("Course complete notification event published for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to publish course complete notification for user {}: {}", enrollment.getUserId(), e.getMessage());
        }
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

        Page<Enrollment> enrollments;
        if (status != null) {
            enrollments = enrollmentRepository.findByCourseTimeIdAndStatusAndTenantId(courseTimeId, status, tenantId, pageable);
        } else {
            enrollments = enrollmentRepository.findByCourseTimeIdAndTenantId(courseTimeId, tenantId, pageable);
        }

        return mapEnrollmentsWithUserInfo(enrollments);
    }

    /**
     * 차수 접근 권한 검증
     * - 강의 소유자(createdBy)인 경우 허용
     * - 해당 차수에 배정된 강사인 경우 허용
     */
    private void validateCourseTimeAccess(Long courseTimeId, Long userId, Long tenantId) {
        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 1. 강의 소유자 확인 (Course.createdBy == userId)
        Course course = courseTime.getCourse();
        if (course != null && course.getCreatedBy() != null && course.getCreatedBy().equals(userId)) {
            log.debug("User {} is the owner of course {}", userId, course.getId());
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

        Page<Enrollment> enrollments;
        if (status != null) {
            enrollments = enrollmentRepository.findByUserIdAndStatusAndTenantId(userId, status, tenantId, pageable);
        } else {
            enrollments = enrollmentRepository.findByUserIdAndTenantId(userId, tenantId, pageable);
        }

        return mapEnrollmentsWithUserInfo(enrollments);
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

        // 진도율 100% 달성 시 자동 수료 처리
        if (request.progressPercent() >= 100 && enrollment.isEnrolled()) {
            enrollment.updateStatus(EnrollmentStatus.COMPLETED);

            // 수료 이벤트 발행 (수료증 자동 발급)
            eventPublisher.publishEvent(new EnrollmentCompletedEvent(
                    this,
                    enrollment.getId(),
                    enrollment.getUserId(),
                    enrollment.getCourseTimeId()
            ));

            // 과정 완료 축하 알림 발송 (템플릿 기반)
            sendCourseCompleteNotification(enrollment);

            log.info("Enrollment auto-completed: enrollmentId={}, progressPercent=100%", enrollmentId);
        }

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

        // 수료 이벤트 발행 (수료증 자동 발급)
        eventPublisher.publishEvent(new EnrollmentCompletedEvent(
                this,
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseTimeId()
        ));

        // 과정 완료 축하 알림 발송 (템플릿 기반)
        sendCourseCompleteNotification(enrollment);

        // 활동 로그 기록
        CourseTime courseTime = courseTimeRepository.findById(enrollment.getCourseTimeId()).orElse(null);
        activityLogService.log(
                ActivityType.ENROLLMENT_COMPLETE,
                String.format("수강완료: %s (점수: %s)", courseTime != null ? courseTime.getTitle() : "과정", request.score()),
                "Enrollment",
                enrollmentId,
                courseTime != null ? courseTime.getTitle() : null
        );

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

        // COMPLETED 상태로 변경 시 수료 이벤트 발행
        if (request.status() == EnrollmentStatus.COMPLETED) {
            eventPublisher.publishEvent(new EnrollmentCompletedEvent(
                    this,
                    enrollment.getId(),
                    enrollment.getUserId(),
                    enrollment.getCourseTimeId()
            ));

            // 과정 완료 축하 알림 발송 (템플릿 기반)
            sendCourseCompleteNotification(enrollment);
        }

        log.info("Enrollment status updated: enrollmentId={}, status={}", enrollmentId, request.status());

        return EnrollmentDetailResponse.from(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse approveEnrollment(Long enrollmentId) {
        log.info("Approving enrollment: enrollmentId={}", enrollmentId);

        Long tenantId = TenantContext.getCurrentTenantId();

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // PENDING 상태 검증 및 승인
        enrollment.approve();

        // 활동 로그 기록
        CourseTime courseTime = courseTimeRepository.findById(enrollment.getCourseTimeId()).orElse(null);
        activityLogService.log(
                ActivityType.ENROLLMENT_CREATE,
                String.format("수강신청 승인: %s", courseTime != null ? courseTime.getTitle() : "과정"),
                "Enrollment",
                enrollmentId,
                courseTime != null ? courseTime.getTitle() : null
        );

        // 수강신청 완료 알림 발송 (승인 완료)
        if (courseTime != null) {
            sendEnrollmentCompleteNotification(tenantId, enrollment, courseTime);
        }

        log.info("Enrollment approved: enrollmentId={}", enrollmentId);

        return EnrollmentDetailResponse.from(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse rejectEnrollment(Long enrollmentId) {
        log.info("Rejecting enrollment: enrollmentId={}", enrollmentId);

        Long tenantId = TenantContext.getCurrentTenantId();

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // PENDING 상태 검증 및 거절
        enrollment.reject();

        // 정원 반환
        courseTimeService.releaseSeat(enrollment.getCourseTimeId());

        // 활동 로그 기록
        CourseTime courseTime = courseTimeRepository.findById(enrollment.getCourseTimeId()).orElse(null);
        activityLogService.log(
                ActivityType.ENROLLMENT_DROP,
                String.format("수강신청 거절: %s", courseTime != null ? courseTime.getTitle() : "과정"),
                "Enrollment",
                enrollmentId,
                courseTime != null ? courseTime.getTitle() : null
        );

        log.info("Enrollment rejected: enrollmentId={}", enrollmentId);

        return EnrollmentDetailResponse.from(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse resubmitEnrollment(Long enrollmentId) {
        log.info("Resubmitting enrollment: enrollmentId={}", enrollmentId);

        Long tenantId = TenantContext.getCurrentTenantId();

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // REJECTED 상태 검증 및 재심사 (PENDING으로 변경)
        enrollment.resubmit();

        // 활동 로그 기록
        CourseTime courseTime = courseTimeRepository.findById(enrollment.getCourseTimeId()).orElse(null);
        activityLogService.log(
                ActivityType.ENROLLMENT_CREATE,
                String.format("수강신청 재심사 요청: %s", courseTime != null ? courseTime.getTitle() : "과정"),
                "Enrollment",
                enrollmentId,
                courseTime != null ? courseTime.getTitle() : null
        );

        log.info("Enrollment resubmitted: enrollmentId={}", enrollmentId);

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

        // 활동 로그 기록
        CourseTime courseTime = courseTimeRepository.findById(enrollment.getCourseTimeId()).orElse(null);
        activityLogService.log(
                ActivityType.ENROLLMENT_DROP,
                String.format("수강취소: %s", courseTime != null ? courseTime.getTitle() : "과정"),
                "Enrollment",
                enrollmentId,
                courseTime != null ? courseTime.getTitle() : null
        );

        log.info("Enrollment cancelled: enrollmentId={}", enrollmentId);
    }

    @Override
    @Transactional
    public BulkEnrollmentResponse bulkEnroll(BulkEnrollmentRequest request, Long userId) {
        log.info("Bulk enrolling user: userId={}, courseTimeCount={}", userId, request.courseTimeIds().size());

        Long tenantId = TenantContext.getCurrentTenantId();
        List<BulkEnrollmentResponse.EnrollmentResult> results = new ArrayList<>();

        for (Long courseTimeId : request.courseTimeIds()) {
            try {
                // 각 차수에 대해 개별적으로 수강신청 처리
                CourseTime courseTime = courseTimeRepository.findByIdWithLock(courseTimeId)
                        .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

                // 테넌트 검증
                if (!courseTime.getTenantId().equals(tenantId)) {
                    results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, "차수를 찾을 수 없습니다"));
                    continue;
                }

                // INVITE_ONLY 방식 체크
                if (courseTime.getEnrollmentMethod() == EnrollmentMethod.INVITE_ONLY) {
                    results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, "초대 전용 과정입니다"));
                    continue;
                }

                // 수강 신청 가능 여부 체크
                if (!courseTime.canEnroll()) {
                    results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, "수강신청 기간이 아닙니다"));
                    continue;
                }

                // 중복 수강 체크
                if (enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, tenantId)) {
                    results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, "이미 수강 중입니다"));
                    continue;
                }

                // 정원 체크
                if (!courseTime.hasUnlimitedCapacity() && !courseTime.hasAvailableSeats()) {
                    results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, "정원이 마감되었습니다"));
                    continue;
                }

                // 정원 증가
                courseTime.incrementEnrollment();

                // EnrollmentMethod에 따른 수강 생성
                Enrollment enrollment;
                if (courseTime.getEnrollmentMethod() == EnrollmentMethod.APPROVAL) {
                    enrollment = Enrollment.createPending(userId, courseTimeId);
                } else {
                    enrollment = Enrollment.createVoluntary(userId, courseTimeId);
                }
                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

                results.add(BulkEnrollmentResponse.EnrollmentResult.success(courseTimeId, savedEnrollment.getId()));

                log.debug("Bulk enrollment success: userId={}, courseTimeId={}, enrollmentId={}, status={}",
                        userId, courseTimeId, savedEnrollment.getId(), savedEnrollment.getStatus());
            } catch (CourseTimeNotFoundException e) {
                results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, "차수를 찾을 수 없습니다"));
            } catch (Exception e) {
                log.warn("Bulk enrollment failed: userId={}, courseTimeId={}, error={}", userId, courseTimeId, e.getMessage());
                results.add(BulkEnrollmentResponse.EnrollmentResult.failure(courseTimeId, e.getMessage()));
            }
        }

        BulkEnrollmentResponse response = BulkEnrollmentResponse.of(results);
        log.info("Bulk enrollment completed: userId={}, successCount={}, failureCount={}",
                userId, response.getSuccessCount(), response.getFailureCount());

        return response;
    }

    // ========== Private Helper Methods ==========

    /**
     * Enrollment 목록에 사용자 정보(이름, 이메일)를 포함하여 EnrollmentResponse로 변환
     * N+1 문제 방지를 위해 배치 조회 사용
     */
    private Page<EnrollmentResponse> mapEnrollmentsWithUserInfo(Page<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return enrollments.map(EnrollmentResponse::from);
        }

        // 사용자 ID 목록 추출
        List<Long> userIds = enrollments.getContent().stream()
                .map(Enrollment::getUserId)
                .distinct()
                .toList();

        // 배치 조회로 사용자 정보 가져오기
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // CourseTime ID 목록 추출
        List<Long> courseTimeIds = enrollments.getContent().stream()
                .map(Enrollment::getCourseTimeId)
                .distinct()
                .toList();

        // 배치 조회로 CourseTime 정보 가져오기
        Map<Long, com.mzc.lp.domain.ts.entity.CourseTime> courseTimeMap = courseTimeRepository.findAllById(courseTimeIds).stream()
                .collect(Collectors.toMap(com.mzc.lp.domain.ts.entity.CourseTime::getId, Function.identity()));

        // Enrollment에 사용자 정보, 실제 종료일, enrollmentMethod 매핑
        return enrollments.map(enrollment -> {
            User user = userMap.get(enrollment.getUserId());
            com.mzc.lp.domain.ts.entity.CourseTime courseTime = courseTimeMap.get(enrollment.getCourseTimeId());

            java.time.LocalDate actualEndDate = calculateActualEndDate(courseTime);
            var enrollmentMethod = courseTime != null ? courseTime.getEnrollmentMethod() : null;

            if (user != null) {
                return EnrollmentResponse.from(enrollment, user.getName(), user.getEmail(), actualEndDate, enrollmentMethod);
            }
            return EnrollmentResponse.from(enrollment, null, null, actualEndDate, enrollmentMethod);
        });
    }

    /**
     * CourseTime의 durationType에 따라 실제 종료일 계산
     * - FIXED: classEndDate 그대로 반환
     * - RELATIVE: classStartDate + durationDays 계산
     * - UNLIMITED: null 반환
     */
    private java.time.LocalDate calculateActualEndDate(com.mzc.lp.domain.ts.entity.CourseTime courseTime) {
        if (courseTime == null) {
            return null;
        }

        return switch (courseTime.getDurationType()) {
            case FIXED -> courseTime.getClassEndDate();
            case RELATIVE -> {
                if (courseTime.getDurationDays() != null && courseTime.getClassStartDate() != null) {
                    yield courseTime.getClassStartDate().plusDays(courseTime.getDurationDays());
                }
                yield null;
            }
            case UNLIMITED -> null;
        };
    }
}
