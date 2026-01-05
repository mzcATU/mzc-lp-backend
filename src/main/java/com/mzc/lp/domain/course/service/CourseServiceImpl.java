package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateCourseRequest;
import com.mzc.lp.domain.course.dto.request.UpdateCourseRequest;
import com.mzc.lp.domain.course.dto.response.CourseDetailResponse;
import com.mzc.lp.domain.course.dto.response.CourseItemResponse;
import com.mzc.lp.domain.course.dto.response.CourseResponse;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.exception.CourseOwnershipException;
import com.mzc.lp.domain.course.repository.CourseRepository;
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

        return courses.map(CourseResponse::from);
    }

    @Override
    public CourseDetailResponse getCourseDetail(Long courseId) {
        log.debug("Getting course detail: courseId={}", courseId);

        Course course = courseRepository.findByIdWithItems(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        List<CourseItemResponse> items = course.getItems().stream()
                .map(CourseItemResponse::from)
                .toList();

        return CourseDetailResponse.from(course, items, items.size());
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request) {
        log.info("Updating course: courseId={}", courseId);

        Course course = courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

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

        log.info("Course updated: id={}", courseId);
        return CourseResponse.from(course);
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
}
