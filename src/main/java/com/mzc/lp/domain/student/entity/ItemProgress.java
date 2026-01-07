package com.mzc.lp.domain.student.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 아이템(차시)별 학습 진도 엔티티
 * - 수강(Enrollment)에 속한 각 학습 아이템의 진도를 추적
 */
@Entity
@Table(name = "sis_item_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sis_item_progress",
                columnNames = {"tenant_id", "enrollment_id", "item_id"}
        ),
        indexes = {
                @Index(name = "idx_sis_item_progress_enrollment", columnList = "tenant_id, enrollment_id"),
                @Index(name = "idx_sis_item_progress_item", columnList = "tenant_id, item_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemProgress extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent;

    @Column(name = "watched_seconds")
    private Integer watchedSeconds;

    @Column(nullable = false)
    private Boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_position_seconds")
    private Integer lastPositionSeconds;

    // 정적 팩토리 메서드
    public static ItemProgress create(Long enrollmentId, Long itemId) {
        ItemProgress progress = new ItemProgress();
        progress.enrollmentId = enrollmentId;
        progress.itemId = itemId;
        progress.progressPercent = 0;
        progress.watchedSeconds = 0;
        progress.completed = false;
        progress.lastPositionSeconds = 0;
        return progress;
    }

    // 비즈니스 메서드

    /**
     * 진도 업데이트
     */
    public void updateProgress(Integer progressPercent, Integer watchedSeconds, Integer lastPositionSeconds) {
        if (progressPercent != null) {
            if (progressPercent < 0 || progressPercent > 100) {
                throw new IllegalArgumentException("Progress percent must be between 0 and 100");
            }
            this.progressPercent = progressPercent;
        }
        if (watchedSeconds != null) {
            this.watchedSeconds = watchedSeconds;
        }
        if (lastPositionSeconds != null) {
            this.lastPositionSeconds = lastPositionSeconds;
        }
    }

    /**
     * 완료 처리
     */
    public void markAsCompleted() {
        this.completed = true;
        this.completedAt = Instant.now();
        this.progressPercent = 100;
    }

    /**
     * 완료 취소 (관리자용)
     */
    public void unmarkCompleted() {
        this.completed = false;
        this.completedAt = null;
    }

    public boolean isCompleted() {
        return this.completed;
    }
}