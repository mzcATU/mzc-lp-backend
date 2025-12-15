package com.mzc.lp.domain.ts.service;

import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.CapacityResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeDetailResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeResponse;
import com.mzc.lp.domain.ts.dto.response.PriceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseTimeService {

    // CRUD
    CourseTimeDetailResponse createCourseTime(CreateCourseTimeRequest request, Long createdBy);

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

    // Public API
    CapacityResponse getCapacity(Long id);

    PriceResponse getPrice(Long id);
}
