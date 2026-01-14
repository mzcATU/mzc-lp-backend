package com.mzc.lp.domain.ts.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.category.repository.CategoryRepository;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.dto.response.CourseTimeCatalogResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimePublicDetailResponse;
import com.mzc.lp.domain.ts.dto.response.CurriculumItemResponse;
import com.mzc.lp.domain.ts.dto.response.InstructorSummaryResponse;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotAvailableException;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeSpecification;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCourseTimeServiceImpl implements PublicCourseTimeService {

    private final CourseTimeRepository courseTimeRepository;
    private final InstructorAssignmentRepository instructorAssignmentRepository;
    private final SnapshotItemRepository snapshotItemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 공개 가능한 상태 목록
     */
    private static final List<CourseTimeStatus> PUBLIC_STATUSES = Arrays.asList(
            CourseTimeStatus.RECRUITING,
            CourseTimeStatus.ONGOING
    );

    @Override
    public Page<CourseTimeCatalogResponse> getPublicCourseTimes(
            List<CourseTimeStatus> statuses,
            DeliveryType deliveryType,
            Long courseId,
            Boolean isFree,
            String keyword,
            Long categoryId,
            Pageable pageable
    ) {
        log.debug("Getting public course times: statuses={}, deliveryType={}, courseId={}, isFree={}, keyword={}, categoryId={}",
                statuses, deliveryType, courseId, isFree, keyword, categoryId);

        Long tenantId = TenantContext.getCurrentTenantId();

        // 상태 필터 기본값: RECRUITING, ONGOING
        List<CourseTimeStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? PUBLIC_STATUSES
                : statuses.stream().filter(PUBLIC_STATUSES::contains).toList();

        // 동적 쿼리 생성
        Specification<CourseTime> spec = CourseTimeSpecification.forPublicCatalog(
                tenantId,
                effectiveStatuses,
                deliveryType,
                courseId,
                isFree,
                keyword,
                categoryId
        );

        Page<CourseTime> courseTimePage = courseTimeRepository.findAll(spec, pageable);

        // N+1 방지: 강사 정보 Bulk 조회
        List<Long> timeIds = courseTimePage.getContent().stream()
                .map(CourseTime::getId)
                .toList();

        Map<Long, List<InstructorSummaryResponse>> instructorMap = getInstructorsByTimeIds(timeIds);

        // N+1 방지: 카테고리 정보 Bulk 조회
        Map<Long, Category> categoryMap = getCategoryMap(courseTimePage.getContent());

        return courseTimePage.map(ct -> {
            Long ctCategoryId = extractCategoryId(ct);
            Category category = ctCategoryId != null ? categoryMap.get(ctCategoryId) : null;
            return CourseTimeCatalogResponse.from(
                    ct,
                    instructorMap.getOrDefault(ct.getId(), List.of()),
                    category
            );
        });
    }

    @Override
    public CourseTimePublicDetailResponse getPublicCourseTime(Long id) {
        log.debug("Getting public course time detail: id={}", id);

        Long tenantId = TenantContext.getCurrentTenantId();

        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(id));

        // 공개 가능한 상태 검증
        if (!PUBLIC_STATUSES.contains(courseTime.getStatus())) {
            throw new CourseTimeNotAvailableException(id);
        }

        // 강사 정보 조회
        List<InstructorSummaryResponse> instructors = getInstructorsByTimeId(id);

        // 커리큘럼 조회
        List<CurriculumItemResponse> curriculum = getCurriculum(courseTime);

        return CourseTimePublicDetailResponse.from(courseTime, curriculum, instructors);
    }

    /**
     * 차수 ID 목록으로 강사 정보 Bulk 조회 (N+1 방지)
     */
    private Map<Long, List<InstructorSummaryResponse>> getInstructorsByTimeIds(List<Long> timeIds) {
        if (timeIds.isEmpty()) {
            return Map.of();
        }

        Long tenantId = TenantContext.getCurrentTenantId();

        // ACTIVE 상태 강사 배정 목록 조회
        List<InstructorAssignment> assignments = instructorAssignmentRepository
                .findActiveByTimeKeyIn(timeIds, tenantId);

        // 강사 User 정보 Bulk 조회
        Set<Long> userIds = assignments.stream()
                .map(InstructorAssignment::getUserKey)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // timeKey별로 그룹핑하여 반환
        return assignments.stream()
                .collect(Collectors.groupingBy(
                        InstructorAssignment::getTimeKey,
                        Collectors.mapping(
                                ia -> InstructorSummaryResponse.from(ia, userMap.get(ia.getUserKey())),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 단일 차수의 강사 정보 조회
     */
    private List<InstructorSummaryResponse> getInstructorsByTimeId(Long timeId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        List<InstructorAssignment> assignments = instructorAssignmentRepository
                .findByTimeKeyAndTenantIdAndStatus(timeId, tenantId, AssignmentStatus.ACTIVE);

        if (assignments.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = assignments.stream()
                .map(InstructorAssignment::getUserKey)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return assignments.stream()
                .map(ia -> InstructorSummaryResponse.from(ia, userMap.get(ia.getUserKey())))
                .toList();
    }

    /**
     * CourseTime의 Snapshot에서 커리큘럼 조회
     */
    private List<CurriculumItemResponse> getCurriculum(CourseTime courseTime) {
        if (courseTime.getSnapshot() == null) {
            return List.of();
        }

        Long snapshotId = courseTime.getSnapshot().getId();
        Long tenantId = TenantContext.getCurrentTenantId();

        // 루트 아이템만 조회 (children은 엔티티에서 자동 로딩)
        List<SnapshotItem> rootItems = snapshotItemRepository
                .findRootItemsWithLo(snapshotId, tenantId);

        return rootItems.stream()
                .map(CurriculumItemResponse::fromWithChildren)
                .toList();
    }

    /**
     * CourseTime 목록에서 카테고리 ID를 추출하여 Category Map 조회
     */
    private Map<Long, Category> getCategoryMap(List<CourseTime> courseTimes) {
        Set<Long> categoryIds = courseTimes.stream()
                .map(this::extractCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    /**
     * CourseTime에서 카테고리 ID 추출 (Course에서 직접)
     */
    private Long extractCategoryId(CourseTime courseTime) {
        Course course = courseTime.getCourse();
        if (course == null) {
            return null;
        }
        return course.getCategoryId();
    }
}
