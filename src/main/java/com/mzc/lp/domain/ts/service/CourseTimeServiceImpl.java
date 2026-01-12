package com.mzc.lp.domain.ts.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.dto.request.CloneCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.CapacityResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeDetailResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeResponse;
import com.mzc.lp.domain.ts.dto.response.PriceResponse;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CapacityExceededException;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.exception.InvalidDateRangeException;
import com.mzc.lp.domain.ts.exception.InvalidStatusTransitionException;
import com.mzc.lp.domain.ts.exception.LocationRequiredException;
import com.mzc.lp.domain.ts.exception.MainInstructorRequiredException;
import com.mzc.lp.domain.ts.exception.UnauthorizedCourseTimeAccessException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.program.exception.ProgramNotFoundException;
import com.mzc.lp.domain.program.exception.ProgramNotApprovedException;
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
    private final ProgramRepository programRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;

    @Override
    public CourseTime getCourseTimeEntity(Long id) {
        return courseTimeRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));
    }

    @Override
    @Transactional
    @SuppressWarnings("removal")
    public CourseTimeDetailResponse createCourseTime(CreateCourseTimeRequest request, Long createdBy) {
        log.info("Creating course time: title={}, programId={}", request.title(), request.programId());

        // Program 조회 및 검증
        Program program = programRepository.findByIdAndTenantId(request.programId(), TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(request.programId()));

        // 승인된 Program만 차수 생성 가능
        if (!program.canCreateTime()) {
            throw new ProgramNotApprovedException(request.programId());
        }

        // 비즈니스 규칙 검증
        validateDateRange(request);
        validateLocationInfo(request);
        validateEnrollmentMethod(request);

        CourseTime courseTime = CourseTime.create(
                request.title(),
                request.deliveryType(),
                request.enrollStartDate(),
                request.enrollEndDate(),
                request.classStartDate(),
                request.classEndDate(),
                request.capacity(),
                request.maxWaitingCount(),
                request.enrollmentMethod(),
                request.minProgressForCompletion(),
                request.price(),
                request.isFree(),
                request.locationInfo(),
                request.allowLateEnrollment() != null ? request.allowLateEnrollment() : false,
                createdBy
        );

        // Program 연결
        courseTime.linkProgram(program);

        // CM 연결 (deprecated - 하위 호환성)
        if (request.cmCourseId() != null) {
            courseTime.linkCourse(request.cmCourseId(), request.cmCourseVersionId());
        }

        CourseTime savedCourseTime = courseTimeRepository.save(courseTime);
        log.info("Course time created: id={}, programId={}", savedCourseTime.getId(), request.programId());

        // B2C: Program DESIGNER를 MAIN 강사로 자동 배정
        List<InstructorAssignmentResponse> instructors = assignCourseDesignerAsMainInstructor(savedCourseTime, program, createdBy);

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
                request.enrollStartDate(),
                request.enrollEndDate(),
                request.classStartDate(),
                request.classEndDate(),
                createdBy
        );

        CourseTime savedCourseTime = courseTimeRepository.save(cloned);
        log.info("Course time cloned: sourceId={}, newId={}", sourceId, savedCourseTime.getId());

        return CourseTimeDetailResponse.from(savedCourseTime);
    }

    @Override
    public Page<CourseTimeResponse> getCourseTimes(CourseTimeStatus status, Long cmCourseId, Pageable pageable) {
        log.debug("Getting course times: status={}, cmCourseId={}", status, cmCourseId);

        Long tenantId = TenantContext.getCurrentTenantId();
        Page<CourseTime> courseTimePage;

        if (cmCourseId != null && status != null) {
            courseTimePage = courseTimeRepository.findByCmCourseIdAndTenantIdAndStatus(
                    cmCourseId, tenantId, status, pageable);
        } else if (cmCourseId != null) {
            courseTimePage = courseTimeRepository.findByCmCourseIdAndTenantId(
                    cmCourseId, tenantId, pageable);
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

        return CourseTimeDetailResponse.from(courseTime, instructors);
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

        // 부분 업데이트
        if (request.title() != null) {
            courseTime.updateTitle(request.title());
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

        log.info("Course time updated: id={}", id);

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

        // Program 연결 여부 검증
        if (courseTime.getProgram() == null) {
            throw new ProgramNotFoundException();
        }

        // Program 승인 상태 검증
        if (!courseTime.getProgram().isApproved()) {
            throw new ProgramNotApprovedException(courseTime.getProgram().getId());
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

        // [R-DATE-02] 학습 시작일 <= 학습 종료일
        if (classStartDate.isAfter(classEndDate)) {
            throw new InvalidDateRangeException("학습 시작일은 학습 종료일 이전이어야 합니다");
        }

        // [R-DATE-03] 모집 종료일 <= 학습 시작일
        if (enrollEndDate.isAfter(classStartDate)) {
            throw new InvalidDateRangeException("모집 종료일은 학습 시작일 이전이어야 합니다");
        }
    }

    private void validateLocationInfo(CreateCourseTimeRequest request) {
        // [R10] OFFLINE/BLENDED일 때 location_info 필수
        if ((request.deliveryType() == com.mzc.lp.domain.ts.constant.DeliveryType.OFFLINE
                || request.deliveryType() == com.mzc.lp.domain.ts.constant.DeliveryType.BLENDED)
                && (request.locationInfo() == null || request.locationInfo().isBlank())) {
            throw new LocationRequiredException();
        }
    }

    private void validateEnrollmentMethod(CreateCourseTimeRequest request) {
        // [R53] APPROVAL + maxWaitingCount > 0 조합 불가
        if (request.enrollmentMethod() == EnrollmentMethod.APPROVAL
                && request.maxWaitingCount() != null
                && request.maxWaitingCount() > 0) {
            throw new IllegalArgumentException("승인제 모집에서는 대기자 기능을 사용할 수 없습니다");
        }
    }

    /**
     * B2C: Program DESIGNER(강의 설계자)를 차수의 MAIN 강사로 자동 배정
     */
    private List<InstructorAssignmentResponse> assignCourseDesignerAsMainInstructor(CourseTime courseTime, Program program, Long operatorId) {
        // Program의 DESIGNER(강의 설계자) 조회
        List<UserCourseRole> designers = userCourseRoleRepository.findByCourseIdAndRole(program.getId(), CourseRole.DESIGNER);

        if (designers.isEmpty()) {
            log.warn("No CourseRole.DESIGNER found for program: programId={}", program.getId());
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
}
