package com.mzc.lp.domain.ts.service;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.dto.response.CourseTimeCatalogResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimePublicDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 학습자용 CourseTime Public API Service
 */
public interface PublicCourseTimeService {

    /**
     * 학습자용 차수 목록 조회 (카탈로그)
     *
     * @param statuses     상태 필터 (기본: RECRUITING, ONGOING)
     * @param deliveryType 운영 방식 필터
     * @param programId    프로그램 ID 필터
     * @param isFree       무료/유료 필터
     * @param keyword      제목 검색 키워드
     * @param pageable     페이징 정보
     * @return 페이징된 차수 목록
     */
    Page<CourseTimeCatalogResponse> getPublicCourseTimes(
            List<CourseTimeStatus> statuses,
            DeliveryType deliveryType,
            Long programId,
            Boolean isFree,
            String keyword,
            Pageable pageable
    );

    /**
     * 학습자용 차수 상세 조회
     *
     * @param id 차수 ID
     * @return 차수 상세 정보 (커리큘럼, 강사 정보 포함)
     */
    CourseTimePublicDetailResponse getPublicCourseTime(Long id);
}
