package com.mzc.lp.domain.assignment.dto.response;

import com.mzc.lp.domain.assignment.constant.SubmissionGrade;
import com.mzc.lp.domain.assignment.constant.SubmissionStatus;
import com.mzc.lp.domain.assignment.entity.AssignmentSubmission;

import java.time.Instant;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        Long studentId,
        String studentName,
        String studentEmail,
        String textContent,
        String fileUrl,
        String fileName,
        SubmissionStatus status,
        Instant submittedAt,
        Integer score,
        SubmissionGrade grade,
        Instant gradedAt,
        Long gradedBy,
        String feedback,
        Instant createdAt,
        Instant updatedAt
) {
    public static SubmissionResponse from(AssignmentSubmission submission) {
        return from(submission, null, null);
    }

    public static SubmissionResponse from(
            AssignmentSubmission submission,
            String studentName,
            String studentEmail
    ) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignmentId(),
                submission.getStudentId(),
                studentName,
                studentEmail,
                submission.getTextContent(),
                submission.getFileUrl(),
                submission.getFileName(),
                submission.getStatus(),
                submission.getSubmittedAt(),
                submission.getScore(),
                submission.getGrade(),
                submission.getGradedAt(),
                submission.getGradedBy(),
                submission.getFeedback(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }
}
