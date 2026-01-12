package com.mzc.lp.domain.notice.service;

import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import com.mzc.lp.domain.notice.dto.request.CreateNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.DistributeNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.UpdateNoticeRequest;
import com.mzc.lp.domain.notice.dto.response.NoticeDistributionStatsResponse;
import com.mzc.lp.domain.notice.dto.response.NoticeDistributionSummaryResponse;
import com.mzc.lp.domain.notice.dto.response.NoticeResponse;
import com.mzc.lp.domain.notice.entity.Notice;
import com.mzc.lp.domain.notice.entity.NoticeDistribution;
import com.mzc.lp.domain.notice.exception.NoticeNotFoundException;
import com.mzc.lp.domain.notice.repository.NoticeDistributionRepository;
import com.mzc.lp.domain.notice.repository.NoticeRepository;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mzc.lp.domain.tenant.entity.Tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeDistributionRepository noticeDistributionRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public NoticeResponse createNotice(CreateNoticeRequest request, Long createdBy) {
        Notice notice = Notice.create(
                request.title(),
                request.content(),
                request.type(),
                createdBy
        );

        if (request.isPinned() != null && request.isPinned()) {
            notice.pin();
        }

        if (request.expiredAt() != null) {
            notice.setExpiration(request.expiredAt());
        }

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponse.from(savedNotice, 0L);
    }

    @Override
    public Page<NoticeResponse> getNotices(String keyword, NoticeStatus status, NoticeType type, Pageable pageable) {
        Page<Notice> notices;

        if (keyword != null && !keyword.isBlank()) {
            if (status != null) {
                notices = noticeRepository.searchByKeywordAndStatus(keyword, status, pageable);
            } else {
                notices = noticeRepository.searchByKeyword(keyword, pageable);
            }
        } else if (status != null && type != null) {
            notices = noticeRepository.findByStatusAndType(status, type, pageable);
        } else if (status != null) {
            notices = noticeRepository.findByStatus(status, pageable);
        } else if (type != null) {
            notices = noticeRepository.findByType(type, pageable);
        } else {
            notices = noticeRepository.findAll(pageable);
        }

        return notices.map(notice -> {
            long count = noticeDistributionRepository.countByNoticeId(notice.getId());
            return NoticeResponse.from(notice, count);
        });
    }

    @Override
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        long count = noticeDistributionRepository.countByNoticeId(noticeId);
        return NoticeResponse.from(notice, count);
    }

    @Override
    @Transactional
    public NoticeResponse updateNotice(Long noticeId, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        notice.update(request.title(), request.content(), request.type());

        if (request.isPinned() != null) {
            if (request.isPinned()) {
                notice.pin();
            } else {
                notice.unpin();
            }
        }

        if (request.expiredAt() != null) {
            notice.setExpiration(request.expiredAt());
        }

        long count = noticeDistributionRepository.countByNoticeId(noticeId);
        return NoticeResponse.from(notice, count);
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        // 배포 정보도 함께 삭제
        noticeDistributionRepository.deleteByNoticeId(noticeId);
        noticeRepository.delete(notice);
    }

    @Override
    @Transactional
    public NoticeResponse publishNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        notice.publish();

        long count = noticeDistributionRepository.countByNoticeId(noticeId);
        return NoticeResponse.from(notice, count);
    }

    @Override
    @Transactional
    public NoticeResponse archiveNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        notice.archive();

        long count = noticeDistributionRepository.countByNoticeId(noticeId);
        return NoticeResponse.from(notice, count);
    }

    @Override
    @Transactional
    public void distributeNotice(Long noticeId, DistributeNoticeRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        for (Long tenantId : request.tenantIds()) {
            if (!noticeDistributionRepository.existsByNoticeIdAndTenantId(noticeId, tenantId)) {
                NoticeDistribution distribution = NoticeDistribution.create(notice, tenantId);
                noticeDistributionRepository.save(distribution);
            }
        }
    }

    @Override
    @Transactional
    public void distributeToAllTenants(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        List<Long> allTenantIds = tenantRepository.findAll()
                .stream()
                .map(t -> t.getId())
                .toList();

        for (Long tenantId : allTenantIds) {
            if (!noticeDistributionRepository.existsByNoticeIdAndTenantId(noticeId, tenantId)) {
                NoticeDistribution distribution = NoticeDistribution.create(notice, tenantId);
                noticeDistributionRepository.save(distribution);
            }
        }
    }

    @Override
    public List<Long> getDistributedTenantIds(Long noticeId) {
        return noticeDistributionRepository.findByNoticeId(noticeId)
                .stream()
                .map(NoticeDistribution::getTenantId)
                .toList();
    }

    @Override
    public Page<NoticeResponse> getNoticesForTenant(Long tenantId, Pageable pageable) {
        return noticeDistributionRepository.findPublishedByTenantId(tenantId, pageable)
                .map(distribution -> NoticeResponse.from(distribution.getNotice(), 0L));
    }

    @Override
    public NoticeResponse getNoticeForTenant(Long noticeId, Long tenantId) {
        NoticeDistribution distribution = noticeDistributionRepository
                .findByNoticeIdAndTenantId(noticeId, tenantId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        return NoticeResponse.from(distribution.getNotice(), 0L);
    }

    @Override
    @Transactional
    public void markNoticeAsRead(Long noticeId, Long tenantId) {
        NoticeDistribution distribution = noticeDistributionRepository
                .findByNoticeIdAndTenantId(noticeId, tenantId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        distribution.markAsRead();
    }

    @Override
    public Page<NoticeDistributionStatsResponse> getDistributionStats(Pageable pageable) {
        // 배포된 적 있는 공지사항 조회 (PUBLISHED 상태)
        Page<Notice> publishedNotices = noticeRepository.findByStatus(NoticeStatus.PUBLISHED, pageable);
        int totalTenants = (int) tenantRepository.count();

        // 테넌트 정보 캐시
        Map<Long, Tenant> tenantMap = tenantRepository.findAll().stream()
                .collect(Collectors.toMap(Tenant::getId, t -> t));

        return publishedNotices.map(notice -> buildDistributionStats(notice, totalTenants, tenantMap));
    }

    @Override
    public NoticeDistributionStatsResponse getDistributionStatsForNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        int totalTenants = (int) tenantRepository.count();
        Map<Long, Tenant> tenantMap = tenantRepository.findAll().stream()
                .collect(Collectors.toMap(Tenant::getId, t -> t));

        return buildDistributionStats(notice, totalTenants, tenantMap);
    }

    @Override
    public NoticeDistributionSummaryResponse getDistributionSummary() {
        int totalTenants = (int) tenantRepository.count();

        // PUBLISHED 상태인 공지사항 수
        long publishedCount = noticeRepository.countByStatus(NoticeStatus.PUBLISHED);

        // 배포된 공지사항 수 (최소 1개 테넌트에 배포됨)
        List<Long> distributedNoticeIds = noticeDistributionRepository.findDistributedNoticeIds();
        int totalDistributions = distributedNoticeIds.size();

        // 전체 배포 중 읽음 수 계산
        int totalReadCount = 0;
        int totalSentCount = 0;

        for (Long noticeId : distributedNoticeIds) {
            totalSentCount += (int) noticeDistributionRepository.countByNoticeId(noticeId);
            totalReadCount += (int) noticeDistributionRepository.countReadByNoticeId(noticeId);
        }

        double averageReadRate = totalSentCount > 0 ? (double) totalReadCount / totalSentCount * 100 : 0;

        return NoticeDistributionSummaryResponse.of(
                totalDistributions,
                (int) publishedCount,  // completedCount (발행 완료)
                0,  // inProgressCount (예약 기능 없으므로 0)
                totalTenants,
                totalReadCount,
                averageReadRate
        );
    }

    private NoticeDistributionStatsResponse buildDistributionStats(Notice notice, int totalTenants, Map<Long, Tenant> tenantMap) {
        List<NoticeDistribution> distributions = noticeDistributionRepository.findByNoticeId(notice.getId());

        int sentCount = distributions.size();
        int readCount = (int) distributions.stream().filter(NoticeDistribution::getIsRead).count();

        List<NoticeDistributionStatsResponse.TenantDistributionInfo> tenantInfos = distributions.stream()
                .map(dist -> {
                    Tenant tenant = tenantMap.get(dist.getTenantId());
                    return new NoticeDistributionStatsResponse.TenantDistributionInfo(
                            dist.getTenantId(),
                            tenant != null ? tenant.getName() : "Unknown",
                            tenant != null ? tenant.getCode() : "unknown",
                            dist.getIsRead(),
                            dist.getCreatedAt(),
                            dist.getIsRead() ? dist.getUpdatedAt() : null
                    );
                })
                .toList();

        return NoticeDistributionStatsResponse.of(
                notice.getId(),
                notice.getTitle(),
                notice.getType().name(),
                notice.getStatus().name(),
                notice.getIsPinned(),
                totalTenants,
                sentCount,
                readCount,
                notice.getPublishedAt(),
                notice.getCreatedAt(),
                tenantInfos
        );
    }
}
