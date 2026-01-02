package com.mzc.lp.domain.iis.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "iis_instructor_assignments",
        uniqueConstraints = {
                // 동일 차수에 동일 강사 중복 배정 방지 (ACTIVE 상태에서)
                @UniqueConstraint(
                        name = "uk_iis_time_user_active",
                        columnNames = {"tenant_id", "time_key", "user_key", "status"}
                )
        },
        indexes = {
                @Index(name = "idx_iis_tenant", columnList = "tenant_id"),
                @Index(name = "idx_iis_user", columnList = "user_key"),
                @Index(name = "idx_iis_time", columnList = "time_key"),
                @Index(name = "idx_iis_status", columnList = "status"),
                @Index(name = "idx_iis_role", columnList = "role"),
                @Index(name = "idx_iis_assigned_at", columnList = "assigned_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorAssignment extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version = 0L;

    // 필수 필드
    @Column(name = "user_key", nullable = false)
    private Long userKey;

    @Column(name = "time_key", nullable = false)
    private Long timeKey;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    // 추가 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstructorRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "replaced_at")
    private Instant replacedAt;

    @Column(name = "assigned_by")
    private Long assignedBy;

    // 정적 팩토리 메서드
    public static InstructorAssignment create(
            Long userKey,
            Long timeKey,
            InstructorRole role,
            Long assignedBy
    ) {
        InstructorAssignment assignment = new InstructorAssignment();
        assignment.userKey = userKey;
        assignment.timeKey = timeKey;
        assignment.assignedAt = Instant.now();
        assignment.role = role;
        assignment.status = AssignmentStatus.ACTIVE;
        assignment.assignedBy = assignedBy;
        return assignment;
    }

    // 비즈니스 메서드

    public void updateRole(InstructorRole newRole) {
        this.role = newRole;
    }

    public void replace() {
        this.status = AssignmentStatus.REPLACED;
        this.replacedAt = Instant.now();
    }

    public void cancel() {
        this.status = AssignmentStatus.CANCELLED;
        this.replacedAt = Instant.now();
    }

    // 검증 메서드

    public boolean isActive() {
        return this.status == AssignmentStatus.ACTIVE;
    }

    public boolean isMain() {
        return this.role == InstructorRole.MAIN;
    }

    public boolean isSub() {
        return this.role == InstructorRole.SUB;
    }

    public boolean isAssistant() {
        return this.role == InstructorRole.ASSISTANT;
    }
}
