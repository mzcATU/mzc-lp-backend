package com.mzc.lp.domain.notice.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.notice.dto.response.NoticeResponse;
import com.mzc.lp.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 시스템 공지사항 조회 컨트롤러 (TA/CO용)
 * SA가 테넌트에 배포한 시스템 공지를 조회
 */
@Tag(name = "System Notices (TA)", description = "테넌트에 배포된 시스템 공지사항 조회 API")
@RestController("systemNoticeForTenantController")
@RequestMapping("/api/ta/notices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
public class SystemNoticeForTenantController {

    private final NoticeService noticeService;

    @Operation(summary = "테넌트 공지사항 목록 조회", description = "현재 테넌트에 배포된 공지사항 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNotices(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<NoticeResponse> response = noticeService.getNoticesForTenant(principal.tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테넌트 공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다")
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        NoticeResponse response = noticeService.getNoticeForTenant(noticeId, principal.tenantId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 읽음 표시", description = "공지사항을 읽음으로 표시합니다")
    @PostMapping("/{noticeId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        noticeService.markNoticeAsRead(noticeId, principal.tenantId());
        return ResponseEntity.ok().build();
    }
}
