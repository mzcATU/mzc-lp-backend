package com.mzc.lp.domain.content.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.dto.request.CreateExternalLinkRequest;
import com.mzc.lp.domain.content.dto.request.UpdateContentRequest;
import com.mzc.lp.domain.content.dto.response.ContentListResponse;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import com.mzc.lp.domain.content.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Validated
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        ContentResponse response = contentService.uploadFile(file, folderId, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/external-link")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentResponse>> createExternalLink(
            @Valid @RequestBody CreateExternalLinkRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        ContentResponse response = contentService.createExternalLink(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ContentListResponse>>> getContents(
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Page<ContentListResponse> response = contentService.getContents(
                tenantId, type, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentResponse>> getContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        ContentResponse response = contentService.getContent(contentId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{contentId}/stream")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Resource> streamContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Resource resource = contentService.getFileAsResource(contentId, tenantId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/{contentId}/download")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Resource> downloadContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        ContentService.ContentDownloadInfo downloadInfo =
                contentService.getFileForDownload(contentId, tenantId);

        String encodedFileName = URLEncoder.encode(downloadInfo.originalFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(downloadInfo.resource());
    }

    @GetMapping("/{contentId}/preview")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Resource> previewContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        ContentService.ContentDownloadInfo downloadInfo =
                contentService.getFileForDownload(contentId, tenantId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(downloadInfo.resource());
    }

    @PutMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentResponse>> updateContent(
            @PathVariable Long contentId,
            @Valid @RequestBody UpdateContentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        ContentResponse response = contentService.updateContent(contentId, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{contentId}/file")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ContentResponse>> replaceFile(
            @PathVariable Long contentId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        ContentResponse response = contentService.replaceFile(contentId, file, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        contentService.deleteContent(contentId, tenantId);
        return ResponseEntity.noContent().build();
    }

    // ========== DESIGNER용 API (본인 콘텐츠 관리) ==========

    @GetMapping("/my")
    @PreAuthorize("hasRole('DESIGNER')")
    public ResponseEntity<ApiResponse<Page<ContentListResponse>>> getMyContents(
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        Page<ContentListResponse> response = contentService.getMyContents(
                tenantId, userId, status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{contentId}/archive")
    @PreAuthorize("hasRole('DESIGNER')")
    public ResponseEntity<ApiResponse<ContentResponse>> archiveContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        ContentResponse response = contentService.archiveContent(contentId, tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{contentId}/restore")
    @PreAuthorize("hasRole('DESIGNER')")
    public ResponseEntity<ApiResponse<ContentResponse>> restoreContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();
        ContentResponse response = contentService.restoreContent(contentId, tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
