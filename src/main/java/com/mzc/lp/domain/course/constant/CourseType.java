package com.mzc.lp.domain.course.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseType {

    ONLINE("온라인"),
    OFFLINE("오프라인"),
    BLENDED("블렌디드");

    private final String description;
}
