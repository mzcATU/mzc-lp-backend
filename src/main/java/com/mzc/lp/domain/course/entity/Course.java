package com.mzc.lp.domain.course.entity;

import com.mzc.lp.common.constant.ValidationMessages;
import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseStatus;
import com.mzc.lp.domain.course.constant.CourseType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version;

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

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column
    private Integer estimatedHours;

    @Column
    private Long categoryId;

    @Column
    private Long createdBy;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cm_course_tags", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "tag", length = 100)
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseItem> items = new ArrayList<>();

    // ===== 정적 팩토리 메서드 =====
    public static Course create(String title, Long createdBy) {
        Course course = new Course();
        course.title = title;
        course.createdBy = createdBy;
        course.status = CourseStatus.DRAFT;
        return course;
    }

    public static Course create(String title, String description, CourseLevel level,
                                CourseType type, Integer estimatedHours,
                                Long categoryId, String thumbnailUrl,
                                LocalDate startDate, LocalDate endDate, List<String> tags,
                                Long createdBy) {
        Course course = new Course();
        course.title = title;
        course.description = description;
        course.level = level;
        course.type = type;
        course.estimatedHours = estimatedHours;
        course.categoryId = categoryId;
        course.thumbnailUrl = thumbnailUrl;
        course.startDate = startDate;
        course.endDate = endDate;
        course.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        course.createdBy = createdBy;
        course.status = CourseStatus.DRAFT;
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

    public void updateStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void updateTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public void update(String title, String description, CourseLevel level,
                       CourseType type, Integer estimatedHours,
                       Long categoryId, String thumbnailUrl,
                       LocalDate startDate, LocalDate endDate, List<String> tags) {
        if (title != null) {
            updateTitle(title);
        }
        this.description = description;
        this.level = level;
        this.type = type;
        this.estimatedHours = estimatedHours;
        this.categoryId = categoryId;
        this.thumbnailUrl = thumbnailUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    // ===== Status 관련 비즈니스 메서드 =====
    public void publish() {
        this.status = CourseStatus.PUBLISHED;
    }

    public void unpublish() {
        this.status = CourseStatus.DRAFT;
    }

    public void updateStatus(CourseStatus status) {
        this.status = status;
    }

    public boolean isDraft() {
        return this.status == CourseStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == CourseStatus.PUBLISHED;
    }

    // ===== 연관관계 편의 메서드 =====
    public void addItem(CourseItem item) {
        this.items.add(item);
        item.assignCourse(this);
    }

    // ===== Private 검증 메서드 =====
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.COURSE_TITLE_REQUIRED);
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException(ValidationMessages.COURSE_TITLE_TOO_LONG);
        }
    }
}
