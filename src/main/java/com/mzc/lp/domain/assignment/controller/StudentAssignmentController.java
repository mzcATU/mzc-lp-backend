package com.mzc.lp.domain.assignment.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.assignment.dto.request.SubmitAssignmentRequest;
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
 * 과제 API (학생용)
 */
@RestController
@RequiredArgsConstructor
@Validated
public class StudentAssignmentController {

    private final AssignmentService assignmentService;

    /**
     * 내 과제 목록 조회
     * GET /api/tu/course-times/{courseTimeId}/assignments
     */
    @GetMapping("/api/tu/course-times/{courseTimeId}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getMyAssignments(
            @PathVariable Long courseTimeId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AssignmentResponse> response = assignmentService.getPublishedAssignments(
                courseTimeId, principal.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 과제 상세 조회
     * GET /api/tu/assignments/{assignmentId}
     */
    @GetMapping("/api/tu/assignments/{assignmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> getAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AssignmentDetailResponse response = assignmentService.getAssignmentForStudent(
                assignmentId, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 과제 제출
     * POST /api/tu/assignments/{assignmentId}/submit
     */
    @PostMapping("/api/tu/assignments/{assignmentId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmitAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SubmissionResponse response = assignmentService.submitAssignment(
                assignmentId, request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 내 제출물 조회
     * GET /api/tu/assignments/{assignmentId}/my-submission
     */
    @GetMapping("/api/tu/assignments/{assignmentId}/my-submission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getMySubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SubmissionResponse response = assignmentService.getMySubmission(assignmentId, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
