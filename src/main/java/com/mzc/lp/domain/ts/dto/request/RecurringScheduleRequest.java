package com.mzc.lp.domain.ts.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/**
 * 정기 수업 일정 요청 DTO
 */
public record RecurringScheduleRequest(
        @NotEmpty(message = "정기 일정 설정 시 요일은 최소 1개 이상 선택해야 합니다")
        List<Integer> daysOfWeek,  // 0=일요일, 1=월요일, ..., 6=토요일

        @NotNull(message = "시작 시간은 필수입니다")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {
}
