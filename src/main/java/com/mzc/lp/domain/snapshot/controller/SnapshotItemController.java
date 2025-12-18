package com.mzc.lp.domain.snapshot.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.MoveSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotItemResponse;
import com.mzc.lp.domain.snapshot.service.SnapshotItemService;
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
@RequestMapping("/api/snapshots/{snapshotId}/items")
@RequiredArgsConstructor
@Validated
public class SnapshotItemController {

    private final SnapshotItemService snapshotItemService;

    /**
     * 아이템 계층 구조 조회
     * GET /api/snapshots/{snapshotId}/items
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SnapshotItemResponse>>> getHierarchy(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SnapshotItemResponse> response = snapshotItemService.getHierarchy(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 아이템 평면 목록 조회
     * GET /api/snapshots/{snapshotId}/items/flat
     */
    @GetMapping("/flat")
    public ResponseEntity<ApiResponse<List<SnapshotItemResponse>>> getFlatItems(
            @PathVariable @Positive Long snapshotId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SnapshotItemResponse> response = snapshotItemService.getFlatItems(snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 아이템 추가 (DRAFT 상태에서만)
     * POST /api/snapshots/{snapshotId}/items
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotItemResponse>> createItem(
            @PathVariable @Positive Long snapshotId,
            @Valid @RequestBody CreateSnapshotItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotItemResponse response = snapshotItemService.createItem(snapshotId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 아이템 수정 (이름 변경)
     * PUT /api/snapshots/{snapshotId}/items/{itemId}
     */
    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotItemResponse>> updateItem(
            @PathVariable @Positive Long snapshotId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody UpdateSnapshotItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotItemResponse response = snapshotItemService.updateItem(snapshotId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 아이템 이동 (DRAFT 상태에서만)
     * PUT /api/snapshots/{snapshotId}/items/{itemId}/move
     */
    @PutMapping("/{itemId}/move")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SnapshotItemResponse>> moveItem(
            @PathVariable @Positive Long snapshotId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody MoveSnapshotItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SnapshotItemResponse response = snapshotItemService.moveItem(snapshotId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 아이템 삭제 (DRAFT 상태에서만)
     * DELETE /api/snapshots/{snapshotId}/items/{itemId}
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable @Positive Long snapshotId,
            @PathVariable @Positive Long itemId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        snapshotItemService.deleteItem(snapshotId, itemId);
        return ResponseEntity.noContent().build();
    }
}
