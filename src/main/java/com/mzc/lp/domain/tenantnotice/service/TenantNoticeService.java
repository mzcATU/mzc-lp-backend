package com.mzc.lp.domain.tenantnotice.service;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.dto.request.CreateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.request.UpdateTenantNoticeRequest;
import com.mzc.lp.domain.tenantnotice.dto.response.TenantNoticeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 테넌트 공지사항 서비스 인터페이스
 */
public interface TenantNoticeService {

    // ============================================
    // TA/TO 관리용 API
    // ============================================

    /**
     * 공지사항 생성
     */
    TenantNoticeResponse createNotice(Long tenantId, CreateTenantNoticeRequest request, Long createdBy, String creatorRole);

    /**
     * 공지사항 수정
     */
    TenantNoticeResponse updateNotice(Long tenantId, Long noticeId, UpdateTenantNoticeRequest request, Long userId, boolean isAdmin);

    /**
     * 공지사항 삭제
     */
    void deleteNotice(Long tenantId, Long noticeId, Long userId, boolean isAdmin);

    /**
     * 공지사항 발행
     */
    TenantNoticeResponse publishNotice(Long tenantId, Long noticeId);

    /**
     * 공지사항 보관
     */
    TenantNoticeResponse archiveNotice(Long tenantId, Long noticeId);

    /**
     * 공지사항 상세 조회 (관리자용)
     */
    TenantNoticeResponse getNotice(Long tenantId, Long noticeId);

    /**
     * 공지사항 목록 조회 (관리자용)
     */
    Page<TenantNoticeResponse> getNotices(Long tenantId, TenantNoticeStatus status, NoticeTargetAudience targetAudience, Pageable pageable);

    /**
     * 공지사항 검색
     */
    Page<TenantNoticeResponse> searchNotices(Long tenantId, String keyword, Pageable pageable);

    // ============================================
    // TU/TO 조회용 API
    // ============================================

    /**
     * 발행된 공지사항 목록 조회 (사용자용)
     */
    Page<TenantNoticeResponse> getVisibleNotices(Long tenantId, NoticeTargetAudience targetAudience, Pageable pageable);

    /**
     * 발행된 공지사항 상세 조회 (사용자용, 조회수 증가)
     */
    TenantNoticeResponse getVisibleNotice(Long tenantId, Long noticeId, NoticeTargetAudience targetAudience);

    /**
     * 발행된 공지 수 조회
     */
    long countVisibleNotices(Long tenantId, NoticeTargetAudience targetAudience);
}
