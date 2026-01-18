package com.mzc.lp.domain.iis.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.AssignmentHistoryResponse;
import com.mzc.lp.domain.iis.dto.response.CourseTimeStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentListResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.dto.response.ConflictingAssignmentInfo;
import com.mzc.lp.domain.iis.dto.response.InstructorAvailabilityResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorDetailStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatisticsResponse;
import com.mzc.lp.domain.iis.dto.response.ScheduleConflictResponse;
import com.mzc.lp.domain.iis.entity.AssignmentHistory;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.exception.CannotModifyInactiveAssignmentException;
import com.mzc.lp.domain.iis.exception.InstructorAlreadyAssignedException;
import com.mzc.lp.domain.iis.exception.InstructorAssignmentNotFoundException;
import com.mzc.lp.domain.iis.exception.InstructorScheduleConflictException;
import com.mzc.lp.domain.iis.exception.MainInstructorAlreadyExistsException;
import com.mzc.lp.domain.iis.exception.UnauthorizedAssignmentAccessException;
import com.mzc.lp.domain.iis.repository.AssignmentHistoryRepository;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.student.dto.response.CourseTimeEnrollmentStatsResponse;
import com.mzc.lp.domain.student.service.EnrollmentStatsService;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.entity.RecurringSchedule;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorAssignmentServiceImpl implements InstructorAssignmentService {

    private final InstructorAssignmentRepository assignmentRepository;
    private final AssignmentHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final EnrollmentStatsService enrollmentStatsService;

    @Override
    @Transactional
    public InstructorAssignmentResponse assignInstructor(Long timeId, AssignInstructorRequest request, Long operatorId) {
        log.info("Assigning instructor: timeId={}, userId={}, role={}, forceAssign={}",
                timeId, request.userId(), request.role(), request.forceAssign());

        Long tenantId = TenantContext.getCurrentTenantId();

        // [Race Condition 방지] CourseTime을 락 대상으로 사용하여 동시 배정 직렬화
        CourseTime targetCourseTime = courseTimeRepository.findByIdWithLock(timeId)
                .orElseThrow(() -> new IllegalArgumentException("CourseTime not found: " + timeId));

        // 중복 배정 체크 (락 상태에서)
        if (assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                timeId, request.userId(), tenantId, AssignmentStatus.ACTIVE)) {
            throw new InstructorAlreadyAssignedException(request.userId(), timeId);
        }

        // MAIN 역할인 경우 기존 MAIN 강사 체크 (락 상태에서)
        if (request.role() == InstructorRole.MAIN) {
            assignmentRepository.findActiveByTimeKeyAndRole(timeId, tenantId, InstructorRole.MAIN)
                    .ifPresent(existing -> {
                        throw new MainInstructorAlreadyExistsException(timeId);
                    });
        }

        // 일정 충돌 검사 (forceAssign이 false인 경우에만)
        if (!Boolean.TRUE.equals(request.forceAssign())) {
            checkScheduleConflict(request.userId(), timeId, targetCourseTime, tenantId);
        }

        // 배정 생성
        InstructorAssignment assignment = InstructorAssignment.create(
                request.userId(),
                timeId,
                request.role(),
                operatorId
        );

        InstructorAssignment saved = assignmentRepository.save(assignment);

        // 이력 저장
        historyRepository.save(AssignmentHistory.ofAssign(saved.getId(), request.role(), operatorId));

        log.info("Instructor assigned: assignmentId={}", saved.getId());

        // User 정보 조회
        User user = userRepository.findById(request.userId()).orElse(null);
        return InstructorAssignmentResponse.from(saved, user);
    }

    @Override
    public List<InstructorAssignmentResponse> getInstructorsByTimeId(Long timeId, AssignmentStatus status) {
        log.debug("Getting instructors by timeId: timeId={}, status={}", timeId, status);

        Long tenantId = TenantContext.getCurrentTenantId();

        List<InstructorAssignment> assignments;
        if (status != null) {
            assignments = assignmentRepository.findByTimeKeyAndTenantIdAndStatus(timeId, tenantId, status);
        } else {
            assignments = assignmentRepository.findByTimeKeyAndTenantId(timeId, tenantId);
        }

        // N+1 방지: User 벌크 조회
        Map<Long, User> userMap = getUserMapByAssignments(assignments);

        return assignments.stream()
                .map(a -> InstructorAssignmentResponse.from(a, userMap.get(a.getUserKey())))
                .toList();
    }

    @Override
    public InstructorAssignmentResponse getAssignment(Long id) {
        log.debug("Getting assignment: id={}", id);

        InstructorAssignment assignment = findAssignmentById(id);
        User user = userRepository.findById(assignment.getUserKey()).orElse(null);
        return InstructorAssignmentResponse.from(assignment, user);
    }

    @Override
    public Page<InstructorAssignmentListResponse> getAssignments(
            Long instructorId,
            Long courseTimeId,
            InstructorRole role,
            AssignmentStatus status,
            Pageable pageable
    ) {
        log.debug("Getting assignments: instructorId={}, courseTimeId={}, role={}, status={}",
                instructorId, courseTimeId, role, status);

        Long tenantId = TenantContext.getCurrentTenantId();

        // 동적 쿼리로 배정 목록 조회
        Page<InstructorAssignment> assignmentPage = assignmentRepository.searchAssignments(
                tenantId, instructorId, courseTimeId, role, status, pageable);

        List<InstructorAssignment> assignments = assignmentPage.getContent();

        if (assignments.isEmpty()) {
            return assignmentPage.map(a -> null);
        }

        // N+1 방지: User 벌크 조회
        Map<Long, User> userMap = getUserMapByAssignments(assignments);

        // N+1 방지: CourseTime 벌크 조회
        List<Long> timeKeys = assignments.stream()
                .map(InstructorAssignment::getTimeKey)
                .distinct()
                .toList();
        Map<Long, CourseTime> courseTimeMap = courseTimeRepository.findAllById(timeKeys).stream()
                .collect(Collectors.toMap(CourseTime::getId, Function.identity()));

        return assignmentPage.map(assignment -> {
            User user = userMap.get(assignment.getUserKey());
            CourseTime courseTime = courseTimeMap.get(assignment.getTimeKey());
            Course course = courseTime != null ? courseTime.getCourse() : null;

            return InstructorAssignmentListResponse.from(assignment, user, courseTime, course);
        });
    }

    @Override
    public Page<InstructorAssignmentResponse> getAssignmentsByUserId(Long userId, AssignmentStatus status, Pageable pageable) {
        log.debug("Getting assignments by userId: userId={}, status={}", userId, status);

        Long tenantId = TenantContext.getCurrentTenantId();
        User user = userRepository.findById(userId).orElse(null);

        if (status != null) {
            return assignmentRepository.findByUserKeyAndTenantIdAndStatus(userId, tenantId, status, pageable)
                    .map(a -> InstructorAssignmentResponse.from(a, user));
        }

        return assignmentRepository.findByUserKeyAndTenantId(userId, tenantId, pageable)
                .map(a -> InstructorAssignmentResponse.from(a, user));
    }

    @Override
    public List<InstructorAssignmentResponse> getMyAssignments(Long userId) {
        log.debug("Getting my assignments: userId={}", userId);

        Long tenantId = TenantContext.getCurrentTenantId();
        User user = userRepository.findById(userId).orElse(null);

        return assignmentRepository.findByUserKeyAndTenantIdAndStatus(userId, tenantId, AssignmentStatus.ACTIVE,
                        Pageable.unpaged())
                .map(a -> InstructorAssignmentResponse.from(a, user))
                .toList();
    }

    @Override
    @Transactional
    public InstructorAssignmentResponse updateRole(Long id, UpdateRoleRequest request, Long operatorId) {
        log.info("Updating role: id={}, newRole={}", id, request.role());

        InstructorAssignment assignment = findAssignmentById(id);

        // ACTIVE 상태 체크
        validateActiveStatus(assignment);

        InstructorRole oldRole = assignment.getRole();
        Long tenantId = TenantContext.getCurrentTenantId();

        // [Race Condition 방지] MAIN으로 변경 시 CourseTime 락
        if (request.role() == InstructorRole.MAIN && oldRole != InstructorRole.MAIN) {
            courseTimeRepository.findByIdWithLock(assignment.getTimeKey())
                    .orElseThrow(() -> new IllegalArgumentException("CourseTime not found: " + assignment.getTimeKey()));

            assignmentRepository.findActiveByTimeKeyAndRole(assignment.getTimeKey(), tenantId, InstructorRole.MAIN)
                    .ifPresent(existing -> {
                        throw new MainInstructorAlreadyExistsException(assignment.getTimeKey());
                    });
        }

        // 역할 변경
        assignment.updateRole(request.role());

        // 이력 저장
        historyRepository.save(AssignmentHistory.ofRoleChange(
                id, oldRole, request.role(), request.reason(), operatorId));

        log.info("Role updated: id={}, oldRole={}, newRole={}", id, oldRole, request.role());

        User user = userRepository.findById(assignment.getUserKey()).orElse(null);
        return InstructorAssignmentResponse.from(assignment, user);
    }

    @Override
    @Transactional
    public InstructorAssignmentResponse replaceInstructor(Long id, ReplaceInstructorRequest request, Long operatorId) {
        log.info("Replacing instructor: id={}, newUserId={}", id, request.newUserId());

        InstructorAssignment oldAssignment = findAssignmentById(id);

        // ACTIVE 상태 체크
        validateActiveStatus(oldAssignment);

        Long tenantId = TenantContext.getCurrentTenantId();
        Long timeId = oldAssignment.getTimeKey();

        // [Race Condition 방지] CourseTime 락으로 동시 교체 직렬화
        courseTimeRepository.findByIdWithLock(timeId)
                .orElseThrow(() -> new IllegalArgumentException("CourseTime not found: " + timeId));

        // 새 강사 중복 배정 체크 (락 상태에서)
        if (assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                timeId, request.newUserId(), tenantId, AssignmentStatus.ACTIVE)) {
            throw new InstructorAlreadyAssignedException(request.newUserId(), timeId);
        }

        // 기존 배정 교체 처리
        oldAssignment.replace();
        historyRepository.save(AssignmentHistory.ofReplace(id, request.reason(), operatorId));

        // 새 배정 생성
        InstructorAssignment newAssignment = InstructorAssignment.create(
                request.newUserId(),
                timeId,
                request.role(),
                operatorId
        );
        InstructorAssignment saved = assignmentRepository.save(newAssignment);
        historyRepository.save(AssignmentHistory.ofAssign(saved.getId(), request.role(), operatorId));

        log.info("Instructor replaced: oldId={}, newId={}", id, saved.getId());

        User user = userRepository.findById(request.newUserId()).orElse(null);
        return InstructorAssignmentResponse.from(saved, user);
    }

    @Override
    @Transactional
    public void cancelAssignment(Long id, CancelAssignmentRequest request, Long operatorId, boolean isTenantAdmin) {
        log.info("Cancelling assignment: id={}, operatorId={}, isTenantAdmin={}", id, operatorId, isTenantAdmin);

        InstructorAssignment assignment = findAssignmentById(id);

        // ACTIVE 상태 체크
        validateActiveStatus(assignment);

        // 소유권 검증: 본인이 배정한 건 또는 TENANT_ADMIN만 취소 가능
        if (!isTenantAdmin && !operatorId.equals(assignment.getAssignedBy())) {
            throw new UnauthorizedAssignmentAccessException("본인이 배정한 강사만 취소할 수 있습니다");
        }

        // 취소 처리
        assignment.cancel();

        // 이력 저장
        historyRepository.save(AssignmentHistory.ofCancel(id, request.reason(), operatorId));

        log.info("Assignment cancelled: id={}", id);
    }

    // ========== TS 모듈 연동용 메서드 ==========

    @Override
    public boolean existsMainInstructor(Long timeId) {
        log.debug("Checking main instructor exists: timeId={}", timeId);

        Long tenantId = TenantContext.getCurrentTenantId();
        return assignmentRepository.existsActiveMainInstructor(timeId, tenantId);
    }

    @Override
    public Map<Long, List<InstructorAssignmentResponse>> getInstructorsByTimeIds(List<Long> timeIds) {
        log.debug("Getting instructors by timeIds: count={}", timeIds.size());

        if (timeIds == null || timeIds.isEmpty()) {
            return Map.of();
        }

        Long tenantId = TenantContext.getCurrentTenantId();

        List<InstructorAssignment> assignments = assignmentRepository.findActiveByTimeKeyIn(timeIds, tenantId);

        // N+1 방지: User 벌크 조회
        Map<Long, User> userMap = getUserMapByAssignments(assignments);

        return assignments.stream()
                .collect(Collectors.groupingBy(
                        InstructorAssignment::getTimeKey,
                        Collectors.mapping(
                                a -> InstructorAssignmentResponse.from(a, userMap.get(a.getUserKey())),
                                Collectors.toList()
                        )
                ));
    }

    // ========== 이력 조회 ==========

    @Override
    public List<AssignmentHistoryResponse> getAssignmentHistories(Long assignmentId, AssignmentAction action) {
        log.debug("Getting assignment histories: assignmentId={}, action={}", assignmentId, action);

        // 배정 존재 여부 확인
        findAssignmentById(assignmentId);

        List<AssignmentHistory> histories;
        if (action != null) {
            histories = historyRepository.findByAssignmentIdAndActionOrderByChangedAtDesc(assignmentId, action);
        } else {
            histories = historyRepository.findByAssignmentIdOrderByChangedAtDesc(assignmentId);
        }

        return histories.stream()
                .map(AssignmentHistoryResponse::from)
                .toList();
    }

    // ========== 통계 API ==========

    @Override
    public InstructorStatisticsResponse getStatistics() {
        log.debug("Getting instructor assignment statistics");

        Long tenantId = TenantContext.getCurrentTenantId();

        // 전체/활성 배정 건수
        long totalAssignments = assignmentRepository.countByTenantId(tenantId);
        long activeAssignments = assignmentRepository.countByTenantIdAndStatus(tenantId, AssignmentStatus.ACTIVE);

        // 역할별 집계
        Map<InstructorRole, Long> byRole = new EnumMap<>(InstructorRole.class);
        for (InstructorRole role : InstructorRole.values()) {
            byRole.put(role, 0L);
        }
        assignmentRepository.countGroupByRole(tenantId).forEach(row -> {
            InstructorRole role = (InstructorRole) row[0];
            Long count = (Long) row[1];
            byRole.put(role, count);
        });

        // 상태별 집계
        Map<AssignmentStatus, Long> byStatus = new EnumMap<>(AssignmentStatus.class);
        for (AssignmentStatus status : AssignmentStatus.values()) {
            byStatus.put(status, 0L);
        }
        assignmentRepository.countGroupByStatus(tenantId).forEach(row -> {
            AssignmentStatus status = (AssignmentStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status, count);
        });

        // 강사별 통계
        List<Object[]> rawStats = assignmentRepository.getInstructorStatistics(tenantId);
        List<Long> userIds = rawStats.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<InstructorStatResponse> instructorStats = new ArrayList<>();
        for (Object[] row : rawStats) {
            // Number 타입으로 캐스팅하여 DB별 타입 차이 대응
            Long userId = ((Number) row[0]).longValue();
            Long totalCount = ((Number) row[1]).longValue();
            Long mainCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long subCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;

            User user = userMap.get(userId);
            String userName = user != null ? user.getName() : null;

            instructorStats.add(InstructorStatResponse.of(userId, userName, totalCount, mainCount, subCount));
        }

        return InstructorStatisticsResponse.of(totalAssignments, activeAssignments, byRole, byStatus, instructorStats);
    }

    @Override
    public InstructorStatResponse getInstructorStatistics(Long userId) {
        log.debug("Getting instructor statistics: userId={}", userId);

        Long tenantId = TenantContext.getCurrentTenantId();

        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getName() : null;

        List<Object[]> result = assignmentRepository.getInstructorStatisticsByUserId(tenantId, userId);

        if (result == null || result.isEmpty() || result.get(0)[0] == null) {
            return InstructorStatResponse.of(userId, userName, 0L, 0L, 0L);
        }

        Object[] stats = result.get(0);

        // Number 타입으로 캐스팅하여 DB별 타입 차이 대응 (Long, Integer 등)
        // SUM은 결과가 없으면 null을 반환하므로 null 체크 필요
        Long totalCount = ((Number) stats[0]).longValue();
        Long mainCount = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
        Long subCount = stats[2] != null ? ((Number) stats[2]).longValue() : 0L;

        return InstructorStatResponse.of(userId, userName, totalCount, mainCount, subCount);
    }

    @Override
    public InstructorStatisticsResponse getStatistics(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting instructor assignment statistics with date range: {} ~ {}", startDate, endDate);

        // 기간이 지정되지 않은 경우 기본 메서드 호출
        if (startDate == null || endDate == null) {
            return getStatistics();
        }

        Long tenantId = TenantContext.getCurrentTenantId();

        // 전체/활성 배정 건수 (기간 필터)
        long totalAssignments = assignmentRepository.countByTenantIdAndDateRange(tenantId, startDate, endDate);
        long activeAssignments = assignmentRepository.countByTenantIdAndStatusAndDateRange(
                tenantId, AssignmentStatus.ACTIVE, startDate, endDate);

        // 역할별 집계 (기간 필터)
        Map<InstructorRole, Long> byRole = new EnumMap<>(InstructorRole.class);
        for (InstructorRole role : InstructorRole.values()) {
            byRole.put(role, 0L);
        }
        assignmentRepository.countGroupByRoleAndDateRange(tenantId, startDate, endDate).forEach(row -> {
            InstructorRole role = (InstructorRole) row[0];
            Long count = (Long) row[1];
            byRole.put(role, count);
        });

        // 상태별 집계 (기간 필터)
        Map<AssignmentStatus, Long> byStatus = new EnumMap<>(AssignmentStatus.class);
        for (AssignmentStatus status : AssignmentStatus.values()) {
            byStatus.put(status, 0L);
        }
        assignmentRepository.countGroupByStatusAndDateRange(tenantId, startDate, endDate).forEach(row -> {
            AssignmentStatus status = (AssignmentStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status, count);
        });

        // 강사별 통계 (기간 필터)
        List<Object[]> rawStats = assignmentRepository.getInstructorStatisticsWithDateRange(tenantId, startDate, endDate);
        List<Long> userIds = rawStats.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<InstructorStatResponse> instructorStats = new ArrayList<>();
        for (Object[] row : rawStats) {
            Long userId = ((Number) row[0]).longValue();
            Long totalCount = ((Number) row[1]).longValue();
            Long mainCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long subCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;

            User user = userMap.get(userId);
            String userName = user != null ? user.getName() : null;

            instructorStats.add(InstructorStatResponse.of(userId, userName, totalCount, mainCount, subCount));
        }

        return InstructorStatisticsResponse.of(totalAssignments, activeAssignments, byRole, byStatus, instructorStats);
    }

    @Override
    public InstructorDetailStatResponse getInstructorDetailStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting instructor detail statistics: userId={}, dateRange={} ~ {}", userId, startDate, endDate);

        Long tenantId = TenantContext.getCurrentTenantId();

        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getName() : null;

        // 기본 통계 조회
        List<Object[]> result;
        if (startDate != null && endDate != null) {
            result = assignmentRepository.getInstructorStatisticsByUserIdAndDateRange(tenantId, userId, startDate, endDate);
        } else {
            result = assignmentRepository.getInstructorStatisticsByUserId(tenantId, userId);
        }

        Long totalCount = 0L;
        Long mainCount = 0L;
        Long subCount = 0L;

        if (result != null && !result.isEmpty() && result.get(0)[0] != null) {
            Object[] stats = result.get(0);
            totalCount = ((Number) stats[0]).longValue();
            mainCount = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
            subCount = stats[2] != null ? ((Number) stats[2]).longValue() : 0L;
        }

        // 차수별 통계 조회
        List<InstructorAssignment> assignments;
        if (startDate != null && endDate != null) {
            assignments = assignmentRepository.findActiveByUserKeyAndDateRange(tenantId, userId, startDate, endDate);
        } else {
            assignments = assignmentRepository.findActiveByUserKey(tenantId, userId);
        }

        List<CourseTimeStatResponse> courseTimeStats = buildCourseTimeStats(assignments);

        return InstructorDetailStatResponse.of(userId, userName, totalCount, mainCount, subCount, courseTimeStats);
    }

    // ========== Private Methods ==========

    /**
     * 차수별 통계 빌드 (수강생 통계 포함)
     */
    private List<CourseTimeStatResponse> buildCourseTimeStats(List<InstructorAssignment> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        // CourseTime 정보 조회
        List<Long> timeKeys = assignments.stream()
                .map(InstructorAssignment::getTimeKey)
                .distinct()
                .toList();

        Map<Long, CourseTime> courseTimeMap = courseTimeRepository.findAllById(timeKeys).stream()
                .collect(Collectors.toMap(CourseTime::getId, Function.identity()));

        List<CourseTimeStatResponse> result = new ArrayList<>();

        for (InstructorAssignment assignment : assignments) {
            CourseTime courseTime = courseTimeMap.get(assignment.getTimeKey());
            if (courseTime == null) {
                continue;
            }

            // 수강생 통계 조회 (SIS 연동)
            Long totalStudents = null;
            Long completedStudents = null;
            BigDecimal completionRate = null;

            try {
                CourseTimeEnrollmentStatsResponse enrollmentStats =
                        enrollmentStatsService.getCourseTimeStats(courseTime.getId());
                if (enrollmentStats != null) {
                    totalStudents = enrollmentStats.totalEnrollments();
                    completedStudents = enrollmentStats.completedCount();
                    completionRate = enrollmentStats.completionRate();
                }
            } catch (Exception e) {
                log.warn("Failed to get enrollment stats for courseTime: {}", courseTime.getId(), e);
            }

            result.add(CourseTimeStatResponse.of(
                    assignment.getTimeKey(),
                    courseTime.getTitle(),
                    courseTime.getTitle(), // timeName은 title로 대체
                    assignment.getRole(),
                    totalStudents,
                    completedStudents,
                    completionRate
            ));
        }

        return result;
    }

    private InstructorAssignment findAssignmentById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return assignmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new InstructorAssignmentNotFoundException(id));
    }

    private void validateActiveStatus(InstructorAssignment assignment) {
        if (!assignment.isActive()) {
            throw new CannotModifyInactiveAssignmentException(assignment.getId());
        }
    }

    /**
     * N+1 방지를 위한 User 벌크 조회 헬퍼 메서드
     */
    private Map<Long, User> getUserMapByAssignments(List<InstructorAssignment> assignments) {
        if (assignments.isEmpty()) {
            return Map.of();
        }

        List<Long> userIds = assignments.stream()
                .map(InstructorAssignment::getUserKey)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    /**
     * 일정 충돌 검사
     * 1. 강사가 동일 기간에 다른 차수에 이미 배정되어 있는지 확인
     * 2. 정기 일정(요일/시간)이 겹치는지 확인
     */
    private void checkScheduleConflict(Long userId, Long targetTimeId, CourseTime targetCourseTime, Long tenantId) {
        // 강사의 기존 ACTIVE 배정 목록 조회
        List<InstructorAssignment> existingAssignments = assignmentRepository.findActiveByUserKey(tenantId, userId);

        if (existingAssignments.isEmpty()) {
            return;
        }

        // 현재 배정하려는 차수를 제외한 기존 배정의 timeKey 목록
        List<Long> existingTimeIds = existingAssignments.stream()
                .map(InstructorAssignment::getTimeKey)
                .filter(timeKey -> !timeKey.equals(targetTimeId))
                .distinct()
                .toList();

        if (existingTimeIds.isEmpty()) {
            return;
        }

        // 기간이 겹치는 CourseTime 조회
        List<CourseTime> conflictingCourseTimes = courseTimeRepository.findByIdInAndDateRangeOverlap(
                existingTimeIds,
                targetCourseTime.getClassStartDate(),
                targetCourseTime.getClassEndDate()
        );

        if (conflictingCourseTimes.isEmpty()) {
            return;
        }

        // 정기 일정이 있는 경우 요일/시간 충돌도 확인
        RecurringSchedule targetSchedule = targetCourseTime.getRecurringSchedule();
        List<CourseTime> actualConflicts;

        if (targetSchedule != null) {
            // 정기 일정이 있으면 요일/시간까지 세밀하게 체크
            actualConflicts = conflictingCourseTimes.stream()
                    .filter(ct -> hasRecurringScheduleConflict(targetSchedule, ct.getRecurringSchedule()))
                    .toList();
        } else {
            // 정기 일정이 없으면 기간만 겹쳐도 충돌
            actualConflicts = conflictingCourseTimes;
        }

        if (!actualConflicts.isEmpty()) {
            List<ScheduleConflictResponse> conflicts = actualConflicts.stream()
                    .map(ct -> ScheduleConflictResponse.of(
                            ct.getId(),
                            ct.getTitle(),
                            ct.getClassStartDate(),
                            ct.getClassEndDate()
                    ))
                    .toList();

            log.warn("Schedule conflict detected: userId={}, targetTimeId={}, conflicts={}",
                    userId, targetTimeId, conflicts.size());

            throw new InstructorScheduleConflictException(userId, conflicts);
        }
    }

    /**
     * 두 정기 일정 간의 충돌 여부 확인
     * - 둘 다 정기 일정이 있으면 요일/시간 충돌 확인
     * - 둘 중 하나라도 정기 일정이 없으면 충돌로 간주 (기간이 겹치므로)
     */
    private boolean hasRecurringScheduleConflict(RecurringSchedule target, RecurringSchedule existing) {
        // 둘 다 정기 일정이 있으면 세밀하게 체크
        if (target != null && existing != null) {
            return target.hasTimeConflict(existing);
        }
        // 둘 중 하나라도 정기 일정이 없으면 기간 충돌만으로 충돌 판정
        return true;
    }

    // ========== 가용성 확인 API ==========

    @Override
    public InstructorAvailabilityResponse checkAvailability(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Checking availability: userId={}, dateRange={} ~ {}", userId, startDate, endDate);

        Long tenantId = TenantContext.getCurrentTenantId();

        // 강사의 기존 ACTIVE 배정 목록 조회
        List<InstructorAssignment> existingAssignments = assignmentRepository.findActiveByUserKey(tenantId, userId);

        if (existingAssignments.isEmpty()) {
            return InstructorAvailabilityResponse.available(userId);
        }

        // 기존 배정의 timeKey 목록
        List<Long> existingTimeIds = existingAssignments.stream()
                .map(InstructorAssignment::getTimeKey)
                .distinct()
                .toList();

        // 기간이 겹치는 CourseTime 조회
        List<CourseTime> conflictingCourseTimes = courseTimeRepository.findByIdInAndDateRangeOverlap(
                existingTimeIds,
                startDate,
                endDate
        );

        if (conflictingCourseTimes.isEmpty()) {
            return InstructorAvailabilityResponse.available(userId);
        }

        // CourseTime ID -> Assignment 매핑 (역할 정보 포함)
        Map<Long, InstructorAssignment> timeIdToAssignment = existingAssignments.stream()
                .collect(Collectors.toMap(
                        InstructorAssignment::getTimeKey,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<ConflictingAssignmentInfo> conflicts = conflictingCourseTimes.stream()
                .map(ct -> {
                    InstructorAssignment assignment = timeIdToAssignment.get(ct.getId());
                    return ConflictingAssignmentInfo.of(ct, assignment != null ? assignment.getRole() : null);
                })
                .toList();

        return InstructorAvailabilityResponse.unavailable(userId, conflicts);
    }

    @Override
    public List<InstructorAvailabilityResponse> checkAvailabilityBulk(List<Long> userIds, LocalDate startDate, LocalDate endDate) {
        log.debug("Checking availability bulk: userCount={}, dateRange={} ~ {}", userIds.size(), startDate, endDate);

        Long tenantId = TenantContext.getCurrentTenantId();

        // 모든 강사의 ACTIVE 배정을 한 번에 조회
        List<InstructorAssignment> allAssignments = assignmentRepository.findActiveByUserKeyIn(tenantId, userIds);

        // 강사별로 그룹핑
        Map<Long, List<InstructorAssignment>> assignmentsByUser = allAssignments.stream()
                .collect(Collectors.groupingBy(InstructorAssignment::getUserKey));

        // 모든 timeKey 수집 (중복 제거)
        List<Long> allTimeIds = allAssignments.stream()
                .map(InstructorAssignment::getTimeKey)
                .distinct()
                .toList();

        // 기간이 겹치는 CourseTime 한 번에 조회
        Map<Long, CourseTime> conflictingCourseTimeMap = Map.of();
        if (!allTimeIds.isEmpty()) {
            List<CourseTime> conflictingCourseTimes = courseTimeRepository.findByIdInAndDateRangeOverlap(
                    allTimeIds,
                    startDate,
                    endDate
            );
            conflictingCourseTimeMap = conflictingCourseTimes.stream()
                    .collect(Collectors.toMap(CourseTime::getId, Function.identity()));
        }

        // 각 강사별로 가용성 계산
        List<InstructorAvailabilityResponse> results = new ArrayList<>();
        Map<Long, CourseTime> finalConflictingMap = conflictingCourseTimeMap;

        for (Long userId : userIds) {
            List<InstructorAssignment> userAssignments = assignmentsByUser.getOrDefault(userId, List.of());

            if (userAssignments.isEmpty()) {
                results.add(InstructorAvailabilityResponse.available(userId));
                continue;
            }

            // 해당 강사의 충돌하는 배정 찾기
            List<ConflictingAssignmentInfo> conflicts = userAssignments.stream()
                    .filter(a -> finalConflictingMap.containsKey(a.getTimeKey()))
                    .map(a -> {
                        CourseTime ct = finalConflictingMap.get(a.getTimeKey());
                        return ConflictingAssignmentInfo.of(ct, a.getRole());
                    })
                    .toList();

            if (conflicts.isEmpty()) {
                results.add(InstructorAvailabilityResponse.available(userId));
            } else {
                results.add(InstructorAvailabilityResponse.unavailable(userId, conflicts));
            }
        }

        return results;
    }
}
