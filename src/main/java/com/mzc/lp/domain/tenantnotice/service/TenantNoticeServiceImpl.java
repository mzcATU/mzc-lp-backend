package com.mzc.lp.domain.tenantnotice.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.service.NotificationService;
import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.dto.request.CreateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.request.UpdateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeDistributionDetailResponse;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeDistributionStatsResponse;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeDistributionSummaryResponse;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeResponse;
import com.mzc.lp.domain.tenantnotice.dto.response.UserDistributionInfoResponse;
import com.mzc.lp.domain.tenantnotice.entity.TenantNotice;
import com.mzc.lp.domain.tenantnotice.repository.TenantNoticeRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 테넌트 공지사항 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantNoticeServiceImpl implements TenantNoticeService {

    private final TenantNoticeRepository tenantNoticeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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

        // SYSTEM 알림 발송: 공지사항 등록 시 해당 테넌트 사용자 전체 알림
        sendNoticePublishedNotifications(tenantId, notice);

        return TenantNoticeResponse.from(notice);
    }

    /**
     * 공지사항 발행 알림 발송 (SYSTEM 타입)
     * targetAudience에 따라 대상 사용자에게 알림 발송 (다중 역할 지원)
     * - ALL: 전체 활성 사용자
     * - OPERATOR: OPERATOR 역할을 가진 사용자 (기본 역할 또는 추가 역할)
     * - USER: USER 역할을 가진 사용자
     * - DESIGNER: DESIGNER 역할을 가진 사용자
     * - INSTRUCTOR: INSTRUCTOR 역할을 가진 사용자
     */
    private void sendNoticePublishedNotifications(Long tenantId, TenantNotice notice) {
        try {
            NoticeTargetAudience targetAudience = notice.getTargetAudience();
            List<Long> userIds;

            switch (targetAudience) {
                case ALL:
                    // 모든 활성 사용자
                    userIds = userRepository.findActiveUserIdsByTenantId(tenantId);
                    break;
                case OPERATOR:
                    // OPERATOR 역할을 가진 사용자 (다중 역할 지원)
                    userIds = userRepository.findActiveUserIdsByTenantIdHavingRole(tenantId, TenantRole.OPERATOR);
                    break;
                case USER:
                    // USER 역할을 가진 사용자 (다중 역할 지원)
                    userIds = userRepository.findActiveUserIdsByTenantIdHavingRole(tenantId, TenantRole.USER);
                    break;
                case DESIGNER:
                    // DESIGNER 역할을 가진 사용자 (다중 역할 지원)
                    userIds = userRepository.findActiveUserIdsByTenantIdHavingRole(tenantId, TenantRole.DESIGNER);
                    break;
                case INSTRUCTOR:
                    // INSTRUCTOR 역할을 가진 사용자 (다중 역할 지원)
                    userIds = userRepository.findActiveUserIdsByTenantIdHavingRole(tenantId, TenantRole.INSTRUCTOR);
                    break;
                default:
                    userIds = userRepository.findActiveUserIdsByTenantId(tenantId);
            }

            log.info("Sending notice notification to {} users for tenant: {} (targetAudience: {})",
                    userIds.size(), tenantId, targetAudience);

            String title = "새 공지사항";
            String message = notice.getTitle();
            String link = "/tu/b2c/notifications?tab=SYSTEM";  // 알림 페이지 공지사항 탭으로 이동

            for (Long userId : userIds) {
                try {
                    notificationService.createNotification(
                            userId,
                            NotificationType.SYSTEM,
                            title,
                            message,
                            link,
                            notice.getId(),
                            "TENANT_NOTICE",
                            null,  // actorId (시스템 발송)
                            null   // actorName
                    );
                } catch (Exception e) {
                    log.warn("Failed to send notice notification to user {}: {}", userId, e.getMessage());
                }
            }
            log.info("Notice notifications sent to {} users", userIds.size());
        } catch (Exception e) {
            log.error("Failed to send notice notifications: {}", e.getMessage());
        }
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
    // TU/CO 다중 역할 지원 API
    // ============================================

    @Override
    public Page<TenantNoticeResponse> getVisibleNoticesForMultipleAudiences(Long tenantId, Set<NoticeTargetAudience> targetAudiences, Pageable pageable) {
        return tenantNoticeRepository.findVisibleNoticesForMultipleAudiences(tenantId, targetAudiences, Instant.now(), pageable)
                .map(TenantNoticeResponse::from);
    }

    @Override
    @Transactional
    public TenantNoticeResponse getVisibleNoticeForMultipleAudiences(Long tenantId, Long noticeId, Set<NoticeTargetAudience> targetAudiences) {
        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);

        // 발행 상태 확인
        if (!notice.isVisible()) {
            throw new BusinessException(ErrorCode.TENANT_NOTICE_NOT_FOUND);
        }

        // 대상 확인: 사용자의 역할 목록에 공지 대상이 포함되어 있는지 확인
        if (!targetAudiences.contains(notice.getTargetAudience())) {
            throw new BusinessException(ErrorCode.TENANT_NOTICE_NOT_FOUND);
        }

        // 조회수 증가
        notice.incrementViewCount();

        return TenantNoticeResponse.from(notice);
    }

    @Override
    public long countVisibleNoticesForMultipleAudiences(Long tenantId, Set<NoticeTargetAudience> targetAudiences) {
        return tenantNoticeRepository.countVisibleNoticesForMultipleAudiences(tenantId, targetAudiences, Instant.now());
    }

    // ============================================
    // 배포 통계 API
    // ============================================

    @Override
    public Page<TenantNoticeDistributionStatsResponse> getDistributionStats(Long tenantId, Pageable pageable) {
        log.info("Getting distribution stats for tenant: {}", tenantId);

        // 발행된 공지사항만 조회 (PUBLISHED, ARCHIVED)
        Page<TenantNotice> publishedNotices = tenantNoticeRepository.findByTenantIdAndStatusInOrderByPublishedAtDesc(
                tenantId,
                List.of(TenantNoticeStatus.PUBLISHED, TenantNoticeStatus.ARCHIVED),
                pageable
        );

        return publishedNotices.map(notice -> {
            int targetUsers = getTargetUserCount(tenantId, notice.getTargetAudience());
            return TenantNoticeDistributionStatsResponse.from(notice, targetUsers);
        });
    }

    @Override
    public TenantNoticeDistributionSummaryResponse getDistributionSummary(Long tenantId) {
        log.info("Getting distribution summary for tenant: {}", tenantId);

        // 발행된 공지 수
        long publishedCount = tenantNoticeRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of(TenantNoticeStatus.PUBLISHED, TenantNoticeStatus.ARCHIVED)
        );

        // 총 열람 수 (viewCount 합계)
        Long totalReadCount = tenantNoticeRepository.sumViewCountByTenantIdAndStatusIn(
                tenantId,
                List.of(TenantNoticeStatus.PUBLISHED, TenantNoticeStatus.ARCHIVED)
        );
        if (totalReadCount == null) {
            totalReadCount = 0L;
        }

        // 총 대상 사용자 수 (테넌트 내 활성 사용자)
        long totalTargetUsers = userRepository.countActiveUsersByTenantId(tenantId);

        return TenantNoticeDistributionSummaryResponse.of(
                publishedCount,
                publishedCount,  // completedCount = publishedCount (발행된 것 = 배포 완료)
                totalReadCount,
                totalTargetUsers
        );
    }

    @Override
    public TenantNoticeDistributionDetailResponse getDistributionDetail(Long tenantId, Long noticeId) {
        log.info("Getting distribution detail for notice: {} in tenant: {}", noticeId, tenantId);

        TenantNotice notice = findNoticeByIdAndTenantId(noticeId, tenantId);

        int targetUserCount = getTargetUserCount(tenantId, notice.getTargetAudience());
        int readCount = notice.getViewCount() != null ? notice.getViewCount() : 0;

        // 대상 사용자 목록 조회
        List<UserDistributionInfoResponse> userDistributions = getTargetUsersForDistribution(
                tenantId, notice.getTargetAudience(), notice.getPublishedAt()
        );

        return TenantNoticeDistributionDetailResponse.of(
                notice.getId(),
                notice.getTitle(),
                notice.getType(),
                notice.getTargetAudience(),
                notice.getPublishedAt(),
                targetUserCount,
                readCount,
                userDistributions
        );
    }

    // ============================================
    // Private Helper Methods
    // ============================================

    private TenantNotice findNoticeByIdAndTenantId(Long noticeId, Long tenantId) {
        return tenantNoticeRepository.findByIdAndTenantId(noticeId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOTICE_NOT_FOUND));
    }

    /**
     * 대상 audience에 해당하는 사용자 수 조회
     */
    private int getTargetUserCount(Long tenantId, NoticeTargetAudience targetAudience) {
        return switch (targetAudience) {
            case ALL -> (int) userRepository.countActiveUsersByTenantId(tenantId);
            case OPERATOR -> userRepository.countActiveUsersByTenantIdAndRole(tenantId, TenantRole.OPERATOR);
            case USER -> userRepository.countActiveUsersByTenantIdAndRole(tenantId, TenantRole.USER);
            case DESIGNER -> userRepository.countActiveUsersByTenantIdAndRole(tenantId, TenantRole.DESIGNER);
            case INSTRUCTOR -> userRepository.countActiveUsersByTenantIdAndRole(tenantId, TenantRole.INSTRUCTOR);
        };
    }

    /**
     * 배포 상세용 대상 사용자 목록 조회
     * 개별 사용자 열람 상태는 추적하지 않으므로 기본값 반환
     */
    private List<UserDistributionInfoResponse> getTargetUsersForDistribution(
            Long tenantId,
            NoticeTargetAudience targetAudience,
            Instant publishedAt
    ) {
        List<Object[]> users = switch (targetAudience) {
            case ALL -> userRepository.findActiveUsersInfoByTenantId(tenantId);
            case OPERATOR -> userRepository.findActiveUsersInfoByTenantIdAndRole(tenantId, TenantRole.OPERATOR);
            case USER -> userRepository.findActiveUsersInfoByTenantIdAndRole(tenantId, TenantRole.USER);
            case DESIGNER -> userRepository.findActiveUsersInfoByTenantIdAndRole(tenantId, TenantRole.DESIGNER);
            case INSTRUCTOR -> userRepository.findActiveUsersInfoByTenantIdAndRole(tenantId, TenantRole.INSTRUCTOR);
        };

        return users.stream()
                .map(row -> UserDistributionInfoResponse.of(
                        (Long) row[0],      // userId
                        (String) row[1],    // userName
                        (String) row[2],    // userEmail
                        (String) row[3],    // userRole
                        false,              // isRead - 개별 추적 없음
                        publishedAt,        // distributedAt
                        null                // readAt
                ))
                .toList();
    }
}
