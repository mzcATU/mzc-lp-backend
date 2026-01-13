package com.mzc.lp.domain.assignment.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.assignment.constant.AssignmentStatus;
import com.mzc.lp.domain.assignment.constant.GradingType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments", indexes = {
        @Index(name = "idx_assignment_course_time", columnList = "tenant_id, course_time_id"),
        @Index(name = "idx_assignment_status", columnList = "tenant_id, status"),
        @Index(name = "idx_assignment_due_date", columnList = "due_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends TenantEntity {

    @Column(name = "course_time_id", nullable = false)
    private Long courseTimeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_type", nullable = false, length = 20)
    private GradingType gradingType;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "passing_score")
    private Integer passingScore;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    // 정적 팩토리 메서드
    public static Assignment create(
            Long courseTimeId,
            String title,
            String description,
            GradingType gradingType,
            Integer maxScore,
            Integer passingScore,
            LocalDateTime dueDate,
            Long createdBy
    ) {
        Assignment assignment = new Assignment();
        assignment.courseTimeId = courseTimeId;
        assignment.title = title;
        assignment.description = description;
        assignment.gradingType = gradingType;
        assignment.maxScore = gradingType == GradingType.SCORE ? (maxScore != null ? maxScore : 100) : null;
        assignment.passingScore = passingScore;
        assignment.dueDate = dueDate;
        assignment.status = AssignmentStatus.DRAFT;
        assignment.createdBy = createdBy;
        return assignment;
    }

    // 비즈니스 메서드
    public void update(
            String title,
            String description,
            GradingType gradingType,
            Integer maxScore,
            Integer passingScore,
            LocalDateTime dueDate
    ) {
        if (this.status != AssignmentStatus.DRAFT) {
            throw new IllegalStateException("발행된 과제는 수정할 수 없습니다.");
        }
        this.title = title;
        this.description = description;
        this.gradingType = gradingType;
        this.maxScore = gradingType == GradingType.SCORE ? (maxScore != null ? maxScore : 100) : null;
        this.passingScore = passingScore;
        this.dueDate = dueDate;
    }

    public void publish() {
        if (this.status != AssignmentStatus.DRAFT) {
            throw new IllegalStateException("이미 발행된 과제입니다.");
        }
        this.status = AssignmentStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void close() {
        if (this.status != AssignmentStatus.PUBLISHED) {
            throw new IllegalStateException("발행된 과제만 마감할 수 있습니다.");
        }
        this.status = AssignmentStatus.CLOSED;
    }

    // 검증 메서드
    public boolean isDraft() {
        return this.status == AssignmentStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == AssignmentStatus.PUBLISHED;
    }

    public boolean isClosed() {
        return this.status == AssignmentStatus.CLOSED;
    }

    public boolean isSubmittable() {
        if (!isPublished()) {
            return false;
        }
        if (dueDate == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(dueDate);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }
}
