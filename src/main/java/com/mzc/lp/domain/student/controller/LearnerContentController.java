package com.mzc.lp.domain.student.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.content.service.ContentService;
import com.mzc.lp.domain.student.service.LearnerContentAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 학습자용 콘텐츠 스트리밍/다운로드 컨트롤러
 * 수강 신청한 강의의 콘텐츠에 대한 접근 권한을 검증 후 파일 제공
 */
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearnerContentController {

    private final LearnerContentAccessService learnerContentAccessService;
    private final ContentService contentService;

    /**
     * 학습자용 콘텐츠 스트리밍
     * - 수강 신청한 강의의 콘텐츠만 접근 가능
     */
    @GetMapping("/contents/{contentId}/stream")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> streamContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();

        // 접근 권한 검증
        learnerContentAccessService.validateContentAccess(contentId, userId, tenantId);

        // 콘텐츠 파일 스트리밍
        ContentService.ContentDownloadInfo downloadInfo =
                contentService.getFileForPreview(contentId, tenantId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(downloadInfo.resource());
    }

    /**
     * 학습자용 콘텐츠 다운로드
     * - 수강 신청한 강의의 콘텐츠만 접근 가능
     * - 다운로드 허용된 콘텐츠만 다운로드 가능
     */
    @GetMapping("/contents/{contentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Long userId = principal.id();

        // 접근 권한 검증
        learnerContentAccessService.validateContentAccess(contentId, userId, tenantId);

        // 콘텐츠 파일 다운로드
        ContentService.ContentDownloadInfo downloadInfo =
                contentService.getFileForDownload(contentId, tenantId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadInfo.originalFileName() + "\"")
                .body(downloadInfo.resource());
    }
}