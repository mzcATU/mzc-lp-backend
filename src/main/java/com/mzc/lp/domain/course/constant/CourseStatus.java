package com.mzc.lp.domain.course.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseStatus {

    DRAFT("작성중"),
    READY("작성완료"),
    REGISTERED("등록됨");

    private final String description;

    /**
     * 해당 상태에서 수정이 가능한지 확인
     * DRAFT, READY: 수정 가능
     * REGISTERED: 수정 불가
     */
    public boolean isModifiable() {
        return this == DRAFT || this == READY;
    }

    /**
     * 해당 상태에서 차수(CourseTime) 생성이 가능한지 확인
     * REGISTERED: 차수 생성 가능
     * DRAFT, READY: 차수 생성 불가
     */
    public boolean canCreateCourseTime() {
        return this == REGISTERED;
    }
}
