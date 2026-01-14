package com.mzc.lp.domain.roadmap.service;

import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.dto.request.CreateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.request.SaveDraftRequest;
import com.mzc.lp.domain.roadmap.dto.request.UpdateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapDetailResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapStatisticsResponse;
import com.mzc.lp.domain.roadmap.repository.RoadmapProgramRepository;
import com.mzc.lp.domain.roadmap.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로드맵 서비스 구현체
 *
 * @deprecated 로드맵 기능은 Phase 3에서 일시적으로 비활성화됨.
 *             Program 엔티티 제거로 인해 향후 Course 기반으로 재설계 필요.
 */
@Deprecated
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapProgramRepository roadmapProgramRepository;

    private static final String DISABLED_MESSAGE = "로드맵 기능은 현재 비활성화되어 있습니다.";

    @Override
    @Transactional
    public RoadmapResponse createRoadmap(CreateRoadmapRequest request, Long authorId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    public RoadmapDetailResponse getRoadmap(Long roadmapId, Long currentUserId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    public Page<RoadmapResponse> getMyRoadmaps(Long authorId, RoadmapStatus status, String sortBy, Pageable pageable) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    @Transactional
    public RoadmapResponse updateRoadmap(Long roadmapId, UpdateRoadmapRequest request, Long currentUserId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    @Transactional
    public RoadmapResponse saveDraft(Long roadmapId, SaveDraftRequest request, Long currentUserId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    @Transactional
    public void deleteRoadmap(Long roadmapId, Long currentUserId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    @Transactional
    public RoadmapResponse duplicateRoadmap(Long roadmapId, Long currentUserId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    public RoadmapStatisticsResponse getStatistics(Long authorId) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }
}
