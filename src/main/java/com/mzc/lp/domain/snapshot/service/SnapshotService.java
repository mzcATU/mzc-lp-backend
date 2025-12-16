package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotDetailResponse;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SnapshotService {

    /**
     * Course(템플릿)에서 스냅샷 생성 (깊은 복사)
     * @param courseId 원본 Course ID
     * @param createdBy 생성자 ID
     * @return 생성된 스냅샷 상세 정보
     */
    SnapshotDetailResponse createSnapshotFromCourse(Long courseId, Long createdBy);

    /**
     * 신규 스냅샷 직접 생성
     * @param request 생성 요청 DTO
     * @param createdBy 생성자 ID
     * @return 생성된 스냅샷 정보
     */
    SnapshotResponse createSnapshot(CreateSnapshotRequest request, Long createdBy);

    /**
     * 스냅샷 목록 조회 (페이징, 상태/생성자 필터)
     * @param status 상태 필터 (optional)
     * @param createdBy 생성자 ID 필터 (optional)
     * @param pageable 페이징 정보
     * @return 스냅샷 목록 페이지
     */
    Page<SnapshotResponse> getSnapshots(SnapshotStatus status, Long createdBy, Pageable pageable);

    /**
     * 스냅샷 상세 조회 (아이템 포함)
     * @param snapshotId 스냅샷 ID
     * @return 스냅샷 상세 정보
     */
    SnapshotDetailResponse getSnapshotDetail(Long snapshotId);

    /**
     * Course의 스냅샷 목록 조회
     * @param courseId Course ID
     * @return 스냅샷 목록
     */
    List<SnapshotResponse> getSnapshotsByCourse(Long courseId);

    /**
     * 스냅샷 수정
     * @param snapshotId 스냅샷 ID
     * @param request 수정 요청 DTO
     * @return 수정된 스냅샷 정보
     */
    SnapshotResponse updateSnapshot(Long snapshotId, UpdateSnapshotRequest request);

    /**
     * 스냅샷 삭제
     * @param snapshotId 스냅샷 ID
     */
    void deleteSnapshot(Long snapshotId);

    /**
     * 스냅샷 발행 (DRAFT → ACTIVE)
     * @param snapshotId 스냅샷 ID
     * @return 변경된 스냅샷 정보
     */
    SnapshotResponse publishSnapshot(Long snapshotId);

    /**
     * 스냅샷 완료 (ACTIVE → COMPLETED)
     * @param snapshotId 스냅샷 ID
     * @return 변경된 스냅샷 정보
     */
    SnapshotResponse completeSnapshot(Long snapshotId);

    /**
     * 스냅샷 보관 (COMPLETED → ARCHIVED)
     * @param snapshotId 스냅샷 ID
     * @return 변경된 스냅샷 정보
     */
    SnapshotResponse archiveSnapshot(Long snapshotId);
}
