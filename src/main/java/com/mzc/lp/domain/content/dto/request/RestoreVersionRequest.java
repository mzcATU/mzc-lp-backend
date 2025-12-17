package com.mzc.lp.domain.content.dto.request;

public record RestoreVersionRequest(
        String changeSummary  // 복원 사유 (선택)
) {}
