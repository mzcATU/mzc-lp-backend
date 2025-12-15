package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateCourseRequest;
import com.mzc.lp.domain.course.dto.request.UpdateCourseRequest;
import com.mzc.lp.domain.course.dto.response.CourseDetailResponse;
import com.mzc.lp.domain.course.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    /**
     * 강의 생성
     * @param request 생성 요청 DTO
     * @return 생성된 강의 정보
     */
    CourseResponse createCourse(CreateCourseRequest request);

    /**
     * 강의 목록 조회 (페이징, 키워드 검색, 카테고리 필터)
     * @param keyword 검색 키워드 (제목)
     * @param categoryId 카테고리 ID (필터)
     * @param pageable 페이징 정보
     * @return 강의 목록 페이지
     */
    Page<CourseResponse> getCourses(String keyword, Long categoryId, Pageable pageable);

    /**
     * 강의 상세 조회 (아이템 포함)
     * @param courseId 강의 ID
     * @return 강의 상세 정보
     */
    CourseDetailResponse getCourseDetail(Long courseId);

    /**
     * 강사별 강의 목록 조회
     * @param instructorId 강사 ID
     * @param pageable 페이징 정보
     * @return 강의 목록 페이지
     */
    Page<CourseResponse> getCoursesByInstructor(Long instructorId, Pageable pageable);

    /**
     * 강의 수정
     * @param courseId 강의 ID
     * @param request 수정 요청 DTO
     * @return 수정된 강의 정보
     */
    CourseResponse updateCourse(Long courseId, UpdateCourseRequest request);

    /**
     * 강의 삭제
     * @param courseId 강의 ID
     */
    void deleteCourse(Long courseId);
}
