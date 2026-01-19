package com.mzc.lp.domain.course.dto.request;

import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseStatus;
import com.mzc.lp.domain.course.constant.CourseType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCourseRequest(
        @Size(max = 255, message = "강의 제목은 255자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
        String description,

        CourseLevel level,

        CourseType type,

        @Positive(message = "예상 학습 시간은 양수여야 합니다")
        Integer estimatedHours,

        Long categoryId,

        @Size(max = 500, message = "썸네일 URL은 500자 이하여야 합니다")
        String thumbnailUrl,

        List<String> tags,

        CourseStatus status
) {
    public UpdateCourseRequest {
        if (title != null) {
            title = title.trim();
        }
    }
}
