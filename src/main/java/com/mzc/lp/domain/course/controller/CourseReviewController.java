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
@RequestMapping("/api/times/{timeId}/reviews")
@RequiredArgsConstructor
@Validated
public class CourseReviewController {

    private final CourseReviewService reviewService;

    /**
     * 리뷰 작성
     * POST /api/times/{timeId}/reviews
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResponse>> createReview(
            @PathVariable Long timeId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseReviewResponse response = reviewService.createReview(timeId, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 리뷰 목록 조회
     * GET /api/times/{timeId}/reviews
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CourseReviewListResponse>> getReviews(
            @PathVariable Long timeId,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        CourseReviewListResponse response = reviewService.getReviews(timeId, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 통계 조회
     * GET /api/times/{timeId}/reviews/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CourseReviewStatsResponse>> getReviewStats(
            @PathVariable Long timeId
    ) {
        CourseReviewStatsResponse response = reviewService.getReviewStats(timeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 리뷰 조회
     * GET /api/times/{timeId}/reviews/my
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResponse>> getMyReview(
            @PathVariable Long timeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseReviewResponse response = reviewService.getMyReview(timeId, principal.id());
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 수정
     * PUT/PATCH /api/times/{timeId}/reviews/{reviewId}
     */
    @RequestMapping(value = "/{reviewId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResponse>> updateReview(
            @PathVariable Long timeId,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseReviewResponse response = reviewService.updateReview(timeId, reviewId, principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 리뷰 삭제
     * DELETE /api/times/{timeId}/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long timeId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = "TENANT_ADMIN".equals(principal.role())
                || "SYSTEM_ADMIN".equals(principal.role());
        reviewService.deleteReview(timeId, reviewId, principal.id(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
