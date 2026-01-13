package com.mzc.lp.domain.assignment.dto.request;

import com.mzc.lp.domain.assignment.constant.GradingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateAssignmentRequest(
        @NotBlank(message = "과제 제목은 필수입니다")
        @Size(max = 200, message = "과제 제목은 200자 이내여야 합니다")
        String title,

        String description,

        @NotNull(message = "채점 방식은 필수입니다")
        GradingType gradingType,

        Integer maxScore,

        Integer passingScore,

        LocalDateTime dueDate
) {
    public CreateAssignmentRequest {
        if (maxScore == null && gradingType == GradingType.SCORE) {
            maxScore = 100;
        }
    }
}
