package com.mzc.lp.domain.student.constant;

/**
 * 수강 상태
 */
public enum EnrollmentStatus {
    ENROLLED,   // 수강 중
    COMPLETED,  // 수료
    DROPPED,    // 중도 포기/취소
    FAILED      // 미이수 (기간 내 미완료)
}
