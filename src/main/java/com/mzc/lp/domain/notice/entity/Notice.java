package com.mzc.lp.domain.notice.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 공지사항 엔티티 (SA 전용)
 * 시스템 전체 또는 특정 테넌트에 배포되는 공지사항
 */
@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeStatus status;

    @Column(nullable = false)
    private Boolean isPinned = false;

    @Column
    private Instant publishedAt;

    @Column
    private Instant expiredAt;

    @Column(nullable = false)
    private Long createdBy;

    // 정적 팩토리 메서드
    public static Notice create(String title, String content, NoticeType type, Long createdBy) {
        Notice notice = new Notice();
        notice.title = title;
        notice.content = content;
        notice.type = type;
        notice.status = NoticeStatus.DRAFT;
        notice.createdBy = createdBy;
        return notice;
    }

    // 비즈니스 메서드
    public void update(String title, String content, NoticeType type) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (type != null) {
            this.type = type;
        }
    }

    public void publish() {
        this.status = NoticeStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void archive() {
        this.status = NoticeStatus.ARCHIVED;
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

    public boolean isPublished() {
        return this.status == NoticeStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == NoticeStatus.DRAFT;
    }

    public boolean isExpired() {
        return this.expiredAt != null && Instant.now().isAfter(this.expiredAt);
    }
}
