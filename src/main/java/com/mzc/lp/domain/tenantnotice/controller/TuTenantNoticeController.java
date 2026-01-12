package com.mzc.lp.domain.tenantnotice.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeResponse;
import com.mzc.lp.domain.tenantnotice.service.TenantNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 테넌트 공지사항 조회 컨트롤러 (TU/CO용)
 * 테넌트 사용자/운영자가 자신에게 발행된 공지사항을 조회
 */
@Tag(name = "Tenant Notices (TU)", description = "테넌트 공지사항 조회 API")
@RestController
@RequestMapping("/api/tu/notices")
@RequiredArgsConstructor
public class TuTenantNoticeController {

    private final TenantNoticeService tenantNoticeService;

    @Operation(summary = "발행된 공지사항 목록 조회", description = "현재 사용자에게 발행된 공지사항 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TenantNoticeResponse>>> getVisibleNotices(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        NoticeTargetAudience targetAudience = getTargetAudienceByRole(principal);
        Page<TenantNoticeResponse> response = tenantNoticeService.getVisibleNotices(
                tenantId, targetAudience, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "발행된 공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다 (조회수 증가)")
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<TenantNoticeResponse>> getVisibleNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        NoticeTargetAudience targetAudience = getTargetAudienceByRole(principal);
        TenantNoticeResponse response = tenantNoticeService.getVisibleNotice(
                tenantId, noticeId, targetAudience
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "발행된 공지사항 수 조회", description = "현재 사용자에게 발행된 공지사항 수를 조회합니다")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countVisibleNotices(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        NoticeTargetAudience targetAudience = getTargetAudienceByRole(principal);
        long count = tenantNoticeService.countVisibleNotices(tenantId, targetAudience);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 사용자 역할에 따른 대상 분류
     * - OPERATOR: 운영자 대상 공지 조회
     * - 그 외: 사용자 대상 공지 조회
     */
    private NoticeTargetAudience getTargetAudienceByRole(UserPrincipal principal) {
        if ("OPERATOR".equals(principal.role())) {
            return NoticeTargetAudience.OPERATOR;
        }
        return NoticeTargetAudience.USER;
    }
}
