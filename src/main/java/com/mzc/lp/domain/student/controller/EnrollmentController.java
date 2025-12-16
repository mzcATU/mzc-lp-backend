package com.mzc.lp.domain.student.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.dto.request.CompleteEnrollmentRequest;
import com.mzc.lp.domain.student.dto.request.ForceEnrollRequest;
import com.mzc.lp.domain.student.dto.request.UpdateEnrollmentStatusRequest;
import com.mzc.lp.domain.student.dto.request.UpdateProgressRequest;
import com.mzc.lp.domain.student.dto.response.CourseTimeEnrollmentStatsResponse;
import com.mzc.lp.domain.student.dto.response.EnrollmentDetailResponse;
import com.mzc.lp.domain.student.dto.response.EnrollmentResponse;
import com.mzc.lp.domain.student.dto.response.ForceEnrollResultResponse;
import com.mzc.lp.domain.student.dto.response.UserEnrollmentStatsResponse;
import com.mzc.lp.domain.student.service.EnrollmentService;
import com.mzc.lp.domain.student.service.EnrollmentStatsService;
import jakarta.validation.Valid;
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
@RequiredArgsConstructor
@Validated
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentStatsService enrollmentStatsService;

    // ========== 수강 신청 API (TS 기반) ==========

    /**
     * 수강 신청
     */
    @PostMapping("/api/times/{courseTimeId}/enrollments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> enroll(
            @PathVariable Long courseTimeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        EnrollmentDetailResponse response = enrollmentService.enroll(courseTimeId, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 강제 배정 (필수 교육)
     */
    @PostMapping("/api/times/{courseTimeId}/enrollments/force")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ForceEnrollResultResponse>> forceEnroll(
            @PathVariable Long courseTimeId,
            @Valid @RequestBody ForceEnrollRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ForceEnrollResultResponse response = enrollmentService.forceEnroll(courseTimeId, request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 차수별 수강생 목록 조회
     */
    @GetMapping("/api/times/{courseTimeId}/enrollments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getEnrollmentsByCourseTime(
            @PathVariable Long courseTimeId,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByCourseTime(courseTimeId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 수강 관리 API ==========

    /**
     * 수강 상세 조회
     */
    @GetMapping("/api/enrollments/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> getEnrollment(
            @PathVariable Long enrollmentId
    ) {
        EnrollmentDetailResponse response = enrollmentService.getEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 진도율 업데이트
     */
    @PatchMapping("/api/enrollments/{enrollmentId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateProgress(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody UpdateProgressRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = hasAdminRole(principal);
        EnrollmentResponse response = enrollmentService.updateProgress(enrollmentId, request, principal.id(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 수료 처리
     */
    @PatchMapping("/api/enrollments/{enrollmentId}/complete")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> completeEnrollment(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody CompleteEnrollmentRequest request
    ) {
        EnrollmentDetailResponse response = enrollmentService.completeEnrollment(enrollmentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상태 변경 (관리자)
     */
    @PatchMapping("/api/enrollments/{enrollmentId}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> updateStatus(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request
    ) {
        EnrollmentDetailResponse response = enrollmentService.updateStatus(enrollmentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 수강 취소
     */
    @DeleteMapping("/api/enrollments/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelEnrollment(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = hasAdminRole(principal);
        enrollmentService.cancelEnrollment(enrollmentId, principal.id(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    // ========== 사용자별 수강 조회 API ==========

    /**
     * 내 수강 목록 조회
     */
    @GetMapping("/api/users/me/enrollments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getMyEnrollments(
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<EnrollmentResponse> response = enrollmentService.getMyEnrollments(principal.id(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자별 수강 이력 조회 (관리자)
     */
    @GetMapping("/api/users/{userId}/enrollments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getEnrollmentsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EnrollmentResponse> response = enrollmentService.getEnrollmentsByUser(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 통계 API ==========

    /**
     * 차수별 수강 통계 조회
     */
    @GetMapping("/api/times/{courseTimeId}/enrollments/stats")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeEnrollmentStatsResponse>> getCourseTimeStats(
            @PathVariable Long courseTimeId
    ) {
        CourseTimeEnrollmentStatsResponse response = enrollmentStatsService.getCourseTimeStats(courseTimeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자별 수강 통계 조회
     */
    @GetMapping("/api/users/{userId}/enrollments/stats")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserEnrollmentStatsResponse>> getUserStats(
            @PathVariable Long userId
    ) {
        UserEnrollmentStatsResponse response = enrollmentStatsService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Helper Methods ==========

    /**
     * 관리자 역할 여부 확인 (OPERATOR, TENANT_ADMIN)
     */
    private boolean hasAdminRole(UserPrincipal principal) {
        String role = principal.role();
        return "OPERATOR".equals(role) || "TENANT_ADMIN".equals(role);
    }
}
