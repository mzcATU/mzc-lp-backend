package com.mzc.lp.domain.program.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.dto.request.ApproveRequest;
import com.mzc.lp.domain.program.dto.request.CreateProgramRequest;
import com.mzc.lp.domain.program.dto.request.RejectRequest;
import com.mzc.lp.domain.program.dto.request.UpdateProgramRequest;
import com.mzc.lp.domain.program.dto.response.PendingProgramResponse;
import com.mzc.lp.domain.program.dto.response.ProgramDetailResponse;
import com.mzc.lp.domain.program.dto.response.ProgramResponse;
import com.mzc.lp.domain.program.service.ProgramService;
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
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService programService;

    /**
     * 프로그램(강의 개설) 생성
     * POST /api/programs
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramResponse>> createProgram(
            @Valid @RequestBody CreateProgramRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramResponse response = programService.createProgram(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 프로그램 목록 조회
     * GET /api/programs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProgramResponse>>> getPrograms(
            @RequestParam(required = false) ProgramStatus status,
            @RequestParam(required = false) Long createdBy,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<ProgramResponse> response = programService.getPrograms(status, createdBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로그램 상세 조회
     * GET /api/programs/{programId}
     */
    @GetMapping("/{programId}")
    public ResponseEntity<ApiResponse<ProgramDetailResponse>> getProgram(
            @PathVariable @Positive Long programId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramDetailResponse response = programService.getProgram(programId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로그램 수정
     * PUT /api/programs/{programId}
     */
    @PutMapping("/{programId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramResponse>> updateProgram(
            @PathVariable @Positive Long programId,
            @Valid @RequestBody UpdateProgramRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramResponse response = programService.updateProgram(programId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로그램 삭제
     * DELETE /api/programs/{programId}
     */
    @DeleteMapping("/{programId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable @Positive Long programId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isTenantAdmin = "TENANT_ADMIN".equals(principal.role());
        programService.deleteProgram(programId, principal.id(), isTenantAdmin);
        return ResponseEntity.noContent().build();
    }

    /**
     * 프로그램 개설 신청 (DRAFT → PENDING)
     * POST /api/programs/{programId}/submit
     */
    @PostMapping("/{programId}/submit")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramResponse>> submitProgram(
            @PathVariable @Positive Long programId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramResponse response = programService.submitProgram(programId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 검토 대기 프로그램 목록 조회 (OPERATOR용)
     * GET /api/programs/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PendingProgramResponse>>> getPendingPrograms(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<PendingProgramResponse> response = programService.getPendingPrograms(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로그램 승인 (PENDING → APPROVED)
     * POST /api/programs/{programId}/approve
     */
    @PostMapping("/{programId}/approve")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramDetailResponse>> approveProgram(
            @PathVariable @Positive Long programId,
            @Valid @RequestBody(required = false) ApproveRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramDetailResponse response = programService.approveProgram(programId, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로그램 반려 (PENDING → REJECTED)
     * POST /api/programs/{programId}/reject
     */
    @PostMapping("/{programId}/reject")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramDetailResponse>> rejectProgram(
            @PathVariable @Positive Long programId,
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramDetailResponse response = programService.rejectProgram(programId, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로그램 종료
     * POST /api/programs/{programId}/close
     */
    @PostMapping("/{programId}/close")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramResponse>> closeProgram(
            @PathVariable @Positive Long programId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramResponse response = programService.closeProgram(programId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스냅샷 연결
     * POST /api/programs/{programId}/snapshot
     */
    @PostMapping("/{programId}/snapshot")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ProgramResponse>> linkSnapshot(
            @PathVariable @Positive Long programId,
            @RequestParam @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgramResponse response = programService.linkSnapshot(programId, snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 프로그램 목록 조회 (로드맵 생성용)
     * GET /api/programs/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('DESIGNER')")
    public ResponseEntity<ApiResponse<Page<ProgramResponse>>> getMyPrograms(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<ProgramResponse> response = programService.getMyPrograms(
                principal.id(),
                search,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
