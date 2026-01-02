package com.mzc.lp.domain.snapshot.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotDetailResponse;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotResponse;
import com.mzc.lp.domain.snapshot.service.SnapshotService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class SnapshotController {

    private final SnapshotService snapshotService;

    /**
     * Course(템플릿)에서 스냅샷 생성
     * POST /api/courses/{courseId}/snapshots
     */
    @PostMapping("/api/courses/{courseId}/snapshots")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotDetailResponse>> createSnapshotFromCourse(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotDetailResponse response = snapshotService.createSnapshotFromCourse(courseId, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Course의 스냅샷 목록 조회
     * GET /api/courses/{courseId}/snapshots
     */
    @GetMapping("/api/courses/{courseId}/snapshots")
    public ResponseEntity<ApiResponse<List<SnapshotResponse>>> getSnapshotsByCourse(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SnapshotResponse> response = snapshotService.getSnapshotsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 신규 스냅샷 직접 생성
     * POST /api/snapshots
     */
    @PostMapping("/api/snapshots")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotResponse>> createSnapshot(
            @Valid @RequestBody CreateSnapshotRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotResponse response = snapshotService.createSnapshot(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 스냅샷 목록 조회 (페이징, 상태/생성자 필터)
     * GET /api/snapshots
     */
    @GetMapping("/api/snapshots")
    public ResponseEntity<ApiResponse<Page<SnapshotResponse>>> getSnapshots(
            @RequestParam(required = false) SnapshotStatus status,
            @RequestParam(required = false) Long createdBy,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<SnapshotResponse> response = snapshotService.getSnapshots(status, createdBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스냅샷 상세 조회
     * GET /api/snapshots/{snapshotId}
     */
    @GetMapping("/api/snapshots/{snapshotId}")
    public ResponseEntity<ApiResponse<SnapshotDetailResponse>> getSnapshotDetail(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotDetailResponse response = snapshotService.getSnapshotDetail(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스냅샷 수정
     * PUT /api/snapshots/{snapshotId}
     */
    @PutMapping("/api/snapshots/{snapshotId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotResponse>> updateSnapshot(
            @PathVariable @Positive Long snapshotId,
            @Valid @RequestBody UpdateSnapshotRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotResponse response = snapshotService.updateSnapshot(snapshotId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스냅샷 삭제
     * DELETE /api/snapshots/{snapshotId}
     */
    @DeleteMapping("/api/snapshots/{snapshotId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteSnapshot(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        snapshotService.deleteSnapshot(snapshotId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 스냅샷 발행 (DRAFT → ACTIVE)
     * POST /api/snapshots/{snapshotId}/publish
     */
    @PostMapping("/api/snapshots/{snapshotId}/publish")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotResponse>> publishSnapshot(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotResponse response = snapshotService.publishSnapshot(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스냅샷 완료 (ACTIVE → COMPLETED)
     * POST /api/snapshots/{snapshotId}/complete
     */
    @PostMapping("/api/snapshots/{snapshotId}/complete")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotResponse>> completeSnapshot(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotResponse response = snapshotService.completeSnapshot(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스냅샷 보관 (COMPLETED → ARCHIVED)
     * POST /api/snapshots/{snapshotId}/archive
     */
    @PostMapping("/api/snapshots/{snapshotId}/archive")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotResponse>> archiveSnapshot(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotResponse response = snapshotService.archiveSnapshot(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
