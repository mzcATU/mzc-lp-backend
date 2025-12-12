package com.mzc.lp.domain.ts.service;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.CourseTimeDetailResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeResponse;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.exception.InvalidDateRangeException;
import com.mzc.lp.domain.ts.exception.InvalidStatusTransitionException;
import com.mzc.lp.domain.ts.exception.LocationRequiredException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseTimeServiceImpl implements CourseTimeService {

    private final CourseTimeRepository courseTimeRepository;

    @Override
    @Transactional
    public CourseTimeDetailResponse createCourseTime(CreateCourseTimeRequest request, Long createdBy) {
        log.info("Creating course time: title={}", request.title());

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

        // CM 연결 (있는 경우)
        if (request.cmCourseId() != null) {
            courseTime.linkCourse(request.cmCourseId(), request.cmCourseVersionId());
        }

        CourseTime savedCourseTime = courseTimeRepository.save(courseTime);
        log.info("Course time created: id={}", savedCourseTime.getId());

        return CourseTimeDetailResponse.from(savedCourseTime);
    }

    @Override
    public Page<CourseTimeResponse> getCourseTimes(CourseTimeStatus status, Long cmCourseId, Pageable pageable) {
        log.debug("Getting course times: status={}, cmCourseId={}", status, cmCourseId);

        if (cmCourseId != null) {
            return courseTimeRepository.findByCmCourseIdAndTenantId(cmCourseId, getCurrentTenantId())
                    .stream()
                    .filter(ct -> status == null || ct.getStatus() == status)
                    .map(CourseTimeResponse::from)
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        }

        if (status != null) {
            return courseTimeRepository.findByTenantIdAndStatus(getCurrentTenantId(), status, pageable)
                    .map(CourseTimeResponse::from);
        }

        return courseTimeRepository.findByTenantId(getCurrentTenantId(), pageable)
                .map(CourseTimeResponse::from);
    }

    @Override
    public CourseTimeDetailResponse getCourseTime(Long id) {
        log.debug("Getting course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        return CourseTimeDetailResponse.from(courseTime);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse updateCourseTime(Long id, UpdateCourseTimeRequest request) {
        log.info("Updating course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
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
        return CourseTimeDetailResponse.from(courseTime);
    }

    @Override
    @Transactional
    public void deleteCourseTime(Long id) {
        log.info("Deleting course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // DRAFT 상태에서만 삭제 가능
        if (!courseTime.isDraft()) {
            throw new InvalidStatusTransitionException();
        }

        courseTimeRepository.delete(courseTime);
        log.info("Course time deleted: id={}", id);
    }

    // ========== 상태 전이 ==========

    @Override
    @Transactional
    public CourseTimeDetailResponse openCourseTime(Long id) {
        log.info("Opening course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        if (!courseTime.isDraft()) {
            throw new InvalidStatusTransitionException(
                    courseTime.getStatus(),
                    CourseTimeStatus.RECRUITING
            );
        }

        // 장소 정보 필수 검증 (OFFLINE/BLENDED)
        if (courseTime.requiresLocationInfo() &&
                (courseTime.getLocationInfo() == null || courseTime.getLocationInfo().isBlank())) {
            throw new LocationRequiredException();
        }

        courseTime.open();
        log.info("Course time opened: id={}", id);

        return CourseTimeDetailResponse.from(courseTime);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse startCourseTime(Long id) {
        log.info("Starting course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        if (!courseTime.isRecruiting()) {
            throw new InvalidStatusTransitionException(
                    courseTime.getStatus(),
                    CourseTimeStatus.ONGOING
            );
        }

        courseTime.startClass();
        log.info("Course time started: id={}", id);

        return CourseTimeDetailResponse.from(courseTime);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse closeCourseTime(Long id) {
        log.info("Closing course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        if (!courseTime.isOngoing()) {
            throw new InvalidStatusTransitionException(
                    courseTime.getStatus(),
                    CourseTimeStatus.CLOSED
            );
        }

        courseTime.close();
        log.info("Course time closed: id={}", id);

        return CourseTimeDetailResponse.from(courseTime);
    }

    @Override
    @Transactional
    public CourseTimeDetailResponse archiveCourseTime(Long id) {
        log.info("Archiving course time: id={}", id);

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        if (!courseTime.isClosed()) {
            throw new InvalidStatusTransitionException(
                    courseTime.getStatus(),
                    CourseTimeStatus.ARCHIVED
            );
        }

        courseTime.archive();
        log.info("Course time archived: id={}", id);

        return CourseTimeDetailResponse.from(courseTime);
    }

    // ========== Private Methods ==========

    private void validateDateRange(CreateCourseTimeRequest request) {
        // [R09] enroll_end_date <= class_end_date
        if (request.enrollEndDate().isAfter(request.classEndDate())) {
            throw new InvalidDateRangeException("모집 종료일은 학습 종료일 이전이어야 합니다");
        }

        // 모집 시작일 <= 모집 종료일
        if (request.enrollStartDate().isAfter(request.enrollEndDate())) {
            throw new InvalidDateRangeException("모집 시작일은 모집 종료일 이전이어야 합니다");
        }

        // 학습 시작일 <= 학습 종료일
        if (request.classStartDate().isAfter(request.classEndDate())) {
            throw new InvalidDateRangeException("학습 시작일은 학습 종료일 이전이어야 합니다");
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

    private Long getCurrentTenantId() {
        // TODO: SecurityContext에서 tenantId 추출
        // 현재는 임시로 1L 반환
        return 1L;
    }
}
