package com.mzc.lp.domain.assignment.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.assignment.constant.GradingType;
import com.mzc.lp.domain.assignment.dto.request.CreateAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.request.GradeSubmissionRequest;
import com.mzc.lp.domain.assignment.dto.request.SubmitAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.request.UpdateAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.response.AssignmentDetailResponse;
import com.mzc.lp.domain.assignment.dto.response.AssignmentResponse;
import com.mzc.lp.domain.assignment.dto.response.SubmissionResponse;
import com.mzc.lp.domain.assignment.entity.Assignment;
import com.mzc.lp.domain.assignment.entity.AssignmentSubmission;
import com.mzc.lp.domain.assignment.exception.AssignmentAlreadySubmittedException;
import com.mzc.lp.domain.assignment.exception.AssignmentDeadlinePassedException;
import com.mzc.lp.domain.assignment.exception.AssignmentNotFoundException;
import com.mzc.lp.domain.assignment.exception.SubmissionNotFoundException;
import com.mzc.lp.domain.assignment.repository.AssignmentRepository;
import com.mzc.lp.domain.assignment.repository.AssignmentSubmissionRepository;
import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.service.NotificationService;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ===== 관리자/강사용 API =====

    @Override
    @Transactional
    public AssignmentResponse createAssignment(Long courseTimeId, CreateAssignmentRequest request, Long createdBy) {
        log.info("Creating assignment for courseTime: {}, createdBy: {}", courseTimeId, createdBy);

        Assignment assignment = Assignment.create(
                courseTimeId,
                request.title(),
                request.description(),
                request.gradingType(),
                request.maxScore(),
                request.passingScore(),
                request.dueDate(),
                createdBy
        );

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment created with id: {}", savedAssignment.getId());

        return AssignmentResponse.from(savedAssignment);
    }

    @Override
    public Page<AssignmentResponse> getAssignments(Long courseTimeId, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return assignmentRepository.findByCourseTimeIdAndTenantIdOrderByCreatedAtDesc(courseTimeId, tenantId, pageable)
                .map(AssignmentResponse::from);
    }

    @Override
    public AssignmentDetailResponse getAssignmentDetail(Long assignmentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        // 통계 정보 조회
        long totalSubmissions = submissionRepository.countByAssignmentIdAndTenantId(assignmentId, tenantId);
        long gradedSubmissions = submissionRepository.countGradedByAssignmentId(assignmentId, tenantId);
        Double averageScore = submissionRepository.findAverageScoreByAssignmentId(assignmentId, tenantId);

        return AssignmentDetailResponse.from(assignment, totalSubmissions, gradedSubmissions, averageScore);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        assignment.update(
                request.title(),
                request.description(),
                request.gradingType(),
                request.maxScore(),
                request.passingScore(),
                request.dueDate()
        );

        log.info("Assignment updated: {}", assignmentId);
        return AssignmentResponse.from(assignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        assignmentRepository.delete(assignment);
        log.info("Assignment deleted: {}", assignmentId);
    }

    @Override
    @Transactional
    public AssignmentResponse publishAssignment(Long assignmentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        assignment.publish();
        log.info("Assignment published: {}", assignmentId);

        // ASSIGNMENT 알림 발송: 수강생 전체에게 알림
        sendAssignmentPublishedNotifications(assignment);

        return AssignmentResponse.from(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse closeAssignment(Long assignmentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        assignment.close();
        log.info("Assignment closed: {}", assignmentId);

        return AssignmentResponse.from(assignment);
    }

    @Override
    public Page<SubmissionResponse> getSubmissions(Long assignmentId, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();

        Page<AssignmentSubmission> submissions = submissionRepository
                .findByAssignmentIdAndTenantIdOrderBySubmittedAtDesc(assignmentId, tenantId, pageable);

        return mapSubmissionsWithUserInfo(submissions);
    }

    @Override
    @Transactional
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long gradedBy) {
        Long tenantId = TenantContext.getCurrentTenantId();
        AssignmentSubmission submission = findSubmissionById(submissionId, tenantId);
        Assignment assignment = findAssignmentById(submission.getAssignmentId(), tenantId);

        if (Boolean.TRUE.equals(request.returnSubmission())) {
            // 반려 처리
            submission.returnSubmission(request.feedback(), gradedBy);
            log.info("Submission returned: {}", submissionId);
        } else {
            // 채점 처리
            if (assignment.getGradingType() == GradingType.SCORE) {
                submission.gradeWithScore(request.score(), request.feedback(), gradedBy);
            } else {
                submission.gradeWithPassFail(request.grade(), request.feedback(), gradedBy);
            }
            log.info("Submission graded: {}", submissionId);

            // ASSIGNMENT 알림 발송: 채점 완료 알림
            sendGradedNotification(submission, assignment);
        }

        return SubmissionResponse.from(submission);
    }

    // ===== 학생용 API =====

    @Override
    public Page<AssignmentResponse> getPublishedAssignments(Long courseTimeId, Long studentId, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return assignmentRepository.findPublishedByCourseTimeId(courseTimeId, tenantId, pageable)
                .map(AssignmentResponse::from);
    }

    @Override
    public AssignmentDetailResponse getAssignmentForStudent(Long assignmentId, Long studentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        // 학생은 발행된 과제만 볼 수 있음
        if (assignment.isDraft()) {
            throw new AssignmentNotFoundException(assignmentId);
        }

        return AssignmentDetailResponse.from(assignment);
    }

    @Override
    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request, Long studentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Assignment assignment = findAssignmentById(assignmentId, tenantId);

        // 제출 가능 여부 확인
        if (!assignment.isSubmittable()) {
            if (assignment.isOverdue()) {
                throw new AssignmentDeadlinePassedException(assignmentId);
            }
            throw new IllegalStateException("과제 제출이 불가능한 상태입니다.");
        }

        // 기존 제출물 확인
        var existingSubmission = submissionRepository
                .findByAssignmentIdAndStudentIdAndTenantId(assignmentId, studentId, tenantId);

        if (existingSubmission.isPresent()) {
            AssignmentSubmission submission = existingSubmission.get();
            if (submission.canResubmit()) {
                // 재제출
                submission.resubmit(request.textContent(), request.fileUrl(), request.fileName());
                log.info("Assignment resubmitted: assignmentId={}, studentId={}", assignmentId, studentId);
                return SubmissionResponse.from(submission);
            } else {
                throw new AssignmentAlreadySubmittedException(assignmentId, studentId);
            }
        }

        // 신규 제출
        AssignmentSubmission submission = AssignmentSubmission.create(
                assignmentId,
                studentId,
                request.textContent(),
                request.fileUrl(),
                request.fileName()
        );

        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        log.info("Assignment submitted: assignmentId={}, studentId={}, submissionId={}",
                assignmentId, studentId, savedSubmission.getId());

        return SubmissionResponse.from(savedSubmission);
    }

    @Override
    public SubmissionResponse getMySubmission(Long assignmentId, Long studentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return submissionRepository.findByAssignmentIdAndStudentIdAndTenantId(assignmentId, studentId, tenantId)
                .map(SubmissionResponse::from)
                .orElse(null);
    }

    // ===== Private Helper Methods =====

    private Assignment findAssignmentById(Long assignmentId, Long tenantId) {
        return assignmentRepository.findByIdAndTenantId(assignmentId, tenantId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
    }

    private AssignmentSubmission findSubmissionById(Long submissionId, Long tenantId) {
        return submissionRepository.findByIdAndTenantId(submissionId, tenantId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
    }

    private Page<SubmissionResponse> mapSubmissionsWithUserInfo(Page<AssignmentSubmission> submissions) {
        if (submissions.isEmpty()) {
            return submissions.map(SubmissionResponse::from);
        }

        List<Long> studentIds = submissions.getContent().stream()
                .map(AssignmentSubmission::getStudentId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return submissions.map(submission -> {
            User user = userMap.get(submission.getStudentId());
            if (user != null) {
                return SubmissionResponse.from(submission, user.getName(), user.getEmail());
            }
            return SubmissionResponse.from(submission);
        });
    }

    /**
     * 과제 발행 알림 발송 (ASSIGNMENT 타입)
     */
    private void sendAssignmentPublishedNotifications(Assignment assignment) {
        try {
            List<Long> userIds = enrollmentRepository.findUserIdsByCourseTimeId(assignment.getCourseTimeId());
            log.info("Sending assignment notification to {} users", userIds.size());

            String title = "새 과제 등록";
            String message = String.format("[%s] 과제가 등록되었습니다.", assignment.getTitle());
            String link = "/assignments/" + assignment.getId();

            for (Long userId : userIds) {
                try {
                    notificationService.createNotification(
                            userId,
                            NotificationType.ASSIGNMENT,
                            title,
                            message,
                            link,
                            assignment.getId(),
                            "ASSIGNMENT",
                            null,
                            null
                    );
                } catch (Exception e) {
                    log.warn("Failed to send assignment notification to user {}: {}", userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send assignment notifications: {}", e.getMessage());
        }
    }

    /**
     * 채점 완료 알림 발송 (ASSIGNMENT 타입)
     */
    private void sendGradedNotification(AssignmentSubmission submission, Assignment assignment) {
        try {
            String title = "과제 채점 완료";
            String message = String.format("[%s] 과제 채점이 완료되었습니다.", assignment.getTitle());
            String link = "/assignments/" + assignment.getId() + "/my-submission";

            notificationService.createNotification(
                    submission.getStudentId(),
                    NotificationType.ASSIGNMENT,
                    title,
                    message,
                    link,
                    submission.getId(),
                    "SUBMISSION",
                    null,
                    null
            );
            log.debug("Graded notification sent to student: {}", submission.getStudentId());
        } catch (Exception e) {
            log.warn("Failed to send graded notification to student {}: {}", submission.getStudentId(), e.getMessage());
        }
    }
}
