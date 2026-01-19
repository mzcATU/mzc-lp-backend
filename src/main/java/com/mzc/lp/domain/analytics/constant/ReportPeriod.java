package com.mzc.lp.domain.analytics.constant;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 리포트 조회 기간
 */
public enum ReportPeriod {
    WEEK(7, "최근 1주"),
    MONTH(30, "최근 1개월"),
    QUARTER(90, "최근 분기"),
    YEAR(365, "최근 1년"),
    ALL(0, "전체");

    private final int days;
    private final String label;

    ReportPeriod(int days, String label) {
        this.days = days;
        this.label = label;
    }

    public int getDays() {
        return days;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 해당 기간의 시작 시점 반환
     */
    public Instant getStartDate() {
        if (this == ALL) {
            return null; // 전체 기간
        }
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }
}
