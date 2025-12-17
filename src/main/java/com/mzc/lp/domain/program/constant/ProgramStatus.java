package com.mzc.lp.domain.program.constant;

public enum ProgramStatus {
    DRAFT,      // 작성 중
    PENDING,    // 검토 대기
    APPROVED,   // 승인됨 (운영 가능)
    REJECTED,   // 반려됨
    CLOSED      // 종료됨
}
