package com.mzc.lp.domain.snapshot.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRelationRequest;
import com.mzc.lp.domain.snapshot.dto.request.SetStartSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotRelationResponse;
import com.mzc.lp.domain.snapshot.service.SnapshotRelationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snapshots/{snapshotId}/relations")
@RequiredArgsConstructor
@Validated
public class SnapshotRelationController {

    private final SnapshotRelationService snapshotRelationService;

    /**
     * 연결 목록 조회
     * GET /api/snapshots/{snapshotId}/relations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SnapshotRelationResponse.SnapshotRelationsResponse>> getRelations(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotRelationResponse.SnapshotRelationsResponse response = snapshotRelationService.getRelations(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 순서대로 아이템 조회
     * GET /api/snapshots/{snapshotId}/relations/ordered
     */
    @GetMapping("/ordered")
    public ResponseEntity<ApiResponse<List<SnapshotRelationResponse.OrderedItem>>> getOrderedItems(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SnapshotRelationResponse.OrderedItem> response = snapshotRelationService.getOrderedItems(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 연결 생성
     * POST /api/snapshots/{snapshotId}/relations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotRelationResponse>> createRelation(
            @PathVariable @Positive Long snapshotId,
            @Valid @RequestBody CreateSnapshotRelationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotRelationResponse response = snapshotRelationService.createRelation(snapshotId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 시작점 설정
     * PUT /api/snapshots/{snapshotId}/relations/start
     */
    @PutMapping("/start")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotRelationResponse>> setStartItem(
            @PathVariable @Positive Long snapshotId,
            @Valid @RequestBody SetStartSnapshotItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotRelationResponse response = snapshotRelationService.setStartItem(snapshotId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 연결 삭제
     * DELETE /api/snapshots/{snapshotId}/relations/{relationId}
     */
    @DeleteMapping("/{relationId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteRelation(
            @PathVariable @Positive Long snapshotId,
            @PathVariable @Positive Long relationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        snapshotRelationService.deleteRelation(snapshotId, relationId);
        return ResponseEntity.noContent().build();
    }
}
