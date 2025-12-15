package com.mzc.lp.domain.course.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_course_items", indexes = {
        @Index(name = "idx_course_item_course", columnList = "course_id"),
        @Index(name = "idx_course_item_parent", columnList = "parent_id"),
        @Index(name = "idx_course_item_lo", columnList = "learning_object_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseItem extends TenantEntity {

    private static final int MAX_DEPTH = 9;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CourseItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseItem> children = new ArrayList<>();

    @Column(name = "learning_object_id")
    private Long learningObjectId;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(nullable = false)
    private Integer depth;

    // ===== 정적 팩토리 메서드 =====
    public static CourseItem createFolder(Course course, String itemName, CourseItem parent) {
        CourseItem item = new CourseItem();
        item.course = course;
        item.itemName = itemName;
        item.parent = parent;
        item.depth = parent != null ? parent.getDepth() + 1 : 0;
        item.learningObjectId = null;
        item.validateDepth();
        return item;
    }

    public static CourseItem createItem(Course course, String itemName,
                                        CourseItem parent, Long learningObjectId) {
        CourseItem item = new CourseItem();
        item.course = course;
        item.itemName = itemName;
        item.parent = parent;
        item.depth = parent != null ? parent.getDepth() + 1 : 0;
        item.learningObjectId = learningObjectId;
        item.validateDepth();
        return item;
    }

    // ===== 비즈니스 메서드 =====
    public boolean isFolder() {
        return this.learningObjectId == null;
    }

    public void updateItemName(String itemName) {
        validateItemName(itemName);
        this.itemName = itemName;
    }

    public void updateLearningObjectId(Long learningObjectId) {
        if (isFolder()) {
            throw new IllegalStateException("폴더에는 학습 객체를 연결할 수 없습니다");
        }
        this.learningObjectId = learningObjectId;
    }

    public void moveTo(CourseItem newParent) {
        int newDepth = newParent != null ? newParent.getDepth() + 1 : 0;

        // 하위 항목들의 최대 깊이 계산
        int maxChildDepth = calculateMaxChildDepth();
        int depthDiff = newDepth - this.depth;

        if (maxChildDepth + depthDiff > MAX_DEPTH) {
            throw new IllegalArgumentException("최대 깊이(10단계)를 초과할 수 없습니다");
        }

        // 순환 참조 검증
        if (newParent != null && isAncestorOf(newParent)) {
            throw new IllegalArgumentException("하위 항목으로 이동할 수 없습니다");
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
    void assignCourse(Course course) {
        this.course = course;
    }

    public void addChild(CourseItem child) {
        this.children.add(child);
        child.parent = this;
    }

    // ===== Private 검증/헬퍼 메서드 =====
    private void validateDepth() {
        if (this.depth > MAX_DEPTH) {
            throw new IllegalArgumentException("최대 깊이(10단계)를 초과할 수 없습니다");
        }
    }

    private void validateItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("항목 이름은 필수입니다");
        }
        if (itemName.length() > 255) {
            throw new IllegalArgumentException("항목 이름은 255자 이하여야 합니다");
        }
    }

    private int calculateMaxChildDepth() {
        if (children.isEmpty()) {
            return this.depth;
        }
        return children.stream()
                .mapToInt(CourseItem::calculateMaxChildDepth)
                .max()
                .orElse(this.depth);
    }

    private boolean isAncestorOf(CourseItem item) {
        CourseItem current = item;
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
        for (CourseItem child : children) {
            child.updateDepthRecursively(newDepth + 1);
        }
    }
}
