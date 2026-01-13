package com.mzc.lp.domain.assignment.constant;

/**
 * 과제 제출 상태
 */
public enum SubmissionStatus {
    SUBMITTED,  // 제출됨 (채점 대기)
    GRADED,     // 채점 완료
    RETURNED    // 반려됨 (재제출 요청)
}
