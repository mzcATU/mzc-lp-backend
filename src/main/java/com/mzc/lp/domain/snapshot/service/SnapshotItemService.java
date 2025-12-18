package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.MoveSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotItemResponse;

import java.util.List;

public interface SnapshotItemService {

    /**
     * 아이템 계층 구조 조회
     * @param snapshotId 스냅샷 ID
     * @return 계층 구조로 정렬된 아이템 목록
     */
    List<SnapshotItemResponse> getHierarchy(Long snapshotId);

    /**
     * 아이템 평면 목록 조회
     * @param snapshotId 스냅샷 ID
     * @return 평면 목록 (children 없음)
     */
    List<SnapshotItemResponse> getFlatItems(Long snapshotId);

    /**
     * 아이템 추가 (DRAFT 상태에서만 가능)
     * @param snapshotId 스냅샷 ID
     * @param request 생성 요청 DTO
     * @return 생성된 아이템 정보
     */
    SnapshotItemResponse createItem(Long snapshotId, CreateSnapshotItemRequest request);

    /**
     * 아이템 수정 (이름 변경)
     * @param snapshotId 스냅샷 ID
     * @param itemId 아이템 ID
     * @param request 수정 요청 DTO
     * @return 수정된 아이템 정보
     */
    SnapshotItemResponse updateItem(Long snapshotId, Long itemId, UpdateSnapshotItemRequest request);

    /**
     * 아이템 이동 (DRAFT 상태에서만 가능)
     * @param snapshotId 스냅샷 ID
     * @param itemId 아이템 ID
     * @param request 이동 요청 DTO
     * @return 이동된 아이템 정보
     */
    SnapshotItemResponse moveItem(Long snapshotId, Long itemId, MoveSnapshotItemRequest request);

    /**
     * 아이템 삭제 (DRAFT 상태에서만 가능, 폴더일 경우 하위 항목도 삭제)
     * @param snapshotId 스냅샷 ID
     * @param itemId 아이템 ID
     */
    void deleteItem(Long snapshotId, Long itemId);
}
