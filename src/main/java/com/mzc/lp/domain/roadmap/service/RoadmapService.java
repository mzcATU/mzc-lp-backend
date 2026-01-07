package com.mzc.lp.domain.roadmap.service;

import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.dto.request.CreateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.request.SaveDraftRequest;
import com.mzc.lp.domain.roadmap.dto.request.UpdateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapDetailResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 로드맵 Service Interface
 */
public interface RoadmapService {

    /**
     * 로드맵 생성
     */
    RoadmapResponse createRoadmap(CreateRoadmapRequest request, Long authorId);

    /**
     * 로드맵 상세 조회
     */
    RoadmapDetailResponse getRoadmap(Long roadmapId, Long currentUserId);

    /**
     * 내 로드맵 목록 조회 (필터링, 정렬 지원)
     */
    Page<RoadmapResponse> getMyRoadmaps(Long authorId, RoadmapStatus status, String sortBy, Pageable pageable);

    /**
     * 로드맵 수정
     */
    RoadmapResponse updateRoadmap(Long roadmapId, UpdateRoadmapRequest request, Long currentUserId);

    /**
     * 로드맵 임시저장 (최소 검증)
     */
    RoadmapResponse saveDraft(Long roadmapId, SaveDraftRequest request, Long currentUserId);

    /**
     * 로드맵 삭제
     */
    void deleteRoadmap(Long roadmapId, Long currentUserId);

    /**
     * 로드맵 복제
     */
    RoadmapResponse duplicateRoadmap(Long roadmapId, Long currentUserId);

    /**
     * 통계 조회
     */
    RoadmapStatisticsResponse getStatistics(Long authorId);
}
