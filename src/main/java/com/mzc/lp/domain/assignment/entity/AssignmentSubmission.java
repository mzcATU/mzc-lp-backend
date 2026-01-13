package com.mzc.lp.domain.assignment.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.assignment.constant.SubmissionGrade;
import com.mzc.lp.domain.assignment.constant.SubmissionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "assignment_submissions", indexes = {
        @Index(name = "idx_submission_assignment", columnList = "tenant_id, assignment_id"),
        @Index(name = "idx_submission_student", columnList = "tenant_id, student_id"),
        @Index(name = "idx_submission_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_submission_assignment_student",
                columnNames = {"tenant_id", "assignment_id", "student_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentSubmission extends TenantEntity {

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private SubmissionGrade grade;

    @Column(name = "graded_at")
    private Instant gradedAt;

    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    // 정적 팩토리 메서드
    public static AssignmentSubmission create(
            Long assignmentId,
            Long studentId,
            String textContent,
            String fileUrl,
            String fileName
    ) {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.assignmentId = assignmentId;
        submission.studentId = studentId;
        submission.textContent = textContent;
        submission.fileUrl = fileUrl;
        submission.fileName = fileName;
        submission.status = SubmissionStatus.SUBMITTED;
        submission.submittedAt = Instant.now();
        return submission;
    }

    // 비즈니스 메서드

    /**
     * 재제출 (RETURNED 상태에서만 가능)
     */
    public void resubmit(String textContent, String fileUrl, String fileName) {
        if (this.status != SubmissionStatus.RETURNED) {
            throw new IllegalStateException("반려된 제출물만 재제출할 수 있습니다.");
        }
        this.textContent = textContent;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.status = SubmissionStatus.SUBMITTED;
        this.submittedAt = Instant.now();
        // 채점 정보 초기화
        this.score = null;
        this.grade = null;
        this.gradedAt = null;
        this.gradedBy = null;
        this.feedback = null;
    }

    /**
     * 점수 채점 (SCORE 방식)
     */
    public void gradeWithScore(Integer score, String feedback, Long gradedBy) {
        this.score = score;
        this.feedback = feedback;
        this.gradedBy = gradedBy;
        this.gradedAt = Instant.now();
        this.status = SubmissionStatus.GRADED;
    }

    /**
     * 합격/불합격 채점 (PASS_FAIL 방식)
     */
    public void gradeWithPassFail(SubmissionGrade grade, String feedback, Long gradedBy) {
        this.grade = grade;
        this.feedback = feedback;
        this.gradedBy = gradedBy;
        this.gradedAt = Instant.now();
        this.status = SubmissionStatus.GRADED;
    }

    /**
     * 반려 (재제출 요청)
     */
    public void returnSubmission(String feedback, Long gradedBy) {
        this.feedback = feedback;
        this.gradedBy = gradedBy;
        this.gradedAt = Instant.now();
        this.status = SubmissionStatus.RETURNED;
    }

    // 검증 메서드
    public boolean isSubmitted() {
        return this.status == SubmissionStatus.SUBMITTED;
    }

    public boolean isGraded() {
        return this.status == SubmissionStatus.GRADED;
    }

    public boolean isReturned() {
        return this.status == SubmissionStatus.RETURNED;
    }

    public boolean canResubmit() {
        return this.status == SubmissionStatus.RETURNED;
    }
}
