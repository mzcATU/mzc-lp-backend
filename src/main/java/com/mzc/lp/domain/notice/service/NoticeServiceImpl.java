package com.mzc.lp.domain.notice.service;

import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import com.mzc.lp.domain.notice.dto.request.CreateNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.DistributeNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.UpdateNoticeRequest;
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

import java.util.List;

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
}
