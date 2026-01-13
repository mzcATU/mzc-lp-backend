package com.mzc.lp.domain.assignment.constant;

/**
 * 과제 상태
 */
public enum AssignmentStatus {
    DRAFT,      // 초안 (수정 가능, 학생에게 미공개)
    PUBLISHED,  // 발행됨 (학생에게 공개, 제출 가능)
    CLOSED      // 마감됨 (제출 불가)
}
