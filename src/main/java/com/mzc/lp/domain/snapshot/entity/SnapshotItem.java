package com.mzc.lp.domain.snapshot.entity;

import com.mzc.lp.common.constant.ValidationMessages;
import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_snapshot_items", indexes = {
        @Index(name = "idx_si_tenant", columnList = "tenant_id"),
        @Index(name = "idx_si_snapshot", columnList = "snapshot_id"),
        @Index(name = "idx_si_parent", columnList = "parent_id"),
        @Index(name = "idx_si_source_item", columnList = "source_item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnapshotItem extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version = 0L;

    private static final int MAX_DEPTH = 9;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private CourseSnapshot snapshot;

    @Column(name = "source_item_id")
    private Long sourceItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SnapshotItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnapshotItem> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_lo_id")
    private SnapshotLearningObject snapshotLearningObject;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(nullable = false)
    private Integer depth;

    @Column(name = "item_type", length = 20)
    private String itemType;

    // ===== 정적 팩토리 메서드 =====
    public static SnapshotItem createFolder(CourseSnapshot snapshot, String itemName, SnapshotItem parent) {
        SnapshotItem item = new SnapshotItem();
        item.snapshot = snapshot;
        item.itemName = itemName;
        item.parent = parent;
        item.depth = parent != null ? parent.getDepth() + 1 : 0;
        item.snapshotLearningObject = null;
        item.itemType = null;
        item.validateDepth();
        return item;
    }

    public static SnapshotItem createItem(CourseSnapshot snapshot, String itemName,
                                          SnapshotItem parent, SnapshotLearningObject snapshotLo,
                                          String itemType) {
        SnapshotItem item = new SnapshotItem();
        item.snapshot = snapshot;
        item.itemName = itemName;
        item.parent = parent;
        item.depth = parent != null ? parent.getDepth() + 1 : 0;
        item.snapshotLearningObject = snapshotLo;
        item.itemType = itemType;
        item.validateDepth();
        return item;
    }

    public static SnapshotItem createFromCourseItem(CourseSnapshot snapshot, Long sourceItemId,
                                                     String itemName, SnapshotItem parent,
                                                     SnapshotLearningObject snapshotLo, String itemType) {
        SnapshotItem item = new SnapshotItem();
        item.snapshot = snapshot;
        item.sourceItemId = sourceItemId;
        item.itemName = itemName;
        item.parent = parent;
        item.depth = parent != null ? parent.getDepth() + 1 : 0;
        item.snapshotLearningObject = snapshotLo;
        item.itemType = itemType;
        item.validateDepth();
        return item;
    }

    // ===== 비즈니스 메서드 =====
    public boolean isFolder() {
        return this.snapshotLearningObject == null;
    }

    public void updateItemName(String itemName) {
        validateItemName(itemName);
        this.itemName = itemName;
    }

    public void updateSnapshotLearningObject(SnapshotLearningObject snapshotLo) {
        if (isFolder()) {
            throw new IllegalStateException("폴더에는 학습 객체를 연결할 수 없습니다");
        }
        this.snapshotLearningObject = snapshotLo;
    }

    public void moveTo(SnapshotItem newParent) {
        int newDepth = newParent != null ? newParent.getDepth() + 1 : 0;

        int maxChildDepth = calculateMaxChildDepth();
        int depthDiff = newDepth - this.depth;

        if (maxChildDepth + depthDiff > MAX_DEPTH) {
            throw new IllegalArgumentException(ValidationMessages.MAX_DEPTH_EXCEEDED);
        }

        if (newParent != null && isAncestorOf(newParent)) {
            throw new IllegalArgumentException(ValidationMessages.CANNOT_MOVE_TO_CHILD);
        }

        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }

        this.parent = newParent;
        updateDepthRecursively(newDepth);

        if (newParent != null) {
            newParent.getChildren().add(this);
        }
    }

    // ===== 연관관계 편의 메서드 =====
    void assignSnapshot(CourseSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void addChild(SnapshotItem child) {
        this.children.add(child);
        child.parent = this;
    }

    // ===== Private 검증/헬퍼 메서드 =====
    private void validateDepth() {
        if (this.depth > MAX_DEPTH) {
            throw new IllegalArgumentException(ValidationMessages.MAX_DEPTH_EXCEEDED);
        }
    }

    private void validateItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.ITEM_NAME_REQUIRED);
        }
        if (itemName.length() > 255) {
            throw new IllegalArgumentException(ValidationMessages.ITEM_NAME_TOO_LONG);
        }
    }

    private int calculateMaxChildDepth() {
        if (children.isEmpty()) {
            return this.depth;
        }
        return children.stream()
                .mapToInt(SnapshotItem::calculateMaxChildDepth)
                .max()
                .orElse(this.depth);
    }

    private boolean isAncestorOf(SnapshotItem item) {
        SnapshotItem current = item;
        while (current != null) {
            if (current.getId() != null && current.getId().equals(this.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void updateDepthRecursively(int newDepth) {
        this.depth = newDepth;
        for (SnapshotItem child : children) {
            child.updateDepthRecursively(newDepth + 1);
        }
    }
}
