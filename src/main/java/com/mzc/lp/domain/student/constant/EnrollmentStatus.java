package com.mzc.lp.domain.student.constant;

/**
 * 수강 상태
 */
public enum EnrollmentStatus {
    PENDING,    // 승인 대기 (APPROVAL 방식 신청 시)
    ENROLLED,   // 수강 중
    COMPLETED,  // 수료
    DROPPED,    // 중도 포기/취소
    REJECTED,   // 승인 거절
    FAILED      // 미이수 (기간 내 미완료)
}
