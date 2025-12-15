package com.mzc.lp.domain.student.service;

import com.mzc.lp.domain.student.dto.response.CourseTimeEnrollmentStatsResponse;
import com.mzc.lp.domain.student.dto.response.UserEnrollmentStatsResponse;

/**
 * 수강 통계 서비스 인터페이스
 */
public interface EnrollmentStatsService {

    /**
     * 차수별 수강 통계 조회
     *
     * @param courseTimeId 차수 ID
     * @return 차수별 수강 통계
     */
    CourseTimeEnrollmentStatsResponse getCourseTimeStats(Long courseTimeId);

    /**
     * 사용자별 수강 통계 조회
     *
     * @param userId 사용자 ID
     * @return 사용자별 수강 통계
     */
    UserEnrollmentStatsResponse getUserStats(Long userId);
}
