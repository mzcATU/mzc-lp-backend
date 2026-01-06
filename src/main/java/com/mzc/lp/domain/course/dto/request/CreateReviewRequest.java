package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "별점은 필수입니다")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다")
        Integer rating,

        @Size(max = 2000, message = "리뷰 내용은 2000자 이하여야 합니다")
        String content
) {
    public CreateReviewRequest {
        if (content != null) {
            content = content.trim();
        }
    }
}
