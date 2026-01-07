package com.mzc.lp.domain.snapshot.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cm_snapshot_los", indexes = {
        @Index(name = "idx_slo_tenant", columnList = "tenant_id"),
        @Index(name = "idx_slo_content", columnList = "content_id"),
        @Index(name = "idx_slo_source_lo", columnList = "source_lo_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnapshotLearningObject extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version;

    @Column(name = "source_lo_id")
    private Long sourceLoId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column
    private Integer duration;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(length = 50)
    private String resolution;

    @Column(length = 50)
    private String codec;

    @Column
    private Long bitrate;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "external_url", length = 2000)
    private String externalUrl;

    @Column(name = "is_customized", nullable = false)
    private Boolean isCustomized;

    // ===== 정적 팩토리 메서드 =====
    public static SnapshotLearningObject create(Long contentId, String displayName) {
        SnapshotLearningObject slo = new SnapshotLearningObject();
        slo.contentId = contentId;
        slo.displayName = displayName;
        slo.isCustomized = false;
        return slo;
    }

    public static SnapshotLearningObject createFromLo(Long sourceLoId, Long contentId,
                                                       String displayName, Integer duration,
                                                       String thumbnailUrl, String resolution,
                                                       String codec, Long bitrate, Integer pageCount,
                                                       String externalUrl) {
        SnapshotLearningObject slo = new SnapshotLearningObject();
        slo.sourceLoId = sourceLoId;
        slo.contentId = contentId;
        slo.displayName = displayName;
        slo.duration = duration;
        slo.thumbnailUrl = thumbnailUrl;
        slo.resolution = resolution;
        slo.codec = codec;
        slo.bitrate = bitrate;
        slo.pageCount = pageCount;
        slo.externalUrl = externalUrl;
        slo.isCustomized = false;
        return slo;
    }

    // ===== 비즈니스 메서드 =====
    public void updateDisplayName(String displayName) {
        validateDisplayName(displayName);
        this.displayName = displayName;
        this.isCustomized = true;
    }

    public void updateDuration(Integer duration) {
        this.duration = duration;
        this.isCustomized = true;
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        this.isCustomized = true;
    }

    // ===== Private 검증 메서드 =====
    private void validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("표시명은 필수입니다");
        }
        if (displayName.length() > 255) {
            throw new IllegalArgumentException("표시명은 255자 이하여야 합니다");
        }
    }
}
