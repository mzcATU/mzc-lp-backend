package com.mzc.lp.domain.tu.controller;

import com.mzc.lp.domain.tu.dto.response.CourseExploreResponse;
import com.mzc.lp.domain.tu.service.TuCourseExploreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TU(Tenant User)용 강의 탐색 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tu/courses")
public class TuCourseExploreController {

    private final TuCourseExploreService tuCourseExploreService;

    /**
     * 강의 목록 조회 (탐색용)
     * GET /api/tu/courses
     */
    @GetMapping
    public ResponseEntity<CourseExploreResponse> getCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        CourseExploreResponse response = tuCourseExploreService.getCourses(
                search, category, level, sortBy, page, pageSize
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 인기 강의 조회
     * GET /api/tu/courses/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<CourseExploreResponse> getPopularCourses(
            @RequestParam(defaultValue = "10") int limit
    ) {
        CourseExploreResponse response = tuCourseExploreService.getPopularCourses(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 신규 강의 조회
     * GET /api/tu/courses/new
     */
    @GetMapping("/new")
    public ResponseEntity<CourseExploreResponse> getNewCourses(
            @RequestParam(defaultValue = "10") int limit
    ) {
        CourseExploreResponse response = tuCourseExploreService.getNewCourses(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 추천 강의 조회
     * GET /api/tu/courses/recommended
     */
    @GetMapping("/recommended")
    public ResponseEntity<CourseExploreResponse> getRecommendedCourses(
            @RequestParam(defaultValue = "10") int limit
    ) {
        CourseExploreResponse response = tuCourseExploreService.getRecommendedCourses(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리 목록 조회
     * GET /api/tu/courses/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Object> getCategories() {
        // 임시 더미 카테고리
        var categories = java.util.List.of(
                java.util.Map.of("id", "all", "name", "전체", "count", 12),
                java.util.Map.of("id", "dev", "name", "개발", "count", 4),
                java.util.Map.of("id", "ai", "name", "AI", "count", 2),
                java.util.Map.of("id", "data", "name", "데이터", "count", 1),
                java.util.Map.of("id", "design", "name", "디자인", "count", 2),
                java.util.Map.of("id", "business", "name", "비즈니스", "count", 1),
                java.util.Map.of("id", "marketing", "name", "마케팅", "count", 1),
                java.util.Map.of("id", "language", "name", "외국어", "count", 1)
        );
        return ResponseEntity.ok(java.util.Map.of("categories", categories));
    }
}
