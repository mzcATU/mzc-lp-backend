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

import java.util.HashSet;
import java.util.Set;

/**
 * 테넌트 공지사항 조회 컨트롤러 (TU/CO용)
 * 테넌트 사용자/운영자가 자신에게 발행된 공지사항을 조회
 * 다중 역할 지원: 사용자의 모든 역할에 해당하는 공지 + ALL 대상 공지 조회
 */
@Tag(name = "Tenant Notices (TU)", description = "테넌트 공지사항 조회 API")
@RestController
@RequestMapping("/api/tu/notices")
@RequiredArgsConstructor
public class TuTenantNoticeController {

    private final TenantNoticeService tenantNoticeService;

    @Operation(summary = "발행된 공지사항 목록 조회", description = "현재 사용자에게 발행된 공지사항 목록을 조회합니다 (다중 역할 지원)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TenantNoticeResponse>>> getVisibleNotices(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Set<NoticeTargetAudience> targetAudiences = getTargetAudiencesByRoles(principal);
        Page<TenantNoticeResponse> response = tenantNoticeService.getVisibleNoticesForMultipleAudiences(
                tenantId, targetAudiences, pageable
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
        Set<NoticeTargetAudience> targetAudiences = getTargetAudiencesByRoles(principal);
        TenantNoticeResponse response = tenantNoticeService.getVisibleNoticeForMultipleAudiences(
                tenantId, noticeId, targetAudiences
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "발행된 공지사항 수 조회", description = "현재 사용자에게 발행된 공지사항 수를 조회합니다")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countVisibleNotices(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Set<NoticeTargetAudience> targetAudiences = getTargetAudiencesByRoles(principal);
        long count = tenantNoticeService.countVisibleNoticesForMultipleAudiences(tenantId, targetAudiences);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 사용자의 모든 역할에 해당하는 공지 대상 Set 반환 (다중 역할 지원)
     * - roles: 테넌트 역할 (OPERATOR, USER 등)
     * - courseRoles: 강의 역할 (DESIGNER, INSTRUCTOR)
     * - 항상 ALL도 포함 (모든 사용자에게 보내는 공지)
     */
    private Set<NoticeTargetAudience> getTargetAudiencesByRoles(UserPrincipal principal) {
        Set<NoticeTargetAudience> audiences = new HashSet<>();

        // 항상 ALL 대상 공지는 볼 수 있음
        audiences.add(NoticeTargetAudience.ALL);

        // 테넌트 역할 기반 대상 추가
        if (principal.roles() != null) {
            for (String role : principal.roles()) {
                switch (role) {
                    case "OPERATOR" -> audiences.add(NoticeTargetAudience.OPERATOR);
                    case "USER" -> audiences.add(NoticeTargetAudience.USER);
                    case "TENANT_ADMIN" -> {
                        // TA는 모든 공지를 볼 수 있음
                        audiences.add(NoticeTargetAudience.OPERATOR);
                        audiences.add(NoticeTargetAudience.USER);
                        audiences.add(NoticeTargetAudience.DESIGNER);
                        audiences.add(NoticeTargetAudience.INSTRUCTOR);
                    }
                }
            }
        }

        // 강의 역할 기반 대상 추가 (DESIGNER, INSTRUCTOR)
        if (principal.courseRoles() != null) {
            for (String courseRole : principal.courseRoles()) {
                switch (courseRole) {
                    case "DESIGNER" -> audiences.add(NoticeTargetAudience.DESIGNER);
                    case "INSTRUCTOR" -> audiences.add(NoticeTargetAudience.INSTRUCTOR);
                }
            }
        }

        // 기본 역할도 확인 (하위 호환성)
        if (principal.role() != null) {
            switch (principal.role()) {
                case "OPERATOR" -> audiences.add(NoticeTargetAudience.OPERATOR);
                case "USER" -> audiences.add(NoticeTargetAudience.USER);
                case "DESIGNER" -> audiences.add(NoticeTargetAudience.DESIGNER);
                case "INSTRUCTOR" -> audiences.add(NoticeTargetAudience.INSTRUCTOR);
            }
        }

        // 최소한 USER는 포함 (일반 사용자)
        if (audiences.size() == 1) { // ALL만 있는 경우
            audiences.add(NoticeTargetAudience.USER);
        }

        return audiences;
    }
}
