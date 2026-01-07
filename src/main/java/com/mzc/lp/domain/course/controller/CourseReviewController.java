package com.mzc.lp.domain.course.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.course.dto.request.CreateReviewRequest;
import com.mzc.lp.domain.course.dto.request.UpdateReviewRequest;
import com.mzc.lp.domain.course.dto.response.CourseReviewListResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewStatsResponse;
import com.mzc.lp.domain.course.service.CourseReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
@RequiredArgsConstructor
@Validated
public class CourseReviewController {

    private final CourseReviewService reviewService;

    /**
     * 리뷰 작성
     * POST /api/courses/{courseId}/reviews
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResponse>> createReview(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseReviewResponse response = reviewService.createReview(courseId, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 리뷰 목록 조회
     * GET /api/courses/{courseId}/reviews
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CourseReviewListResponse>> getReviews(
            @PathVariable Long courseId,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize
    ) {
        CourseReviewListResponse response = reviewService.getReviews(courseId, sortBy, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 통계 조회
     * GET /api/courses/{courseId}/reviews/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CourseReviewStatsResponse>> getReviewStats(
            @PathVariable Long courseId
    ) {
        CourseReviewStatsResponse response = reviewService.getReviewStats(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 리뷰 조회
     * GET /api/courses/{courseId}/reviews/my
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResponse>> getMyReview(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseReviewResponse response = reviewService.getMyReview(courseId, principal.id());
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 수정
     * PUT /api/courses/{courseId}/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResponse>> updateReview(
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseReviewResponse response = reviewService.updateReview(courseId, reviewId, principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 삭제
     * DELETE /api/courses/{courseId}/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = "TENANT_ADMIN".equals(principal.role())
                || "SYSTEM_ADMIN".equals(principal.role());
        reviewService.deleteReview(courseId, reviewId, principal.id(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
