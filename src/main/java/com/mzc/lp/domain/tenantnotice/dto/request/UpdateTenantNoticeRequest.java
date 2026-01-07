package com.mzc.lp.domain.tenantnotice.dto.request;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeType;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * 테넌트 공지사항 수정 요청
 */
public record UpdateTenantNoticeRequest(
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        String title,

        String content,

        TenantNoticeType type,

        NoticeTargetAudience targetAudience,

        Boolean isPinned,

        Instant expiredAt
) {
}
