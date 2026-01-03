package com.mzc.lp.domain.analytics.dto.response;

import java.util.List;
import java.util.Map;

/**
 * 활동 통계 응답 DTO
 */
public record ActivityStatsResponse(
        long totalActivities,
        long todayActivities,
        long activeUsers,
        Map<String, Long> byActivityType,
        List<DailyActivityCount> dailyTrend,
        List<HourlyActivityCount> hourlyTrend
) {
    public record DailyActivityCount(String date, long count) {}
    public record HourlyActivityCount(int hour, long count) {}
}
