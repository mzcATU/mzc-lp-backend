package com.mzc.lp.domain.notice.entity;

import com.mzc.lp.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 테넌트 배포 엔티티
 * 특정 테넌트에 공지사항을 배포
 */
@Entity
@Table(name = "notice_distributions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"notice_id", "tenant_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeDistribution extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Boolean isRead = false;

    // 정적 팩토리 메서드
    public static NoticeDistribution create(Notice notice, Long tenantId) {
        NoticeDistribution distribution = new NoticeDistribution();
        distribution.notice = notice;
        distribution.tenantId = tenantId;
        return distribution;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
