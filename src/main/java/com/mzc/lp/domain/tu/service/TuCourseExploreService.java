package com.mzc.lp.domain.tu.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.tu.dto.response.CourseExploreItemResponse;
import com.mzc.lp.domain.tu.dto.response.CourseExploreResponse;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TuCourseExploreService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * TU용 강의 목록 조회
     */
    public CourseExploreResponse getCourses(
            String search,
            String category,
            String level,
            String sortBy,
            int page,
            int pageSize
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();

        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Course> coursePage = courseRepository.findByTenantId(tenantId, pageable);

        List<CourseExploreItemResponse> courses = coursePage.getContent().stream()
                .map(this::toCourseExploreItem)
                .toList();

        return CourseExploreResponse.of(
                courses,
                coursePage.getTotalElements(),
                page,
                pageSize
        );
    }

    /**
     * 인기 강의 조회
     */
    public CourseExploreResponse getPopularCourses(int limit) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Course> coursePage = courseRepository.findByTenantId(tenantId, pageable);

        List<CourseExploreItemResponse> courses = coursePage.getContent().stream()
                .map(this::toCourseExploreItem)
                .toList();

        return CourseExploreResponse.of(courses, courses.size(), 0, limit);
    }

    /**
     * 신규 강의 조회 (최근 30일)
     */
    public CourseExploreResponse getNewCourses(int limit) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Course> coursePage = courseRepository.findByTenantId(tenantId, pageable);

        List<CourseExploreItemResponse> courses = coursePage.getContent().stream()
                .map(this::toCourseExploreItem)
                .toList();

        return CourseExploreResponse.of(courses, courses.size(), 0, limit);
    }

    /**
     * 추천 강의 조회
     */
    public CourseExploreResponse getRecommendedCourses(int limit) {
        return getPopularCourses(limit);
    }

    private CourseExploreItemResponse toCourseExploreItem(Course course) {
        // 생성자 정보 조회
        String instructorName = "강사 미정";
        if (course.getCreatedBy() != null) {
            instructorName = userRepository.findById(course.getCreatedBy())
                    .map(User::getName)
                    .orElse("강사 미정");
        }

        // 더미 데이터 (추후 실제 데이터로 교체)
        Random random = new Random(course.getId()); // ID 기반 시드로 일관된 값 생성
        BigDecimal originalPrice = BigDecimal.valueOf(50000 + random.nextInt(100000));
        BigDecimal price = originalPrice.multiply(BigDecimal.valueOf(0.7 + random.nextDouble() * 0.3));
        int studentCount = random.nextInt(5000);
        double rating = 4.0 + random.nextDouble();
        int reviewCount = random.nextInt(500);
        boolean isBestseller = studentCount > 3000;

        return CourseExploreItemResponse.from(
                course,
                instructorName,
                price.setScale(0, java.math.RoundingMode.DOWN),
                originalPrice.setScale(0, java.math.RoundingMode.DOWN),
                studentCount,
                Math.round(rating * 10.0) / 10.0,
                reviewCount,
                isBestseller
        );
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sortBy) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating" -> Sort.by(Sort.Direction.DESC, "createdAt"); // 추후 rating 필드 추가 시 변경
            case "price_low" -> Sort.by(Sort.Direction.ASC, "createdAt"); // 추후 price 필드 추가 시 변경
            case "price_high" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
