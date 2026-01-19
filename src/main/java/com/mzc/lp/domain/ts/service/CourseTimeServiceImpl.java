package com.mzc.lp.domain.ts.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.category.repository.CategoryRepository;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.exception.CourseNotRegisteredException;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.service.SnapshotService;
import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.dto.request.CloneCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.RecurringScheduleRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.*;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.entity.RecurringSchedule;
import com.mzc.lp.domain.ts.validator.CourseTimeConstraintValidator;
import com.mzc.lp.domain.ts.exception.CapacityExceededException;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.exception.InvalidDateRangeException;
import com.mzc.lp.domain.ts.exception.InvalidStatusTransitionException;
import com.mzc.lp.domain.ts.exception.LocationRequiredException;
import com.mzc.lp.domain.ts.exception.MainInstructorRequiredException;
import com.mzc.lp.domain.ts.exception.UnauthorizedCourseTimeAccessException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseTimeServiceImpl implements CourseTimeService {

    private final CourseTimeRepository courseTimeRepository;
    private final InstructorAssignmentService instructorAssignmentService;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final CourseRepository courseRepository;
    private final CourseSnapshotRepository snapshotRepository;
    private final SnapshotService snapshotService;
    private final CourseTimeConstraintValidator constraintValidator;
    private final CategoryRepository categoryRepository;

    @Override
    public CourseTime getCourseTimeEntity(Long id) {
        return courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse createCourseTime(CreateCourseTimeRequest request, Long createdBy) {
        log.info("Creating course time from course: title={}, courseId={}", request.title(), request.courseId());

        // Course 조회 및 검증
        Course course = courseRepository.findByIdAndTenantId(request.courseId(), TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(request.courseId()));

        // REGISTERED 상태인 Course만 차수 생성 가능
        if (!course.canCreateCourseTime()) {
            throw new CourseNotRegisteredException(request.courseId(), course.getStatus().name());
        }

        // 제약 조건 검증
        CourseTimeValidationResult validationResult = constraintValidator.validate(request, course);
        if (!validationResult.valid()) {
            String errorMessages = validationResult.errors().stream()
                    .map(e -> e.ruleCode() + ": " + e.message().messageCode())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Validation failed");
            throw new IllegalArgumentException(errorMessages);
        }

        // 기존 날짜 검증 (일부 중복되지만 기존 호환성 유지)
        validateDateRange(request);

        // RecurringSchedule 변환
        RecurringSchedule recurringSchedule = toRecurringSchedule(request.recurringSchedule());

        // CourseTime 생성
        CourseTime courseTime = CourseTime.create(
                request.title(),
                request.description(),
                request.deliveryType(),
                request.durationType(),
                request.enrollStartDate(),
                request.enrollEndDate(),
                request.classStartDate(),
                request.classEndDate(),
                request.durationDays(),
                request.capacity(),
                request.maxWaitingCount(),
                request.enrollmentMethod(),
                request.minProgressForCompletion(),
                request.price(),
                request.isFree(),
                request.locationInfo(),
                request.allowLateEnrollment() != null ? request.allowLateEnrollment() : false,
                recurringSchedule,
                createdBy
        );

        // Snapshot 생성 (Course 딥카피)
        var snapshotDetail = snapshotService.createSnapshotFromCourse(request.courseId(), createdBy);
        CourseSnapshot snapshot = snapshotRepository.findById(snapshotDetail.snapshotId())
                .orElseThrow(() -> new IllegalStateException("Snapshot 생성 실패"));

        // Course와 Snapshot 연결
        courseTime.linkCourseAndSnapshot(course, snapshot);

        CourseTime savedCourseTime = courseTimeRepository.save(courseTime);
        log.info("Course time created: id={}, courseId={}, snapshotId={}, status={}, enrollStartDate={}, qualityRating={}",
                savedCourseTime.getId(), request.courseId(), snapshot.getId(),
                savedCourseTime.getStatus(), request.enrollStartDate(), validationResult.qualityRating());

        // DESIGNER를 MAIN 강사로 자동 배정
        List<InstructorAssignmentResponse> instructors = assignCourseDesignerAsMainInstructor(
                savedCourseTime, course, createdBy);

        return CourseTimeDetailResponse.from(savedCourseTime, instructors);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse cloneCourseTime(Long sourceId, CloneCourseTimeRequest request, Long createdBy) {
        log.info("Cloning course time: sourceId={}, newTitle={}", sourceId, request.title());

        // 원본 차수 조회
        CourseTime source = courseTimeRepository.findByIdAndTenantId(sourceId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(sourceId));

        // 날짜 검증
        validateDateRange(
                request.enrollStartDate(),
                request.enrollEndDate(),
                request.classStartDate(),
                request.classEndDate()
        );

        // 복제 생성
        CourseTime cloned = CourseTime.cloneFrom(
                source,
                request.title(),
                request.description(),
                request.enrollStartDate(),
                request.enrollEndDate(),
                request.classStartDate(),
                request.classEndDate(),
                createdBy
        );

        CourseTime savedCourseTime = courseTimeRepository.save(cloned);
        log.info("Course time cloned: sourceId={}, newId={}, status={}, enrollStartDate={}",
                sourceId, savedCourseTime.getId(), savedCourseTime.getStatus(), request.enrollStartDate());

        return CourseTimeDetailResponse.from(savedCourseTime);
    }

    @Override
    public Page<CourseTimeResponse> getCourseTimes(CourseTimeStatus status, Long courseId, Pageable pageable) {
        log.debug("Getting course times: status={}, courseId={}", status, courseId);

        Long tenantId = TenantContext.getCurrentTenantId();
        Page<CourseTime> courseTimePage;

        if (courseId != null && status != null) {
            courseTimePage = courseTimeRepository.findByCourseIdAndTenantIdAndStatus(
                    courseId, tenantId, status, pageable);
        } else if (courseId != null) {
            courseTimePage = courseTimeRepository.findByCourseIdAndTenantId(
                    courseId, tenantId, pageable);
        } else if (status != null) {
            courseTimePage = courseTimeRepository.findByTenantIdAndStatus(
                    tenantId, status, pageable);
        } else {
            courseTimePage = courseTimeRepository.findByTenantId(tenantId, pageable);
        }

        List<Long> timeIds = courseTimePage.getContent().stream()
                .map(CourseTime::getId)
                .toList();

        Map<Long, List<InstructorAssignmentResponse>> instructorMap =
                instructorAssignmentService.getInstructorsByTimeIds(timeIds);

        return courseTimePage.map(ct ->
                CourseTimeResponse.from(ct, instructorMap.getOrDefault(ct.getId(), List.of()))
        );
    }

    @Override
    public CourseTimeDetailResponse getCourseTime(Long id) {
        log.debug("Getting course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        List<InstructorAssignmentResponse> instructors =
                instructorAssignmentService.getInstructorsByTimeId(id, AssignmentStatus.ACTIVE);

        // 카테고리명 조회
        String categoryName = null;
        if (courseTime.getCourse() != null && courseTime.getCourse().getCategoryId() != null) {
            categoryName = categoryRepository.findById(courseTime.getCourse().getCategoryId())
                    .map(Category::getName)
                    .orElse(null);
        }

        return CourseTimeDetailResponse.from(courseTime, categoryName, instructors);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse updateCourseTime(Long id, UpdateCourseTimeRequest request) {
        log.info("Updating course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // DRAFT 또는 RECRUITING 상태에서만 수정 가능
        if (!courseTime.isDraft() && !courseTime.isRecruiting()) {
            throw new InvalidStatusTransitionException();
        }

        // 제약 조건 검증
        Course course = courseTime.getCourse();
        CourseTimeValidationResult validationResult = constraintValidator.validate(request, courseTime, course);
        if (!validationResult.valid()) {
            String errorMessages = validationResult.errors().stream()
                    .map(e -> e.ruleCode() + ": " + e.message().messageCode())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Validation failed");
            throw new IllegalArgumentException(errorMessages);
        }

        // 부분 업데이트
        if (request.title() != null) {
            courseTime.updateTitle(request.title());
        }

        if (request.description() != null) {
            courseTime.updateDescription(request.description());
        }

        if (request.durationType() != null || request.durationDays() != null) {
            courseTime.updateDurationType(
                    request.durationType() != null ? request.durationType() : courseTime.getDurationType(),
                    request.durationDays() != null ? request.durationDays() : courseTime.getDurationDays()
            );
        }

        if (request.enrollStartDate() != null || request.enrollEndDate() != null
                || request.classStartDate() != null || request.classEndDate() != null) {
            courseTime.updatePeriod(
                    request.enrollStartDate() != null ? request.enrollStartDate() : courseTime.getEnrollStartDate(),
                    request.enrollEndDate() != null ? request.enrollEndDate() : courseTime.getEnrollEndDate(),
                    request.classStartDate() != null ? request.classStartDate() : courseTime.getClassStartDate(),
                    request.classEndDate() != null ? request.classEndDate() : courseTime.getClassEndDate()
            );
        }

        if (request.capacity() != null || request.maxWaitingCount() != null) {
            courseTime.updateCapacity(
                    request.capacity() != null ? request.capacity() : courseTime.getCapacity(),
                    request.maxWaitingCount() != null ? request.maxWaitingCount() : courseTime.getMaxWaitingCount()
            );
        }

        if (request.price() != null || request.isFree() != null) {
            courseTime.updatePrice(
                    request.price() != null ? request.price() : courseTime.getPrice(),
                    request.isFree() != null ? request.isFree() : courseTime.isFree()
            );
        }

        if (request.locationInfo() != null) {
            courseTime.updateLocationInfo(request.locationInfo());
        }

        if (request.allowLateEnrollment() != null) {
            courseTime.updateAllowLateEnrollment(request.allowLateEnrollment());
        }

        if (request.minProgressForCompletion() != null) {
            courseTime.updateMinProgress(request.minProgressForCompletion());
        }

        // 정기 일정 업데이트 (null이면 변경 없음, 빈 요일 배열이면 삭제)
        if (request.recurringSchedule() != null) {
            if (request.recurringSchedule().daysOfWeek() == null || request.recurringSchedule().daysOfWeek().isEmpty()) {
                courseTime.clearRecurringSchedule();
            } else {
                RecurringSchedule schedule = toRecurringSchedule(request.recurringSchedule());
                courseTime.updateRecurringSchedule(schedule);
            }
        }

        log.info("Course time updated: id={}, qualityRating={}", id, validationResult.qualityRating());

        List<InstructorAssignmentResponse> instructors =
                instructorAssignmentService.getInstructorsByTimeId(id, AssignmentStatus.ACTIVE);

        return CourseTimeDetailResponse.from(courseTime, instructors);
    }

    @Override
    @Transactional
    public void deleteCourseTime(Long id, Long currentUserId, boolean isTenantAdmin) {
        log.info("Deleting course time: id={}, currentUserId={}, isTenantAdmin={}", id, currentUserId, isTenantAdmin);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // DRAFT 상태에서만 삭제 가능
        if (!courseTime.isDraft()) {
            throw new InvalidStatusTransitionException();
        }

        // 소유권 검증: 본인이 생성한 차수 또는 TENANT_ADMIN만 삭제 가능
        if (!isTenantAdmin && !currentUserId.equals(courseTime.getCreatedBy())) {
            throw new UnauthorizedCourseTimeAccessException("본인이 생성한 차수만 삭제할 수 있습니다");
        }

        courseTimeRepository.delete(courseTime);
        log.info("Course time deleted: id={}", id);
    }

    // ========== 상태 전이 ==========

    @Override
    @Transactional
    public CourseTimeDetailResponse openCourseTime(Long id) {
        log.info("Opening course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // Course 연결 여부 검증
        if (courseTime.getCourse() == null) {
            throw new CourseNotFoundException();
        }

        // Course REGISTERED 상태 검증
        if (!courseTime.getCourse().canCreateCourseTime()) {
            throw new CourseNotRegisteredException(courseTime.getCourse().getId(), courseTime.getCourse().getStatus().name());
        }

        // 장소 정보 필수 검증 (OFFLINE/BLENDED)
        if (courseTime.requiresLocationInfo() &&
                (courseTime.getLocationInfo() == null || courseTime.getLocationInfo().isBlank())) {
            throw new LocationRequiredException();
        }

        // MAIN 강사 필수 검증 (R-IIS-01, R-TS-OPEN)
        if (!instructorAssignmentService.existsMainInstructor(id)) {
            throw new MainInstructorRequiredException(id);
        }

        // 상태 전이 (Entity에서 DRAFT 상태 검증)
        courseTime.open();
        log.info("Course time opened: id={}", id);

        List<InstructorAssignmentResponse> instructors =
                instructorAssignmentService.getInstructorsByTimeId(id, AssignmentStatus.ACTIVE);

        return CourseTimeDetailResponse.from(courseTime, instructors);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse startCourseTime(Long id) {
        log.info("Starting course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // 상태 전이 (Entity에서 RECRUITING 상태 검증)
        courseTime.startClass();
        log.info("Course time started: id={}", id);

        List<InstructorAssignmentResponse> instructors =
                instructorAssignmentService.getInstructorsByTimeId(id, AssignmentStatus.ACTIVE);

        return CourseTimeDetailResponse.from(courseTime, instructors);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse closeCourseTime(Long id) {
        log.info("Closing course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // 상태 전이 (Entity에서 ONGOING 상태 검증)
        courseTime.close();
        log.info("Course time closed: id={}", id);

        List<InstructorAssignmentResponse> instructors =
                instructorAssignmentService.getInstructorsByTimeId(id, AssignmentStatus.ACTIVE);

        return CourseTimeDetailResponse.from(courseTime, instructors);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse archiveCourseTime(Long id) {
        log.info("Archiving course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // 상태 전이 (Entity에서 CLOSED 상태 검증)
        courseTime.archive();
        log.info("Course time archived: id={}", id);

        List<InstructorAssignmentResponse> instructors =
                instructorAssignmentService.getInstructorsByTimeId(id, AssignmentStatus.ACTIVE);

        return CourseTimeDetailResponse.from(courseTime, instructors);
    }

    // ========== Private Methods ==========

    private void validateDateRange(CreateCourseTimeRequest request) {
        validateDateRange(
                request.enrollStartDate(),
                request.enrollEndDate(),
                request.classStartDate(),
                request.classEndDate(),
                true
        );
    }

    private void validateDateRange(
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate
    ) {
        validateDateRange(enrollStartDate, enrollEndDate, classStartDate, classEndDate, false);
    }

    private void validateDateRange(
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate,
            boolean isNewCreation
    ) {
        // [R-DATE-04] 모집 시작일 >= 오늘 (신규 생성 시)
        if (isNewCreation && enrollStartDate.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("모집 시작일은 오늘 이후여야 합니다");
        }

        // [R-DATE-01] 모집 시작일 <= 모집 종료일
        if (enrollStartDate.isAfter(enrollEndDate)) {
            throw new InvalidDateRangeException("모집 시작일은 모집 종료일 이전이어야 합니다");
        }

        // [R-DATE-02] 학습 시작일 <= 학습 종료일 (UNLIMITED 타입은 classEndDate가 null이므로 검증 스킵)
        if (classEndDate != null && classStartDate.isAfter(classEndDate)) {
            throw new InvalidDateRangeException("학습 시작일은 학습 종료일 이전이어야 합니다");
        }

        // [R-DATE-03] 모집 종료일 <= 학습 시작일
        if (enrollEndDate.isAfter(classStartDate)) {
            throw new InvalidDateRangeException("모집 종료일은 학습 시작일 이전이어야 합니다");
        }
    }

    /**
     * RecurringScheduleRequest를 RecurringSchedule 엔티티로 변환
     */
    private RecurringSchedule toRecurringSchedule(RecurringScheduleRequest request) {
        if (request == null) {
            return null;
        }
        return RecurringSchedule.create(request.daysOfWeek(), request.startTime(), request.endTime());
    }

    /**
     * DESIGNER(강의 설계자)를 차수의 MAIN 강사로 자동 배정
     */
    private List<InstructorAssignmentResponse> assignCourseDesignerAsMainInstructor(
            CourseTime courseTime, Course course, Long operatorId) {
        // Course의 DESIGNER(강의 설계자) 조회
        List<UserCourseRole> designers = userCourseRoleRepository.findByCourseIdAndRole(course.getId(), CourseRole.DESIGNER);

        if (designers.isEmpty()) {
            log.warn("No CourseRole.DESIGNER found for course: courseId={}", course.getId());
            return List.of();
        }

        // 첫 번째 DESIGNER를 MAIN 강사로 배정
        UserCourseRole designerRole = designers.get(0);
        Long designerId = designerRole.getUser().getId();

        AssignInstructorRequest assignRequest = new AssignInstructorRequest(designerId, InstructorRole.MAIN, false);
        InstructorAssignmentResponse assignment = instructorAssignmentService.assignInstructor(
                courseTime.getId(), assignRequest, operatorId);

        log.info("DESIGNER assigned as MAIN instructor: courseTimeId={}, userId={}", courseTime.getId(), designerId);

        return List.of(assignment);
    }

    // ========== 정원 관리 (SIS에서 호출) ==========

    @Override
    @Transactional
    public void occupySeat(Long courseTimeId) {
        log.info("Occupying seat: courseTimeId={}", courseTimeId);

        // [R04] 비관적 락으로 조회
        CourseTime courseTime = courseTimeRepository.findByIdWithLock(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // [R02] capacity = null이면 무제한
        if (!courseTime.hasUnlimitedCapacity() && !courseTime.hasAvailableSeats()) {
            throw new CapacityExceededException(courseTime.getCapacity(), courseTime.getCurrentEnrollment());
        }

        courseTime.incrementEnrollment();
        log.info("Seat occupied: courseTimeId={}, currentEnrollment={}",
                courseTimeId, courseTime.getCurrentEnrollment());
    }

    @Override
    @Transactional
    public void releaseSeat(Long courseTimeId) {
        log.info("Releasing seat: courseTimeId={}", courseTimeId);

        // [R04] 비관적 락으로 조회
        CourseTime courseTime = courseTimeRepository.findByIdWithLock(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        courseTime.decrementEnrollment();
        log.info("Seat released: courseTimeId={}, currentEnrollment={}",
                courseTimeId, courseTime.getCurrentEnrollment());
    }

    @Override
    @Transactional
    public void forceOccupySeat(Long courseTimeId) {
        log.info("Force occupying seat: courseTimeId={}", courseTimeId);

        // 비관적 락으로 조회 (정원 초과 허용)
        CourseTime courseTime = courseTimeRepository.findByIdWithLock(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 정원 체크 없이 증가 (강제 배정)
        courseTime.incrementEnrollment();
        log.info("Seat force occupied: courseTimeId={}, currentEnrollment={}",
                courseTimeId, courseTime.getCurrentEnrollment());
    }

    // ========== Public API ==========

    @Override
    public CapacityResponse getCapacity(Long id) {
        log.debug("Getting capacity: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        return CapacityResponse.from(courseTime);
    }

    @Override
    public PriceResponse getPrice(Long id) {
        log.debug("Getting price: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        return PriceResponse.from(courseTime);
    }

    // ========== Form Data & Validation ==========

    @Override
    public CourseTimeFormDataResponse getFormData(Long courseId) {
        log.debug("Getting form data for course: courseId={}", courseId);

        Course course = courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return CourseTimeFormDataResponse.from(course);
    }

    @Override
    public CourseTimeValidationResult validateCreateRequest(CreateCourseTimeRequest request) {
        log.debug("Validating create request for course: courseId={}", request.courseId());

        Course course = courseRepository.findByIdAndTenantId(request.courseId(), TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(request.courseId()));

        return constraintValidator.validate(request, course);
    }

    @Override
    public CourseTimeValidationResult validateUpdateRequest(Long courseTimeId, UpdateCourseTimeRequest request) {
        log.debug("Validating update request for courseTime: id={}", courseTimeId);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(courseTimeId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        Course course = courseTime.getCourse();

        return constraintValidator.validate(request, courseTime, course);
    }
}
