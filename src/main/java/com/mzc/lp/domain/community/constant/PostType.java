package com.mzc.lp.domain.community.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostType {
    QUESTION("question"),       // 질문
    DISCUSSION("discussion"),   // 토론/스터디 모집
    TIP("tip"),                 // 학습 팁
    REVIEW("review"),           // 강의 후기
    ANNOUNCEMENT("announcement"); // 공지

    private final String value;

    PostType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PostType fromValue(String value) {
        for (PostType type : PostType.values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PostType: " + value);
    }
}
