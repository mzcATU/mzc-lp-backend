package com.mzc.lp.domain.ts.service;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.dto.request.CloneCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.CapacityResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeDetailResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeResponse;
import com.mzc.lp.domain.ts.dto.response.PriceResponse;
import com.mzc.lp.domain.ts.entity.CourseTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseTimeService {

    // Entity 조회 (다른 Controller에서 상태 검증용)
    CourseTime getCourseTimeEntity(Long id);

    // CRUD
    CourseTimeDetailResponse createCourseTime(CreateCourseTimeRequest request, Long createdBy);

    CourseTimeDetailResponse cloneCourseTime(Long sourceId, CloneCourseTimeRequest request, Long createdBy);

    Page<CourseTimeResponse> getCourseTimes(CourseTimeStatus status, Long cmCourseId, Pageable pageable);

    CourseTimeDetailResponse getCourseTime(Long id);

    CourseTimeDetailResponse updateCourseTime(Long id, UpdateCourseTimeRequest request);

    void deleteCourseTime(Long id);

    // 상태 전이
    CourseTimeDetailResponse openCourseTime(Long id);

    CourseTimeDetailResponse startCourseTime(Long id);

    CourseTimeDetailResponse closeCourseTime(Long id);

    CourseTimeDetailResponse archiveCourseTime(Long id);

    // 정원 관리 (SIS에서 호출)
    void occupySeat(Long courseTimeId);

    void releaseSeat(Long courseTimeId);

    /**
     * 강제 배정용 정원 증가 (정원 초과 허용, Lock 적용)
     * 관리자가 강제로 수강 신청시킬 때 사용
     */
    void forceOccupySeat(Long courseTimeId);

    // Public API
    CapacityResponse getCapacity(Long id);

    PriceResponse getPrice(Long id);
}
