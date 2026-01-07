package com.mzc.lp.domain.student.service;

import com.mzc.lp.domain.student.dto.request.UpdateItemProgressRequest;
import com.mzc.lp.domain.student.dto.response.ItemProgressResponse;

import java.util.List;

public interface ItemProgressService {

    /**
     * 수강별 전체 아이템 진도 목록 조회
     */
    List<ItemProgressResponse> getItemProgressList(Long enrollmentId, Long userId, boolean isAdmin);

    /**
     * 특정 아이템 진도 조회
     */
    ItemProgressResponse getItemProgress(Long enrollmentId, Long itemId, Long userId, boolean isAdmin);

    /**
     * 아이템 진도 업데이트
     */
    ItemProgressResponse updateItemProgress(
            Long enrollmentId,
            Long itemId,
            UpdateItemProgressRequest request,
            Long userId,
            boolean isAdmin
    );

    /**
     * 아이템 완료 처리
     */
    ItemProgressResponse markItemComplete(Long enrollmentId, Long itemId, Long userId, boolean isAdmin);
}