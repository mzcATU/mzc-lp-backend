package com.mzc.lp.domain.tenantnotice.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.dto.request.CreateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.request.UpdateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeResponse;
import com.mzc.lp.domain.tenantnotice.service.TenantNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 테넌트 공지사항 컨트롤러 (TA/TO용)
 * 테넌트 관리자/운영자가 공지사항을 관리
 */
@Tag(name = "Tenant Notices", description = "테넌트 공지사항 관리 API")
@RestController
@RequestMapping("/api/tenant/notices")
@RequiredArgsConstructor
public class TenantNoticeController {

    private final TenantNoticeService tenantNoticeService;

    @Operation(summary = "공지사항 생성", description = "새로운 테넌트 공지사항을 생성합니다")
    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TenantNoticeResponse>> createNotice(
            @Valid @RequestBody CreateTenantNoticeRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantNoticeResponse response = tenantNoticeService.createNotice(
                tenantId,
                request,
                principal.id(),
                principal.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 목록 조회", description = "테넌트의 공지사항 목록을 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Page<TenantNoticeResponse>>> getNotices(
            @RequestParam(required = false) TenantNoticeStatus status,
            @RequestParam(required = false) NoticeTargetAudience targetAudience,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Page<TenantNoticeResponse> response = tenantNoticeService.getNotices(
                tenantId, status, targetAudience, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 검색", description = "테넌트의 공지사항을 키워드로 검색합니다")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Page<TenantNoticeResponse>>> searchNotices(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Page<TenantNoticeResponse> response = tenantNoticeService.searchNotices(
                tenantId, keyword, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다")
    @GetMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TenantNoticeResponse>> getNotice(
            @PathVariable Long noticeId
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantNoticeResponse response = tenantNoticeService.getNotice(tenantId, noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다")
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TenantNoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody UpdateTenantNoticeRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        boolean isAdmin = isAdmin(principal);
        TenantNoticeResponse response = tenantNoticeService.updateNotice(
                tenantId, noticeId, request, principal.id(), isAdmin
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다")
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        boolean isAdmin = isAdmin(principal);
        tenantNoticeService.deleteNotice(tenantId, noticeId, principal.id(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공지사항 발행", description = "공지사항을 발행 상태로 변경합니다")
    @PostMapping("/{noticeId}/publish")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TenantNoticeResponse>> publishNotice(
            @PathVariable Long noticeId
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantNoticeResponse response = tenantNoticeService.publishNotice(tenantId, noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 보관", description = "공지사항을 보관 상태로 변경합니다")
    @PostMapping("/{noticeId}/archive")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<TenantNoticeResponse>> archiveNotice(
            @PathVariable Long noticeId
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantNoticeResponse response = tenantNoticeService.archiveNotice(tenantId, noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return "TENANT_ADMIN".equals(principal.role())
                || "SYSTEM_ADMIN".equals(principal.role());
    }
}
