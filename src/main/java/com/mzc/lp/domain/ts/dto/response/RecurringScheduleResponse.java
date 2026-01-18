package com.mzc.lp.domain.ts.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mzc.lp.domain.ts.entity.RecurringSchedule;

import java.time.LocalTime;
import java.util.List;

/**
 * 정기 수업 일정 응답 DTO
 */
public record RecurringScheduleResponse(
        List<Integer> daysOfWeek,  // 0=일요일, 1=월요일, ..., 6=토요일

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {
    public static RecurringScheduleResponse from(RecurringSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return new RecurringScheduleResponse(
                schedule.getDaysOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}
