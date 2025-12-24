package com.mzc.lp.domain.iis.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.AssignmentHistoryResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorDetailStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatisticsResponse;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class InstructorAssignmentController {

    private final InstructorAssignmentService assignmentService;

    // ========== 배정 단건 API ==========
    // 차수 기준 API는 CourseTimeInstructorController에서 처리 (차수 상태 검증 포함)

    @GetMapping("/api/instructor-assignments/{id}")
    public ResponseEntity<ApiResponse<InstructorAssignmentResponse>> getAssignment(
            @PathVariable Long id
    ) {
        InstructorAssignmentResponse response = assignmentService.getAssignment(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/instructor-assignments/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorAssignmentResponse>> updateRole(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        InstructorAssignmentResponse response = assignmentService.updateRole(id, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/instructor-assignments/{id}/replace")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorAssignmentResponse>> replaceInstructor(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReplaceInstructorRequest request
    ) {
        InstructorAssignmentResponse response = assignmentService.replaceInstructor(id, request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/api/instructor-assignments/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> cancelAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) CancelAssignmentRequest request
    ) {
        assignmentService.cancelAssignment(id,
                request != null ? request : new CancelAssignmentRequest(null),
                principal.id());
        return ResponseEntity.noContent().build();
    }

    // ========== 이력 조회 API ==========

    @GetMapping("/api/instructor-assignments/{id}/histories")
    public ResponseEntity<ApiResponse<List<AssignmentHistoryResponse>>> getAssignmentHistories(
            @PathVariable Long id,
            @RequestParam(required = false) AssignmentAction action
    ) {
        List<AssignmentHistoryResponse> response = assignmentService.getAssignmentHistories(id, action);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 사용자 기준 API ==========

    @GetMapping("/api/users/{userId}/instructor-assignments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<InstructorAssignmentResponse>>> getAssignmentsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) AssignmentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<InstructorAssignmentResponse> response = assignmentService.getAssignmentsByUserId(
                userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/users/me/instructor-assignments")
    public ResponseEntity<ApiResponse<List<InstructorAssignmentResponse>>> getMyAssignments(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<InstructorAssignmentResponse> response = assignmentService.getMyAssignments(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 통계 API ==========

    @GetMapping("/api/instructor-assignments/statistics")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorStatisticsResponse>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        InstructorStatisticsResponse response = assignmentService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/users/me/instructor-statistics")
    public ResponseEntity<ApiResponse<InstructorDetailStatResponse>> getMyInstructorStatistics(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        InstructorDetailStatResponse response = assignmentService.getInstructorDetailStatistics(
                principal.id(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/users/{userId}/instructor-statistics")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorDetailStatResponse>> getInstructorStatistics(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        InstructorDetailStatResponse response = assignmentService.getInstructorDetailStatistics(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
