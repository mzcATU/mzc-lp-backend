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

    /**
     * DRAFT 또는 READY -> READY 상태로 전환
     * @throws IllegalStateException REGISTERED 상태에서는 변경 불가
     */
    public void markAsReady() {
        validateModifiable();
        this.status = CourseStatus.READY;
    }

    /**
     * DRAFT 또는 READY -> DRAFT 상태로 전환
     * @throws IllegalStateException REGISTERED 상태에서는 변경 불가
     */
    public void markAsDraft() {
        validateModifiable();
        this.status = CourseStatus.DRAFT;
    }

    /**
     * READY -> REGISTERED (단방향, 되돌릴 수 없음)
     * @throws IllegalStateException READY가 아닌 상태에서는 호출 불가
     */
    public void register() {
        if (this.status != CourseStatus.READY) {
            throw new IllegalStateException(
                    String.format("READY 상태의 강의만 등록할 수 있습니다. 현재 상태: %s", this.status.getDescription()));
        }
        this.status = CourseStatus.REGISTERED;
    }

    // ===== 상태 확인 헬퍼 메서드 =====

    public boolean isModifiable() {
        return this.status.isModifiable();
    }

    public boolean canCreateCourseTime() {
        return this.status.canCreateCourseTime();
    }

    public boolean isDraft() {
        return this.status == CourseStatus.DRAFT;
    }

    public boolean isReady() {
        return this.status == CourseStatus.READY;
    }

    public boolean isRegistered() {
        return this.status == CourseStatus.REGISTERED;
    }

    // ===== Private 상태 검증 메서드 =====

    private void validateModifiable() {
        if (!isModifiable()) {
            throw new IllegalStateException(
                    String.format("현재 상태(%s)에서는 강의를 수정할 수 없습니다.", this.status.getDescription()));
        }
    }

    // ===== Deprecated 메서드 (하위 호환성) =====

    /** @deprecated Use {@link #markAsReady()} instead */
    @Deprecated
    public void publish() {
        markAsReady();
    }

    /** @deprecated Use {@link #markAsDraft()} instead */
    @Deprecated
    public void unpublish() {
        markAsDraft();
    }

    /** @deprecated 상태 변경은 markAsReady(), markAsDraft(), register() 메서드를 사용하세요 */
    @Deprecated
    public void updateStatus(CourseStatus status) {
        this.status = status;
    }

    /** @deprecated Use {@link #isReady()} or {@link #isRegistered()} instead */
    @Deprecated
    public boolean isPublished() {
        return this.status == CourseStatus.READY || this.status == CourseStatus.REGISTERED;
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
