package com.mzc.lp.domain.analytics.constant;

/**
 * 리포트 유형
 */
public enum ReportType {
    USERS("사용자 현황", "등록된 사용자 목록 및 상태"),
    COURSES("강좌 현황", "강좌 목록 및 수강 통계"),
    LEARNING("학습 진도", "사용자별 학습 진행 현황"),
    COMPLETION("수료 현황", "강좌별 수료자 통계"),
    ENGAGEMENT("참여도 분석", "사용자 활동 및 참여 지표");

    private final String name;
    private final String description;

    ReportType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
