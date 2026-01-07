package com.mzc.lp.domain.tenantnotice.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 테넌트 공지사항 엔티티
 * - TA → TO: 테넌트 관리자가 운영자에게 보내는 공지
 * - TA → TU: 테넌트 관리자가 사용자에게 보내는 공지
 * - TO → TU: 운영자가 사용자에게 보내는 공지
 */
@Entity
@Table(name = "tenant_notices", indexes = {
        @Index(name = "idx_tenant_notice_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_tenant_notice_status", columnList = "status"),
        @Index(name = "idx_tenant_notice_target", columnList = "target_audience"),
        @Index(name = "idx_tenant_notice_published_at", columnList = "published_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantNotice extends BaseTimeEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantNoticeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantNoticeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 20)
    private NoticeTargetAudience targetAudience;

    @Column(name = "creator_role", nullable = false, length = 30)
    private String creatorRole; // TENANT_ADMIN or OPERATOR

    @Column(nullable = false)
    private Boolean isPinned = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @Column(nullable = false)
    private Long createdBy;

    @Column
    private Integer viewCount = 0;

    // 정적 팩토리 메서드
    public static TenantNotice create(
            Long tenantId,
            String title,
            String content,
            TenantNoticeType type,
            NoticeTargetAudience targetAudience,
            String creatorRole,
            Long createdBy
    ) {
        TenantNotice notice = new TenantNotice();
        notice.tenantId = tenantId;
        notice.title = title;
        notice.content = content;
        notice.type = type;
        notice.targetAudience = targetAudience;
        notice.creatorRole = creatorRole;
        notice.status = TenantNoticeStatus.DRAFT;
        notice.createdBy = createdBy;
        notice.viewCount = 0;
        return notice;
    }

    // 비즈니스 메서드
    public void update(String title, String content, TenantNoticeType type, NoticeTargetAudience targetAudience, Boolean isPinned, Instant expiredAt) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (type != null) {
            this.type = type;
        }
        if (targetAudience != null) {
            this.targetAudience = targetAudience;
        }
        if (isPinned != null) {
            this.isPinned = isPinned;
        }
        this.expiredAt = expiredAt;
    }

    public void publish() {
        this.status = TenantNoticeStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void archive() {
        this.status = TenantNoticeStatus.ARCHIVED;
    }

    public void setExpiration(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public boolean isPublished() {
        return this.status == TenantNoticeStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == TenantNoticeStatus.DRAFT;
    }

    public boolean isExpired() {
        return this.expiredAt != null && Instant.now().isAfter(this.expiredAt);
    }

    public boolean isVisible() {
        return isPublished() && !isExpired();
    }
}
