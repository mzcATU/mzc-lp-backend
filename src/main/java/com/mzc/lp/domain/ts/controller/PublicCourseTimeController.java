package com.mzc.lp.domain.ts.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.dto.response.CourseTimeCatalogResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimePublicDetailResponse;
import com.mzc.lp.domain.ts.service.PublicCourseTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학습자용 CourseTime Public API Controller
 */
@RestController
@RequestMapping("/api/public/course-times")
@RequiredArgsConstructor
@Validated
public class PublicCourseTimeController {

    private final PublicCourseTimeService publicCourseTimeService;

    /**
     * 학습자용 차수 목록 조회 (카탈로그)
     *
     * @param status       상태 필터 (다중 선택 가능, 기본: RECRUITING, ONGOING)
     * @param deliveryType 운영 방식 필터
     * @param courseId     강의 ID 필터
     * @param isFree       무료/유료 필터
     * @param keyword      제목 검색 키워드
     * @param categoryId   카테고리 ID 필터
     * @param pageable     페이징 정보
     * @return 페이징된 차수 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseTimeCatalogResponse>>> getPublicCourseTimes(
            @RequestParam(required = false) List<CourseTimeStatus> status,
            @RequestParam(required = false) DeliveryType deliveryType,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CourseTimeCatalogResponse> response = publicCourseTimeService.getPublicCourseTimes(
                status, deliveryType, courseId, isFree, keyword, categoryId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 학습자용 차수 상세 조회
     *
     * @param id 차수 ID
     * @return 차수 상세 정보 (커리큘럼, 강사 정보 포함)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseTimePublicDetailResponse>> getPublicCourseTime(
            @PathVariable Long id
    ) {
        CourseTimePublicDetailResponse response = publicCourseTimeService.getPublicCourseTime(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
