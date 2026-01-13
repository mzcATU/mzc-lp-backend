package com.mzc.lp.domain.assignment.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.assignment.dto.request.CreateAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.request.GradeSubmissionRequest;
import com.mzc.lp.domain.assignment.dto.request.UpdateAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.response.AssignmentDetailResponse;
import com.mzc.lp.domain.assignment.dto.response.AssignmentResponse;
import com.mzc.lp.domain.assignment.dto.response.SubmissionResponse;
import com.mzc.lp.domain.assignment.service.AssignmentService;
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

/**
 * 과제 관리 API (강사/관리자용)
 */
@RestController
@RequiredArgsConstructor
@Validated
public class AssignmentController {

    private final AssignmentService assignmentService;

    // ========== 과제 CRUD ==========

    /**
     * 과제 생성
     * POST /api/ta/course-times/{courseTimeId}/assignments
     */
    @PostMapping("/api/ta/course-times/{courseTimeId}/assignments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @PathVariable Long courseTimeId,
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AssignmentResponse response = assignmentService.createAssignment(courseTimeId, request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 과제 목록 조회
     * GET /api/ta/course-times/{courseTimeId}/assignments
     */
    @GetMapping("/api/ta/course-times/{courseTimeId}/assignments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAssignments(
            @PathVariable Long courseTimeId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AssignmentResponse> response = assignmentService.getAssignments(courseTimeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 과제 상세 조회
     * GET /api/ta/assignments/{assignmentId}
     */
    @GetMapping("/api/ta/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> getAssignmentDetail(
            @PathVariable Long assignmentId
    ) {
        AssignmentDetailResponse response = assignmentService.getAssignmentDetail(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 과제 수정
     * PUT /api/ta/assignments/{assignmentId}
     */
    @PutMapping("/api/ta/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody UpdateAssignmentRequest request
    ) {
        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 과제 삭제
     * DELETE /api/ta/assignments/{assignmentId}
     */
    @DeleteMapping("/api/ta/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long assignmentId
    ) {
        assignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    // ========== 과제 상태 전이 ==========

    /**
     * 과제 발행
     * POST /api/ta/assignments/{assignmentId}/publish
     */
    @PostMapping("/api/ta/assignments/{assignmentId}/publish")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> publishAssignment(
            @PathVariable Long assignmentId
    ) {
        AssignmentResponse response = assignmentService.publishAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 과제 마감
     * POST /api/ta/assignments/{assignmentId}/close
     */
    @PostMapping("/api/ta/assignments/{assignmentId}/close")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> closeAssignment(
            @PathVariable Long assignmentId
    ) {
        AssignmentResponse response = assignmentService.closeAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 제출물 관리 ==========

    /**
     * 제출물 목록 조회
     * GET /api/ta/assignments/{assignmentId}/submissions
     */
    @GetMapping("/api/ta/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Page<SubmissionResponse>>> getSubmissions(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<SubmissionResponse> response = assignmentService.getSubmissions(assignmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 채점
     * POST /api/ta/submissions/{submissionId}/grade
     */
    @PostMapping("/api/ta/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SubmissionResponse response = assignmentService.gradeSubmission(submissionId, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
