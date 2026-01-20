package com.mzc.lp.domain.student.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.constant.EnrollmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "sis_enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sis_enrollment",
                columnNames = {"tenant_id", "user_id", "course_time_id"}
        ),
        indexes = {
                @Index(name = "idx_sis_enrollment_user", columnList = "tenant_id, user_id"),
                @Index(name = "idx_sis_enrollment_course_time", columnList = "tenant_id, course_time_id"),
                @Index(name = "idx_sis_enrollment_status", columnList = "tenant_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_time_id", nullable = false)
    private Long courseTimeId;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent;

    private Integer score;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "enrolled_by")
    private Long enrolledBy;

    // 정적 팩토리 메서드 - 자발적 수강신청 (선착순)
    public static Enrollment createVoluntary(Long userId, Long courseTimeId) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseTimeId = courseTimeId;
        enrollment.enrolledAt = Instant.now();
        enrollment.type = EnrollmentType.VOLUNTARY;
        enrollment.status = EnrollmentStatus.ENROLLED;
        enrollment.progressPercent = 0;
        enrollment.enrolledBy = userId;
        return enrollment;
    }

    // 정적 팩토리 메서드 - 승인 대기 수강신청 (승인제)
    public static Enrollment createPending(Long userId, Long courseTimeId) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseTimeId = courseTimeId;
        enrollment.enrolledAt = Instant.now();
        enrollment.type = EnrollmentType.VOLUNTARY;
        enrollment.status = EnrollmentStatus.PENDING;
        enrollment.progressPercent = 0;
        enrollment.enrolledBy = userId;
        return enrollment;
    }

    // 정적 팩토리 메서드 - 강제 배정 (필수 교육)
    public static Enrollment createMandatory(Long userId, Long courseTimeId, Long enrolledBy) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseTimeId = courseTimeId;
        enrollment.enrolledAt = Instant.now();
        enrollment.type = EnrollmentType.MANDATORY;
        enrollment.status = EnrollmentStatus.ENROLLED;
        enrollment.progressPercent = 0;
        enrollment.enrolledBy = enrolledBy;
        return enrollment;
    }

    // 비즈니스 메서드

    public void updateProgress(Integer progressPercent) {
        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException("Progress percent must be between 0 and 100");
        }
        this.progressPercent = progressPercent;
    }

    public void complete(Integer score) {
        this.status = EnrollmentStatus.COMPLETED;
        this.score = score;
        this.completedAt = Instant.now();
        this.progressPercent = 100;
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    public void approve() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING enrollment can be approved");
        }
        this.status = EnrollmentStatus.ENROLLED;
    }

    public void reject() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING enrollment can be rejected");
        }
        this.status = EnrollmentStatus.DROPPED;
    }

    public void fail() {
        this.status = EnrollmentStatus.FAILED;
    }

    public void updateStatus(EnrollmentStatus status) {
        this.status = status;
        if (status == EnrollmentStatus.COMPLETED) {
            this.completedAt = Instant.now();
        }
    }

    // 검증 메서드

    public boolean isEnrolled() {
        return this.status == EnrollmentStatus.ENROLLED;
    }

    public boolean isCompleted() {
        return this.status == EnrollmentStatus.COMPLETED;
    }

    public boolean isDropped() {
        return this.status == EnrollmentStatus.DROPPED;
    }

    public boolean isFailed() {
        return this.status == EnrollmentStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == EnrollmentStatus.PENDING;
    }

    public boolean canCancel() {
        return this.status == EnrollmentStatus.ENROLLED || this.status == EnrollmentStatus.PENDING;
    }

    public boolean isVoluntary() {
        return this.type == EnrollmentType.VOLUNTARY;
    }

    public boolean isMandatory() {
        return this.type == EnrollmentType.MANDATORY;
    }
}
