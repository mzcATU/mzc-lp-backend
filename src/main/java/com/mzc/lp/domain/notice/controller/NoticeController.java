package com.mzc.lp.domain.notice.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import com.mzc.lp.domain.notice.dto.request.CreateNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.DistributeNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.UpdateNoticeRequest;
import com.mzc.lp.domain.notice.dto.response.NoticeResponse;
import com.mzc.lp.domain.notice.service.NoticeService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공지사항 컨트롤러 (SA용)
 * Super Admin이 시스템 공지를 관리하고 테넌트에 배포
 */
@Tag(name = "Notices (SA)", description = "시스템 공지사항 관리 API")
@RestController
@RequestMapping("/api/sa/notices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 생성", description = "새로운 공지사항을 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @Valid @RequestBody CreateNoticeRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: userDetails에서 실제 userId 추출 로직 필요
        Long createdBy = 1L; // 임시값
        NoticeResponse response = noticeService.createNotice(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNotices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) NoticeStatus status,
            @RequestParam(required = false) NoticeType type,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<NoticeResponse> response = noticeService.getNotices(keyword, status, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다")
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @PathVariable Long noticeId
    ) {
        NoticeResponse response = noticeService.getNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다")
    @PutMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody UpdateNoticeRequest request
    ) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId
    ) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공지사항 발행", description = "공지사항을 발행 상태로 변경합니다")
    @PostMapping("/{noticeId}/publish")
    public ResponseEntity<ApiResponse<NoticeResponse>> publishNotice(
            @PathVariable Long noticeId
    ) {
        NoticeResponse response = noticeService.publishNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 보관", description = "공지사항을 보관 상태로 변경합니다")
    @PostMapping("/{noticeId}/archive")
    public ResponseEntity<ApiResponse<NoticeResponse>> archiveNotice(
            @PathVariable Long noticeId
    ) {
        NoticeResponse response = noticeService.archiveNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 배포", description = "특정 테넌트들에게 공지사항을 배포합니다")
    @PostMapping("/{noticeId}/distribute")
    public ResponseEntity<Void> distributeNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody DistributeNoticeRequest request
    ) {
        noticeService.distributeNotice(noticeId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 테넌트 배포", description = "모든 테넌트에게 공지사항을 배포합니다")
    @PostMapping("/{noticeId}/distribute-all")
    public ResponseEntity<Void> distributeToAllTenants(
            @PathVariable Long noticeId
    ) {
        noticeService.distributeToAllTenants(noticeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "배포된 테넌트 조회", description = "공지사항이 배포된 테넌트 ID 목록을 조회합니다")
    @GetMapping("/{noticeId}/tenants")
    public ResponseEntity<ApiResponse<List<Long>>> getDistributedTenants(
            @PathVariable Long noticeId
    ) {
        List<Long> tenantIds = noticeService.getDistributedTenantIds(noticeId);
        return ResponseEntity.ok(ApiResponse.success(tenantIds));
    }
}
