package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.student.dto.request.UpdateItemProgressRequest;
import com.mzc.lp.domain.student.dto.response.ItemProgressResponse;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.entity.ItemProgress;
import com.mzc.lp.domain.student.exception.EnrollmentNotFoundException;
import com.mzc.lp.domain.student.exception.UnauthorizedEnrollmentAccessException;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.student.repository.ItemProgressRepository;
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
     * 전체 수강 진도율 업데이트 (아이템 완료율 기반)
     */
    private void updateEnrollmentProgress(Long enrollmentId, Long tenantId) {
        Double completionRate = itemProgressRepository.getCompletionRateByEnrollmentId(enrollmentId, tenantId);

        if (completionRate != null) {
            Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                    .orElse(null);

            if (enrollment != null) {
                enrollment.updateProgress(completionRate.intValue());
                enrollmentRepository.save(enrollment);

                log.info("Updated enrollment progress: enrollmentId={}, progressPercent={}",
                        enrollmentId, completionRate.intValue());
            }
        }
    }
}