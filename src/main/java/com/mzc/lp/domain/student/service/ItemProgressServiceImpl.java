package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.student.dto.request.UpdateItemProgressRequest;
import com.mzc.lp.domain.student.dto.response.ItemProgressResponse;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.entity.ItemProgress;
import com.mzc.lp.domain.student.exception.EnrollmentNotFoundException;
import com.mzc.lp.domain.student.exception.UnauthorizedEnrollmentAccessException;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.student.repository.ItemProgressRepository;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemProgressServiceImpl implements ItemProgressService {

    private final ItemProgressRepository itemProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final SnapshotItemRepository snapshotItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemProgressResponse> getItemProgressList(Long enrollmentId, Long userId, boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 수강 정보 확인 및 권한 검증
        Enrollment enrollment = getEnrollmentWithAccessCheck(enrollmentId, userId, isAdmin, tenantId);

        List<ItemProgress> progressList = itemProgressRepository.findByEnrollmentIdAndTenantId(enrollmentId, tenantId);

        log.info("Retrieved item progress list: enrollmentId={}, count={}", enrollmentId, progressList.size());

        return progressList.stream()
                .map(ItemProgressResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemProgressResponse getItemProgress(Long enrollmentId, Long itemId, Long userId, boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 수강 정보 확인 및 권한 검증
        getEnrollmentWithAccessCheck(enrollmentId, userId, isAdmin, tenantId);

        ItemProgress progress = itemProgressRepository
                .findByEnrollmentIdAndItemIdAndTenantId(enrollmentId, itemId, tenantId)
                .orElse(null);

        if (progress == null) {
            // 진도 기록이 없으면 빈 응답 반환 (아직 학습 시작 안 함)
            return new ItemProgressResponse(null, enrollmentId, itemId, 0, 0, false, null, 0);
        }

        return ItemProgressResponse.from(progress);
    }

    @Override
    @Transactional
    public ItemProgressResponse updateItemProgress(
            Long enrollmentId,
            Long itemId,
            UpdateItemProgressRequest request,
            Long userId,
            boolean isAdmin
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 수강 정보 확인 및 권한 검증
        getEnrollmentWithAccessCheck(enrollmentId, userId, isAdmin, tenantId);

        // 기존 진도 조회 또는 새로 생성 (tenantId는 @PrePersist에서 자동 설정됨)
        ItemProgress progress = itemProgressRepository
                .findByEnrollmentIdAndItemIdAndTenantId(enrollmentId, itemId, tenantId)
                .orElseGet(() -> ItemProgress.create(enrollmentId, itemId));

        // 진도 업데이트
        progress.updateProgress(
                request.progressPercent(),
                request.watchedSeconds(),
                request.lastPositionSeconds()
        );

        ItemProgress saved = itemProgressRepository.save(progress);

        log.info("Updated item progress: enrollmentId={}, itemId={}, progressPercent={}",
                enrollmentId, itemId, request.progressPercent());

        // 전체 수강 진도율 업데이트
        updateEnrollmentProgress(enrollmentId, tenantId);

        return ItemProgressResponse.from(saved);
    }

    @Override
    @Transactional
    public ItemProgressResponse markItemComplete(Long enrollmentId, Long itemId, Long userId, boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 수강 정보 확인 및 권한 검증
        getEnrollmentWithAccessCheck(enrollmentId, userId, isAdmin, tenantId);

        // 기존 진도 조회 또는 새로 생성 (tenantId는 @PrePersist에서 자동 설정됨)
        ItemProgress progress = itemProgressRepository
                .findByEnrollmentIdAndItemIdAndTenantId(enrollmentId, itemId, tenantId)
                .orElseGet(() -> ItemProgress.create(enrollmentId, itemId));

        // 완료 처리
        progress.markAsCompleted();

        ItemProgress saved = itemProgressRepository.save(progress);

        log.info("Marked item as complete: enrollmentId={}, itemId={}", enrollmentId, itemId);

        // 전체 수강 진도율 업데이트
        updateEnrollmentProgress(enrollmentId, tenantId);

        return ItemProgressResponse.from(saved);
    }

    /**
     * 수강 정보 조회 및 접근 권한 확인
     */
    private Enrollment getEnrollmentWithAccessCheck(Long enrollmentId, Long userId, boolean isAdmin, Long tenantId) {
        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 확인 (관리자가 아닌 경우)
        if (!isAdmin && !enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(enrollmentId, userId);
        }

        return enrollment;
    }

    /**
     * 전체 수강 진도율 업데이트 (스냅샷 아이템 기준 완료율)
     * 진도율 = 완료된 아이템 수 / 스냅샷 전체 아이템 수 × 100
     */
    private void updateEnrollmentProgress(Long enrollmentId, Long tenantId) {
        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElse(null);

        if (enrollment == null) {
            return;
        }

        // 1. Enrollment → CourseTime → Program → Snapshot 순서로 snapshotId 조회
        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(enrollment.getCourseTimeId(), tenantId)
                .orElse(null);

        if (courseTime == null || courseTime.getProgram() == null || courseTime.getProgram().getSnapshot() == null) {
            log.warn("Cannot calculate progress: missing snapshot for enrollmentId={}", enrollmentId);
            return;
        }

        Long snapshotId = courseTime.getProgram().getSnapshot().getId();

        // 2. 스냅샷의 전체 학습 아이템 수 조회 (폴더 제외, LearningObject가 있는 아이템만)
        List<SnapshotItem> snapshotItems = snapshotItemRepository.findItemsOnlyBySnapshotId(snapshotId, tenantId);
        int totalItems = snapshotItems.size();

        if (totalItems == 0) {
            log.warn("No learnable items in snapshot: snapshotId={}", snapshotId);
            return;
        }

        // 3. 완료된 아이템 수 조회
        long completedItems = itemProgressRepository.countByEnrollmentIdAndCompletedAndTenantId(enrollmentId, true, tenantId);

        // 4. 진도율 계산
        int progressPercent = (int) Math.round((completedItems * 100.0) / totalItems);
        progressPercent = Math.min(progressPercent, 100); // 최대 100%

        // 5. 진도율 업데이트
        enrollment.updateProgress(progressPercent);
        enrollmentRepository.save(enrollment);

        log.info("Updated enrollment progress: enrollmentId={}, completedItems={}/{}, progressPercent={}",
                enrollmentId, completedItems, totalItems, progressPercent);
    }
}