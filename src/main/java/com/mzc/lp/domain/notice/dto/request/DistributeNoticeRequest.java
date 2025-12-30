package com.mzc.lp.domain.notice.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DistributeNoticeRequest(
        @NotEmpty
        List<Long> tenantIds
) {
}
