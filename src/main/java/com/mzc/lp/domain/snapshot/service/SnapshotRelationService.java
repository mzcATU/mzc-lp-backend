package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRelationRequest;
import com.mzc.lp.domain.snapshot.dto.request.SetStartSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotRelationResponse;

import java.util.List;

public interface SnapshotRelationService {

    /**
     * 연결 목록 조회
     * @param snapshotId 스냅샷 ID
     * @return 연결 목록과 순서 정보
     */
    SnapshotRelationResponse.SnapshotRelationsResponse getRelations(Long snapshotId);

    /**
     * 순서대로 아이템 조회 (Linked List 순회)
     * @param snapshotId 스냅샷 ID
     * @return 순서대로 정렬된 아이템 목록
     */
    List<SnapshotRelationResponse.OrderedItem> getOrderedItems(Long snapshotId);

    /**
     * 연결 생성
     * @param snapshotId 스냅샷 ID
     * @param request 연결 생성 요청 DTO
     * @return 생성된 연결 정보
     */
    SnapshotRelationResponse createRelation(Long snapshotId, CreateSnapshotRelationRequest request);

    /**
     * 시작점 설정
     * @param snapshotId 스냅샷 ID
     * @param request 시작점 설정 요청 DTO
     * @return 설정된 시작점 연결 정보
     */
    SnapshotRelationResponse setStartItem(Long snapshotId, SetStartSnapshotItemRequest request);

    /**
     * 연결 삭제
     * @param snapshotId 스냅샷 ID
     * @param relationId 연결 ID
     */
    void deleteRelation(Long snapshotId, Long relationId);
}
