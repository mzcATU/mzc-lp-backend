package com.mzc.lp.domain.program.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.exception.ProgramNotModifiableException;
import com.mzc.lp.domain.program.exception.ProgramStatusException;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "cm_programs", indexes = {
        @Index(name = "idx_program_tenant", columnList = "tenant_id"),
        @Index(name = "idx_program_status", columnList = "status"),
        @Index(name = "idx_program_creator", columnList = "creator_id"),
        @Index(name = "idx_program_snapshot", columnList = "snapshot_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Program extends TenantEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProgramLevel level;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProgramType type;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    private CourseSnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgramStatus status;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 승인 정보
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approval_comment", length = 500)
    private String approvalComment;

    // 반려 정보
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    // 제출 정보
    @Column(name = "submitted_at")
    private Instant submittedAt;

    // ===== 정적 팩토리 메서드 =====
    public static Program create(String title, Long creatorId) {
        Program program = new Program();
        program.title = title;
        program.creatorId = creatorId;
        program.status = ProgramStatus.DRAFT;
        return program;
    }

    public static Program create(String title, String description, String thumbnailUrl,
                                  ProgramLevel level, ProgramType type, Integer estimatedHours,
                                  Long creatorId) {
        Program program = new Program();
        program.title = title;
        program.description = description;
        program.thumbnailUrl = thumbnailUrl;
        program.level = level;
        program.type = type;
        program.estimatedHours = estimatedHours;
        program.creatorId = creatorId;
        program.status = ProgramStatus.DRAFT;
        return program;
    }

    // ===== 비즈니스 메서드 =====
    public void update(String title, String description, String thumbnailUrl,
                       ProgramLevel level, ProgramType type, Integer estimatedHours) {
        validateModifiable();
        if (title != null) {
            validateTitle(title);
            this.title = title;
        }
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.level = level;
        this.type = type;
        this.estimatedHours = estimatedHours;
    }

    public void linkSnapshot(CourseSnapshot snapshot) {
        validateModifiable();
        this.snapshot = snapshot;
    }

    public void submit() {
        if (this.status != ProgramStatus.DRAFT && this.status != ProgramStatus.REJECTED) {
            throw new ProgramStatusException(this.status, "제출");
        }
        this.status = ProgramStatus.PENDING;
        this.submittedAt = Instant.now();
        // 반려 정보 초기화 (재제출 시)
        this.rejectionReason = null;
        this.rejectedAt = null;
    }

    public void approve(Long operatorId, String comment) {
        if (this.status != ProgramStatus.PENDING) {
            throw new ProgramStatusException(this.status, "승인");
        }
        this.status = ProgramStatus.APPROVED;
        this.approvedBy = operatorId;
        this.approvedAt = Instant.now();
        this.approvalComment = comment;
    }

    public void reject(Long operatorId, String reason) {
        if (this.status != ProgramStatus.PENDING) {
            throw new ProgramStatusException(this.status, "반려");
        }
        validateRejectionReason(reason);
        this.status = ProgramStatus.REJECTED;
        this.rejectionReason = reason;
        this.rejectedAt = Instant.now();
    }

    public void close() {
        if (this.status != ProgramStatus.APPROVED && this.status != ProgramStatus.DRAFT) {
            throw new ProgramStatusException(this.status, "종료");
        }
        this.status = ProgramStatus.CLOSED;
    }

    // ===== 상태 확인 메서드 =====
    public boolean isDraft() {
        return this.status == ProgramStatus.DRAFT;
    }

    public boolean isPending() {
        return this.status == ProgramStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == ProgramStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == ProgramStatus.REJECTED;
    }

    public boolean isClosed() {
        return this.status == ProgramStatus.CLOSED;
    }

    public boolean isModifiable() {
        return this.status == ProgramStatus.DRAFT || this.status == ProgramStatus.REJECTED;
    }

    public boolean canCreateTime() {
        return this.status == ProgramStatus.APPROVED;
    }

    // ===== Private 검증 메서드 =====
    private void validateModifiable() {
        if (!isModifiable()) {
            throw new ProgramNotModifiableException(this.status);
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("제목은 255자 이하여야 합니다");
        }
    }

    private void validateRejectionReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("반려 사유는 필수입니다");
        }
        if (reason.length() > 500) {
            throw new IllegalArgumentException("반려 사유는 500자 이하여야 합니다");
        }
    }
}
