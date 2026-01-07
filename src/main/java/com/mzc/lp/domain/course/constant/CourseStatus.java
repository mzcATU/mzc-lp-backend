package com.mzc.lp.domain.course.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseStatus {

    DRAFT("작성중"),
    PUBLISHED("발행됨");

    private final String description;
}
