package com.mzc.lp.domain.course.dto.request;

import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank(message = "강의 제목은 필수입니다")
        @Size(max = 255, message = "강의 제목은 255자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
        String description,

        CourseLevel level,

        CourseType type,

        @Positive(message = "예상 학습 시간은 양수여야 합니다")
        Integer estimatedHours,

        Long categoryId,

        Long instructorId
) {
    public CreateCourseRequest {
        if (title != null) {
            title = title.trim();
        }
    }
}
