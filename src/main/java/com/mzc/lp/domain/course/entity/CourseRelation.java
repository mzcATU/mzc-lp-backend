package com.mzc.lp.domain.course.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cr_course_relations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"from_item_id", "to_item_id"}),
       indexes = {
           @Index(name = "idx_relation_from", columnList = "from_item_id"),
           @Index(name = "idx_relation_to", columnList = "to_item_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRelation extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_item_id")
    private CourseItem fromItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_item_id", nullable = false)
    private CourseItem toItem;

    // ===== 정적 팩토리 메서드 =====
    public static CourseRelation create(CourseItem fromItem, CourseItem toItem) {
        validateRelation(fromItem, toItem);

        CourseRelation relation = new CourseRelation();
        relation.fromItem = fromItem;
        relation.toItem = toItem;
        return relation;
    }

    public static CourseRelation createStartPoint(CourseItem toItem) {
        if (toItem == null) {
            throw new IllegalArgumentException("시작점 항목은 필수입니다");
        }
        if (toItem.isFolder()) {
            throw new IllegalArgumentException("폴더는 학습 순서에 포함할 수 없습니다");
        }

        CourseRelation relation = new CourseRelation();
        relation.fromItem = null;
        relation.toItem = toItem;
        return relation;
    }

    // ===== 비즈니스 메서드 =====
    public boolean isStartPoint() {
        return this.fromItem == null;
    }

    public void updateFromItem(CourseItem fromItem) {
        validateRelation(fromItem, this.toItem);
        this.fromItem = fromItem;
    }

    public void updateToItem(CourseItem toItem) {
        validateRelation(this.fromItem, toItem);
        this.toItem = toItem;
    }

    // ===== Private 검증 메서드 =====
    private static void validateRelation(CourseItem fromItem, CourseItem toItem) {
        if (toItem == null) {
            throw new IllegalArgumentException("대상 항목은 필수입니다");
        }
        if (toItem.isFolder()) {
            throw new IllegalArgumentException("폴더는 학습 순서에 포함할 수 없습니다");
        }
        if (fromItem != null) {
            if (fromItem.isFolder()) {
                throw new IllegalArgumentException("폴더는 학습 순서에 포함할 수 없습니다");
            }
            if (fromItem.getId() != null && fromItem.getId().equals(toItem.getId())) {
                throw new IllegalArgumentException("자기 자신을 참조할 수 없습니다");
            }
        }
    }
}
