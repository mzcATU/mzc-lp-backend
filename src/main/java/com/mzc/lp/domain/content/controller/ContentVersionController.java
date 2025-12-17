package com.mzc.lp.domain.content.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.content.dto.request.RestoreVersionRequest;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import com.mzc.lp.domain.content.dto.response.ContentVersionResponse;
import com.mzc.lp.domain.content.service.ContentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents/{contentId}/versions")
@RequiredArgsConstructor
public class ContentVersionController {

    private final ContentVersionService contentVersionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<ContentVersionResponse>>> getVersionHistory(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        List<ContentVersionResponse> response = contentVersionService.getVersionHistory(contentId, tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{versionNumber}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentVersionResponse>> getVersion(
            @PathVariable Long contentId,
            @PathVariable Integer versionNumber,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        ContentVersionResponse response = contentVersionService.getVersion(contentId, versionNumber, tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{versionNumber}/restore")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentResponse>> restoreVersion(
            @PathVariable Long contentId,
            @PathVariable Integer versionNumber,
            @RequestBody(required = false) RestoreVersionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        String changeSummary = request != null ? request.changeSummary() : null;
        ContentResponse response = contentVersionService.restoreVersion(contentId, versionNumber, tenantId, userId, changeSummary);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
