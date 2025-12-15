package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateFolderRequest;
import com.mzc.lp.domain.course.dto.request.CreateItemRequest;
import com.mzc.lp.domain.course.dto.request.MoveItemRequest;
import com.mzc.lp.domain.course.dto.request.UpdateItemNameRequest;
import com.mzc.lp.domain.course.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.course.dto.response.CourseItemHierarchyResponse;
import com.mzc.lp.domain.course.dto.response.CourseItemResponse;

import java.util.List;

public interface CourseItemService {

    /**
     * 차시 추가
     * @param courseId 강의 ID
     * @param request 생성 요청 DTO
     * @return 생성된 차시 정보
     */
    CourseItemResponse createItem(Long courseId, CreateItemRequest request);

    /**
     * 폴더 생성
     * @param courseId 강의 ID
     * @param request 생성 요청 DTO
     * @return 생성된 폴더 정보
     */
    CourseItemResponse createFolder(Long courseId, CreateFolderRequest request);

    /**
     * 계층 구조 조회
     * @param courseId 강의 ID
     * @return 계층 구조로 정렬된 항목 목록
     */
    List<CourseItemHierarchyResponse> getHierarchy(Long courseId);

    /**
     * 순서대로 차시 조회 (폴더 제외, Relation 기반)
     * @param courseId 강의 ID
     * @return 학습 순서로 정렬된 차시 목록
     */
    List<CourseItemResponse> getOrderedItems(Long courseId);

    /**
     * 항목 이동
     * @param courseId 강의 ID
     * @param request 이동 요청 DTO
     * @return 이동된 항목 정보
     */
    CourseItemResponse moveItem(Long courseId, MoveItemRequest request);

    /**
     * 항목 이름 변경
     * @param courseId 강의 ID
     * @param itemId 항목 ID
     * @param request 이름 변경 요청 DTO
     * @return 수정된 항목 정보
     */
    CourseItemResponse updateItemName(Long courseId, Long itemId, UpdateItemNameRequest request);

    /**
     * 학습 객체 변경 (차시 전용)
     * @param courseId 강의 ID
     * @param itemId 항목 ID
     * @param request LO 변경 요청 DTO
     * @return 수정된 항목 정보
     */
    CourseItemResponse updateLearningObject(Long courseId, Long itemId, UpdateLearningObjectRequest request);

    /**
     * 항목 삭제 (폴더일 경우 하위 항목도 삭제)
     * @param courseId 강의 ID
     * @param itemId 항목 ID
     */
    void deleteItem(Long courseId, Long itemId);
}
