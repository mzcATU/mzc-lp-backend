package com.mzc.lp.domain.course.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class CourseIncompleteException extends BusinessException {

    private static final Map<String, String> FIELD_LABELS = Map.of(
            "title", "제목",
            "description", "설명",
            "categoryId", "카테고리",
            "items", "차시"
    );

    private final List<String> missingFields;

    public CourseIncompleteException() {
        super(ErrorCode.CM_COURSE_INCOMPLETE);
        this.missingFields = List.of();
    }

    public CourseIncompleteException(Long courseId, List<String> missingFields) {
        super(ErrorCode.CM_COURSE_INCOMPLETE, buildMessage(courseId, missingFields));
        this.missingFields = missingFields;
    }

    private static String buildMessage(Long courseId, List<String> missingFields) {
        String labels = missingFields.stream()
                .map(field -> FIELD_LABELS.getOrDefault(field, field))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "완성되지 않은 강의입니다. 누락 항목: " + labels + " (ID: " + courseId + ")";
    }
}
