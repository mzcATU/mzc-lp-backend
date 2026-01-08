package com.mzc.lp.domain.dashboard.constant;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 대시보드 기간 필터
 */
public enum DashboardPeriod {
    LAST_7_DAYS("7d", 7),
    LAST_30_DAYS("30d", 30);

    private final String code;
    private final int days;

    DashboardPeriod(String code, int days) {
        this.code = code;
        this.days = days;
    }

    public String getCode() {
        return code;
    }

    public int getDays() {
        return days;
    }

    /**
     * 코드로 DashboardPeriod 조회
     *
     * @param code "7d" 또는 "30d"
     * @return DashboardPeriod 또는 null (전체 기간)
     */
    public static DashboardPeriod fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (DashboardPeriod period : values()) {
            if (period.code.equalsIgnoreCase(code)) {
                return period;
            }
        }
        return null;
    }

    /**
     * 시작 일자 계산 (오늘 포함)
     * 예: 7d면 오늘 ~ 6일 전
     */
    public Instant getStartInstant() {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 종료 일자 계산 (오늘 23:59:59까지 포함)
     */
    public Instant getEndInstant() {
        LocalDate endDate = LocalDate.now().plusDays(1);
        return endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
