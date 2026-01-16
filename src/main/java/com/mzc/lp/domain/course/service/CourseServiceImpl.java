package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.constant.CourseStatus;
import com.mzc.lp.domain.course.dto.request.CreateCourseRequest;
import com.mzc.lp.domain.course.dto.request.UpdateCourseRequest;
import com.mzc.lp.domain.course.dto.response.CourseDetailResponse;
import com.mzc.lp.domain.course.dto.response.CourseItemResponse;
import com.mzc.lp.domain.course.dto.response.CourseResponse;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.exception.CourseIncompleteException;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.exception.CourseNotModifiableException;
import com.mzc.lp.domain.course.exception.CourseOwnershipException;
import com.mzc.lp.domain.course.exception.InvalidCourseStatusTransitionException;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.course.repository.CourseReviewRepository;
import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.category.repository.CategoryRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import com.mzc.lp.common.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CourseTimeRepository courseTimeRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, Long createdBy) {
        log.info("Creating course: title={}, createdBy={}", request.title(), createdBy);

        Course course = Course.create(
                request.title(),
                request.description(),
                request.level(),
                request.type(),
                request.estimatedHours(),
                request.categoryId(),
                request.thumbnailUrl(),
                request.startDate(),
                request.endDate(),
                request.tags(),
                createdBy
        );

        Course savedCourse = courseRepository.save(course);
        log.info("Course created: id={}, title={}", savedCourse.getId(), savedCourse.getTitle());

        return CourseResponse.from(savedCourse);
    }

    @Override
    public Page<CourseResponse> getCourses(String keyword, Long categoryId, Pageable pageable) {
        log.debug("Getting courses: keyword={}, categoryId={}", keyword, categoryId);

        Page<Course> courses;

        if (keyword != null && !keyword.isBlank() && categoryId != null) {
            courses = courseRepository.findByTenantIdAndTitleContainingAndCategoryId(
                    TenantContext.getCurrentTenantId(), keyword, categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            courses = courseRepository.findByTenantIdAndTitleContaining(
                    TenantContext.getCurrentTenantId(), keyword, pageable);
        } else if (categoryId != null) {
            courses = courseRepository.findByTenantIdAndCategoryId(
                    TenantContext.getCurrentTenantId(), categoryId, pageable);
        } else {
            courses = courseRepository.findByTenantId(TenantContext.getCurrentTenantId(), pageable);
        }

        Long tenantId = TenantContext.getCurrentTenantId();

        // creatorId 목록 추출 및 User 일괄 조회 (N+1 방지)
        List<Long> creatorIds = courses.getContent().stream()
                .map(Course::getCreatedBy)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> creatorNameMap = getCreatorNameMap(creatorIds);

        // categoryId 목록 추출 및 Category 일괄 조회 (N+1 방지)
        List<Long> categoryIds = courses.getContent().stream()
                .map(Course::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> categoryNameMap = getCategoryNameMap(categoryIds);

        // courseId 목록 추출 및 timeCount 일괄 조회 (N+1 방지)
        List<Long> courseIds = courses.getContent().stream()
                .map(Course::getId)
                .toList();

        Map<Long, Integer> timeCountMap = getTimeCountMap(courseIds, tenantId);

        return courses.map(course -> CourseResponse.from(
                course,
                0,
                creatorNameMap.get(course.getCreatedBy()),
                categoryNameMap.get(course.getCategoryId()),
                timeCountMap.getOrDefault(course.getId(), 0)
        ));
    }

    @Override
    public CourseDetailResponse getCourseDetail(Long courseId) {
        log.debug("Getting course detail: courseId={}", courseId);

        Long tenantId = TenantContext.getCurrentTenantId();
        Course course = courseRepository.findByIdWithItems(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        List<CourseItemResponse> items = course.getItems().stream()
                .map(CourseItemResponse::from)
                .toList();

        // 리뷰 통계 조회
        Object[] stats = reviewRepository.findReviewStatsForCourse(courseId, tenantId);
        Long reviewCount = 0L;
        Double averageRating = null;

        if (stats != null && stats.length >= 2) {
            // stats[0]은 COUNT, stats[1]은 AVG
            // 리뷰가 없을 경우 COUNT는 0, AVG는 null
            if (stats[0] instanceof Number count) {
                reviewCount = count.longValue();
            }
            if (stats[1] instanceof Number avg) {
                averageRating = avg.doubleValue();
            }
        }

        // 생성자 이름 조회
        String creatorName = null;
        if (course.getCreatedBy() != null) {
            creatorName = userRepository.findById(course.getCreatedBy())
                    .map(User::getName)
                    .orElse(null);
        }

        return CourseDetailResponse.from(course, items, items.size(), averageRating, reviewCount, creatorName);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request) {
        log.info("Updating course: courseId={}", courseId);

        Course course = courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 수정 가능 상태 검증
        if (!course.isModifiable()) {
            throw new CourseNotModifiableException(courseId, course.getStatus());
        }

        course.update(
                request.title(),
                request.description(),
                request.level(),
                request.type(),
                request.estimatedHours(),
                request.categoryId(),
                request.thumbnailUrl(),
                request.startDate(),
                request.endDate(),
                request.tags()
        );

        // status 업데이트
        if (request.status() != null) {
            handleStatusTransition(course, request.status());
        }

        log.info("Course updated: id={}", courseId);
        return CourseResponse.from(course, course.getItems().size());
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId, Long currentUserId, boolean isTenantAdmin) {
        log.info("Deleting course: courseId={}, currentUserId={}, isTenantAdmin={}", courseId, currentUserId, isTenantAdmin);

        Course course = courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 소유권 검증: 본인이 생성한 Course 또는 TENANT_ADMIN만 삭제 가능
        if (!isTenantAdmin && !currentUserId.equals(course.getCreatedBy())) {
            throw new CourseOwnershipException("본인이 생성한 강의만 삭제할 수 있습니다");
        }

        courseRepository.delete(course);
        log.info("Course deleted: id={}", courseId);
    }

    @Override
    public Page<CourseResponse> getMyCourses(Long creatorId, Pageable pageable) {
        log.debug("Getting my courses: creatorId={}", creatorId);

        Page<Course> courses = courseRepository.findByTenantIdAndCreatedBy(
                TenantContext.getCurrentTenantId(), creatorId, pageable);

        // itemCount 일괄 조회 (N+1 방지)
        List<Long> courseIds = courses.getContent().stream()
                .map(Course::getId)
                .toList();

        Map<Long, Integer> itemCountMap = getItemCountMap(courseIds);

        return courses.map(course -> CourseResponse.from(
                course,
                itemCountMap.getOrDefault(course.getId(), 0)
        ));
    }

    private Map<Long, Integer> getItemCountMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return courseRepository.countItemsByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
    }

    private Map<Long, String> getCreatorNameMap(List<Long> creatorIds) {
        if (creatorIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getName
                ));
    }

    private Map<Long, String> getCategoryNameMap(List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        Category::getName
                ));
    }

    private Map<Long, Integer> getTimeCountMap(List<Long> courseIds, Long tenantId) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return courseTimeRepository.countByCourseIds(courseIds, tenantId).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
    }

    @Override
    @Transactional
    @Deprecated
    public CourseResponse publishCourse(Long courseId) {
        log.warn("Deprecated publishCourse() called. Use readyCourse() instead.");
        return readyCourse(courseId);
    }

    @Override
    @Transactional
    @Deprecated
    public CourseResponse unpublishCourse(Long courseId) {
        log.warn("Deprecated unpublishCourse() called. Use unreadyCourse() instead.");
        return unreadyCourse(courseId);
    }

    @Override
    @Transactional
    public CourseResponse readyCourse(Long courseId) {
        log.info("Setting course to READY: courseId={}", courseId);
        Long tenantId = TenantContext.getCurrentTenantId();
        Course course = courseRepository.findByIdWithItems(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (!course.isModifiable()) {
            throw new CourseNotModifiableException(courseId, course.getStatus());
        }
        validateCompleteness(course);
        course.markAsReady();

        log.info("Course set to READY: id={}", courseId);
        return CourseResponse.from(course, course.getItems().size());
    }

    @Override
    @Transactional
    public CourseResponse unreadyCourse(Long courseId) {
        log.info("Setting course to DRAFT: courseId={}", courseId);
        Course course = courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (!course.isModifiable()) {
            throw new CourseNotModifiableException(courseId, course.getStatus());
        }
        course.markAsDraft();

        log.info("Course set to DRAFT: id={}", courseId);
        return CourseResponse.from(course);
    }

    @Override
    @Transactional
    public CourseResponse registerCourse(Long courseId) {
        log.info("Registering course: courseId={}", courseId);
        Long tenantId = TenantContext.getCurrentTenantId();
        Course course = courseRepository.findByIdWithItems(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getStatus() != CourseStatus.READY) {
            throw new InvalidCourseStatusTransitionException(course.getStatus(), CourseStatus.REGISTERED);
        }
        course.register();

        log.info("Course registered: id={}", courseId);
        return CourseResponse.from(course, course.getItems().size());
    }

    private void handleStatusTransition(Course course, CourseStatus targetStatus) {
        CourseStatus currentStatus = course.getStatus();
        if (currentStatus == targetStatus) return;

        switch (targetStatus) {
            case DRAFT -> course.markAsDraft();
            case READY -> {
                validateCompleteness(course);
                course.markAsReady();
            }
            case REGISTERED -> {
                if (currentStatus != CourseStatus.READY) {
                    throw new InvalidCourseStatusTransitionException(currentStatus, targetStatus);
                }
                course.register();
            }
        }
    }

    private void validateCompleteness(Course course) {
        boolean isComplete = course.getTitle() != null && !course.getTitle().isBlank()
                && course.getDescription() != null && !course.getDescription().isBlank()
                && course.getCategoryId() != null
                && !course.getItems().isEmpty();

        if (!isComplete) {
            throw new CourseIncompleteException(course.getId());
        }
    }
}
