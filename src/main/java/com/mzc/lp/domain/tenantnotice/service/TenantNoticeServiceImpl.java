package com.mzc.lp.domain.tenantnotice.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.dto.request.CreateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.request.UpdateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeResponse;
import com.mzc.lp.domain.tenantnotice.entity.TenantNotice;
import com.mzc.lp.domain.tenantnotice.repository.TenantNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 테넌트 공지사항 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantNoticeServiceImpl implements TenantNoticeService {

    private final TenantNoticeRepository tenantNoticeRepository;

    // ============================================
    // TA/CO 관리용 API
    // ============================================

    @Override
    @Transactional
    public TenantNoticeResponse createNotice(Long tenantId, CreateTenantNoticeRequest request, Long createdBy, String creatorRole) {
        log.info("Creating tenant notice for tenant: {}, createdBy: {}, role: {}", tenantId, createdBy, creatorRole);

        TenantNotice notice = TenantNotice.create(
                tenantId,
                request.title(),
                request.content(),
                request.type(),
                request.targetAudience(),
                creatorRole,
                createdBy
        );

        if (request.isPinned() != null && request.isPinned()) {
            notice.pin();
        }

        if (request.expiredAt() != null) {
            notice.setExpiration(request.expiredAt());
        }

        TenantNotice savedNotice = tenantNoticeRepository.save(notice);
        log.info("Created tenant notice with id: {}", savedNotice.getId());

        return TenantNoticeResponse.from(savedNotice);
    }

    @Override
    @Transactional
    public TenantNoticeResponse updateNotice(Long tenantId, Long noticeId, UpdateTenantNoticeRequest request, Long userId, boolean isAdmin) {
        log.info("Updating tenant notice: {} for tenant: {}", noticeId, tenantId);

        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);

        // 권한 확인: 관리자이거나 작성자 본인만 수정 가능
        if (!isAdmin && !notice.getCreatedBy().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        notice.update(
                request.title(),
                request.content(),
                request.type(),
                request.targetAudience(),
                request.isPinned(),
                request.expiredAt()
        );

        log.info("Updated tenant notice: {}", noticeId);
        return TenantNoticeResponse.from(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(Long tenantId, Long noticeId, Long userId, boolean isAdmin) {
        log.info("Deleting tenant notice: {} for tenant: {}", noticeId, tenantId);

        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);

        // 권한 확인: 관리자이거나 작성자 본인만 삭제 가능
        if (!isAdmin && !notice.getCreatedBy().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        tenantNoticeRepository.delete(notice);
        log.info("Deleted tenant notice: {}", noticeId);
    }

    @Override
    @Transactional
    public TenantNoticeResponse publishNotice(Long tenantId, Long noticeId) {
        log.info("Publishing tenant notice: {} for tenant: {}", noticeId, tenantId);

        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);

        if (notice.isPublished()) {
            throw new BusinessException(ErrorCode.TENANT_NOTICE_ALREADY_PUBLISHED);
        }

        notice.publish();
        log.info("Published tenant notice: {}", noticeId);

        return TenantNoticeResponse.from(notice);
    }

    @Override
    @Transactional
    public TenantNoticeResponse archiveNotice(Long tenantId, Long noticeId) {
        log.info("Archiving tenant notice: {} for tenant: {}", noticeId, tenantId);

        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);
        notice.archive();
        log.info("Archived tenant notice: {}", noticeId);

        return TenantNoticeResponse.from(notice);
    }

    @Override
    public TenantNoticeResponse getNotice(Long tenantId, Long noticeId) {
        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);
        return TenantNoticeResponse.from(notice);
    }

    @Override
    public Page<TenantNoticeResponse> getNotices(Long tenantId, TenantNoticeStatus status, NoticeTargetAudience targetAudience, Pageable pageable) {
        Page<TenantNotice> notices;

        if (status != null && targetAudience != null) {
            notices = tenantNoticeRepository.findByTenantIdAndStatusAndTargetAudienceOrderByIsPinnedDescCreatedAtDesc(
                    tenantId, status, targetAudience, pageable);
        } else if (status != null) {
            notices = tenantNoticeRepository.findByTenantIdAndStatusOrderByIsPinnedDescCreatedAtDesc(
                    tenantId, status, pageable);
        } else if (targetAudience != null) {
            notices = tenantNoticeRepository.findByTenantIdAndTargetAudienceOrderByIsPinnedDescCreatedAtDesc(
                    tenantId, targetAudience, pageable);
        } else {
            notices = tenantNoticeRepository.findByTenantIdOrderByIsPinnedDescCreatedAtDesc(tenantId, pageable);
        }

        return notices.map(TenantNoticeResponse::from);
    }

    @Override
    public Page<TenantNoticeResponse> searchNotices(Long tenantId, String keyword, Pageable pageable) {
        return tenantNoticeRepository.searchByKeyword(tenantId, keyword, pageable)
                .map(TenantNoticeResponse::from);
    }

    // ============================================
    // TU/CO 조회용 API
    // ============================================

    @Override
    public Page<TenantNoticeResponse> getVisibleNotices(Long tenantId, NoticeTargetAudience targetAudience, Pageable pageable) {
        return tenantNoticeRepository.findVisibleNotices(tenantId, targetAudience, Instant.now(), pageable)
                .map(TenantNoticeResponse::from);
    }

    @Override
    @Transactional
    public TenantNoticeResponse getVisibleNotice(Long tenantId, Long noticeId, NoticeTargetAudience targetAudience) {
        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);

        // 발행 상태 확인
        if (!notice.isVisible()) {
            throw new BusinessException(ErrorCode.TENANT_NOTICE_NOT_FOUND);
        }

        // 대상 확인
        if (notice.getTargetAudience() != targetAudience) {
            throw new BusinessException(ErrorCode.TENANT_NOTICE_NOT_FOUND);
        }

        // 조회수 증가
        notice.incrementViewCount();

        return TenantNoticeResponse.from(notice);
    }

    @Override
    public long countVisibleNotices(Long tenantId, NoticeTargetAudience targetAudience) {
        return tenantNoticeRepository.countVisibleNotices(tenantId, targetAudience, Instant.now());
    }

    // ============================================
    // Private Helper Methods
    // ============================================

    private TenantNotice findNoticeByIdAndTenantId(Long noticeId, Long tenantId) {
        return tenantNoticeRepository.findByIdAndTenantId(noticeId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOTICE_NOT_FOUND));
    }
}
