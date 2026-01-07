package com.mzc.lp.domain.student.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.student.dto.request.UpdateItemProgressRequest;
import com.mzc.lp.domain.student.dto.response.ItemProgressResponse;
import com.mzc.lp.domain.student.service.ItemProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollments/{enrollmentId}/items")
public class ItemProgressController {

    private final ItemProgressService itemProgressService;

    /**
     * 수강별 전체 아이템 진도 목록 조회
     */
    @GetMapping("/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ItemProgressResponse>>> getItemProgressList(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = hasAdminRole(principal);
        List<ItemProgressResponse> response = itemProgressService.getItemProgressList(
                enrollmentId, principal.id(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 아이템 진도 조회
     */
    @GetMapping("/{itemId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItemProgressResponse>> getItemProgress(
            @PathVariable Long enrollmentId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = hasAdminRole(principal);
        ItemProgressResponse response = itemProgressService.getItemProgress(
                enrollmentId, itemId, principal.id(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 아이템 진도 업데이트
     */
    @PatchMapping("/{itemId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItemProgressResponse>> updateItemProgress(
            @PathVariable Long enrollmentId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemProgressRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = hasAdminRole(principal);
        ItemProgressResponse response = itemProgressService.updateItemProgress(
                enrollmentId, itemId, request, principal.id(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 아이템 완료 처리
     */
    @PostMapping("/{itemId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItemProgressResponse>> markItemComplete(
            @PathVariable Long enrollmentId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = hasAdminRole(principal);
        ItemProgressResponse response = itemProgressService.markItemComplete(
                enrollmentId, itemId, principal.id(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 관리자 역할 여부 확인 (OPERATOR, TENANT_ADMIN)
     */
    private boolean hasAdminRole(UserPrincipal principal) {
        String role = principal.role();
        return "OPERATOR".equals(role) || "TENANT_ADMIN".equals(role);
    }
}