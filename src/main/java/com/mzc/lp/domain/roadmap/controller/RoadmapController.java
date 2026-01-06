package com.mzc.lp.domain.roadmap.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.dto.request.CreateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.request.SaveDraftRequest;
import com.mzc.lp.domain.roadmap.dto.request.UpdateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapDetailResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapStatisticsResponse;
import com.mzc.lp.domain.roadmap.service.RoadmapService;
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

/**
 * 로드맵 Controller
 */
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    /**
     * 로드맵 생성
     * POST /api/roadmaps
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<ApiResponse<RoadmapResponse>> createRoadmap(
            @Valid @RequestBody CreateRoadmapRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapResponse response = roadmapService.createRoadmap(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 내 로드맵 목록 조회 (필터링, 정렬 지원)
     * GET /api/roadmaps?status=draft&sortBy=updatedAt
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<ApiResponse<Page<RoadmapResponse>>> getMyRoadmaps(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapStatus roadmapStatus = status != null
                ? RoadmapStatus.valueOf(status.toUpperCase())
                : null;

        Page<RoadmapResponse> response = roadmapService.getMyRoadmaps(
                principal.id(),
                roadmapStatus,
                sortBy,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로드맵 상세 조회
     * GET /api/roadmaps/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoadmapDetailResponse>> getRoadmap(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapDetailResponse response = roadmapService.getRoadmap(id, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로드맵 수정 (전체 검증)
     * PATCH /api/roadmaps/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<ApiResponse<RoadmapResponse>> updateRoadmap(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateRoadmapRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapResponse response = roadmapService.updateRoadmap(id, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로드맵 임시저장 (최소 검증)
     * PATCH /api/roadmaps/{id}/draft
     */
    @PatchMapping("/{id}/draft")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<ApiResponse<RoadmapResponse>> saveDraft(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SaveDraftRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapResponse response = roadmapService.saveDraft(id, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로드맵 삭제
     * DELETE /api/roadmaps/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<Void> deleteRoadmap(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        roadmapService.deleteRoadmap(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    /**
     * 로드맵 복제
     * POST /api/roadmaps/{id}/duplicate
     */
    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<ApiResponse<RoadmapResponse>> duplicateRoadmap(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapResponse response = roadmapService.duplicateRoadmap(id, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 통계 조회
     * GET /api/roadmaps/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OWNER')")
    public ResponseEntity<ApiResponse<RoadmapStatisticsResponse>> getStatistics(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RoadmapStatisticsResponse response = roadmapService.getStatistics(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
