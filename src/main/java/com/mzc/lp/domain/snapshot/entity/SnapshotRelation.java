package com.mzc.lp.domain.snapshot.entity;

import com.mzc.lp.common.constant.ValidationMessages;
import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cm_snapshot_relations",
       uniqueConstraints = @UniqueConstraint(name = "uk_snapshot_to_item", columnNames = "to_item_id"),
       indexes = {
           @Index(name = "idx_sr_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sr_snapshot", columnList = "snapshot_id"),
           @Index(name = "idx_sr_from", columnList = "from_item_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnapshotRelation extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private CourseSnapshot snapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_item_id")
    private SnapshotItem fromItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_item_id", nullable = false)
    private SnapshotItem toItem;

    // ===== 정적 팩토리 메서드 =====
    public static SnapshotRelation create(CourseSnapshot snapshot, SnapshotItem fromItem, SnapshotItem toItem) {
        validateRelation(fromItem, toItem);

        SnapshotRelation relation = new SnapshotRelation();
        relation.snapshot = snapshot;
        relation.fromItem = fromItem;
        relation.toItem = toItem;
        return relation;
    }

    public static SnapshotRelation createStartPoint(CourseSnapshot snapshot, SnapshotItem toItem) {
        if (toItem == null) {
            throw new IllegalArgumentException(ValidationMessages.START_POINT_REQUIRED);
        }
        if (toItem.isFolder()) {
            throw new IllegalArgumentException(ValidationMessages.FOLDER_CANNOT_BE_IN_LEARNING_ORDER);
        }

        SnapshotRelation relation = new SnapshotRelation();
        relation.snapshot = snapshot;
        relation.fromItem = null;
        relation.toItem = toItem;
        return relation;
    }

    // ===== 비즈니스 메서드 =====
    public boolean isStartPoint() {
        return this.fromItem == null;
    }

    public void updateFromItem(SnapshotItem fromItem) {
        validateRelation(fromItem, this.toItem);
        this.fromItem = fromItem;
    }

    public void updateToItem(SnapshotItem toItem) {
        validateRelation(this.fromItem, toItem);
        this.toItem = toItem;
    }

    // ===== 연관관계 편의 메서드 =====
    void assignSnapshot(CourseSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    // ===== Private 검증 메서드 =====
    private static void validateRelation(SnapshotItem fromItem, SnapshotItem toItem) {
        if (toItem == null) {
            throw new IllegalArgumentException(ValidationMessages.TARGET_ITEM_REQUIRED);
        }
        if (toItem.isFolder()) {
            throw new IllegalArgumentException(ValidationMessages.FOLDER_CANNOT_BE_IN_LEARNING_ORDER);
        }
        if (fromItem != null) {
            if (fromItem.isFolder()) {
                throw new IllegalArgumentException(ValidationMessages.FOLDER_CANNOT_BE_IN_LEARNING_ORDER);
            }
            if (fromItem.getId() != null && fromItem.getId().equals(toItem.getId())) {
                throw new IllegalArgumentException(ValidationMessages.CANNOT_REFERENCE_SELF);
            }
        }
    }
}
