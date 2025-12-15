package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateRelationRequest;
import com.mzc.lp.domain.course.dto.request.SetStartItemRequest;
import com.mzc.lp.domain.course.dto.response.AutoRelationResponse;
import com.mzc.lp.domain.course.dto.response.CourseRelationResponse;
import com.mzc.lp.domain.course.dto.response.RelationCreateResponse;
import com.mzc.lp.domain.course.dto.response.SetStartItemResponse;

public interface CourseRelationService {

    /**
     * 학습 순서 설정
     * @param courseId 강의 ID
     * @param request 순서 생성 요청 DTO
     * @return 생성 결과
     */
    RelationCreateResponse createRelations(Long courseId, CreateRelationRequest request);

    /**
     * 학습 순서 조회
     * @param courseId 강의 ID
     * @return 학습 순서 정보
     */
    CourseRelationResponse getRelations(Long courseId);

    /**
     * 학습 순서 수정 (전체 교체)
     * @param courseId 강의 ID
     * @param request 순서 수정 요청 DTO
     * @return 수정 결과
     */
    RelationCreateResponse updateRelations(Long courseId, CreateRelationRequest request);

    /**
     * 시작점 설정
     * @param courseId 강의 ID
     * @param request 시작점 설정 요청 DTO
     * @return 설정 결과
     */
    SetStartItemResponse setStartItem(Long courseId, SetStartItemRequest request);

    /**
     * 자동 순서 생성 (depth 기준)
     * @param courseId 강의 ID
     * @return 생성 결과
     */
    AutoRelationResponse createAutoRelations(Long courseId);

    /**
     * 순서 연결 삭제
     * @param courseId 강의 ID
     * @param relationId 연결 ID
     */
    void deleteRelation(Long courseId, Long relationId);
}
