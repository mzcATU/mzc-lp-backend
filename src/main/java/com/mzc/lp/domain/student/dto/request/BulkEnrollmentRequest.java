package com.mzc.lp.domain.student.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkEnrollmentRequest(
        @NotEmpty(message = "courseTimeIds는 필수입니다")
        @Size(max = 100, message = "한 번에 최대 100개까지 수강신청 가능합니다")
        List<Long> courseTimeIds
) {
}
