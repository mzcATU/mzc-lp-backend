package com.mzc.lp.common.dto.stats;

/**
 * 프로그램별 통계 Projection
 * 내 강의 통계에서 프로그램별 차수 수, 수강생 수, 수료율 조회용
 */
public interface ProgramStatsProjection {

    Long getProgramId();

    String getTitle();

    Long getCourseTimeCount();

    Long getTotalStudents();

    Double getCompletionRate();
}
