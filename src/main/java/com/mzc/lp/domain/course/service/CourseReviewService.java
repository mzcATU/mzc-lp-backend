package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateReviewRequest;
import com.mzc.lp.domain.course.dto.request.UpdateReviewRequest;
import com.mzc.lp.domain.course.dto.response.CourseReviewListResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewStatsResponse;

public interface CourseReviewService {

    /**
     * 코스 차수 리뷰 작성
     * @param courseTimeId 코스 차수 ID
     * @param userId 사용자 ID
     * @param request 리뷰 작성 요청
     * @return 생성된 리뷰 정보
     */
    CourseReviewResponse createReview(Long courseTimeId, Long userId, CreateReviewRequest request);

    /**
     * 코스 차수 리뷰 목록 조회 (페이징)
     * @param courseTimeId 코스 차수 ID
     * @param sortBy 정렬 기준 (latest, rating)
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 리뷰 목록 및 통계
     */
    CourseReviewListResponse getReviews(Long courseTimeId, String sortBy, int page, int pageSize);

    /**
     * 코스 차수 리뷰 통계 조회
     * @param courseTimeId 코스 차수 ID
     * @return 평균 별점 및 리뷰 개수
     */
    CourseReviewStatsResponse getReviewStats(Long courseTimeId);

    /**
     * 내 리뷰 조회
     * @param courseTimeId 코스 차수 ID
     * @param userId 사용자 ID
     * @return 리뷰 정보 (없으면 null)
     */
    CourseReviewResponse getMyReview(Long courseTimeId, Long userId);

    /**
     * 리뷰 수정
     * @param courseTimeId 코스 차수 ID
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID
     * @param request 리뷰 수정 요청
     * @return 수정된 리뷰 정보
     */
    CourseReviewResponse updateReview(Long courseTimeId, Long reviewId, Long userId, UpdateReviewRequest request);

    /**
     * 리뷰 삭제 (본인 또는 관리자)
     * @param courseTimeId 코스 차수 ID
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID
     * @param isAdmin 관리자 여부
     */
    void deleteReview(Long courseTimeId, Long reviewId, Long userId, boolean isAdmin);
}
