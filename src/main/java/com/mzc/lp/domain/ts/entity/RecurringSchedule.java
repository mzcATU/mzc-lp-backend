package com.mzc.lp.domain.ts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.List;

/**
 * 정기 수업 일정 (Embeddable)
 * FIXED + OFFLINE/BLENDED/LIVE 차수에서 사용
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringSchedule {

    /**
     * 수업 요일 목록 (JSON 저장)
     * 0=일요일, 1=월요일, ..., 6=토요일
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule_days_of_week", columnDefinition = "json")
    private List<Integer> daysOfWeek;

    /**
     * 수업 시작 시간 (예: 19:00)
     */
    @Column(name = "schedule_start_time")
    private LocalTime startTime;

    /**
     * 수업 종료 시간 (예: 21:00)
     */
    @Column(name = "schedule_end_time")
    private LocalTime endTime;

    public static RecurringSchedule create(List<Integer> daysOfWeek, LocalTime startTime, LocalTime endTime) {
        RecurringSchedule schedule = new RecurringSchedule();
        schedule.daysOfWeek = daysOfWeek;
        schedule.startTime = startTime;
        schedule.endTime = endTime;
        return schedule;
    }

    public void update(List<Integer> daysOfWeek, LocalTime startTime, LocalTime endTime) {
        this.daysOfWeek = daysOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 다른 일정과 시간이 겹치는지 확인
     */
    public boolean hasTimeConflict(RecurringSchedule other) {
        if (other == null || this.daysOfWeek == null || other.daysOfWeek == null) {
            return false;
        }

        // 요일이 겹치는지 확인
        boolean hasDayOverlap = this.daysOfWeek.stream()
                .anyMatch(day -> other.daysOfWeek.contains(day));

        if (!hasDayOverlap) {
            return false;
        }

        // 시간이 겹치는지 확인
        // A.start < B.end && A.end > B.start
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
}
