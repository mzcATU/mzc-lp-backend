package com.mzc.lp.domain.learning.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.learning.dto.request.CreateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.MoveContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.response.ContentFolderResponse;
import com.mzc.lp.domain.learning.service.ContentFolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content-folders")
@RequiredArgsConstructor
@Validated
public class ContentFolderController {

    private final ContentFolderService contentFolderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentFolderResponse>> create(
            @Valid @RequestBody CreateContentFolderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ContentFolderResponse response = contentFolderService.create(request, principal.tenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<ContentFolderResponse>>> getFolderTree(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ContentFolderResponse> response = contentFolderService.getFolderTree(principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{folderId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentFolderResponse>> getFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ContentFolderResponse response = contentFolderService.getFolder(folderId, principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{folderId}/children")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<ContentFolderResponse>>> getChildFolders(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ContentFolderResponse> response = contentFolderService.getChildFolders(
                folderId, principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{folderId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentFolderResponse>> update(
            @PathVariable Long folderId,
            @Valid @RequestBody UpdateContentFolderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ContentFolderResponse response = contentFolderService.update(
                folderId, request, principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{folderId}/move")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentFolderResponse>> move(
            @PathVariable Long folderId,
            @Valid @RequestBody MoveContentFolderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ContentFolderResponse response = contentFolderService.move(
                folderId, request, principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{folderId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        contentFolderService.delete(folderId, principal.tenantId());
        return ResponseEntity.noContent().build();
    }
}
