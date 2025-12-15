package com.mzc.lp.domain.course.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.course.dto.request.CreateCourseRequest;
import com.mzc.lp.domain.course.dto.request.UpdateCourseRequest;
import com.mzc.lp.domain.course.dto.response.CourseDetailResponse;
import com.mzc.lp.domain.course.dto.response.CourseResponse;
import com.mzc.lp.domain.course.service.CourseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Validated
public class CourseController {

    private final CourseService courseService;

    /**
     * 강의 생성
     * POST /api/courses
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 강의 목록 조회 (페이징, 키워드 검색, 카테고리 필터)
     * GET /api/courses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<CourseResponse> response = courseService.getCourses(keyword, categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 강의 상세 조회
     * GET /api/courses/{courseId}
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseDetailResponse response = courseService.getCourseDetail(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 강의 수정
     * PUT /api/courses/{courseId}
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody UpdateCourseRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 강의 삭제
     * DELETE /api/courses/{courseId}
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
