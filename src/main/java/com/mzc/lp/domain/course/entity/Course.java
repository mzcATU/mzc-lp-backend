package com.mzc.lp.domain.course.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends TenantEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CourseLevel level;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CourseType type;

    @Column
    private Integer estimatedHours;

    @Column
    private Long categoryId;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseItem> items = new ArrayList<>();

    // ===== 정적 팩토리 메서드 =====
    public static Course create(String title) {
        Course course = new Course();
        course.title = title;
        return course;
    }

    public static Course create(String title, String description, CourseLevel level,
                                CourseType type, Integer estimatedHours,
                                Long categoryId, String thumbnailUrl) {
        Course course = new Course();
        course.title = title;
        course.description = description;
        course.level = level;
        course.type = type;
        course.estimatedHours = estimatedHours;
        course.categoryId = categoryId;
        course.thumbnailUrl = thumbnailUrl;
        return course;
    }

    // ===== 비즈니스 메서드 =====
    public void updateTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateLevel(CourseLevel level) {
        this.level = level;
    }

    public void updateType(CourseType type) {
        this.type = type;
    }

    public void updateEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public void updateCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void update(String title, String description, CourseLevel level,
                       CourseType type, Integer estimatedHours,
                       Long categoryId, String thumbnailUrl) {
        if (title != null) {
            updateTitle(title);
        }
        this.description = description;
        this.level = level;
        this.type = type;
        this.estimatedHours = estimatedHours;
        this.categoryId = categoryId;
        this.thumbnailUrl = thumbnailUrl;
    }

    // ===== 연관관계 편의 메서드 =====
    public void addItem(CourseItem item) {
        this.items.add(item);
        item.assignCourse(this);
    }

    // ===== Private 검증 메서드 =====
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("강의 제목은 필수입니다");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("강의 제목은 255자 이하여야 합니다");
        }
    }
}
