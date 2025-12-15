package com.mzc.lp.domain.iis.entity;

import com.mzc.lp.common.entity.BaseEntity;
import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "iis_assignment_history", indexes = {
        @Index(name = "idx_iis_history_assignment", columnList = "assignment_id"),
        @Index(name = "idx_iis_history_changed_at", columnList = "changed_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentHistory extends BaseEntity {

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private AssignmentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private AssignmentStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_role", length = 20)
    private InstructorRole oldRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_role", length = 20)
    private InstructorRole newRole;

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    // 정적 팩토리 메서드 - 배정
    public static AssignmentHistory ofAssign(
            Long assignmentId,
            InstructorRole role,
            Long changedBy
    ) {
        AssignmentHistory history = new AssignmentHistory();
        history.assignmentId = assignmentId;
        history.action = AssignmentAction.ASSIGN;
        history.newStatus = AssignmentStatus.ACTIVE;
        history.newRole = role;
        history.changedBy = changedBy;
        history.changedAt = Instant.now();
        return history;
    }

    // 정적 팩토리 메서드 - 교체
    public static AssignmentHistory ofReplace(
            Long assignmentId,
            String reason,
            Long changedBy
    ) {
        AssignmentHistory history = new AssignmentHistory();
        history.assignmentId = assignmentId;
        history.action = AssignmentAction.REPLACE;
        history.oldStatus = AssignmentStatus.ACTIVE;
        history.newStatus = AssignmentStatus.REPLACED;
        history.reason = reason;
        history.changedBy = changedBy;
        history.changedAt = Instant.now();
        return history;
    }

    // 정적 팩토리 메서드 - 취소
    public static AssignmentHistory ofCancel(
            Long assignmentId,
            String reason,
            Long changedBy
    ) {
        AssignmentHistory history = new AssignmentHistory();
        history.assignmentId = assignmentId;
        history.action = AssignmentAction.CANCEL;
        history.oldStatus = AssignmentStatus.ACTIVE;
        history.newStatus = AssignmentStatus.CANCELLED;
        history.reason = reason;
        history.changedBy = changedBy;
        history.changedAt = Instant.now();
        return history;
    }

    // 정적 팩토리 메서드 - 역할 변경
    public static AssignmentHistory ofRoleChange(
            Long assignmentId,
            InstructorRole oldRole,
            InstructorRole newRole,
            String reason,
            Long changedBy
    ) {
        AssignmentHistory history = new AssignmentHistory();
        history.assignmentId = assignmentId;
        history.action = AssignmentAction.ROLE_CHANGE;
        history.oldRole = oldRole;
        history.newRole = newRole;
        history.reason = reason;
        history.changedBy = changedBy;
        history.changedAt = Instant.now();
        return history;
    }
}
