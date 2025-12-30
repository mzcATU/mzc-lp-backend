package com.mzc.lp.domain.notice.service;

import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import com.mzc.lp.domain.notice.dto.request.CreateNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.DistributeNoticeRequest;
import com.mzc.lp.domain.notice.dto.request.UpdateNoticeRequest;
import com.mzc.lp.domain.notice.dto.response.NoticeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeService {

    // CRUD
    NoticeResponse createNotice(CreateNoticeRequest request, Long createdBy);

    Page<NoticeResponse> getNotices(String keyword, NoticeStatus status, NoticeType type, Pageable pageable);

    NoticeResponse getNotice(Long noticeId);

    NoticeResponse updateNotice(Long noticeId, UpdateNoticeRequest request);

    void deleteNotice(Long noticeId);

    // 상태 변경
    NoticeResponse publishNotice(Long noticeId);

    NoticeResponse archiveNotice(Long noticeId);

    // 배포
    void distributeNotice(Long noticeId, DistributeNoticeRequest request);

    void distributeToAllTenants(Long noticeId);

    List<Long> getDistributedTenantIds(Long noticeId);
}
