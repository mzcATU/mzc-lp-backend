package com.mzc.lp.domain.snapshot.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.exception.SnapshotStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_snapshots", indexes = {
        @Index(name = "idx_snapshot_tenant", columnList = "tenant_id"),
        @Index(name = "idx_snapshot_status", columnList = "status"),
        @Index(name = "idx_snapshot_created_by", columnList = "created_by"),
        @Index(name = "idx_snapshot_source", columnList = "source_course_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSnapshot extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_course_id")
    private Course sourceCourse;

    @Column(name = "snapshot_name", nullable = false, length = 255)
    private String snapshotName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String hashtags;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SnapshotStatus status;

    @Column(nullable = false)
    private Integer version;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnapshotItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnapshotRelation> relations = new ArrayList<>();

    // ===== 정적 팩토리 메서드 =====
    public static CourseSnapshot create(String snapshotName, Long createdBy) {
        CourseSnapshot snapshot = new CourseSnapshot();
        snapshot.snapshotName = snapshotName;
        snapshot.createdBy = createdBy;
        snapshot.status = SnapshotStatus.DRAFT;
        snapshot.version = 1;
        return snapshot;
    }

    public static CourseSnapshot createFromCourse(Course course, Long createdBy) {
        CourseSnapshot snapshot = new CourseSnapshot();
        snapshot.sourceCourse = course;
        snapshot.snapshotName = course.getTitle();
        snapshot.description = course.getDescription();
        snapshot.createdBy = createdBy;
        snapshot.status = SnapshotStatus.DRAFT;
        snapshot.version = 1;
        return snapshot;
    }

    public static CourseSnapshot create(String snapshotName, String description,
                                         String hashtags, Long createdBy) {
        CourseSnapshot snapshot = new CourseSnapshot();
        snapshot.snapshotName = snapshotName;
        snapshot.description = description;
        snapshot.hashtags = hashtags;
        snapshot.createdBy = createdBy;
        snapshot.status = SnapshotStatus.DRAFT;
        snapshot.version = 1;
        return snapshot;
    }

    // ===== 비즈니스 메서드 =====
    public void updateSnapshotName(String snapshotName) {
        validateModifiable();
        validateSnapshotName(snapshotName);
        this.snapshotName = snapshotName;
    }

    public void updateDescription(String description) {
        validateModifiable();
        this.description = description;
    }

    public void updateHashtags(String hashtags) {
        validateModifiable();
        this.hashtags = hashtags;
    }

    public void update(String snapshotName, String description, String hashtags) {
        validateModifiable();
        if (snapshotName != null) {
            validateSnapshotName(snapshotName);
            this.snapshotName = snapshotName;
        }
        this.description = description;
        this.hashtags = hashtags;
    }

    public void publish() {
        if (this.status != SnapshotStatus.DRAFT) {
            throw new SnapshotStateException(this.status, "발행");
        }
        this.status = SnapshotStatus.ACTIVE;
    }

    public void complete() {
        if (this.status != SnapshotStatus.ACTIVE) {
            throw new SnapshotStateException(this.status, "완료");
        }
        this.status = SnapshotStatus.COMPLETED;
    }

    public void archive() {
        if (this.status != SnapshotStatus.COMPLETED) {
            throw new SnapshotStateException(this.status, "보관");
        }
        this.status = SnapshotStatus.ARCHIVED;
    }

    public boolean isDraft() {
        return this.status == SnapshotStatus.DRAFT;
    }

    public boolean isActive() {
        return this.status == SnapshotStatus.ACTIVE;
    }

    public boolean isModifiable() {
        return this.status == SnapshotStatus.DRAFT || this.status == SnapshotStatus.ACTIVE;
    }

    public boolean isItemModifiable() {
        return this.status == SnapshotStatus.DRAFT;
    }

    // ===== 연관관계 편의 메서드 =====
    public void addItem(SnapshotItem item) {
        this.items.add(item);
        item.assignSnapshot(this);
    }

    public void addRelation(SnapshotRelation relation) {
        this.relations.add(relation);
        relation.assignSnapshot(this);
    }

    // ===== Private 검증 메서드 =====
    private void validateModifiable() {
        if (!isModifiable()) {
            throw new SnapshotStateException(this.status, "수정");
        }
    }

    private void validateSnapshotName(String snapshotName) {
        if (snapshotName == null || snapshotName.isBlank()) {
            throw new IllegalArgumentException("스냅샷 이름은 필수입니다");
        }
        if (snapshotName.length() > 255) {
            throw new IllegalArgumentException("스냅샷 이름은 255자 이하여야 합니다");
        }
    }
}
