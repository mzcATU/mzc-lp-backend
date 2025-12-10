package com.mzc.lp.domain.user.constant;

public enum CourseRole {
    DESIGNER,       // 강의 설계자 (커리큘럼 구성, 콘텐츠 제작)
    OWNER,          // 강의 소유자 (B2C: 소유 + 강사, B2B: 소유만)
    INSTRUCTOR      // 강사 (B2B/KPOP 전용, 강의 진행)
}
